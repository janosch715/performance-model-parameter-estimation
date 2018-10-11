package tools.vitruv.applications.pcmjava.seffstatements.parameters.loop;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.palladiosimulator.pcm.PcmFactory;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.seff.LoopAction;
import org.palladiosimulator.pcm.seff.SeffFactory;

import de.uka.ipd.sdq.stoex.Expression;
import de.uka.ipd.sdq.stoex.StoexFactory;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.LoggingUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParametersUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.MonitoringDataSet;
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
		MonitoringDataSet reader = new KiekerMonitoringReader("./test-data/withnames");

		this.loopEstimation.updateModels(reader.getServiceCalls(), reader.getLoops());

		double loopEstimationResult = this.loopEstimation.estimateIterations(this.loopAction,
				ServiceParametersUtil.buildServiceCall("a", 12));

		assertEquals(12.0, loopEstimationResult, 10e-5);
	}
	
	@Test
	public void something() {
		//StoexFactory.eINSTANCE.creat
		PCMRandomVariable asd = CoreFactory.eINSTANCE.createPCMRandomVariable();
		asd.setSpecification("1 * a.VALUE ^ 2 * a.VALUE");
		Expression asd2 = asd.getExpression();
		String asd3 = asd2.toString();
	}
	
	
	private LoopAction createLoopAction() {
		LoopAction loopAction = SeffFactory.eINSTANCE.createLoopAction();
		loopAction.setId("A.methodA-loop-0");
		return loopAction;
	}
}
