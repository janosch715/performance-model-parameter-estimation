package tools.vitruv.applications.pcmjava.seffstatements.parameters.branch;

import java.util.List;
import java.util.Set;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.monitoring.records.BranchRecord;

public interface BranchDataSet {

	Set<String> getBranchIds();

	List<BranchRecord> getBranchRecords(String branchId);
	
	String getBranchNotExecutedId();

}