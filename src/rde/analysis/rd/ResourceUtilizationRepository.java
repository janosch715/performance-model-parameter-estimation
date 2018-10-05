package rde.analysis.rd;

import java.util.Set;
import java.util.SortedMap;

public interface ResourceUtilizationRepository {
	SortedMap<Long, Double> getUtilization(String resourceId);
	Set<String> getResourceIds();
}
