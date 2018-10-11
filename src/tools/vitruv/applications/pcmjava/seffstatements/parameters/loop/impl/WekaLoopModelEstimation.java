package tools.vitruv.applications.pcmjava.seffstatements.parameters.loop.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.WekaDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.loop.LoopDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.monitoring.records.LoopRecord;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.util.WekaServiceParametersModel;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class WekaLoopModelEstimation {

	private final ServiceCallDataSet serviceCallRepository;

	private final LoopDataSet loopIterationRepository;

	public WekaLoopModelEstimation(ServiceCallDataSet serviceCallRepository,
			LoopDataSet loopIterationRepository) {
		this.serviceCallRepository = serviceCallRepository;
		this.loopIterationRepository = loopIterationRepository;
	}

	public Map<String, LoopModel> estimateAll() {
		HashMap<String, LoopModel> returnValue = new HashMap<String, LoopModel>();
		for (String loopId : this.loopIterationRepository.getLoopIds()) {
			returnValue.put(loopId, this.estimate(loopId));
		}
		return returnValue;
	}

	public LoopModel estimate(String loopId) {
		try {
			return this.internEstimate(loopId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private LoopModel internEstimate(String loopId) throws Exception {
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
	
	private static class WekaLoopModel implements LoopModel {
		
		private final LinearRegression classifier;
		private final WekaServiceParametersModel parametersConversion;

		public WekaLoopModel(LinearRegression classifier, WekaServiceParametersModel parametersConversion) {
			this.classifier = classifier;
			this.parametersConversion = parametersConversion;
		}

		@Override
		public double estimateIterations(ServiceCall serviceCall) {
			Instance parametersInstance = this.parametersConversion.buildInstance(serviceCall.getParameters(), 0);
			try {
				return this.classifier.classifyInstance(parametersInstance);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public String getIterationsStochasticExpression() {
			StringJoiner result = new StringJoiner(" + ");
			double[] coefficients = classifier.coefficients();
			for (int i = 0; i < coefficients.length - 2; i++) {
				if (coefficients[i] == 0.0) {
					continue;
				}
				StringBuilder coefficientPart = new StringBuilder();
				String paramStoEx = parametersConversion.getStochasticExpressionForIndex(i);
				coefficientPart.append(round(coefficients[i])).append(" * ").append(paramStoEx);
				result.add(coefficientPart.toString());
			}
			result.add(String.valueOf(round(coefficients[coefficients.length - 1])));
			return result.toString();
		}
		
		private static double round(double value) {
			return Math.round(value * 1000.0) / 1000.0;
		}
	}
}
