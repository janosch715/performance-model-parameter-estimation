package tools.vitruv.applications.pcmjava.seffstatements.parameters;

import org.palladiosimulator.pcm.repository.Repository;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.impl.BranchEstimationImpl;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.loop.impl.LoopEstimationImpl;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl.ResourceDemandEstimationImpl;

public class SeffParameterEstimation {

	private final LoopEstimationImpl loopEstimation;

	private final BranchEstimationImpl branchEstimation;

	private final ResourceDemandEstimationImpl resourceDemandEstimation;

	public SeffParameterEstimation() {
		this.loopEstimation = new LoopEstimationImpl();
		this.branchEstimation = new BranchEstimationImpl();
		this.resourceDemandEstimation = new ResourceDemandEstimationImpl(this.loopEstimation, this.branchEstimation);
	}

	public void updateModels(Repository pcmModel, MonitoringDataSet monitoringDataSet) {
		this.loopEstimation.updateModels(monitoringDataSet.getServiceCalls(), monitoringDataSet.getLoops());
		this.branchEstimation.updateModels(monitoringDataSet.getServiceCalls(), monitoringDataSet.getBranches());
		this.resourceDemandEstimation.updateModels(pcmModel, monitoringDataSet.getServiceCalls(),
				monitoringDataSet.getResourceUtilizations(), monitoringDataSet.getResponseTimes());
	}
}
