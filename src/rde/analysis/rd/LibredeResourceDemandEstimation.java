package rde.analysis.rd;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import monitoring.records.ResponseTimeRecord;
import rde.analysis.KiekerServiceCallRecordFilter;
import rde.analysis.ServiceParameters;
import tools.descartes.librede.Librede;
import tools.descartes.librede.LibredeResults;
import tools.descartes.librede.approach.ServiceDemandLawApproach;
import tools.descartes.librede.configuration.ConfigurationFactory;
import tools.descartes.librede.configuration.ConfigurationPackage;
import tools.descartes.librede.configuration.DataSourceConfiguration;
import tools.descartes.librede.configuration.EstimationApproachConfiguration;
import tools.descartes.librede.configuration.EstimationSpecification;
import tools.descartes.librede.configuration.FileTraceConfiguration;
import tools.descartes.librede.configuration.InputSpecification;
import tools.descartes.librede.configuration.LibredeConfiguration;
import tools.descartes.librede.configuration.OutputSpecification;
import tools.descartes.librede.configuration.Resource;
import tools.descartes.librede.configuration.ResourceDemand;
import tools.descartes.librede.configuration.Service;
import tools.descartes.librede.configuration.TraceToEntityMapping;
import tools.descartes.librede.configuration.ValidationSpecification;
import tools.descartes.librede.configuration.ValidatorConfiguration;
import tools.descartes.librede.configuration.WorkloadDescription;
import tools.descartes.librede.datasource.IDataSource;
import tools.descartes.librede.datasource.memory.InMemoryDataSource;
import tools.descartes.librede.linalg.MatrixBuilder;
import tools.descartes.librede.linalg.VectorBuilder;
import tools.descartes.librede.metrics.Aggregation;
import tools.descartes.librede.metrics.StandardMetrics;
import tools.descartes.librede.repository.TimeSeries;
import tools.descartes.librede.units.Quantity;
import tools.descartes.librede.units.Ratio;
import tools.descartes.librede.units.Time;
import tools.descartes.librede.units.UnitsFactory;
import tools.descartes.librede.validation.ResponseTimeValidator;

public class LibredeResourceDemandEstimation {
	
	static {
		Librede.initLogging();
		Librede.init();
	}

	private static final ConfigurationFactory configurationFactory = 
			ConfigurationPackage.eINSTANCE.getConfigurationFactory();

	private static final String DATA_SOURCE_NAME = "InMemoryDataSource";
	
	private final ResourceUtilizationRepository resourceUtilization;
	
	private final KiekerResponseTimeFilter responseTimeRepository;
	
	private final KiekerServiceCallRecordFilter serviceCallRepository;
	
	private final Map<String, ServiceParameters> idToServiceParameters;
	
	private final Map<String, Resource> idToResource;

	public LibredeResourceDemandEstimation( 
			ResourceUtilizationRepository resourceUtilization,
			KiekerResponseTimeFilter responseTimeRepository,
			KiekerServiceCallRecordFilter serviceCallRepository) {
		this.resourceUtilization = resourceUtilization;
		this.responseTimeRepository = responseTimeRepository;
		this.serviceCallRepository = serviceCallRepository;
		this.idToServiceParameters = new HashMap<String, ServiceParameters>();
		this.idToResource = new HashMap<String, Resource>();
		this.estimate();
	}
	
	private void estimate() {
		this.buildWorkloadDescription();
		this.addAllResourcesUtilization();
		this.addAllResourceDemands();
		this.buildLibredeConfig();

		Map<String, IDataSource> dataSources = 
				Collections.singletonMap(dataSourceConfig.getName(), dataSource);
		LibredeResults results = Librede.execute(libredeConfig, dataSources);
		
		System.out.println();
	}
	
	private InMemoryDataSource dataSource;
	
	private DataSourceConfiguration dataSourceConfig;
	
	private WorkloadDescription workloadDescription;
	
	private InputSpecification inputSpecification;
	
	private LibredeConfiguration libredeConfig;
	
