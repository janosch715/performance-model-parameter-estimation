package tools.vitruv.applications.pcmjava.seffstatements.parameters;

public interface ServiceCall {

	ServiceParameters getParameters();
	
	String getServiceExecutionId();
	
	String getServiceId();
	
	String getCallerServiceExecutionId();
	
	String getCallerId();
	
	long getEntryTime();
	
	long getExitTime();
}
