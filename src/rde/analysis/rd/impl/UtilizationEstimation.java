package rde.analysis.rd.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.Repository;
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

import rde.analysis.ServiceCallDataSet;
import rde.analysis.ServiceCall;
import rde.analysis.branch.WekaBranchModelRepository;
import rde.analysis.loop.LoopEstimation;
import rde.analysis.rd.ResourceDemandEstimation;

public class UtilizationEstimation {

	private static class ServiceCallRdEstimation {
		private final Map<String, Double> resourceDemands;
		private final ServiceCall serviceCall;

		public ServiceCallRdEstimation(ServiceCall serviceCall,
				Map<String, Double> resourceDemands) {
			this.resourceDemands = resourceDemands;
			this.serviceCall = serviceCall;
		}

	}

	private final Set<String> ignoredInternalActionIds;

	private final Repository pcmRepository;

	private final Map<String, ResourceDemandingSEFF> serviceIdToSeff;

	private final ServiceCallDataSet serviceCallRepository;

	private final LoopEstimation loopEstimation;

	private final WekaBranchModelRepository branchEstimation;

	private final ResourceDemandEstimation rdEstimation;

	private final SortedMap<Long, List<ServiceCallRdEstimation>> estimations;

	private long firstServiceCallOn = Long.MAX_VALUE;

	private long lastServiceCallOn = Long.MIN_VALUE;

	public UtilizationEstimation(Set<String> ignoredInternalActionIds, Repository pcmRepository,
			ServiceCallDataSet serviceCallRepository, LoopEstimation loopEstimation,
			WekaBranchModelRepository branchEstimation, ResourceDemandEstimation rdEstimation) {

		this.ignoredInternalActionIds = ignoredInternalActionIds;
		this.pcmRepository = pcmRepository;
		this.serviceCallRepository = serviceCallRepository;
		this.loopEstimation = loopEstimation;
		this.branchEstimation = branchEstimation;
		this.rdEstimation = rdEstimation;
		this.estimations = new TreeMap<Long, List<ServiceCallRdEstimation>>();

		this.serviceIdToSeff = this.pcmRepository.getComponents__Repository().stream()
				.filter(BasicComponent.class::isInstance).map(component -> (BasicComponent) component)
				.flatMap(component -> component.getServiceEffectSpecifications__BasicComponent().stream())
				.filter(ResourceDemandingSEFF.class::isInstance).map(component -> (ResourceDemandingSEFF) component)
				.collect(Collectors.toMap(seff -> seff.getDescribedService__SEFF().getId(), seff -> seff));

		this.estimateAllResourceDemands();
	}
	
	public SortedMap<Long, Double> estimateRemainingUtilization(
			String resourceId, 
			SortedMap<Long, Double> completeResourceUtilization) {
		
		SortedMap<Long, Double> monitoredActionsUtilization =
				new TreeMap<Long, Double>();
		
		Entry<Long, Double> lastUtilizationRecord = null;
		for (Entry<Long, Double> utilizationRecord : completeResourceUtilization.entrySet()) {
			if (lastUtilizationRecord != null) {
				Map<String, Double> currentNotMonitoredUtilization = 
						this.estimateUtilization(lastUtilizationRecord.getKey(), utilizationRecord.getKey());
				
				Optional<Entry<String, Double>> currentNotMonitoredUtilizationOfResourceEntry = 
					currentNotMonitoredUtilization.entrySet().stream()
						.filter(d -> d.getKey().equals(resourceId))
						.findFirst();
				
				if (currentNotMonitoredUtilizationOfResourceEntry.isPresent() == false) {
					continue;
				}
				Double currentNotMonitoredUtilizationOfResource = 
						currentNotMonitoredUtilizationOfResourceEntry.get().getValue();
				
				Double currentMonitoredActionUtilization = 
						utilizationRecord.getValue() - currentNotMonitoredUtilizationOfResource;
				
				currentMonitoredActionUtilization = Math.max(currentMonitoredActionUtilization, 0.0);
				monitoredActionsUtilization.put(utilizationRecord.getKey(), currentMonitoredActionUtilization);
			}
			lastUtilizationRecord = utilizationRecord;
		}
		return monitoredActionsUtilization;
	}

