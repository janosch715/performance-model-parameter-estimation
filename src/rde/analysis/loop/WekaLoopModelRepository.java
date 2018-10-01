package rde.analysis.loop;

import java.util.HashMap;
import java.util.Map;

import org.palladiosimulator.pcm.seff.LoopAction;

import rde.analysis.ServiceParameters;

public class WekaLoopModelRepository {
	private final Map<String, WekaLoopModel> modelCache;
	
	public WekaLoopModelRepository() {
		this.modelCache = new HashMap<String, WekaLoopModel>();
	}
	
	public void add(Map<String, WekaLoopModel> models) {
		this.modelCache.putAll(models);
	}
	
	public double estimateIterations(LoopAction loop, ServiceParameters serviceParameters) {
		WekaLoopModel loopModel = this.modelCache.get(loop.getId());
		if (loopModel == null) {
			throw new IllegalArgumentException("A estimation for loop with id " + loop.getId() + " was not found.");
		}
		return loopModel.estimateIterations(serviceParameters);
	}
}
