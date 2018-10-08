package rde.analysis.rd.impl;

import rde.analysis.ServiceCall;

public interface ResourceDemandModel {

	double estimate(ServiceCall serviceCall);

}