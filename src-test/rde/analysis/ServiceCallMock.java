package rde.analysis;

public class ServiceCallMock implements ServiceCall {

	private ServiceParameters parameters = ServiceParameters.EMPTY;
	private String serviceExecutionId = "not set";
	private String serviceId = "not set";
	private String callerServiceExecutionId = "not set";
	private String callerId = "not set";
	private long entryTime = 0;
	private long exitTime = 0;
	
	public ServiceCallMock() {
	}
	
	public ServiceCallMock(ServiceParameters parameters) {
		this.parameters = parameters;
	}


	@Override
	public ServiceParameters getParameters() {
		return this.parameters;
	}

	@Override
	public String getServiceExecutionId() {
		return this.serviceExecutionId;
	}

	@Override
	public String getServiceId() {
		return this.serviceId;
	}

	@Override
	public String getCallerServiceExecutionId() {
		return this.callerServiceExecutionId;
	}

	@Override
	public String getCallerId() {
		return this.callerId;
	}

	@Override
	public long getEntryTime() {
		return this.entryTime;
	}

	@Override
	public long getExitTime() {
		return this.exitTime;
	}

	public void setParameters(ServiceParameters parameters) {
		this.parameters = parameters;
	}

	public void setServiceExecutionId(String serviceExecutionId) {
		this.serviceExecutionId = serviceExecutionId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public void setCallerServiceExecutionId(String callerServiceExecutionId) {
		this.callerServiceExecutionId = callerServiceExecutionId;
	}

	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}

	public void setEntryTime(long entryTime) {
		this.entryTime = entryTime;
	}

	public void setExitTime(long exitTime) {
		this.exitTime = exitTime;
	}

}
