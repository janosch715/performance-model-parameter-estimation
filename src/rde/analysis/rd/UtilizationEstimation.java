package rde.analysis.rd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.LoopAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;

import rde.analysis.ServiceExecutionItem;
import rde.analysis.ServiceParameters;
import rde.analysis.branch.WekaBranchModelRepository;
import rde.analysis.loop.WekaLoopModelRepository;

public class UtilizationEstimation {

	private static class ServiceCallRdEstimation {
		private final Map<ProcessingResourceType, Double> resourceDemands;
		private final ServiceExecutionItem serviceCall;

		public ServiceCallRdEstimation(ServiceExecutionItem serviceCall,
				Map<ProcessingResourceType, Double> resourceDemands) {
			this.resourceDemands = resourceDemands;
			this.serviceCall = serviceCall;
		}

	}

	private final Set<String> ignoredInternalActionIds;

	private final Repository pcmRepository;

	private final Map<String, ResourceDemandingSEFF> serviceIdToSeff;

	private final List<ServiceExecutionItem> serviceCalls;

	private final WekaLoopModelRepository loopEstimation;

	private final WekaBranchModelRepository branchEstimation;

	private final WekaResourceDemandModelRepository rdEstimation;

	private final SortedMap<Long, List<ServiceCallRdEstimation>> estimations;

	private long firstServiceCallOn = Long.MAX_VALUE;

	private long lastServiceCallOn = Long.MIN_VALUE;

	public UtilizationEstimation(Set<String> ignoredInternalActionIds, Repository pcmRepository,
			List<ServiceExecutionItem> serviceCalls, WekaLoopModelRepository loopEstimation,
			WekaBranchModelRepository branchEstimation, WekaResourceDemandModelRepository rdEstimation) {

		this.ignoredInternalActionIds = ignoredInternalActionIds;
		this.pcmRepository = pcmRepository;
		this.serviceCalls = serviceCalls;
		this.loopEstimation = loopEstimation;
		this.branchEstimation = branchEstimation;
		this.rdEstimation = rdEstimation;
		this.estimations = new TreeMap<Long, List<ServiceCallRdEstimation>>();

		this.serviceIdToSeff = this.pcmRepository.getComponents__Repository().stream()
				.filter(BasicComponent.class::isInstance).map(component -> (BasicComponent) component)
				.flatMap(component -> component.getServiceEffectSpecifications__BasicComponent().stream())
				.filter(ResourceDemandingSEFF.class::isInstance).map(component -> (ResourceDemandingSEFF) component)
				.collect(Collectors.toMap(seff -> seff.getDescribedService__SEFF().getId(), seff -> seff));

		this.estimate();
	}

	public Map<ProcessingResourceType, Double> estimate(long fromInclusive, long toExclusive) {
		Map<ProcessingResourceType, Double> utilization = new HashMap<ProcessingResourceType, Double>();
		for (Entry<Long, List<ServiceCallRdEstimation>> serviceCallRdEstimations : this.estimations
				.subMap(fromInclusive, toExclusive).entrySet()) {
			for (ServiceCallRdEstimation serviceCallRdEstimation : serviceCallRdEstimations.getValue()) {
				addResourceDemands(utilization, serviceCallRdEstimation.resourceDemands);
			}
		}
		long interval = toExclusive - fromInclusive;
		multiplyResourceDemands(utilization, 1.0 / interval);
		return utilization;
	}

	private void estimate() {
		for (ServiceExecutionItem serviceCall : this.serviceCalls) {
			Map<ProcessingResourceType, Double> estimatedRds = this
					.estimateResourceDemand(serviceCall.getRecord().getServiceId(), serviceCall.getParameters());
			ServiceCallRdEstimation item = new ServiceCallRdEstimation(serviceCall, estimatedRds);

			long entryTime = serviceCall.getRecord().getEntryTime();
			List<ServiceCallRdEstimation> rdsForTime = this.estimations.get(entryTime);
			if (rdsForTime == null) {
				rdsForTime = new ArrayList<UtilizationEstimation.ServiceCallRdEstimation>();
				this.estimations.put(entryTime, rdsForTime);
			}
			rdsForTime.add(item);

			this.firstServiceCallOn = Math.min(this.firstServiceCallOn, entryTime);

			this.lastServiceCallOn = Math.max(this.lastServiceCallOn, entryTime);
		}
	}

	private Map<ProcessingResourceType, Double> estimateResourceDemand(String serviceId,
			ServiceParameters serviceParameters) {
		ResourceDemandingBehaviour seff = this.serviceIdToSeff.get(serviceId);
		if (seff == null) {
			throw new IllegalArgumentException("No seff for service id " + serviceId + " found.");
		}
		Map<ProcessingResourceType, Double> resourceDemands = new HashMap<ProcessingResourceType, Double>();
		this.estimateSeffResourceDemand(seff, serviceParameters, resourceDemands);
		return resourceDemands;
	}

