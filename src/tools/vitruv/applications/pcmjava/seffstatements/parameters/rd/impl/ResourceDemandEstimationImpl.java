package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
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

	private static final Logger LOGGER = Logger.getLogger(ResourceDemandEstimationImpl.class);
	private final Map<String, Map<String, ResourceDemandModel>> modelCache;
	private final ParametricDependencyEstimationStrategy parametricDependencyEstimationStrategy;
	private final LoopEstimation loopEstimation;
	private final BranchEstimation branchEstimation;

	public ResourceDemandEstimationImpl(LoopEstimation loopEstimation, BranchEstimation branchEstimation) {
		this.modelCache = new HashMap<String, Map<String, ResourceDemandModel>>();
		this.parametricDependencyEstimationStrategy = new WekaParametricDependencyEstimationStrategy();
		this.loopEstimation = loopEstimation;
		this.branchEstimation = branchEstimation;
	}

	public void updateModels(Repository pcmRepository, ServiceCallDataSet serviceCalls,
			ResourceUtilizationDataSet resourceUtilizations, ResponseTimeDataSet responseTimes) {

		Set<String> internalActionsToEstimate = responseTimes.getInternalActionIds();
		
		if (internalActionsToEstimate.isEmpty()) {
			LOGGER.info("No internal action records in data set. So resource demand estimation is skipped.");
			return;
		}

		ResourceUtilizationEstimation resourceUtilizationEstimation = new ResourceUtilizationEstimationImpl(
				internalActionsToEstimate, pcmRepository, serviceCalls, this.loopEstimation, this.branchEstimation,
				this);

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
