package rde.analysis.branch;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import rde.analysis.ServiceParameters;

public class WekaBranchModelRepository {
	private final Map<String, WekaBranchModel> modelCache;
	
	public WekaBranchModelRepository() {
		this.modelCache = new HashMap<String, WekaBranchModel>();
	}
	
	public void add(Map<String, WekaBranchModel> models) {
		this.modelCache.putAll(models);
	}
	
	public AbstractBranchTransition estimateBranch(BranchAction branch, ServiceParameters serviceParameters) {
		WekaBranchModel branchModel = this.modelCache.get(branch.getId());
		if (branchModel == null) {
			throw new IllegalArgumentException("An estimation for branch with id " + branch.getId() + " was not found.");
		}
		String estimatedBranchId = branchModel.estimateBranchId(serviceParameters);
		
		Optional<AbstractBranchTransition> estimatedBranch = branch.getBranches_Branch().stream()
				.filter(transition -> transition.getId().equals(estimatedBranchId))
				.findFirst();
		
		if (estimatedBranch.isPresent() == false) {
			throw new IllegalArgumentException("The estimated branch transition with id " + estimatedBranchId + " does not exist in SEFF.");
		}
		
		return estimatedBranch.get();
	}
}
