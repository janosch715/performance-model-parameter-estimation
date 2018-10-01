package rde.analysis;

import monitoring.records.ServiceCallRecord;

public class ServiceExecutionItem {
	private final ServiceParameters parameters;
	private final ServiceCallRecord record;

	public ServiceExecutionItem(ServiceCallRecord record) {
		this.parameters = ServiceParameters.buildFromJson(record.getParameters());
		this.record = record;
	}

	public ServiceParameters getParameters() {
		return parameters;
	}

	public ServiceCallRecord getRecord() {
		return record;
	}
}
