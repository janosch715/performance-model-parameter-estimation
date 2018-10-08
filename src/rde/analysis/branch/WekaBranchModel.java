package rde.analysis.branch;

import java.util.Random;

import rde.analysis.ServiceParameters;
import rde.analysis.util.WekaServiceParametersModel;
import weka.classifiers.functions.Logistic;
import weka.core.Instance;
import weka.core.Instances;

public class WekaBranchModel {
	
	private final Logistic classifier;
	private final WekaServiceParametersModel parametersModel;
	private final Random random;

	public WekaBranchModel(
			Logistic classifier, 
			WekaServiceParametersModel parametersConversion, 
			Random random) {
		this.classifier = classifier;
		this.parametersModel = parametersConversion;
		this.random = random;
	}

	public String estimateBranchId(ServiceParameters parameters) {
		Instance parametersInstance = this.parametersModel.buildInstance(parameters, 0);
		Instances dataset = this.parametersModel.buildDataSet();
		dataset.add(parametersInstance);
		double[] branchDistribution;
		try {
			branchDistribution = this.classifier.distributionForInstance(dataset.firstInstance());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		double selectedBranchPropability = this.random.nextDouble();
		int selectedBranchIndex = 0;
		double branchPropabilitySum = 0.0;
		while (true) {
			if (selectedBranchIndex >= branchDistribution.length) {
				throw new IllegalArgumentException("The branch has propability distribution.");
			}
			branchPropabilitySum += branchDistribution[selectedBranchIndex];
			if (selectedBranchPropability < branchPropabilitySum) {
				break;
			}
			selectedBranchIndex++;
		}
		
		return this.parametersModel.getClassAttribute().value(selectedBranchIndex);
	}
}
