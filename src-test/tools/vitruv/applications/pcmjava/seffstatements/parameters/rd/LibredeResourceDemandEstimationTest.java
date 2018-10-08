package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.LoggingUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl.LibredeResourceDemandEstimation;

public class LibredeResourceDemandEstimationTest {

	@BeforeClass
	public static void setUp() {
		LoggingUtil.InitConsoleLogger();
	}

	@Test
	public void estimateLoopIterationTest() {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/simple");
		
		LibredeResourceDemandEstimation estimation = 
			new LibredeResourceDemandEstimation(
					reader.getCpuRepository(), 
					reader.getResponseTimeRepository(), 
					reader.getCallRecordRepository());
		
		estimation.saveConfig("./test-data/simple/temp.librede");
		
		estimation.estimate();
	}
	
	@Test
	public void estimateLoopIterationTest2() {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/withnames");
		
		LibredeResourceDemandEstimation estimation = 
			new LibredeResourceDemandEstimation(
					reader.getCpuRepository(), 
					reader.getResponseTimeRepository(), 
					reader.getCallRecordRepository());
		
		estimation.saveConfig("./test-data/withnames/temp.librede");
	}
}
