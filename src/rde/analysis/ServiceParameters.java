package rde.analysis;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceParameters {
	
	private static final Logger LOGGER = Logger.getLogger(ServiceParameters.class);
	
	public static ServiceParameters EMPTY = new ServiceParameters();
	
	public static ServiceParameters buildFromJson(String parameters) {
		if (parameters == null || parameters.isEmpty()) {
			return EMPTY;
		}
		try {
			return new ServiceParameters(parameters);
		} catch (IOException e) {
			LOGGER.warn(String.format("Could not parse parameters %s.", parameters), e);
			return EMPTY;
		}
	}
	
	public static ServiceParameters build(Map<String, Object> parameters) {
		if (parameters == null || parameters.isEmpty()) {
			return EMPTY;
		}
		return new ServiceParameters(parameters);
	}
	
	private static final TypeReference<TreeMap<String, Object>> PARSED_PARAMETERS_TYPE_REF = 
			new TypeReference<TreeMap<String, Object>>() {};
			
	private static final ObjectMapper mapper;
	
	private final SortedMap<String, Object> parameters;
	private final SortedMap<String, Object> readOnlyParameters;
	
	static {
		mapper = new ObjectMapper();
		mapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
	}
	
	private ServiceParameters(Map<String, Object> parameters) {
		this.parameters = new TreeMap<String, Object>(parameters);
		this.readOnlyParameters = Collections.unmodifiableSortedMap(this.parameters);
	}
	
	private ServiceParameters() {
		this.parameters = Collections.emptySortedMap();
		this.readOnlyParameters = Collections.unmodifiableSortedMap(this.parameters);
	}
	
	private ServiceParameters(String parameters) throws IOException {
		this.parameters = mapper.readValue(parameters, PARSED_PARAMETERS_TYPE_REF);
		this.readOnlyParameters = Collections.unmodifiableSortedMap(this.parameters);
	}
	
	public SortedMap<String, Object> getParameters() {
		return this.readOnlyParameters;
	}

	@Override
	public int hashCode() {
		return parameters.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
			
		if (obj == null) {
			return false;
		}
			
		if (getClass() != obj.getClass()) {
			return false;
		}
			
		ServiceParameters other = (ServiceParameters) obj;
		return parameters.equals(other.parameters);
	}
}
