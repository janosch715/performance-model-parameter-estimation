package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl;

import java.util.Map;
import java.util.Random;
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

public class WekaResourceDemandModelEstimation {

	private static class WekaResourceDemandModel implements ResourceDemandModel {

		private final Classifier classifier;
		private final WekaServiceParametersModel parametersConversion;

		public WekaResourceDemandModel(Classifier classifier, WekaServiceParametersModel parametersConversion) {
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
	}
	
	private ResourceDemandModel estimateResourceDemandModel(String internalActionId, String resourceId,
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

		// 10-fold cross-validation
		Evaluation evaluation = new Evaluation(dataset);
		evaluation.crossValidateModel(linReg, dataset, 10, new Random(1));
		System.out.println(evaluation.toSummaryString());

		linReg.buildClassifier(dataset);
		System.out.println(linReg);

		return new WekaResourceDemandModel(linReg, parametersConversion);
	}

	
}
