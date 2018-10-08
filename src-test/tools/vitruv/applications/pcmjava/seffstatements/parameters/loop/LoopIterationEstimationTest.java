package tools.vitruv.applications.pcmjava.seffstatements.parameters.loop;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.palladiosimulator.pcm.seff.LoopAction;
import org.palladiosimulator.pcm.seff.SeffFactory;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.LoggingUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParametersUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.loop.impl.LoopEstimationImpl;

public class LoopIterationEstimationTest {

	@BeforeClass
	public static void setUp() {
		LoggingUtil.InitConsoleLogger();
	}

	private LoopEstimationImpl loopEstimation;
	private LoopAction loopAction;

	@Before
	public void setUpTest() {
		this.loopEstimation = new LoopEstimationImpl();
		this.loopAction = this.createLoopAction();
	}

	@Test
	public void estimateLoopIterationTest() {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/withnames");

		this.loopEstimation.updateModels(reader.getCallRecordRepository(), reader.getLoopRepository());

		double loopEstimationResult = this.loopEstimation.estimateIterations(this.loopAction,
				ServiceParametersUtil.buildServiceCall("a", 12));

		assertEquals(12.0, loopEstimationResult, 10e-5);
	}

	private LoopAction createLoopAction() {
		LoopAction loopAction = SeffFactory.eINSTANCE.createLoopAction();
		loopAction.setId("A.methodA-loop-0");
		return loopAction;
	}
}
