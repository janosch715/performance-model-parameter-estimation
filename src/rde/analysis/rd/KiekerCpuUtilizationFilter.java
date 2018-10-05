package rde.analysis.rd;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.record.system.CPUUtilizationRecord;

@Plugin(description = "A filter for cpu utilization records.")
public final class KiekerCpuUtilizationFilter extends AbstractFilterPlugin implements ResourceUtilizationRepository {

	private final Map<String, SortedMap<Long, Double>> cpuUtilization;
	
	public KiekerCpuUtilizationFilter(Configuration configuration, IProjectContext projectContext) {
		super(configuration, projectContext);
		this.cpuUtilization = new HashMap<String, SortedMap<Long, Double>>();
	}
	
	@Override
	public SortedMap<Long, Double> getUtilization(String resourceId) {
		return this.cpuUtilization.get(resourceId);
	}
	
	@Override
	public Set<String> getResourceIds() {
		return this.cpuUtilization.keySet();
	}

	/**
	 * The name of the input port for incoming events. 
	 */
	public static final String INPUT_PORT_NAME_EVENTS = "inputEvent";
	
	@InputPort(
			name = INPUT_PORT_NAME_EVENTS, 
			description = "Input for cpu utilization records.", 
			eventTypes = { CPUUtilizationRecord.class })
	public final void inputEvent(final CPUUtilizationRecord record) {
		if (record.getCpuID().equals("0") == false) {
			return;
		}
		String cpuId = "_oro4gG3fEdy4YaaT-RYrLQ";
		
		SortedMap<Long, Double> singleCpuUtilization = this.cpuUtilization.get(cpuId);
		if (singleCpuUtilization == null) {
			singleCpuUtilization = new TreeMap<Long, Double>();
			this.cpuUtilization.put(cpuId, singleCpuUtilization);
		}
		singleCpuUtilization.put(record.getTimestamp(), record.getTotalUtilization());
	}

	@Override
	public Configuration getCurrentConfiguration() {
		return new Configuration();
	}
}