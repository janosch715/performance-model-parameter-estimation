package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.palladiosimulator.pcm.repository.Repository;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.BranchEstimation;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.loop.LoopEstimation;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.ResourceDemandEstimation;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.ResponseTimeDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.utilization.ResourceUtilizationDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.utilization.ResourceUtilizationEstimation;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.utilization.impl.ResourceUtilizationEstimationImpl;

public class ResourceDemandEstimationImpl implements ResourceDemandEstimation {

	private final Map<String, Map<String, ResourceDemandModel>> modelCache;
	private final ParametricDependencyEstimationStrategy parametricDependencyEstimationStrategy;

	public ResourceDemandEstimationImpl() {
		this.modelCache = new HashMap<String, Map<String, ResourceDemandModel>>();
		this.parametricDependencyEstimationStrategy = new WekaParametricDependencyEstimationStrategy();
	}

	public void updateModels(Repository pcmRepository, ResourceUtilizationDataSet resourceUtilizations,
			ResponseTimeDataSet responseTimes, ServiceCallDataSet serviceCalls, LoopEstimation loopEstimation,
			BranchEstimation branchEstimation) {

		Set<String> internalActionsToEstimate = responseTimes.getInternalActionIds();

		ResourceUtilizationEstimation resourceUtilizationEstimation = new ResourceUtilizationEstimationImpl(
				internalActionsToEstimate, pcmRepository, serviceCalls, loopEstimation, branchEstimation, this);

		ResourceUtilizationDataSet remainingResourceUtilization = resourceUtilizationEstimation
				.estimateRemainingUtilization(resourceUtilizations);

		LibredeResourceDemandEstimation estimation = new LibredeResourceDemandEstimation(
				this.parametricDependencyEstimationStrategy, remainingResourceUtilization, responseTimes, serviceCalls);

		Map<String, Map<String, ResourceDemandModel>> newModels = estimation.estimateResourceDemandModels();

		modelCache.putAll(newModels);
	}

	@Override
	public double estimateResourceDemand(String internalActionId, String resourceId, ServiceCall serviceCall) {
		Map<String, ResourceDemandModel> resourceModels = this.modelCache.get(internalActionId);
		if (resourceModels == null) {
			throw new IllegalArgumentException("An estimation for resource demand with internal action id "
					+ internalActionId + " was not found.");
		}
		ResourceDemandModel rdModel = resourceModels.get(resourceId);
		if (rdModel == null) {
			throw new IllegalArgumentException("An estimation for resource demand for resource id " + resourceId
					+ " for internal action id " + internalActionId + " was not found.");
		}
		return rdModel.estimate(serviceCall);
	}
}
