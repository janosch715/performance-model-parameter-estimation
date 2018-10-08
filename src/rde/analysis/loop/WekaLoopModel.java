package rde.analysis.loop;

import rde.analysis.ServiceParameters;
import rde.analysis.util.WekaServiceParametersModel;
import weka.classifiers.Classifier;
import weka.core.Instance;

public class WekaLoopModel implements LoopModel {
	
	private final Classifier classifier;
	private final WekaServiceParametersModel parametersConversion;

	public WekaLoopModel(Classifier classifier, WekaServiceParametersModel parametersConversion) {
		this.classifier = classifier;
		this.parametersConversion = parametersConversion;
	}

	@Override
	public double estimateIterations(ServiceParameters parameters) {
		Instance parametersInstance = this.parametersConversion.buildInstance(parameters, 0);
		try {
			return this.classifier.classifyInstance(parametersInstance);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
