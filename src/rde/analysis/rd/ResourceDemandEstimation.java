package rde.analysis.rd;

import rde.analysis.ServiceCall;
import rde.analysis.ServiceParameters;

public interface ResourceDemandEstimation {
	double estimateResourceDemand(String internalActionId, String resourceId,
			ServiceCall serviceCall);
}