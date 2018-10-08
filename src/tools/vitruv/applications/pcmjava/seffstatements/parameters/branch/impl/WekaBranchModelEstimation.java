package tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.WekaDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.BranchDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.monitoring.records.BranchRecord;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.util.WekaServiceParametersModel;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class WekaBranchModelEstimation {

	private final ServiceCallDataSet serviceCalls;

	private final BranchDataSet branchExecutions;

	private final Random random;

	public WekaBranchModelEstimation(ServiceCallDataSet serviceCalls, BranchDataSet branchExecutions) {
		this(serviceCalls, branchExecutions, ThreadLocalRandom.current());
	}

	public WekaBranchModelEstimation(ServiceCallDataSet serviceCalls, BranchDataSet branchExecutions, Random random) {
		this.serviceCalls = serviceCalls;
		this.branchExecutions = branchExecutions;
		this.random = random;
	}

	public Map<String, BranchModel> estimateAll() {
		Map<String, BranchModel> returnValue = new HashMap<String, BranchModel>();
		for (String branchId : this.branchExecutions.getBranchIds()) {
			returnValue.put(branchId, this.estimate(branchId));
		}
		return returnValue;
	}

	public BranchModel estimate(String branchId) {
		try {
			return this.internEstimate(branchId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private BranchModel internEstimate(String branchId) throws Exception {
		List<BranchRecord> records = this.branchExecutions.getBranchRecords(branchId);

		if (records.size() == 0) {
			throw new IllegalStateException("No records for branch id " + branchId + " found.");
		}

		Set<String> executedBranchIds = new HashSet<String>();

		for (BranchRecord record : records) {
			executedBranchIds.add(record.getExecutedBranchId());
		}

		return this.estimate(branchId, executedBranchIds);
	}

	public WekaBranchModel estimate(String branchId, Set<String> branchExecutionIds) throws Exception {
		List<BranchRecord> records = this.branchExecutions.getBranchRecords(branchId);

		if (records.size() == 0) {
			throw new IllegalStateException("No records for branch id " + branchId + " found.");
		}

		BranchRecord firstRecord = records.get(0);

		List<String> branchExecutedLabels = branchExecutionIds.stream().collect(Collectors.toList());

		Attribute branchExecutedAttribute = new Attribute("branchExecuted", branchExecutedLabels);

		WekaDataSet dataSetBuilder = new WekaDataSet(this.serviceCalls, firstRecord.getServiceExecutionId(),
				branchExecutedAttribute);

		for (BranchRecord record : records) {
			double classValue = branchExecutedLabels.indexOf(record.getExecutedBranchId());
			dataSetBuilder.addInstance(record.getServiceExecutionId(), classValue);
		}

		Instances dataset = dataSetBuilder.getDataSet();

		System.out.println("Estimating branch execution for branch " + branchId);

		Logistic linReg = new Logistic();

		Evaluation evaluation = new Evaluation(dataset);
		evaluation.crossValidateModel(linReg, dataset, 10, new Random(1));
		System.out.println(evaluation.toSummaryString());

		linReg.buildClassifier(dataset);
		System.out.println(linReg);

		return new WekaBranchModel(linReg, dataSetBuilder.getParametersConversion(), this.random,
				this.branchExecutions.getBranchNotExecutedId());
	}

	private static class WekaBranchModel implements BranchModel {

		private final Logistic classifier;
		private final WekaServiceParametersModel parametersModel;
		private final Random random;
		private final String branchNotExecutedId;

		public WekaBranchModel(Logistic classifier, WekaServiceParametersModel parametersConversion, Random random,
				String branchNotExecutedId) {
			this.classifier = classifier;
			this.parametersModel = parametersConversion;
			this.random = random;
			this.branchNotExecutedId = branchNotExecutedId;
		}

		@Override
		public Optional<String> estimateBranchId(ServiceCall serviceCall) {
			Instance parametersInstance = this.parametersModel.buildInstance(serviceCall.getParameters(), 0);
			Instances dataset = this.parametersModel.buildDataSet();
			dataset.add(parametersInstance);
			double[] branchDistribution;
			try {
				branchDistribution = this.classifier.distributionForInstance(dataset.firstInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			double selectedBranchPropability = this.random.nextDouble();
			int selectedBranchIndex = 0;
			double branchPropabilitySum = 0.0;
			while (true) {
				if (selectedBranchIndex >= branchDistribution.length) {
					throw new IllegalArgumentException("The branch has propability distribution.");
				}
				branchPropabilitySum += branchDistribution[selectedBranchIndex];
				if (selectedBranchPropability < branchPropabilitySum) {
					break;
				}
				selectedBranchIndex++;
			}

			String result = this.parametersModel.getClassAttribute().value(selectedBranchIndex);
			
			if (result.equals(this.branchNotExecutedId)) {
				return Optional.empty();
			} else {
				return Optional.of(result);
			}
		}
	}
}
