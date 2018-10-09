package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd;

import java.util.List;
import java.util.Set;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.monitoring.records.ResponseTimeRecord;

public interface ResponseTimeDataSet {

	Set<String> getInternalActionIds();

	Set<String> getResourceIds(String internalActionId);

	List<ResponseTimeRecord> getResponseTimes(String internalActionId, String resourceId);

	Long getLatestEntry();

	Long getEarliestEntry();
	
	double timeToSeconds(long time);

}