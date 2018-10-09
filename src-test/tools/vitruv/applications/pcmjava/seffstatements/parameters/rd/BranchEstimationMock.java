package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd;

import java.util.Optional;

import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.BranchEstimation;

public class BranchEstimationMock implements BranchEstimation {
	@Override
	public Optional<AbstractBranchTransition> estimateBranch(BranchAction branch, ServiceCall serviceCall) {
		return Optional.empty();
	}
}