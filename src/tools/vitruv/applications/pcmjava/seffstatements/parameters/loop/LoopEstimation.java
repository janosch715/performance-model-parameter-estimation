package tools.vitruv.applications.pcmjava.seffstatements.parameters.loop;

import org.palladiosimulator.pcm.seff.LoopAction;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;

public interface LoopEstimation {

	double estimateIterations(LoopAction loop, ServiceCall serviceCall);

}