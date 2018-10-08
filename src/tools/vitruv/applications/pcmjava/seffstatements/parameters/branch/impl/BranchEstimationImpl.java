package tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.BranchDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.BranchEstimation;

public class BranchEstimationImpl implements BranchEstimation {
	private final Map<String, BranchModel> modelCache;
	private final Random random;
	
	public BranchEstimationImpl() {
		this(ThreadLocalRandom.current());
	}
	
	public BranchEstimationImpl(Random random) {
		this.modelCache = new HashMap<String, BranchModel>();
		this.random = random;
	}
	
	public void updateModels(ServiceCallDataSet serviceCalls,
			BranchDataSet branchExecutions) {
		WekaBranchModelEstimation estimation = 
				new WekaBranchModelEstimation(serviceCalls, branchExecutions, this.random);
		
		Map<String, BranchModel> branchModels = estimation.estimateAll();
		
		this.modelCache.putAll(branchModels);
	}
	
	@Override
	public Optional<AbstractBranchTransition> estimateBranch(BranchAction branch, ServiceCall serviceCall) {
		BranchModel branchModel = this.modelCache.get(branch.getId());
		if (branchModel == null) {
			throw new IllegalArgumentException("An estimation for branch with id " + branch.getId() + " was not found.");
		}
		Optional<String> estimatedBranchId = branchModel.estimateBranchId(serviceCall);
		
		if (estimatedBranchId.isPresent() == false) {
			return Optional.empty();
		}
		
		Optional<AbstractBranchTransition> estimatedBranch = branch.getBranches_Branch().stream()
				.filter(transition -> transition.getId().equals(estimatedBranchId.get()))
				.findFirst();
		
		if (estimatedBranch.isPresent() == false) {
			throw new IllegalArgumentException("The estimated branch transition with id " + estimatedBranchId.get() + " does not exist in SEFF.");
		}
		
		return Optional.of(estimatedBranch.get());
	}
}
