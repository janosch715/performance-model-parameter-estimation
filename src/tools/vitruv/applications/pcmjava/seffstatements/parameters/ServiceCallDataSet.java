package tools.vitruv.applications.pcmjava.seffstatements.parameters;

import java.util.List;
import java.util.Set;

public interface ServiceCallDataSet {

	List<ServiceCall> getServiceCalls();

	Set<String> getServiceIds();

	Set<String> getCallerIds();

	ServiceParameters getParametersOfServiceCall(String serviceExecutionId);

	List<ServiceParameters> getParametersOfService(String serviceId);

	Set<String> getServiceIdsForCallerId(String callerId);

}