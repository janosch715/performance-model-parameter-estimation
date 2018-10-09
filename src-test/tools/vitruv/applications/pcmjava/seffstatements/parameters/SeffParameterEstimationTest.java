package tools.vitruv.applications.pcmjava.seffstatements.parameters;

import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.palladiosimulator.pcm.repository.Repository;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.LoggingUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.PcmUtils;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParametersUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl.ResourceDemandEstimationImpl;

public class SeffParameterEstimationTest {

	@BeforeClass
	public static void setUp() {
		LoggingUtil.InitConsoleLogger();
	}

	@Test
	public void estimateIterationTest() {
		MonitoringDataSet reader = new KiekerMonitoringReader("./test-data/simple");
		Repository pcmModel = PcmUtils.loadModel("./test-data/simple/default.repository");

		MonitoringDataSet reader2 = new KiekerMonitoringReader("./test-data/simple-iteration");
		Repository pcmModel2 = PcmUtils.loadModel("./test-data/simple-iteration/default2.repository");

		SeffParameterEstimation estimation = new SeffParameterEstimation();
		estimation.updateModels(pcmModel, reader);
		estimation.updateModels(pcmModel2, reader2);
		
	}
}
