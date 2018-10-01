package rde.analysis.rd;

import java.util.HashMap;
import java.util.Map;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;

import rde.analysis.ServiceParameters;

public class WekaResourceDemandModelRepository {
	private final Map<String, WekaResourceDemandModel> modelCache;
	
	public WekaResourceDemandModelRepository() {
		this.modelCache = new HashMap<String, WekaResourceDemandModel>();
	}
	
	public void add(Map<String, WekaResourceDemandModel> models) {
		this.modelCache.putAll(models);
	}
	
	public double estimateResourceDemand(
			String internalActionId, 
			ParametricResourceDemand resourceDemand,
			ServiceParameters serviceParameters) {
		WekaResourceDemandModel rdModel = this.modelCache.get(internalActionId);
		if (rdModel == null) {
			throw new IllegalArgumentException("A estimation for resource demand with id " + internalActionId + " was not found.");
		}
		return rdModel.estimateResourceDemand(serviceParameters);
	}
}
