package tools.vitruv.applications.pcmjava.seffstatements.parameters;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.MonitoringDataSet;

public class KiekerMonitoringReaderTest {
	
	@BeforeClass
	public static void setUp() {
		// create appender
		ConsoleAppender console = new ConsoleAppender(new PatternLayout("%d [%p|%c|%C{1}] %m%n")); 
		console.setThreshold(Level.ALL);
		console.activateOptions();
		Logger.getRootLogger().addAppender(console);
	}
	
	@Test
	public void kiekerReadTest() {
		MonitoringDataSet reader = new KiekerMonitoringReader("./test-data/withnames");
		
		// Check service ids
		Set<String> serviceIds = reader.getServiceCalls().getServiceIds();
		
		Set<String> expectedServiceIds = new HashSet<String>();
		expectedServiceIds.add("A.methodA");
		expectedServiceIds.add("B.methodB1");
		
		assertEquals(expectedServiceIds, serviceIds);
		
		// Check caller ids
		Set<String> callerIds = reader.getServiceCalls().getCallerIds();
		
		Set<String> expectedCallerIds = new HashSet<String>();
		expectedCallerIds.add("externalB.methodB1");
		expectedCallerIds.add("<not set>");
		
		assertEquals(expectedCallerIds, callerIds);
		
		// Check loop ids
		Set<String> loopIds = reader.getLoops().getLoopIds();
		
		Set<String> expectedLoopIds = new HashSet<String>();
		expectedLoopIds.add("A.methodA-loop-0");
		
		assertEquals(expectedLoopIds, loopIds);
		
		// Check branch ids
		Set<String> branchIds = reader.getBranches().getBranchIds();
		
		Set<String> expectedBranchIds = new HashSet<String>();
		expectedBranchIds.add("A.methodA-branch-0");
		
		assertEquals(expectedBranchIds, branchIds);
		
		// Check resource demand ids
		Set<String> resourceDemandIds = reader.getResponseTimes().getInternalActionIds();
		
		Set<String> expectedResourceDemandIds = new HashSet<String>();
		expectedResourceDemandIds.add("A.methodA-internal-0");
		expectedResourceDemandIds.add("A.methodA-internal-1");
		
		assertEquals(expectedResourceDemandIds, resourceDemandIds);
	}
}
