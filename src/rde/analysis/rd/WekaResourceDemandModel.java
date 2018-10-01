package rde.analysis.rd;

import rde.analysis.ServiceParameters;
import rde.analysis.WekaServiceParametersModel;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class WekaResourceDemandModel {
	
	private final Classifier classifier;
	private final WekaServiceParametersModel parametersConversion;

	public WekaResourceDemandModel(Classifier classifier, WekaServiceParametersModel parametersConversion) {
		this.classifier = classifier;
		this.parametersConversion = parametersConversion;
	}

	public double estimateResourceDemand(ServiceParameters parameters) {
		Instance parametersInstance = this.parametersConversion.buildInstance(parameters, 0);
		try {
			return this.classifier.classifyInstance(parametersInstance);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
