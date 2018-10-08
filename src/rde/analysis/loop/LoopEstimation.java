package rde.analysis.loop;

import org.palladiosimulator.pcm.seff.LoopAction;

import rde.analysis.ServiceCall;

public interface LoopEstimation {

	double estimateIterations(LoopAction loop, ServiceCall serviceCall);

}