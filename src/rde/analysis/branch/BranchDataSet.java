package rde.analysis.branch;

import java.util.List;
import java.util.Set;

import monitoring.records.BranchRecord;

public interface BranchDataSet {

	Set<String> getBranchIds();

	List<BranchRecord> getBranchRecords(String branchId);

}