	private void buildWorkloadDescription() {
		this.dataSource = new InMemoryDataSource();
		this.dataSource.setName(DATA_SOURCE_NAME);

		this.dataSourceConfig = configurationFactory.createDataSourceConfiguration();
		dataSourceConfig.setName(DATA_SOURCE_NAME);
		dataSourceConfig.setType(dataSource.getClass().getCanonicalName());

		this.workloadDescription = configurationFactory.createWorkloadDescription();
		this.inputSpecification = configurationFactory.createInputSpecification();
		inputSpecification.getDataSources().add(dataSourceConfig);
	}
	
	private static TimeSeries buildTimeSeries(SortedMap<Long, Double> values) {
		VectorBuilder time = VectorBuilder.create(values.size());
		MatrixBuilder matrix = MatrixBuilder.create(values.size(), 1);
		for (Entry<Long, Double> ds : values.entrySet()) {
			time.add(ds.getKey() / 1.0e9);
			matrix.addRow(ds.getValue());
		}
		return new TimeSeries(time.toVector(), matrix.toMatrix());
	}
	
	private void addAllResourcesUtilization() {
		for (String resourceId : this.resourceUtilization.getResourceIds()) {
			this.addResourceUtilization(resourceId, this.resourceUtilization.getUtilization(resourceId));
		}
	}
	
	private void addResourceUtilization(String resourceId, SortedMap<Long, Double> values) {
		Resource resource = configurationFactory.createResource();
		resource.setName(resourceId);
		this.idToResource.put(resourceId, resource);
		
		FileTraceConfiguration configuration = configurationFactory.createFileTraceConfiguration();
		configuration.setLocation(resourceId);
		configuration.setMetric(StandardMetrics.UTILIZATION);
		configuration.setUnit(Ratio.PERCENTAGE);
		configuration.setInterval(UnitsFactory.eINSTANCE.createQuantity(10, Time.SECONDS));
		configuration.setAggregation(Aggregation.AVERAGE);
		configuration.setDataSource(dataSourceConfig);
		configuration.setFile("");

		TraceToEntityMapping mapping = configurationFactory.createTraceToEntityMapping();
		mapping.setEntity(resource);
		mapping.setTraceColumn(1);
		configuration.getMappings().add(mapping);

		workloadDescription.getResources().add(resource);
		dataSource.append(resourceId, buildTimeSeries(values));
		inputSpecification.getObservations().add(configuration);
	}
	
	private void addAllResourceDemands() {
		for (String internalActionId : this.responseTimeRepository.getInternalActionIds()) {
			for (String resourceId : this.responseTimeRepository.getResourceIds(internalActionId)) {
				List<ResponseTimeRecord> responseTimeRecords = 
						this.responseTimeRepository.getResponseTimes(internalActionId, resourceId);
				this.addResourceDemands(internalActionId, resourceId, responseTimeRecords);
			}
		}
	}
	
	private void addResourceDemands(
			String internalActionId, 
			String resourceId, 
			List<ResponseTimeRecord> responseTimeRecords) {
		
		Map<ServiceParameters, String> parametersToId = new HashMap<ServiceParameters, String>();
		int distinctParameterId = 0;
		
		Map<String, SortedMap<Long, Double>> parameterIdToRts = 
				new HashMap<String, SortedMap<Long,Double>>();
		
		for (ResponseTimeRecord responseTimeRecord : responseTimeRecords) {
			ServiceParameters parameters = 
					this.serviceCallRepository.getParametersOfServiceCall(responseTimeRecord.getServiceExecutionId());
			
			String rtId = parametersToId.get(parameters);
			if (rtId == null) {
				rtId = internalActionId + "#" + resourceId + "#" + distinctParameterId;
				distinctParameterId++;
				parametersToId.put(parameters, rtId);
				this.idToServiceParameters.put(rtId, parameters);
			}
			
			SortedMap<Long, Double> rts = parameterIdToRts.get(rtId);
			if (rts == null) {
				rts = new TreeMap<Long, Double>();
				parameterIdToRts.put(rtId, rts);
			}
			
			double reponseTime = 
					(responseTimeRecord.getStopTime() - responseTimeRecord.getStartTime()) / 1.0e9;
			
			rts.put(responseTimeRecord.getStartTime(), reponseTime);
		}
		
		for (Entry<String, SortedMap<Long, Double>> responseTimes : parameterIdToRts.entrySet()) {
			this.addResourceDemand(resourceId, responseTimes.getKey(), responseTimes.getValue());
		}
	}
	
