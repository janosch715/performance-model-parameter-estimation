package rde.analysis.branch;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;
import rde.analysis.KiekerMonitoringReader;
import rde.analysis.ServiceParameters;
import rde.analysis.branch.BranchExecutionEstimation;
import rde.analysis.branch.WekaBranchModel;

public class BranchEstimationTest {

	@BeforeClass
	public static void setUp() {
		// create appender
		ConsoleAppender console = new ConsoleAppender(new PatternLayout("%d [%p|%c|%C{1}] %m%n"));
		console.setThreshold(Level.ALL);
		console.activateOptions();
		Logger.getRootLogger().addAppender(console);
	}

	@Test
	public void estimateBranchTest() {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/withnames");
		
		Map<String, Object> testParametersA3 = new HashMap<String, Object>();
		testParametersA3.put("a", 3);
		ServiceParameters testServiceParametersA3 = ServiceParameters.build(testParametersA3);
	
		BranchExecutionEstimation branchEstimation = 
				new BranchExecutionEstimation(reader.getCallRecordRepository(), reader.getBranchRepository(), new Random(0));
		
		Map<String, WekaBranchModel> branchResult = branchEstimation.estimateAll();
		
		String branchEstimationResult = branchResult.get("A.methodA-branch-0").estimateBranchId(testServiceParametersA3);
		assertEquals("A.methodA-branch-0-0", branchEstimationResult);
	}
}
