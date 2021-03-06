package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl;

import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;
import java.util.Map.Entry;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.util.WekaServiceParametersModel;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class WekaParametricDependencyEstimationStrategy implements ParametricDependencyEstimationStrategy {

	private static class WekaResourceDemandModel implements ResourceDemandModel {

		private final LinearRegression classifier;
		private final WekaServiceParametersModel parametersConversion;

		public WekaResourceDemandModel(LinearRegression classifier, WekaServiceParametersModel parametersConversion) {
			this.classifier = classifier;
			this.parametersConversion = parametersConversion;
		}

		@Override
		public double estimate(ServiceCall serviceCall) {
			Instance parametersInstance = this.parametersConversion.buildInstance(serviceCall.getParameters(), 0);
			try {
				return this.classifier.classifyInstance(parametersInstance);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String getResourceDemandStochasticExpression() {
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
	
	@Override
	public ResourceDemandModel estimateResourceDemandModel(String internalActionId, String resourceId,
			Map<ServiceParameters, Double> resourceDemands) {
		try {
			return this.internEstimateResourceDemandModel(internalActionId, resourceId, resourceDemands);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ResourceDemandModel internEstimateResourceDemandModel(String internalActionId, String resourceId,
			Map<ServiceParameters, Double> resourceDemands) throws Exception {
		ServiceParameters prototypeParameters = resourceDemands.keySet().iterator().next();

		Attribute classAttribute = new Attribute("resourceDemand");
		WekaServiceParametersModel parametersConversion = new WekaServiceParametersModel(prototypeParameters,
				classAttribute);
		Instances dataset = parametersConversion.buildDataSet();

		for (Entry<ServiceParameters, Double> rdEntry : resourceDemands.entrySet()) {
			Instance dataPoint = parametersConversion.buildInstance(rdEntry.getKey(), rdEntry.getValue());
			dataset.add(dataPoint);
		}

		System.out.println("Estimating resource demand for internal action id " + internalActionId + " and resource id "
				+ resourceId + ".");

		LinearRegression linReg = new LinearRegression();

		Evaluation evaluation = new Evaluation(dataset);
		int folds = dataset.size() / 10;
		folds = Math.max(folds, 2);
		evaluation.crossValidateModel(linReg, dataset, folds, new Random(1));
		System.out.println(evaluation.toSummaryString());

		linReg.buildClassifier(dataset);
		System.out.println(linReg);

		return new WekaResourceDemandModel(linReg, parametersConversion);
	}

}
