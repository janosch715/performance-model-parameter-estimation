package rde.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import monitoring.records.ServiceCallRecord;

@Plugin(description = "A filter for service call records.")
public final class KiekerServiceCallRecordFilter extends AbstractFilterPlugin {

	/**
	 * The name of the input port for incoming events.
	 */
	public static final String INPUT_PORT_NAME_EVENTS = "inputEvent";

	private final Map<String, ServiceExecutionItem> serviceExecutionIdToCall;
	private final Map<String, ArrayList<ServiceExecutionItem>> serviceIdToCall;
	private final Map<String, Map<String, ArrayList<ServiceExecutionItem>>> callerIdToServiceIdToCall;
	private final List<ServiceExecutionItem> allServiceCalls;

	public List<ServiceExecutionItem> getServiceCalls() {
		return this.allServiceCalls;
	}
	
	public Set<String> getServiceIds() {
		return this.serviceIdToCall.keySet();
	}

	public Set<String> getCallerIds() {
		return this.callerIdToServiceIdToCall.keySet();
	}
	
	public ServiceParameters getParametersOfServiceCall(String serviceExecutionId) {
		ServiceExecutionItem executionItem = this.serviceExecutionIdToCall.get(serviceExecutionId);
		if (executionItem == null) {
			throw new IllegalArgumentException(String.format("The service call with id %s does not exist.", serviceExecutionId));
		}
		return executionItem.getParameters();
	}

	public List<ServiceParameters> getParametersOfService(String serviceId) {
		List<ServiceExecutionItem> serviceExecutions = this.serviceIdToCall.get(serviceId);
		if (serviceExecutions == null) {
			throw new IllegalArgumentException(String.format("The service with id %s does not exist.", serviceId));
		}
		return serviceExecutions.stream().map(d -> d.getParameters()).collect(Collectors.toList());
	}

	public Set<String> getServiceIdsForCallerId(String callerId) {
		Map<String, ArrayList<ServiceExecutionItem>> serviceIdToCalls = this.callerIdToServiceIdToCall.get(callerId);
		if (serviceIdToCalls == null) {
			return Collections.emptySet();
		}
		return serviceIdToCalls.keySet();
	}

	public KiekerServiceCallRecordFilter(Configuration configuration, IProjectContext projectContext) {
		super(configuration, projectContext);
		this.serviceExecutionIdToCall = new HashMap<String, ServiceExecutionItem>();
		this.serviceIdToCall = new HashMap<String, ArrayList<ServiceExecutionItem>>();
		this.callerIdToServiceIdToCall = new HashMap<String, Map<String, ArrayList<ServiceExecutionItem>>>();
		this.allServiceCalls = new ArrayList<ServiceExecutionItem>();
	}

	@InputPort(name = INPUT_PORT_NAME_EVENTS, description = "Input for service call records.", eventTypes = {
			ServiceCallRecord.class })
	public final void inputEvent(final ServiceCallRecord record) {
		ServiceExecutionItem item = new ServiceExecutionItem(record);
		
		this.allServiceCalls.add(item);
		
		this.serviceExecutionIdToCall.put(record.getServiceExecutionId(), item);

		ArrayList<ServiceExecutionItem> serviceIdToCallList = this.serviceIdToCall.get(record.getServiceId());
		if (serviceIdToCallList == null) {
			serviceIdToCallList = new ArrayList<ServiceExecutionItem>();
			this.serviceIdToCall.put(record.getServiceId(), serviceIdToCallList);
		}
		serviceIdToCallList.add(item);

		Map<String, ArrayList<ServiceExecutionItem>> callerIdToServiceId = this.callerIdToServiceIdToCall
				.get(record.getCallerId());
		if (callerIdToServiceId == null) {
			callerIdToServiceId = new HashMap<String, ArrayList<ServiceExecutionItem>>();
			this.callerIdToServiceIdToCall.put(record.getCallerId(), callerIdToServiceId);
		}

		ArrayList<ServiceExecutionItem> callerIdCalls = callerIdToServiceId.get(record.getServiceId());
		if (callerIdCalls == null) {
			callerIdCalls = new ArrayList<ServiceExecutionItem>();
			callerIdToServiceId.put(record.getServiceId(), callerIdCalls);
		}
		callerIdCalls.add(item);
	}

	@Override
	public Configuration getCurrentConfiguration() {
		return new Configuration();
	}
}