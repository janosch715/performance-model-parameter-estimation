package rde.analysis.rd;

import java.util.List;
import java.util.Set;

import monitoring.records.ResponseTimeRecord;

public interface ResponseTimeDataSet {

	Set<String> getInternalActionIds();

	Set<String> getResourceIds(String internalActionId);

	List<ResponseTimeRecord> getResponseTimes(String internalActionId, String resourceId);

	Long getLatestEntry();

	Long getEarliestEntry();

}