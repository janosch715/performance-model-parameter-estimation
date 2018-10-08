package rde.analysis.loop;

import java.util.HashMap;
import java.util.Map;

import org.palladiosimulator.pcm.seff.LoopAction;

import rde.analysis.ServiceCall;
import rde.analysis.ServiceParameters;

public class LoopEstimationImpl implements LoopEstimation {
	private final Map<String, LoopModel> modelCache;
	
	public LoopEstimationImpl() {
		this.modelCache = new HashMap<String, LoopModel>();
	}
	
	public void add(Map<String, WekaLoopModel> models) {
		this.modelCache.putAll(models);
	}
	
	@Override
	public double estimateIterations(LoopAction loop, ServiceCall serviceCall) {
		LoopModel loopModel = this.modelCache.get(loop.getId());
		if (loopModel == null) {
			throw new IllegalArgumentException("A estimation for loop with id " + loop.getId() + " was not found.");
		}
		return loopModel.estimateIterations(serviceCall.getParameters());
	}
}
