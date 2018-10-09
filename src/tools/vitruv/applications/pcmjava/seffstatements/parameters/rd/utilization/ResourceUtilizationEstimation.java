package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.utilization;

public interface ResourceUtilizationEstimation {

	ResourceUtilizationDataSet estimateRemainingUtilization(ResourceUtilizationDataSet completeResourceUtilization);

}