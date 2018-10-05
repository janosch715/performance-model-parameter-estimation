package rde.analysis.loop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import monitoring.records.LoopRecord;
import rde.analysis.KiekerServiceCallRecordFilter;
import rde.analysis.WekaDataSet;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.Instances;

public class LoopIterationEstimation {

	private final KiekerServiceCallRecordFilter serviceCallRepository;

	private final KiekerLoopIterationFilter loopIterationRepository;

	public LoopIterationEstimation(KiekerServiceCallRecordFilter serviceCallRepository,
			KiekerLoopIterationFilter loopIterationRepository) {
		this.serviceCallRepository = serviceCallRepository;
		this.loopIterationRepository = loopIterationRepository;
	}

	public Map<String, WekaLoopModel> estimateAll() {
		HashMap<String, WekaLoopModel> returnValue = new HashMap<String, WekaLoopModel>();
		for (String loopId : this.loopIterationRepository.getLoopIds()) {
			returnValue.put(loopId, this.estimate(loopId));
		}
		return returnValue;
	}

	public WekaLoopModel estimate(String loopId) {
		try {
			return this.internEstimate(loopId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private WekaLoopModel internEstimate(String loopId) throws Exception {
		List<LoopRecord> records = this.loopIterationRepository.getLoopRecords(loopId);

		if (records.size() == 0) {
			throw new IllegalStateException("No records for loop id " + loopId + " found.");
		}

		LoopRecord firstRecord = records.get(0);
		
		Attribute loopIterations = new Attribute("loopIterations");
		
		WekaDataSet dataSetBuilder = 
				new WekaDataSet(
						this.serviceCallRepository, 
						firstRecord.getServiceExecutionId(),
						loopIterations);

		for (LoopRecord record : records) {
			dataSetBuilder.addInstance(record.getServiceExecutionId(), record.getLoopIterationCount());
		}
		
		Instances dataset = dataSetBuilder.getDataSet();

		System.out.println("Estimating loop iteration for loop " + loopId);
		
		LinearRegression linReg = new LinearRegression();

		// 10-fold cross-validation
		Evaluation evaluation = new Evaluation(dataset);
		evaluation.crossValidateModel(linReg, dataset, 10, new Random(1));
		System.out.println(evaluation.toSummaryString());
		
		linReg.buildClassifier(dataset);
		System.out.println(linReg);
		
		return new WekaLoopModel(linReg, dataSetBuilder.getParametersConversion());
	}
}
