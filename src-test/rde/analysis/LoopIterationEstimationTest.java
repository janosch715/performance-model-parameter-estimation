package rde.analysis;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;

import junit.framework.Assert;
import kieker.analysis.AnalysisController;
import kieker.analysis.IAnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;
import rde.analysis.branch.BranchExecutionEstimation;
import rde.analysis.branch.KiekerBranchExecutionFilter;
import rde.analysis.branch.WekaBranchModel;
import rde.analysis.loop.KiekerLoopIterationFilter;
import rde.analysis.loop.LoopIterationEstimation;
import rde.analysis.loop.WekaLoopModel;
import rde.analysis.rd.KiekerCpuUtilizationFilter;
import rde.analysis.rd.KiekerResponseTimeFilter;

public class LoopIterationEstimationTest {

	@BeforeClass
	public static void setUp() {
		// create appender
		ConsoleAppender console = new ConsoleAppender(new PatternLayout("%d [%p|%c|%C{1}] %m%n"));
		console.setThreshold(Level.ALL);
		console.activateOptions();
		Logger.getRootLogger().addAppender(console);
	}

	private ResourceDemandingSEFF createSEFFWithAbstractActions(final AbstractAction... abstractActions) {
		final ResourceDemandingSEFF expectedSeff = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
		this.addActionsToBehaviour(expectedSeff, abstractActions);
		return expectedSeff;
	}

	private void addActionsToBehaviour(final ResourceDemandingBehaviour behaviour,
			final AbstractAction... abstractActions) {
		behaviour.getSteps_Behaviour().add(SeffFactory.eINSTANCE.createStartAction());

		for (final AbstractAction abstractAction : abstractActions) {
			behaviour.getSteps_Behaviour().add(abstractAction);
		}

		behaviour.getSteps_Behaviour().add(SeffFactory.eINSTANCE.createStopAction());
	}

	@Test
	public void kiekerReadTest() throws Exception {
		// Create Kieker.Analysis instance
		final IAnalysisController analysisInstance = new AnalysisController();

		// Set filesystem monitoring log input directory for our analysis
		final Configuration fsReaderConfig = new Configuration();
		fsReaderConfig.setProperty(FSReader.CONFIG_PROPERTY_NAME_INPUTDIRS, "D:\\Users\\JP\\AppData\\Local\\Temp\\kieker-20180926-090336-690232280810686-UTC--KIEKER-SINGLETON");
		final FSReader reader = new FSReader(fsReaderConfig, analysisInstance);

		// record filter
		KiekerResponseTimeFilter responseTimeFilter = new KiekerResponseTimeFilter(new Configuration(),
				analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, responseTimeFilter,
				KiekerResponseTimeFilter.INPUT_PORT_NAME_EVENTS);

		// record filter
		KiekerServiceCallRecordFilter callRecordFilter = new KiekerServiceCallRecordFilter(new Configuration(),
				analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, callRecordFilter,
				KiekerServiceCallRecordFilter.INPUT_PORT_NAME_EVENTS);

		// record filter
		KiekerCpuUtilizationFilter cpuFilter = new KiekerCpuUtilizationFilter(new Configuration(), analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, cpuFilter,
				KiekerCpuUtilizationFilter.INPUT_PORT_NAME_EVENTS);

		// record filter
		KiekerLoopIterationFilter loopFilter = new KiekerLoopIterationFilter(new Configuration(), analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, loopFilter,
				KiekerLoopIterationFilter.INPUT_PORT_NAME_EVENTS);

		// record filter
		KiekerBranchExecutionFilter branchFilter = new KiekerBranchExecutionFilter(new Configuration(),
				analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, branchFilter,
				KiekerBranchExecutionFilter.INPUT_PORT_NAME_EVENTS);

		// Start the analysis
		analysisInstance.run();
		
		LoopIterationEstimation estimation = 
				new LoopIterationEstimation(callRecordFilter, loopFilter);
		
		Map<String, WekaLoopModel> loopResult = estimation.estimateAll();
		
		Map<String, Object> testParametersA12 = new HashMap<String, Object>();
		testParametersA12.put("a", 12);
		ServiceParameters testServiceParametersA12 = ServiceParameters.build(testParametersA12);
		
		Map<String, Object> testParametersA3 = new HashMap<String, Object>();
		testParametersA3.put("a", 3);
		ServiceParameters testServiceParametersA3 = ServiceParameters.build(testParametersA3);
		
		double loopEstimationResult = loopResult.get("A.methodA-loop-0").estimateIterations(testServiceParametersA12);
		assertEquals(12.0, loopEstimationResult, 10e-5);
		
		BranchExecutionEstimation branchEstimation = 
				new BranchExecutionEstimation(callRecordFilter, branchFilter, new Random(0));
		
		Map<String, WekaBranchModel> branchResult = branchEstimation.estimateAll();
		
		String branchEstimationResult = branchResult.get("A.methodA-branch-0").estimateBranchId(testServiceParametersA3);
		assertEquals("A.methodA-branch-0-0", branchEstimationResult);
	}
}
