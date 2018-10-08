package rde.analysis.loop.impl;

import java.util.HashMap;
import java.util.Map;

import org.palladiosimulator.pcm.seff.LoopAction;

import rde.analysis.ServiceCall;
import rde.analysis.ServiceCallDataSet;
import rde.analysis.loop.LoopEstimation;
import rde.analysis.loop.LoopIDataSet;

public class LoopEstimationImpl implements LoopEstimation {
	private final Map<String, LoopModel> modelCache;
	
	public LoopEstimationImpl() {
		this.modelCache = new HashMap<String, LoopModel>();
	}
	
	public void updateModels(ServiceCallDataSet serviceCalls,
			LoopIDataSet loopIterations) {
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
