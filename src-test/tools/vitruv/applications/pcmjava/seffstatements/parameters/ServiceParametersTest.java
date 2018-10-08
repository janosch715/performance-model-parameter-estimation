package tools.vitruv.applications.pcmjava.seffstatements.parameters;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParameters;


public class ServiceParametersTest {
	
	@Test
	public void emptyJsonTest() {
		ServiceParameters parsedParameters = ServiceParameters.buildFromJson("{}");
		assertEquals(0, parsedParameters.getParameters().size());
	}
	
	@Test
	public void nullValueTest() {
		ServiceParameters parsedParameters = ServiceParameters.buildFromJson(null);
		assertEquals(0, parsedParameters.getParameters().size());
	}
	
	@Test
	public void emptyStringTest() {
		ServiceParameters parsedParameters = ServiceParameters.buildFromJson("");
		assertEquals(0, parsedParameters.getParameters().size());
	}
	
	@Test
	public void multiValuesTest() {
		ServiceParameters parsedParameters = ServiceParameters.buildFromJson("{\"a\":1,}");
		assertEquals(1, parsedParameters.getParameters().size());
	}
}
