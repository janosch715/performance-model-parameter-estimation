package tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.impl;

import java.util.Optional;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCall;

public interface BranchModel {

	Optional<String> estimateBranchId(ServiceCall serviceCall);

}