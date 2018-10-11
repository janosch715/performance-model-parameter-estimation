package tools.vitruv.applications.pcmjava.seffstatements.parameters.loop.impl;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;

public interface LoopModel {

	double estimateIterations(ServiceCall serviceCall);

	String getIterationsStochasticExpression();

}