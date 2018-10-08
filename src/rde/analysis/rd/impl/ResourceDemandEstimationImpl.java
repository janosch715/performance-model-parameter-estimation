package rde.analysis.rd.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;

import rde.analysis.ServiceCall;
import rde.analysis.ServiceCallDataSet;
import rde.analysis.ServiceParameters;
import rde.analysis.rd.ResourceDemandEstimation;
import rde.analysis.rd.ResourceUtilizationDataSet;
import rde.analysis.rd.ResponseTimeDataSet;

public class ResourceDemandEstimationImpl implements ResourceDemandEstimation {

	private final Map<String, Map<String, ResourceDemandModel>> modelCache;

	public ResourceDemandEstimationImpl() {
		this.modelCache = new HashMap<String, Map<String, ResourceDemandModel>>();
	}

	public void updateEstimations(ResourceUtilizationDataSet resourceUtilizations,
			ResponseTimeDataSet responseTimes, ServiceCallDataSet serviceCalls) {

		
	}

	public double estimateResourceDemand(String internalActionId, String resourceId,
			ServiceCall serviceCall) {
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

	private static class ResourceUtilizationRepositoryImpl implements ResourceUtilizationDataSet {

		private Map<String, SortedMap<Long, Double>> utilization;

		public ResourceUtilizationRepositoryImpl(Map<String, SortedMap<Long, Double>> utilization) {
			this.utilization = utilization;
		}

		@Override
		public SortedMap<Long, Double> getUtilization(String resourceId) {
			return this.utilization.get(resourceId);
		}

		@Override
		public Set<String> getResourceIds() {
			return this.utilization.keySet();
		}
	}
}
