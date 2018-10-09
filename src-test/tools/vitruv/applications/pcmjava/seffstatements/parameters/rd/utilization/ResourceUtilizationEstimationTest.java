package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.utilization;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import org.palladiosimulator.pcm.resourcetype.ResourceRepository;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.LoggingUtil;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.PcmUtils;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.BranchEstimationMock;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.LoopEstimationMock;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.utilization.impl.ResourceUtilizationEstimationImpl;

public class ResourceUtilizationEstimationTest {

	public void getResourceIds() {
		Repository pcmModel = PcmUtils.loadModel("./test-data/simple/default.repository");
		List<ProcessingResourceType> seffIdToSeff = pcmModel.getComponents__Repository().stream()
				.filter(BasicComponent.class::isInstance).map(component -> (BasicComponent) component)
				.flatMap(component -> component.getServiceEffectSpecifications__BasicComponent().stream())
				.filter(ResourceDemandingSEFF.class::isInstance).map(component -> (ResourceDemandingSEFF) component)
				.flatMap(seff -> seff.getSteps_Behaviour().stream()).filter(InternalAction.class::isInstance)
				.map(internalAction -> (InternalAction) internalAction)
				.flatMap(internalAction -> internalAction.getResourceDemand_Action().stream())
				.map(rd -> rd.getRequiredResource_ParametricResourceDemand()).collect(Collectors.toList());
		
		ResourceRepository asd = seffIdToSeff.get(0).getResourceRepository_ResourceType();
	}
	
	@Test
	public void estimationTest() {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/simple");
		Repository pcmModel = PcmUtils.loadModel("./test-data/simple/default.repository");

		ResourceUtilizationEstimationImpl estimation = new ResourceUtilizationEstimationImpl(Collections.emptySet(),
				pcmModel, reader.getCallRecordRepository(), new LoopEstimationMock(), new BranchEstimationMock(),
				new ResourceDemandEstimationMock());
		
		ResourceUtilizationDataSet results = estimation.estimateRemainingUtilization(reader.getCpuRepository());

		assertEquals(1.0, (double)reader.getCpuRepository().getUtilization("_oro4gG3fEdy4YaaT-RYrLQ").get(1538841944174331860L), 0.00001);
		assertEquals(0.0, (double)results.getUtilization("_oro4gG3fEdy4YaaT-RYrLQ").get(1538841944174331860L), 0.00001);
	}

	@Test
	public void checkIgnoreInternalActionsTest() {
		KiekerMonitoringReader reader = new KiekerMonitoringReader("./test-data/simple");
		Repository pcmModel = PcmUtils.loadModel("./test-data/simple/default.repository");

		Set<String> allInternalActionIds = reader.getResponseTimeRepository().getInternalActionIds();

		ResourceUtilizationEstimationImpl estimation = new ResourceUtilizationEstimationImpl(allInternalActionIds,
				pcmModel, reader.getCallRecordRepository(), new LoopEstimationMock(), new BranchEstimationMock(),
				new ResourceDemandEstimationMock());

		ResourceUtilizationDataSet results = estimation.estimateRemainingUtilization(reader.getCpuRepository());

		for (String resourceId : reader.getCpuRepository().getResourceIds()) {
			assertEquals(reader.getCpuRepository().getUtilization(resourceId), results.getUtilization(resourceId));
		}
	}
}
