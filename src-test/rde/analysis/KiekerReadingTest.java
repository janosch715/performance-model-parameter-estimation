package rde.analysis;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;

import kieker.analysis.AnalysisController;
import kieker.analysis.IAnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;
import rde.analysis.branch.KiekerBranchExecutionFilter;
import rde.analysis.loop.KiekerLoopIterationFilter;
import rde.analysis.rd.KiekerCpuUtilizationFilter;
import rde.analysis.rd.KiekerResponseTimeFilter;

public class KiekerReadingTest {
	
	@BeforeClass
	public static void setUp() {
		// create appender
		ConsoleAppender console = new ConsoleAppender(new PatternLayout("%d [%p|%c|%C{1}] %m%n")); 
		console.setThreshold(Level.ALL);
		console.activateOptions();
		Logger.getRootLogger().addAppender(console);
	}
	
	@Test
	public void kiekerReadTest() throws IllegalStateException, AnalysisConfigurationException {
		// Create Kieker.Analysis instance
		final IAnalysisController analysisInstance = new AnalysisController();

		// Set filesystem monitoring log input directory for our analysis
		final Configuration fsReaderConfig = new Configuration();
		fsReaderConfig.setProperty(FSReader.CONFIG_PROPERTY_NAME_INPUTDIRS, "./kieker-test-data");
		final FSReader reader = new FSReader(fsReaderConfig, analysisInstance);

		// record filter
		KiekerResponseTimeFilter responseTimeFilter = new KiekerResponseTimeFilter(new Configuration(), analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, responseTimeFilter,
				KiekerResponseTimeFilter.INPUT_PORT_NAME_EVENTS);
		
		// record filter
		KiekerServiceCallRecordFilter callRecordFilter = new KiekerServiceCallRecordFilter(new Configuration(), analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, callRecordFilter,
				KiekerServiceCallRecordFilter.INPUT_PORT_NAME_EVENTS);

		// record filter
		KiekerCpuUtilizationFilter cpuFilter = new KiekerCpuUtilizationFilter(new Configuration(),
				analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, cpuFilter,
				KiekerCpuUtilizationFilter.INPUT_PORT_NAME_EVENTS);
		
		// record filter
		KiekerLoopIterationFilter loopFilter = new KiekerLoopIterationFilter(new Configuration(),
				analysisInstance);

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
		
		// Check service ids
		Set<String> serviceIds = callRecordFilter.getServiceIds();
		
		Set<String> expectedServiceIds = new HashSet<String>();
		expectedServiceIds.add("A.methodA");
		expectedServiceIds.add("B.methodB1");
		
		assertEquals(expectedServiceIds, serviceIds);
		
		// Check caller ids
		Set<String> callerIds = callRecordFilter.getCallerIds();
		
		Set<String> expectedCallerIds = new HashSet<String>();
		expectedCallerIds.add("externalB.methodB1");
		expectedCallerIds.add("<not set>");
		
		assertEquals(expectedCallerIds, callerIds);
		
		// Check loop ids
		Set<String> loopIds = loopFilter.getLoopIds();
		
		Set<String> expectedLoopIds = new HashSet<String>();
		expectedLoopIds.add("A.methodA-loop-0");
		
		assertEquals(expectedLoopIds, loopIds);
		
		// Check branch ids
		Set<String> branchIds = branchFilter.getBranchIds();
		
		Set<String> expectedBranchIds = new HashSet<String>();
		expectedBranchIds.add("A.methodA-branch-0");
		
		assertEquals(expectedBranchIds, branchIds);
		
		// Check resource demand ids
		Set<String> resourceDemandIds = responseTimeFilter.getInternalActionIds();
		
		Set<String> expectedResourceDemandIds = new HashSet<String>();
		expectedResourceDemandIds.add("A.methodA-internal-0");
		expectedResourceDemandIds.add("A.methodA-internal-1");
		
		assertEquals(expectedResourceDemandIds, resourceDemandIds);
	}
}
