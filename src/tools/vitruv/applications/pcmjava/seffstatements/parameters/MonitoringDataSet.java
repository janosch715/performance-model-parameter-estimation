package tools.vitruv.applications.pcmjava.seffstatements.parameters;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.BranchDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.loop.LoopDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.ResponseTimeDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.utilization.ResourceUtilizationDataSet;

public interface MonitoringDataSet {

	ResponseTimeDataSet getResponseTimes();

	ServiceCallDataSet getServiceCalls();

	ResourceUtilizationDataSet getResourceUtilizations();

	LoopDataSet getLoops();

	BranchDataSet getBranches();

}