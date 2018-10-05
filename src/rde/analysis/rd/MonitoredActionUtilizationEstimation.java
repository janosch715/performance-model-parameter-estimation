package rde.analysis.rd;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import rde.analysis.KiekerServiceCallRecordFilter;
import rde.analysis.ServiceExecutionItem;
import rde.analysis.branch.WekaBranchModelRepository;
import rde.analysis.loop.WekaLoopModelRepository;

public class MonitoredActionUtilizationEstimation {

	private final Repository pcmRepository; 
	private final KiekerServiceCallRecordFilter serviceCallsRepository;
	private final KiekerResponseTimeFilter responseTimeRepository;
	private final WekaLoopModelRepository loopEstimation;
	private final WekaBranchModelRepository branchEstimation;
	private final WekaResourceDemandModelRepository rdEstimation;
	private final KiekerCpuUtilizationFilter cpuUtilizationRepository;

	public MonitoredActionUtilizationEstimation( 
			Repository pcmRepository, 
			KiekerServiceCallRecordFilter serviceCallsRepository,
			KiekerResponseTimeFilter responseTimeRepository,
			KiekerCpuUtilizationFilter cpuUtilizationRepository,
			WekaLoopModelRepository loopEstimation,
			WekaBranchModelRepository branchEstimation,
			WekaResourceDemandModelRepository rdEstimation) {
		this.pcmRepository = pcmRepository;
		this.serviceCallsRepository = serviceCallsRepository;
		this.responseTimeRepository = responseTimeRepository;
		this.cpuUtilizationRepository = cpuUtilizationRepository;
		this.loopEstimation = loopEstimation;
		this.branchEstimation = branchEstimation;
		this.rdEstimation = rdEstimation;
	}

	public void estimateMonitoredActionUtilization(String resourceId) {
		Set<String> monitoredInternalActionIds = 
				this.responseTimeRepository.getInternalActionIds();
		
		List<ServiceExecutionItem> monitoredServiceCalls = this.serviceCallsRepository.getServiceCalls();
		
		UtilizationEstimation utilizationEstimation = 
				new UtilizationEstimation(
						monitoredInternalActionIds, 
						pcmRepository, 
						monitoredServiceCalls, 
						loopEstimation, 
						branchEstimation, 
						rdEstimation);
		
		SortedMap<Long, Double> utilizationRecords = 
				this.cpuUtilizationRepository.getUtilization(resourceId);
		
		SortedMap<Long, Double> monitoredActionsUtilization =
				new TreeMap<Long, Double>();
		
		Entry<Long, Double> lastUtilizationRecord = null;
		for (Entry<Long, Double> utilizationRecord : utilizationRecords.entrySet()) {
			if (lastUtilizationRecord != null) {
				Map<ProcessingResourceType, Double> currentNotMonitoredUtilization = 
						utilizationEstimation.estimate(lastUtilizationRecord.getKey(), utilizationRecord.getKey());
				
				Optional<Entry<ProcessingResourceType, Double>> currentNotMonitoredUtilizationOfResourceEntry = 
					currentNotMonitoredUtilization.entrySet().stream()
						.filter(d -> d.getKey().getId().equals(resourceId))
						.findFirst();
				
				Double currentNotMonitoredUtilizationOfResource = 
						currentNotMonitoredUtilizationOfResourceEntry.get().getValue();
				
				Double currentMonitoredActionUtilization = 
						utilizationRecord.getValue() - currentNotMonitoredUtilizationOfResource;
				
				currentMonitoredActionUtilization = Math.max(currentMonitoredActionUtilization, 0.0);
				monitoredActionsUtilization.put(utilizationRecord.getKey(), currentMonitoredActionUtilization);
			}
			lastUtilizationRecord = utilizationRecord;
		}
	}
}
