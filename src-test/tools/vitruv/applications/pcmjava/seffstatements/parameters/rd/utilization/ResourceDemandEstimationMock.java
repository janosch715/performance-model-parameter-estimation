package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.utilization;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.ResourceDemandEstimation;

public class ResourceDemandEstimationMock implements ResourceDemandEstimation {
	@Override
	public double estimateResourceDemand(String internalActionId, String resourceId, ServiceCall serviceCall) {
		return 1.0;
	}
}