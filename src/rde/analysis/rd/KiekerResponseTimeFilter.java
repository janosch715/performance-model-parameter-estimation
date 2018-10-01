package rde.analysis.rd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import monitoring.records.ResponseTimeRecord;

@Plugin(description = "A filter for response time records.")
public final class KiekerResponseTimeFilter extends AbstractFilterPlugin {

	private static class ResponseTimeItem {
		private final Double responseTime;
		private final ResponseTimeRecord record;
		
		public ResponseTimeItem(ResponseTimeRecord record) {
			this.responseTime = (record.getStopTime() - record.getStartTime()) / 1.0e9;
			this.record = record;
		}
		
		public Double getStartTime() {
			return this.record.getStartTime() / 1.0e9; 
		}
		
		public Double getResponseTime() {
			return this.responseTime; 
		}
	}
	
	private final Map<String, ArrayList<ResponseTimeItem>> resourceDemandIdToResponseTimeRecord;
	
	public Set<String> getResourceDemandIds() {
		return this.resourceDemandIdToResponseTimeRecord.keySet();
	}
	
	private Double earliestEntry = Double.POSITIVE_INFINITY;
	private Double latestEntry = Double.NEGATIVE_INFINITY;

	public KiekerResponseTimeFilter(Configuration configuration, IProjectContext projectContext) {
		super(configuration, projectContext);
		this.resourceDemandIdToResponseTimeRecord = 
				new HashMap<String, ArrayList<ResponseTimeItem>>();
	}
	
	public Double getLatestEntry() {
		return this.latestEntry;
	}
	
	public Double getEarliestEntry() {
		return this.earliestEntry;
	}

	/**
	 * The name of the input port for incoming events. 
	 */
	public static final String INPUT_PORT_NAME_EVENTS = "inputEvent";
	
	@InputPort(
			name = INPUT_PORT_NAME_EVENTS, 
			description = "Input for response time records.", 
			eventTypes = { ResponseTimeRecord.class })
	public final void inputEvent(final ResponseTimeRecord record) {
		String resourceDemandId = record.getResourceDemandId();
		ArrayList<ResponseTimeItem> responseTimeRecords = this.resourceDemandIdToResponseTimeRecord.get(resourceDemandId);
		if (responseTimeRecords == null) {
			responseTimeRecords = new ArrayList<ResponseTimeItem>();
			this.resourceDemandIdToResponseTimeRecord.put(resourceDemandId, responseTimeRecords);
		}
		ResponseTimeItem item = new ResponseTimeItem(record);
		responseTimeRecords.add(item);
		
		this.earliestEntry = Math.min(this.earliestEntry, item.getStartTime());
		this.latestEntry = Math.max(this.latestEntry, item.getStartTime());
	}

	@Override
	public Configuration getCurrentConfiguration() {
		return new Configuration();
	}
}