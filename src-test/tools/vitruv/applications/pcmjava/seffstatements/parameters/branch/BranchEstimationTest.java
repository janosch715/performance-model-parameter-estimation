package tools.vitruv.applications.pcmjava.seffstatements.parameters.branch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.GuardedBranchTransition;
import org.palladiosimulator.pcm.seff.SeffFactory;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.LoggingUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParametersUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.impl.BranchEstimationImpl;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.impl.KiekerMonitoringReader;

public class BranchEstimationTest {

	@BeforeClass
	public static void setUp() {
		LoggingUtil.InitConsoleLogger();
	}

	private BranchEstimationImpl branchEstimation;
	private BranchAction branchAction;

	@Before
	public void setUpTest() {
		this.branchEstimation = new BranchEstimationImpl(new Random(1));
		this.branchAction = this.createBranchAction();
	}

	@Test
	public void estimateBranchExecutedTest() {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/withnames");

		this.branchEstimation.updateModels(reader.getCallRecordRepository(), reader.getBranchRepository());

		Optional<AbstractBranchTransition> result = this.branchEstimation.estimateBranch(this.branchAction,
				ServiceParametersUtil.buildServiceCall("a", 3));

		assertTrue(result.isPresent());
		assertEquals("A.methodA-branch-0-0", result.get().getId());
	}

	@Test
	public void estimateNoBranchExecutedTest() {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/withnames");

		this.branchEstimation.updateModels(reader.getCallRecordRepository(), reader.getBranchRepository());

		Optional<AbstractBranchTransition> result = this.branchEstimation.estimateBranch(this.branchAction,
				ServiceParametersUtil.buildServiceCall("a", 6));

		assertFalse(result.isPresent());
	}

	private BranchAction createBranchAction() {
		BranchAction branchAction = SeffFactory.eINSTANCE.createBranchAction();
		branchAction.setId("A.methodA-branch-0");
		GuardedBranchTransition branchTransition = SeffFactory.eINSTANCE.createGuardedBranchTransition();
		branchTransition.setId("A.methodA-branch-0-0");
		branchAction.getBranches_Branch().add(branchTransition);
		return branchAction;
	}
}
