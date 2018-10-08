package rde.analysis.util;

import java.util.ArrayList;
import java.util.Map.Entry;

import rde.analysis.ServiceParameters;

import java.util.Set;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaServiceParametersModel {
	
	private final Attribute classAttribute;
	
	private final ArrayList<Attribute> attributes;
	
	public WekaServiceParametersModel(ServiceParameters basedOnParameters, Attribute classAttribute) {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for (Entry<String, Object> parameter : basedOnParameters.getParameters().entrySet()) {
			Attribute wekaAttribute = new Attribute(parameter.getKey());
			attributes.add(wekaAttribute);
			
			Attribute wekaAttributeQubic = new Attribute(parameter.getKey() + "²");
			attributes.add(wekaAttributeQubic);
		}
		
		attributes.add(classAttribute);

		this.attributes = attributes;
		this.classAttribute = classAttribute;
	}
	
	public Instances buildDataSet() {
		Instances instances = new Instances("dataset", this.attributes, 0);
		instances.setClass(classAttribute);
		return instances;
	}
	
	public Instance buildInstance(ServiceParameters serviceParameters, double classValue) {
		Set<Entry<String, Object>> parameters = serviceParameters.getParameters().entrySet();
		int entryCount = parameters.size() * 2 + 1;
		double[] values = new double[entryCount];
		int i = 0;

		for (Entry<String, Object> parameter : parameters) {
			values[i] = (double) (Integer) parameter.getValue();
			values[i + 1] = Math.pow(values[i], 2.0);
			i += 2;
		}
		
		values[i] = classValue;
		return new DenseInstance(1.0, values);
	}
	
	public ArrayList<Attribute> getAttributes() {
		return this.attributes;
	}
	
	public Attribute getClassAttribute() {
		return this.classAttribute;
	}
}
