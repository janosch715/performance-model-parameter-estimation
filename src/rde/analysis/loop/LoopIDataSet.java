package rde.analysis.loop;

import java.util.List;
import java.util.Set;

import monitoring.records.LoopRecord;

public interface LoopIDataSet {

	List<LoopRecord> getLoopRecords(String loopId);

	Set<String> getLoopIds();

}
