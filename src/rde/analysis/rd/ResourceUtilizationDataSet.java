package rde.analysis.rd;

import java.util.Set;
import java.util.SortedMap;

public interface ResourceUtilizationDataSet {
	SortedMap<Long, Double> getUtilization(String resourceId);
	Set<String> getResourceIds();
}
