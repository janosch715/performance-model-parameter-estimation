package rde.analysis.branch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import monitoring.records.BranchRecord;
import rde.analysis.ServiceCallDataSet;
import rde.analysis.WekaDataSet;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Attribute;
import weka.core.Instances;

public class BranchExecutionEstimation {

	private final ServiceCallDataSet serviceCallRepository;

	private final KiekerBranchExecutionFilter branchExecutionRepository;
	
	private final Random random;

	public BranchExecutionEstimation(
			ServiceCallDataSet serviceCallRepository,
			KiekerBranchExecutionFilter branchExecutionRepository) {
		this(serviceCallRepository, branchExecutionRepository, new Random((long) Math.random()));
	}
	
	public BranchExecutionEstimation(
			ServiceCallDataSet serviceCallRepository,
			KiekerBranchExecutionFilter branchExecutionRepository,
			Random random) {
		this.serviceCallRepository = serviceCallRepository;
		this.branchExecutionRepository = branchExecutionRepository;
		this.random = random;
	}

	public Map<String, WekaBranchModel> estimateAll() {
		Map<String, WekaBranchModel> returnValue = new HashMap<String, WekaBranchModel>(); 
		for (String branchId : this.branchExecutionRepository.getBranchIds()) {
			returnValue.put(branchId, this.estimate(branchId));
		}
		return returnValue;
	}

	public WekaBranchModel estimate(String branchId) {
		try {
			return this.internEstimate(branchId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private WekaBranchModel internEstimate(String branchId) throws Exception {
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
}
