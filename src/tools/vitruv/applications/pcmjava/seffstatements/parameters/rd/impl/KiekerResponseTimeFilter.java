package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.monitoring.records.ResponseTimeRecord;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.ResponseTimeDataSet;

@Plugin(description = "A filter for response time records.")
public final class KiekerResponseTimeFilter extends AbstractFilterPlugin implements ResponseTimeDataSet {

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
	
	private final Map<String, Map<String, ArrayList<ResponseTimeRecord>>> internalActionIdAndReosurceIdToResponseTimeRecord;
	
	@Override
	public Set<String> getInternalActionIds() {
		return this.internalActionIdAndReosurceIdToResponseTimeRecord.keySet();
	}
	
	@Override
	public Set<String> getResourceIds(String internalActionId) {
		return this.internalActionIdAndReosurceIdToResponseTimeRecord.get(internalActionId).keySet();
	}
	
	@Override
	public List<ResponseTimeRecord> getResponseTimes(String internalActionId, String resourceId) {
		return this.internalActionIdAndReosurceIdToResponseTimeRecord.get(internalActionId).get(resourceId);
	}
	
	private Long earliestEntry = Long.MAX_VALUE;
	private Long latestEntry = Long.MIN_VALUE;

	public KiekerResponseTimeFilter(Configuration configuration, IProjectContext projectContext) {
		super(configuration, projectContext);
		this.internalActionIdAndReosurceIdToResponseTimeRecord = 
				new HashMap<String, Map<String,ArrayList<ResponseTimeRecord>>>();
	}
	
	@Override
	public Long getLatestEntry() {
		return this.latestEntry;
	}
	
	@Override
	public Long getEarliestEntry() {
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
		String internalActionId = record.getInternalActionId();
		Map<String, ArrayList<ResponseTimeRecord>> resourceToResponseTimeRecord = 
				this.internalActionIdAndReosurceIdToResponseTimeRecord.get(internalActionId);
		if (resourceToResponseTimeRecord == null) {
			resourceToResponseTimeRecord = new HashMap<String, ArrayList<ResponseTimeRecord>>();
			this.internalActionIdAndReosurceIdToResponseTimeRecord.put(internalActionId, resourceToResponseTimeRecord);
		}
		
		String resourceId = record.getResourceId();
		ArrayList<ResponseTimeRecord> responseTimeRecords = 
				resourceToResponseTimeRecord.get(resourceId);
		if (responseTimeRecords == null) {
			responseTimeRecords = new ArrayList<ResponseTimeRecord>();
			resourceToResponseTimeRecord.put(resourceId, responseTimeRecords);
		}
		responseTimeRecords.add(record);
		
		this.earliestEntry = Math.min(this.earliestEntry, record.getStartTime());
		this.latestEntry = Math.max(this.latestEntry, record.getStartTime());
	}

	@Override
	public Configuration getCurrentConfiguration() {
		return new Configuration();
	}
}