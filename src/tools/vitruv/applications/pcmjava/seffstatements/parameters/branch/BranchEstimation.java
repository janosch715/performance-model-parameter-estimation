package tools.vitruv.applications.pcmjava.seffstatements.parameters.branch;

import java.util.Optional;

import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;

public interface BranchEstimation {

	Optional<AbstractBranchTransition> estimateBranch(BranchAction branch, ServiceCall serviceCall);

}