	private void addResourceDemand(
			String resourceId,
			String resourceDemandId, 
			SortedMap<Long, Double> responseTimes) {
		
		Service service = configurationFactory.createService();
		service.setName(resourceDemandId);

		ResourceDemand d = configurationFactory.createResourceDemand();
		d.setName(resourceId);
		d.setResource(this.idToResource.get(resourceId));
		service.getTasks().add(d);

		TimeSeries timeSeries = buildTimeSeries(responseTimes);
		dataSource.append(resourceDemandId, timeSeries);

		FileTraceConfiguration configuration = configurationFactory.createFileTraceConfiguration();
		configuration.setLocation(resourceDemandId);
		configuration.setMetric(StandardMetrics.RESPONSE_TIME);
		configuration.setUnit(Time.SECONDS);
		configuration.setInterval(UnitsFactory.eINSTANCE.createQuantity(0, Time.SECONDS));
		configuration.setAggregation(Aggregation.NONE);
		configuration.setDataSource(dataSourceConfig);
		configuration.setFile("");

		TraceToEntityMapping mapping = configurationFactory.createTraceToEntityMapping();
		mapping.setEntity(service);
		mapping.setTraceColumn(1);
		configuration.getMappings().add(mapping);

		workloadDescription.getServices().add(service);
		inputSpecification.getObservations().add(configuration);
	}
	
	public void buildLibredeConfig() {
		ValidatorConfiguration validatorConfiguration = 
				configurationFactory.createValidatorConfiguration();
		validatorConfiguration.setType(ResponseTimeValidator.class.getCanonicalName());

		ValidationSpecification validationSpecification = configurationFactory.createValidationSpecification();
		validationSpecification.setValidateEstimates(true);
		validationSpecification.setValidationFolds(5);
		validationSpecification.getValidators().add(validatorConfiguration);

		OutputSpecification outputSpecification = configurationFactory.createOutputSpecification();

		EstimationApproachConfiguration estimationConf = configurationFactory.createEstimationApproachConfiguration();
		estimationConf.setType(ServiceDemandLawApproach.class.getCanonicalName());

		EstimationSpecification estimationSpecification = configurationFactory.createEstimationSpecification();
		estimationSpecification.getApproaches().add(estimationConf);

		UnitsFactory unitsFactory = UnitsFactory.eINSTANCE;
		
		Quantity<Time> startTime = unitsFactory.createQuantity(this.responseTimeRepository.getEarliestEntry() / 1.0e9, Time.SECONDS);
		Quantity<Time> endTime = unitsFactory.createQuantity(this.responseTimeRepository.getLatestEntry() / 1.0e9, Time.SECONDS);
		
		startTime = startTime.plus(unitsFactory.createQuantity(5, Time.SECONDS));
		endTime = endTime.minus(unitsFactory.createQuantity(5, Time.SECONDS));

		estimationSpecification.setStartTimestamp(startTime);
		estimationSpecification.setEndTimestamp(endTime);
		
		estimationSpecification.setWindow(30);
		estimationSpecification.setRecursive(false);
		estimationSpecification.setAutomaticApproachSelection(false);
		Quantity<Time> halfMinute = unitsFactory.createQuantity(30, Time.SECONDS);
		estimationSpecification.setStepSize(halfMinute);

		this.libredeConfig = configurationFactory.createLibredeConfiguration();
		libredeConfig.setEstimation(estimationSpecification);
		libredeConfig.setInput(inputSpecification);
		libredeConfig.setOutput(outputSpecification);
		libredeConfig.setValidation(validationSpecification);
		libredeConfig.setWorkloadDescription(workloadDescription);
	}
	
	public void saveConfig(String filePath) throws IOException {
		ResourceSet resourceSet = new ResourceSetImpl();

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				org.eclipse.emf.ecore.resource.Resource.Factory.Registry.DEFAULT_EXTENSION,
				new XMIResourceFactoryImpl());

		ResourceSet resSet = new ResourceSetImpl();
		org.eclipse.emf.ecore.resource.Resource resource = resSet
				.createResource(org.eclipse.emf.common.util.URI.createFileURI(filePath));
		resource.getContents().add(this.libredeConfig);
		resource.save(Collections.EMPTY_MAP);
	}
}
