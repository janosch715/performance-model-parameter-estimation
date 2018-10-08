package rde.analysis.rd.impl;

import rde.analysis.ServiceParameters;

public interface ResourceDemandModel {

	double estimate(ServiceParameters parameters);

}