package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;

public interface ResourceDemandEstimation {
	double estimateResourceDemand(String internalActionId, String resourceId,
			ServiceCall serviceCall);
}