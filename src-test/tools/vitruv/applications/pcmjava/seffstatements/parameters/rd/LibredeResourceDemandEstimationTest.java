package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.LoggingUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParametersUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl.LibredeResourceDemandEstimation;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl.ResourceDemandModel;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl.WekaParametricDependencyEstimationStrategy;

public class LibredeResourceDemandEstimationTest {

	private WekaParametricDependencyEstimationStrategy modelEstimationStartegy;

	@BeforeClass
	public static void setUp() {
		LoggingUtil.InitConsoleLogger();
	}

	@Before
	public void before() {
		this.modelEstimationStartegy = new WekaParametricDependencyEstimationStrategy();
	}

	@Test
	public void estimateResourceDemandsTest() {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/simple");

		LibredeResourceDemandEstimation estimation = new LibredeResourceDemandEstimation(this.modelEstimationStartegy,
				reader.getCpuRepository(), reader.getResponseTimeRepository(), reader.getCallRecordRepository());

		Map<String, Map<String, Map<ServiceParameters, Double>>> results = estimation.estimateResourceDemands();

		assertEquals(2, results.size());
		assertEquals(1, results.get("_dhstIMjSEeiWRYm1yDC5rQ").size());
		assertEquals(1, results.get("_OkrUMMjSEeiWRYm1yDC5rQ").size());

		Map<ServiceParameters, Double> rds1 = results.get("_OkrUMMjSEeiWRYm1yDC5rQ").get("_oro4gG3fEdy4YaaT-RYrLQ");
		assertEquals(10, rds1.size());
		assertEquals(0.0, (double) rds1.get(ServiceParametersUtil.buildParameters("a", 0)), 0.000001);
		assertEquals(0.00108, (double) rds1.get(ServiceParametersUtil.buildParameters("a", 9)), 0.000001);

		Map<ServiceParameters, Double> rds2 = results.get("_dhstIMjSEeiWRYm1yDC5rQ").get("_oro4gG3fEdy4YaaT-RYrLQ");
		assertEquals(9, rds2.size());
		assertEquals(0.0003, (double) rds2.get(ServiceParametersUtil.buildParameters("a", 1)), 0.0001);
		assertEquals(0.0003, (double) rds2.get(ServiceParametersUtil.buildParameters("a", 9)), 0.0001);
	}

	@Test
	public void estimateResourceDemandModelsTest() {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/simple");

		LibredeResourceDemandEstimation estimation = new LibredeResourceDemandEstimation(this.modelEstimationStartegy,
				reader.getCpuRepository(), reader.getResponseTimeRepository(), reader.getCallRecordRepository());

		Map<String, Map<String, ResourceDemandModel>> results = estimation.estimateResourceDemandModels();

		assertEquals(2, results.size());
		assertEquals(1, results.get("_dhstIMjSEeiWRYm1yDC5rQ").size());
		assertEquals(1, results.get("_OkrUMMjSEeiWRYm1yDC5rQ").size());

		ResourceDemandModel rds1 = results.get("_OkrUMMjSEeiWRYm1yDC5rQ").get("_oro4gG3fEdy4YaaT-RYrLQ");
		assertEquals(0.0, (double) rds1.estimate(ServiceParametersUtil.buildServiceCall("a", 0)), 0.0001);
		assertEquals(0.00108, (double) rds1.estimate(ServiceParametersUtil.buildServiceCall("a", 9)), 0.0001);

		ResourceDemandModel rds2 = results.get("_dhstIMjSEeiWRYm1yDC5rQ").get("_oro4gG3fEdy4YaaT-RYrLQ");
		assertEquals(0.0003, (double) rds2.estimate(ServiceParametersUtil.buildServiceCall("a", 1)), 0.0001);
		assertEquals(0.0003, (double) rds2.estimate(ServiceParametersUtil.buildServiceCall("a", 9)), 0.0001);
	}

	@Test
	public void buildConfigTest() throws Exception {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/withnames");

		LibredeResourceDemandEstimation estimation = new LibredeResourceDemandEstimation(this.modelEstimationStartegy,
				reader.getCpuRepository(), reader.getResponseTimeRepository(), reader.getCallRecordRepository());

		Files.deleteIfExists(Paths.get("./test-data/withnames/temp.librede"));
		estimation.saveConfig("./test-data/withnames/temp.librede");
	}
}
