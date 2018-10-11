package tools.vitruv.applications.pcmjava.seffstatements.parameters.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParameters;

import org.apache.log4j.Logger;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class WekaServiceParametersModel {

	private static final Logger LOGGER = Logger.getLogger(WekaServiceParametersModel.class);

	private final Attribute classAttribute;

	private final ArrayList<Attribute> attributes;

	private final List<WekaServiceParameter> parameters;

	private final Map<String, List<WekaServiceParameter>> parametersToAttributes;

	public WekaServiceParametersModel(ServiceParameters basedOnParameters, Attribute classAttribute) {
		this.attributes = new ArrayList<Attribute>();
		this.parameters = new ArrayList<WekaServiceParameter>();
		this.parametersToAttributes = new HashMap<String, List<WekaServiceParameter>>();
		
		for (Entry<String, Object> parameter : basedOnParameters.getParameters().entrySet()) {
			this.addParameter(parameter.getKey(), parameter.getValue());
		}

		this.attributes.add(classAttribute);
		this.classAttribute = classAttribute;
	}

	public Instances buildDataSet() {
		Instances instances = new Instances("dataset", this.attributes, 0);
		instances.setClass(classAttribute);
		return instances;
	}

	public Instance buildInstance(ServiceParameters serviceParameters, double classValue) {
		double[] values = new double[parameters.size() + 1];

		for (Entry<String, Object> parameter : serviceParameters.getParameters().entrySet()) {
			List<WekaServiceParameter> wekaParameters = parametersToAttributes.get(parameter.getKey());
			for (WekaServiceParameter wekaServiceParameter : wekaParameters) {
				wekaServiceParameter.setValue(parameter.getValue(), values);
			}
		}

		values[parameters.size()] = classValue;
		return new DenseInstance(1.0, values);
	}

	public ArrayList<Attribute> getAttributes() {
		return this.attributes;
	}

	public Attribute getClassAttribute() {
		return this.classAttribute;
	}

	public String getStochasticExpressionForIndex(int idx) {
		return this.parameters.get(idx).getStochasticExpression();
	}

	private void addParameter(String name, Object value) {
		if (value instanceof Integer || value instanceof Double) {
			this.addNumericParameter(name);
		} else {
			LOGGER.warn("Handling parameter of type " + value.getClass().getSimpleName() + " is not implemented.");
		}
	}

	private void addNumericParameter(String name) {
		List<WekaServiceParameter> newParameters = new ArrayList<WekaServiceParameter>();
		int index = this.parameters.size();
		NumericWekaServiceParameter numeric = new NumericWekaServiceParameter(name, index);
		newParameters.add(numeric);
		this.parameters.add(numeric);
		this.attributes.add(numeric.getWekaAttribute());
		index++;
		QuadraticNumericWekaServiceParameter numeric2 = new QuadraticNumericWekaServiceParameter(name, index);
		newParameters.add(numeric2);
		this.parameters.add(numeric2);
		this.attributes.add(numeric2.getWekaAttribute());
		
		this.parametersToAttributes.put(name, newParameters);
	}

	private static abstract class WekaServiceParameter {
		private final int index;
		private final String parameterName;
		private final Attribute wekaAttribute;

		public WekaServiceParameter(String parameterName, int index, Attribute wekaAttribute) {
			this.index = index;
			this.parameterName = parameterName;
			this.wekaAttribute = wekaAttribute;
		}

		public Attribute getWekaAttribute() {
			return this.wekaAttribute;
		}

		public int getIndex() {
			return this.index;
		}

		public String getParameterName() {
			return this.parameterName;
		}

		public abstract void setValue(Object value, double[] result);

		public abstract String getStochasticExpression();
	}

	private static class NumericWekaServiceParameter extends WekaServiceParameter {

		public NumericWekaServiceParameter(String parameterName, int index) {
			super(parameterName, index, new Attribute(parameterName));
		}

		@Override
		public void setValue(Object value, double[] result) {
			double castedValue = 0.0;
			if (value instanceof Integer) {
				castedValue = (double) (Integer) value;
			} else if (value instanceof Double) {
				castedValue = (Double) value;
			}

			result[this.getIndex()] = castedValue;
		}

		@Override
		public String getStochasticExpression() {
			return this.getParameterName() + ".VALUE";
		}

	}

	private static class QuadraticNumericWekaServiceParameter extends WekaServiceParameter {

		public QuadraticNumericWekaServiceParameter(String parameterName, int index) {
			super(parameterName, index, new Attribute(parameterName + "²"));
		}

		@Override
		public void setValue(Object value, double[] result) {
			double castedValue = 0.0;
			if (value instanceof Integer) {
				castedValue = (double) (Integer) value;
			} else if (value instanceof Double) {
				castedValue = (Double) value;
			}

			result[this.getIndex()] = Math.pow(castedValue, 2.0);
		}

		@Override
		public String getStochasticExpression() {
			return "(" + this.getParameterName() + ".VALUE ^ 2)";
		}

	}
}
