package tools.vitruv.applications.pcmjava.seffstatements.parameters.loop.impl;

import java.util.HashMap;
import java.util.Map;

import org.palladiosimulator.pcm.seff.LoopAction;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.loop.LoopDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.loop.LoopEstimation;

public class LoopEstimationImpl implements LoopEstimation {
	private final Map<String, LoopModel> modelCache;
	
	public LoopEstimationImpl() {
		this.modelCache = new HashMap<String, LoopModel>();
	}
	
	public void updateModels(ServiceCallDataSet serviceCalls,
			LoopDataSet loopIterations) {
		WekaLoopModelEstimation estimation = 
				new WekaLoopModelEstimation(serviceCalls, loopIterations);
		
		Map<String, LoopModel> loopModels = estimation.estimateAll();
		
		this.modelCache.putAll(loopModels);
	}
	
	@Override
	public double estimateIterations(LoopAction loop, ServiceCall serviceCall) {
		LoopModel loopModel = this.modelCache.get(loop.getId());
		if (loopModel == null) {
			throw new IllegalArgumentException("A estimation for loop with id " + loop.getId() + " was not found.");
		}
		return loopModel.estimateIterations(serviceCall);
	}
}
