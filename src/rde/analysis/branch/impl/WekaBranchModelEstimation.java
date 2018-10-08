package rde.analysis.branch.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import monitoring.records.BranchRecord;
import rde.analysis.ServiceCall;
import rde.analysis.ServiceCallDataSet;
import rde.analysis.WekaDataSet;
import rde.analysis.branch.BranchDataSet;
import rde.analysis.util.WekaServiceParametersModel;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class WekaBranchModelEstimation {

	private final ServiceCallDataSet serviceCallRepository;

	private final BranchDataSet branchExecutionRepository;
	
	private final Random random;

	public WekaBranchModelEstimation(
			ServiceCallDataSet serviceCallRepository,
			BranchDataSet branchExecutionRepository) {
		this(serviceCallRepository, branchExecutionRepository, new Random((long) Math.random()));
	}
	
	public WekaBranchModelEstimation(
			ServiceCallDataSet serviceCallRepository,
			BranchDataSet branchExecutionRepository,
			Random random) {
		this.serviceCallRepository = serviceCallRepository;
		this.branchExecutionRepository = branchExecutionRepository;
		this.random = random;
	}

	public Map<String, BranchModel> estimateAll() {
		Map<String, BranchModel> returnValue = new HashMap<String, BranchModel>(); 
		for (String branchId : this.branchExecutionRepository.getBranchIds()) {
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
		List<BranchRecord> records = this.branchExecutionRepository.getBranchRecords(branchId);

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
		List<BranchRecord> records = this.branchExecutionRepository.getBranchRecords(branchId);

		if (records.size() == 0) {
			throw new IllegalStateException("No records for branch id " + branchId + " found.");
		}

		BranchRecord firstRecord = records.get(0);
		
		List<String> branchExecutedLabels = 
				branchExecutionIds.stream().collect(Collectors.toList());
		
		Attribute branchExecutedAttribute = 
				new Attribute("branchExecuted", branchExecutedLabels);

		WekaDataSet dataSetBuilder = 
				new WekaDataSet(
						this.serviceCallRepository, 
						firstRecord.getServiceExecutionId(),
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
		
		return new WekaBranchModel(linReg, dataSetBuilder.getParametersConversion(), this.random);
	}
	
	private static class WekaBranchModel implements BranchModel {
		
		private final Logistic classifier;
		private final WekaServiceParametersModel parametersModel;
		private final Random random;

		public WekaBranchModel(
				Logistic classifier, 
				WekaServiceParametersModel parametersConversion, 
				Random random) {
			this.classifier = classifier;
			this.parametersModel = parametersConversion;
			this.random = random;
		}

		/* (non-Javadoc)
		 * @see rde.analysis.branch.BranchModel#estimateBranchId(rde.analysis.ServiceCall)
		 */
		@Override
		public String estimateBranchId(ServiceCall serviceCall) {
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
			
			return this.parametersModel.getClassAttribute().value(selectedBranchIndex);
		}
	}
}
