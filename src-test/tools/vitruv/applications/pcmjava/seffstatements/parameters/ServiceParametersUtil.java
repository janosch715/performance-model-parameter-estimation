package tools.vitruv.applications.pcmjava.seffstatements.parameters;

import java.util.HashMap;
import java.util.Map;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParameters;

public class ServiceParametersUtil {
	public static ServiceParameters buildParameters(String name, Object value) {
		Map<String, Object> testParameters = new HashMap<String, Object>();
		testParameters.put(name, value);
		return ServiceParameters.build(testParameters);
	}
	
	public static ServiceCall buildServiceCall(String parameterName, Object parameterValue) {
		return new ServiceCallMock(buildParameters(parameterName, parameterValue));
	}
}
