package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.utilization;

import java.util.Set;
import java.util.SortedMap;

public interface ResourceUtilizationDataSet {
	SortedMap<Long, Double> getUtilization(String resourceId);
	Set<String> getResourceIds();
	double timeToSeconds(long time);
}
