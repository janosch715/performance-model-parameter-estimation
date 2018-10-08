package rde.analysis.branch;

import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;

import rde.analysis.ServiceCall;

public interface BranchEstimation {

	AbstractBranchTransition estimateBranch(BranchAction branch, ServiceCall serviceCall);

}