	private void estimateSeffResourceDemand(ResourceDemandingBehaviour seff, ServiceParameters serviceParameters,
			Map<ProcessingResourceType, Double> resourceDemands) {
		StartAction startAction = findStartAction(seff);
		this.estimateResourceDemand(startAction.getSuccessor_AbstractAction(), serviceParameters, resourceDemands);
	}

	private void estimateResourceDemand(AbstractAction action, ServiceParameters serviceParameters,
			Map<ProcessingResourceType, Double> resourceDemands) {
		AbstractAction currentAction = action;
		while (true) {
			if (currentAction instanceof BranchAction) {
				currentAction = this.estimateBranchResourceDemand((BranchAction) currentAction, serviceParameters,
						resourceDemands);
			} else if (currentAction instanceof LoopAction) {
				currentAction = this.estimateLoopResourceDemand((LoopAction) currentAction, serviceParameters,
						resourceDemands);
			} else if (currentAction instanceof InternalAction) {
				currentAction = this.estimateInternalActionResourceDemand((InternalAction) currentAction,
						serviceParameters, resourceDemands);
			} else if (currentAction instanceof StopAction) {
				return;
			} else {
				throw new UnsupportedOperationException(
						"Dont know how to handle " + currentAction.eClass().getName() + ".");
			}
		}
	}

	private AbstractAction estimateInternalActionResourceDemand(InternalAction internalAction,
			ServiceParameters serviceParameters, Map<ProcessingResourceType, Double> resourceDemands) {
		if (this.ignoredInternalActionIds.contains(internalAction.getId()) == false) {
			for (ParametricResourceDemand rd : internalAction.getResourceDemand_Action()) {
				double estimatedRd = this.rdEstimation.estimateResourceDemand(internalAction.getId(), rd,
						serviceParameters);
				addResourceDemands(resourceDemands, rd.getRequiredResource_ParametricResourceDemand(), estimatedRd);
			}
		}
		return internalAction.getSuccessor_AbstractAction();
	}

	private AbstractAction estimateBranchResourceDemand(BranchAction branchAction, ServiceParameters serviceParameters,
			Map<ProcessingResourceType, Double> resourceDemands) {
		AbstractBranchTransition estimatedBranch = this.branchEstimation.estimateBranch(branchAction,
				serviceParameters);
		ResourceDemandingBehaviour branchSeff = estimatedBranch.getBranchBehaviour_BranchTransition();
		this.estimateSeffResourceDemand(branchSeff, serviceParameters, resourceDemands);
		return branchAction.getSuccessor_AbstractAction();
	}

	private AbstractAction estimateLoopResourceDemand(LoopAction loopAction, ServiceParameters serviceParameters,
			Map<ProcessingResourceType, Double> resourceDemands) {
		double iterations = this.loopEstimation.estimateIterations(loopAction, serviceParameters);
		Map<ProcessingResourceType, Double> innerLoopResourceDemands = new HashMap<ProcessingResourceType, Double>();
		ResourceDemandingBehaviour loopSeff = loopAction.getBodyBehaviour_Loop();
		this.estimateSeffResourceDemand(loopSeff, serviceParameters, innerLoopResourceDemands);
		multiplyResourceDemands(innerLoopResourceDemands, iterations);
		addResourceDemands(resourceDemands, innerLoopResourceDemands);
		return loopAction.getSuccessor_AbstractAction();
	}

	private static void addResourceDemands(Map<ProcessingResourceType, Double> rds,
			Map<ProcessingResourceType, Double> rdsToAdd) {
		for (Entry<ProcessingResourceType, Double> rd : rdsToAdd.entrySet()) {
			addResourceDemands(rds, rd.getKey(), rd.getValue());
		}
	}

	private static void addResourceDemands(Map<ProcessingResourceType, Double> rds, ProcessingResourceType resourceType,
			double resourceDemand) {
		Double rdValue = rds.get(resourceType);
		if (rdValue != null) {
			rdValue = 0.0;
		}
		rdValue += resourceDemand;
		rds.put(resourceType, rdValue);
	}

	private static void multiplyResourceDemands(Map<ProcessingResourceType, Double> rds, double factor) {
		for (Entry<ProcessingResourceType, Double> rd : rds.entrySet()) {
			rds.put(rd.getKey(), rd.getValue() * factor);
		}
	}

	private static StartAction findStartAction(ResourceDemandingBehaviour seff) {
		List<StartAction> foundStartActions = seff.getSteps_Behaviour().stream().filter(StartAction.class::isInstance)
				.map(startAction -> (StartAction) startAction).collect(Collectors.toList());

		if (foundStartActions.size() == 0) {
			throw new IllegalArgumentException("The seff " + seff.getId() + " does not have a start action.");
		}

		if (foundStartActions.size() > 1) {
			throw new IllegalArgumentException("The seff " + seff.getId() + " has multiple start actions.");
		}

		return foundStartActions.get(0);
	}
}
