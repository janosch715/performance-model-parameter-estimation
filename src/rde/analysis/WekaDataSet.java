package rde.analysis;

import rde.analysis.util.WekaServiceParametersModel;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class WekaDataSet {

	private final ServiceCallDataSet serviceCallRepository;
	
	private WekaServiceParametersModel parametersConversion;
	
	private Instances dataset;
	
	public WekaDataSet(
			ServiceCallDataSet serviceCallRepository, 
			String initialServiceExecutionId,
			Attribute classAttribute) {
		this.serviceCallRepository = serviceCallRepository;

		ServiceParameters firstRecordParameters = this.serviceCallRepository
				.getParametersOfServiceCall(initialServiceExecutionId);
		
		this.parametersConversion = new WekaServiceParametersModel(firstRecordParameters, classAttribute);
		this.dataset = this.parametersConversion.buildDataSet();
	}
	
	public void addInstance(String serviceExecutionId, double classValue) {
		ServiceParameters recordParameters = 
				this.serviceCallRepository.getParametersOfServiceCall(serviceExecutionId);
		Instance dataPoint = this.parametersConversion.buildInstance(recordParameters, classValue);
		this.dataset.add(dataPoint);
	}
	
	public Instances getDataSet() {
		return this.dataset;
	}
	
	public WekaServiceParametersModel getParametersConversion() {
		return parametersConversion;
	}
}
