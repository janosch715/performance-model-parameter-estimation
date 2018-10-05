package rde.analysis.loop;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;
import rde.analysis.KiekerMonitoringReader;
import rde.analysis.ServiceParameters;
import rde.analysis.loop.LoopIterationEstimation;
import rde.analysis.loop.WekaLoopModel;

public class LoopIterationEstimationTest {

	@BeforeClass
	public static void setUp() {
		// create appender
		ConsoleAppender console = new ConsoleAppender(new PatternLayout("%d [%p|%c|%C{1}] %m%n"));
		console.setThreshold(Level.ALL);
		console.activateOptions();
		Logger.getRootLogger().addAppender(console);
	}

	@Test
	public void estimateLoopIterationTest() {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/withnames");
		
		LoopIterationEstimation estimation = 
				new LoopIterationEstimation(reader.getCallRecordRepository(), reader.getLoopRepository());
		
		Map<String, WekaLoopModel> loopResult = estimation.estimateAll();
		
		Map<String, Object> testParametersA12 = new HashMap<String, Object>();
		testParametersA12.put("a", 12);
		ServiceParameters testServiceParametersA12 = ServiceParameters.build(testParametersA12);
		
		double loopEstimationResult = loopResult.get("A.methodA-loop-0").estimateIterations(testServiceParametersA12);
		assertEquals(12.0, loopEstimationResult, 10e-5);
	}
}
