package rde.analysis;

public interface ServiceCall {

	ServiceParameters getParameters();
	
	String getServiceExecutionId();
	
	String getServiceId();
	
	String getCallerServiceExecutionId();
	
	String getCallerId();
	
	long getEntryTime();
	
	long getExitTime();
}
