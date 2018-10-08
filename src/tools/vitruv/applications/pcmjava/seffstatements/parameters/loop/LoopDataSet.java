package tools.vitruv.applications.pcmjava.seffstatements.parameters.loop;

import java.util.List;
import java.util.Set;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.monitoring.records.LoopRecord;

public interface LoopDataSet {

	List<LoopRecord> getLoopRecords(String loopId);

	Set<String> getLoopIds();

}
