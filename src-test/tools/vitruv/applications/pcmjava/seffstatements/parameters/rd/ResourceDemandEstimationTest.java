package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd;

import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.palladiosimulator.pcm.repository.Repository;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.LoggingUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParametersUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl.ResourceDemandEstimationImpl;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.util.PcmUtils;

public class ResourceDemandEstimationTest {

	@BeforeClass
	public static void setUp() {
		LoggingUtil.InitConsoleLogger();
	}

	@Test
	public void estimateAllTest() {
		MonitoringDataSet reader = new KiekerMonitoringReader("./test-data/simple");
		Repository pcmModel = PcmUtils.loadModel("./test-data/simple/default.repository");

		ResourceDemandEstimationImpl rdEstimation = new ResourceDemandEstimationImpl(new LoopEstimationMock(),
				new BranchEstimationMock());
		rdEstimation.updateModels(pcmModel, reader.getServiceCalls(), reader.getResourceUtilizations(),
				reader.getResponseTimes());

		double result1 = rdEstimation.estimateResourceDemand("_OkrUMMjSEeiWRYm1yDC5rQ", "_oro4gG3fEdy4YaaT-RYrLQ",
				ServiceParametersUtil.buildServiceCall("a", 1));
		assertEquals(0.00001, result1, 0.0001);

		double result2 = rdEstimation.estimateResourceDemand("_OkrUMMjSEeiWRYm1yDC5rQ", "_oro4gG3fEdy4YaaT-RYrLQ",
				ServiceParametersUtil.buildServiceCall("a", 8));
		assertEquals(0.00081, result2, 0.0001);
	}

}
