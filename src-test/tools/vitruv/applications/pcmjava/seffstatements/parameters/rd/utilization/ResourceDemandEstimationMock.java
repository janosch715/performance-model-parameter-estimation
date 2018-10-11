package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.utilization;

import org.palladiosimulator.pcm.repository.Repository;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.ResourceDemandEstimation;

public class ResourceDemandEstimationMock implements ResourceDemandEstimation {
	@Override
	public double estimateResourceDemand(String internalActionId, String resourceId, ServiceCall serviceCall) {
		return 1.0;
	}

	@Override
	public void applyEstimations(Repository pcmModel) {
	}
}