package rde.analysis.rd;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import kieker.common.record.system.CPUUtilizationRecord;

@Plugin(description = "A filter for cpu utilization records.")
public final class KiekerCpuUtilizationFilter extends AbstractFilterPlugin {

	private final Map<String, TreeMap<Double, Double>> cpuUtilization;
	
	public KiekerCpuUtilizationFilter(Configuration configuration, IProjectContext projectContext) {
		super(configuration, projectContext);
		this.cpuUtilization = new HashMap<String, TreeMap<Double, Double>>();
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
		TreeMap<Double, Double> singleCpuUtilization = this.cpuUtilization.get(record.getCpuID());
		if (singleCpuUtilization == null) {
			singleCpuUtilization = new TreeMap<Double, Double>();
			this.cpuUtilization.put(record.getCpuID(), singleCpuUtilization);
		}
		singleCpuUtilization.put(record.getTimestamp() / 1.0e9, record.getTotalUtilization());
	}

	@Override
	public Configuration getCurrentConfiguration() {
		return new Configuration();
	}
}