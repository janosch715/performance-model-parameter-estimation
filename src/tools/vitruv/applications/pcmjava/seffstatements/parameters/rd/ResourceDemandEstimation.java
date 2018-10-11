package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd;

import org.palladiosimulator.pcm.repository.Repository;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;

public interface ResourceDemandEstimation {
	double estimateResourceDemand(String internalActionId, String resourceId,
			ServiceCall serviceCall);
	
	void applyEstimations(Repository pcmModel);
}