	public Map<String, Double> estimateUtilization(long fromInclusive, long toExclusive) {
		Map<String, Double> utilization = new HashMap<String, Double>();
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

	private void estimateAllResourceDemands() {
		for (ServiceCall serviceCall : this.serviceCallRepository.getServiceCalls()) {
			Map<String, Double> estimatedRds = this
					.estimateResourceDemand(serviceCall);
			ServiceCallRdEstimation item = new ServiceCallRdEstimation(serviceCall, estimatedRds);

			long entryTime = serviceCall.getEntryTime();
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

	private Map<String, Double> estimateResourceDemand(ServiceCall serviceCall) {
		ResourceDemandingBehaviour seff = this.serviceIdToSeff.get(serviceCall.getServiceId());
		if (seff == null) {
			throw new IllegalArgumentException("No seff for service id " + serviceCall.getServiceId() + " found.");
		}
		Map<String, Double> resourceDemands = new HashMap<String, Double>();
		this.estimateSeffResourceDemand(seff, serviceCall, resourceDemands);
		return resourceDemands;
	}

	private void estimateSeffResourceDemand(ResourceDemandingBehaviour seff, ServiceCall serviceCall,
			Map<String, Double> resourceDemands) {
		StartAction startAction = findStartAction(seff);
		this.estimateResourceDemand(startAction.getSuccessor_AbstractAction(), serviceCall, resourceDemands);
	}

	private void estimateResourceDemand(AbstractAction action, ServiceCall serviceCall,
			Map<String, Double> resourceDemands) {
		AbstractAction currentAction = action;
		while (true) {
			if (currentAction instanceof BranchAction) {
				currentAction = this.estimateBranchResourceDemand((BranchAction) currentAction, serviceCall,
						resourceDemands);
			} else if (currentAction instanceof LoopAction) {
				currentAction = this.estimateLoopResourceDemand((LoopAction) currentAction, serviceCall,
						resourceDemands);
			} else if (currentAction instanceof InternalAction) {
				currentAction = this.estimateInternalActionResourceDemand((InternalAction) currentAction,
						serviceCall, resourceDemands);
			} else if (currentAction instanceof StopAction) {
				return;
			} else {
				throw new UnsupportedOperationException(
						"Dont know how to handle " + currentAction.eClass().getName() + ".");
			}
		}
	}

	private AbstractAction estimateInternalActionResourceDemand(InternalAction internalAction,
			ServiceCall serviceCall, Map<String, Double> resourceDemands) {
		if (this.ignoredInternalActionIds.contains(internalAction.getId()) == false) {
			for (ParametricResourceDemand rd : internalAction.getResourceDemand_Action()) {
				String resourceId = rd.getRequiredResource_ParametricResourceDemand().getId();
				double estimatedRd = this.rdEstimation.estimateResourceDemand(internalAction.getId(), resourceId,
						serviceCall);
				addResourceDemands(resourceDemands, rd.getRequiredResource_ParametricResourceDemand().getId(), estimatedRd);
			}
		}
		return internalAction.getSuccessor_AbstractAction();
	}

	private AbstractAction estimateBranchResourceDemand(BranchAction branchAction, ServiceCall serviceCall,
			Map<String, Double> resourceDemands) {
		AbstractBranchTransition estimatedBranch = this.branchEstimation.estimateBranch(branchAction,
				serviceCall);
		ResourceDemandingBehaviour branchSeff = estimatedBranch.getBranchBehaviour_BranchTransition();
		this.estimateSeffResourceDemand(branchSeff, serviceCall, resourceDemands);
		return branchAction.getSuccessor_AbstractAction();
	}

	private AbstractAction estimateLoopResourceDemand(LoopAction loopAction, ServiceCall serviceCall,
			Map<String, Double> resourceDemands) {
		double iterations = this.loopEstimation.estimateIterations(loopAction, serviceCall);
		Map<String, Double> innerLoopResourceDemands = new HashMap<String, Double>();
		ResourceDemandingBehaviour loopSeff = loopAction.getBodyBehaviour_Loop();
		this.estimateSeffResourceDemand(loopSeff, serviceCall, innerLoopResourceDemands);
		multiplyResourceDemands(innerLoopResourceDemands, iterations);
		addResourceDemands(resourceDemands, innerLoopResourceDemands);
		return loopAction.getSuccessor_AbstractAction();
	}

	private static void addResourceDemands(Map<String, Double> rds,
			Map<String, Double> rdsToAdd) {
		for (Entry<String, Double> rd : rdsToAdd.entrySet()) {
			addResourceDemands(rds, rd.getKey(), rd.getValue());
		}
	}

	private static void addResourceDemands(Map<String, Double> rds, String resourceId,
			double resourceDemand) {
		Double rdValue = rds.get(resourceId);
		if (rdValue != null) {
			rdValue = 0.0;
		}
		rdValue += resourceDemand;
		rds.put(resourceId, rdValue);
	}

	private static void multiplyResourceDemands(Map<String, Double> rds, double factor) {
		for (Entry<String, Double> rd : rds.entrySet()) {
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
