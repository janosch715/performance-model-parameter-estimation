package rde.analysis.branch.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;

import rde.analysis.ServiceCall;
import rde.analysis.branch.BranchEstimation;

public class BranchEstimationImpl implements BranchEstimation {
	private final Map<String, BranchModel> modelCache;
	
	public BranchEstimationImpl() {
		this.modelCache = new HashMap<String, BranchModel>();
	}
	
	public void add(Map<String, BranchModel> models) {
		this.modelCache.putAll(models);
	}
	
	@Override
	public AbstractBranchTransition estimateBranch(BranchAction branch, ServiceCall serviceCall) {
		BranchModel branchModel = this.modelCache.get(branch.getId());
		if (branchModel == null) {
			throw new IllegalArgumentException("An estimation for branch with id " + branch.getId() + " was not found.");
		}
		String estimatedBranchId = branchModel.estimateBranchId(serviceCall);
		
		Optional<AbstractBranchTransition> estimatedBranch = branch.getBranches_Branch().stream()
				.filter(transition -> transition.getId().equals(estimatedBranchId))
				.findFirst();
		
		if (estimatedBranch.isPresent() == false) {
			throw new IllegalArgumentException("The estimated branch transition with id " + estimatedBranchId + " does not exist in SEFF.");
		}
		
		return estimatedBranch.get();
	}
}
