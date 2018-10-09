package tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import tools.descartes.librede.ApproachResult;
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
import tools.descartes.librede.linalg.Vector;
import tools.descartes.librede.linalg.VectorBuilder;
import tools.descartes.librede.metrics.Aggregation;
import tools.descartes.librede.metrics.StandardMetrics;
import tools.descartes.librede.repository.TimeSeries;
import tools.descartes.librede.units.Quantity;
import tools.descartes.librede.units.Ratio;
import tools.descartes.librede.units.Time;
import tools.descartes.librede.units.UnitsFactory;
import tools.descartes.librede.validation.ResponseTimeValidator;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.monitoring.records.ResponseTimeRecord;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.ResponseTimeDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.rd.utilization.ResourceUtilizationDataSet;

public class LibredeResourceDemandEstimation {

	static {
		Librede.initLogging();
		Librede.init();
	}

	private static final ConfigurationFactory configurationFactory = ConfigurationPackage.eINSTANCE
			.getConfigurationFactory();

	private static final String DATA_SOURCE_NAME = "InMemoryDataSource";

	private static final Class<ServiceDemandLawApproach> ESTIMATION_APPROACH = ServiceDemandLawApproach.class;

	private final ResourceUtilizationDataSet resourceUtilization;

	private final ResponseTimeDataSet responseTimeRepository;

	private final ServiceCallDataSet serviceCallRepository;

	private final Map<String, ResourceDemandInfo> idToServiceParameters;

	private final Map<String, Resource> idToResource;

	private final ParametricDependencyEstimationStrategy parametricDependencyEstimationStrategy;

	private static class ResourceDemandInfo {
		private final ServiceParameters serviceParameters;
		private final String internalActionId;
		private final String resourceId;

		public ResourceDemandInfo(ServiceParameters serviceParameters, String internalActionId, String resourceId) {
			this.serviceParameters = serviceParameters;
			this.internalActionId = internalActionId;
			this.resourceId = resourceId;
		}
	}

	public LibredeResourceDemandEstimation(
			ParametricDependencyEstimationStrategy parametricDependencyEstimationStrategy,
			ResourceUtilizationDataSet resourceUtilization, ResponseTimeDataSet responseTimeRepository,
			ServiceCallDataSet serviceCallRepository) {
		this.resourceUtilization = resourceUtilization;
		this.responseTimeRepository = responseTimeRepository;
		this.serviceCallRepository = serviceCallRepository;
		this.parametricDependencyEstimationStrategy = parametricDependencyEstimationStrategy;
		this.idToServiceParameters = new HashMap<String, ResourceDemandInfo>();
		this.idToResource = new HashMap<String, Resource>();

		this.buildConfig();
	}

	private void buildConfig() {
		this.buildWorkloadDescription();
		this.addAllResourcesUtilization();
		this.addAllResourceDemands();
		this.buildLibredeConfig();
	}

	public Map<String, Map<String, ResourceDemandModel>> estimateResourceDemandModels() {
		Map<String, Map<String, Map<ServiceParameters, Double>>> rds = this.estimateResourceDemands();
		Map<String, Map<String, ResourceDemandModel>> results = new HashMap<String, Map<String, ResourceDemandModel>>();
		for (Entry<String, Map<String, Map<ServiceParameters, Double>>> rd : rds.entrySet()) {
			Map<String, ResourceDemandModel> rdModels = new HashMap<String, ResourceDemandModel>();
			results.put(rd.getKey(), rdModels);
			for (Entry<String, Map<ServiceParameters, Double>> rdOfResource : rd.getValue().entrySet()) {
				ResourceDemandModel model = this.parametricDependencyEstimationStrategy
						.estimateResourceDemandModel(rd.getKey(), rdOfResource.getKey(), rdOfResource.getValue());
				rdModels.put(rdOfResource.getKey(), model);
			}
		}
		return results;
	}

	public Map<String, Map<String, Map<ServiceParameters, Double>>> estimateResourceDemands() {

		Map<String, IDataSource> dataSources = Collections.singletonMap(dataSourceConfig.getName(), dataSource);
		LibredeResults libredeResults = Librede.execute(libredeConfig, dataSources);
		ApproachResult approachResult = libredeResults.getApproachResults(ESTIMATION_APPROACH);

		ResourceDemand[] resultResourceDemandIndex = approachResult.getResultOfFold(0).getStateVariables();
		Vector meanResults = approachResult.getMeanEstimates();

		Map<String, Map<String, Map<ServiceParameters, Double>>> results = new HashMap<String, Map<String, Map<ServiceParameters, Double>>>();

		for (int i = 0; i < resultResourceDemandIndex.length; i++) {
			String rdId = resultResourceDemandIndex[i].getName();
			ResourceDemandInfo rdInfo = this.idToServiceParameters.get(rdId);

			Map<String, Map<ServiceParameters, Double>> resourceEntries = results.get(rdInfo.internalActionId);
			if (resourceEntries == null) {
				resourceEntries = new HashMap<String, Map<ServiceParameters, Double>>();
				results.put(rdInfo.internalActionId, resourceEntries);
			}

			Map<ServiceParameters, Double> rdEntries = resourceEntries.get(rdInfo.resourceId);
			if (rdEntries == null) {
				rdEntries = new HashMap<ServiceParameters, Double>();
				resourceEntries.put(rdInfo.resourceId, rdEntries);
			}

			rdEntries.put(rdInfo.serviceParameters, meanResults.get(i));
		}

		return results;
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

	private static TimeSeries buildTimeSeries(SortedMap<Long, Double> values, Function<Long, Double> timeToSeconds) {
		VectorBuilder time = VectorBuilder.create(values.size());
		MatrixBuilder matrix = MatrixBuilder.create(values.size(), 1);
		for (Entry<Long, Double> ds : values.entrySet()) {
			time.add(timeToSeconds.apply(ds.getKey()));
			matrix.addRow(ds.getValue());
		}
		return new TimeSeries(time.toVector(), matrix.toMatrix());
	}

	private void addAllResourcesUtilization() {
		for (String resourceId : this.resourceUtilization.getResourceIds()) {
			this.addResourceUtilization(resourceId, this.resourceUtilization.getUtilization(resourceId),
					this.resourceUtilization::timeToSeconds);
		}
	}

	private void addResourceUtilization(String resourceId, SortedMap<Long, Double> values,
			Function<Long, Double> timeToSeconds) {
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
		dataSource.append(resourceId, buildTimeSeries(values, timeToSeconds));
		inputSpecification.getObservations().add(configuration);
	}

	private void addAllResourceDemands() {
		for (String internalActionId : this.responseTimeRepository.getInternalActionIds()) {
			for (String resourceId : this.responseTimeRepository.getResourceIds(internalActionId)) {
				this.addResourceDemands(internalActionId, resourceId);
			}
		}
	}

	private void addResourceDemands(String internalActionId, String resourceId) {

		Map<ServiceParameters, String> parametersToId = new HashMap<ServiceParameters, String>();
		int distinctParameterId = 0;

		Map<String, SortedMap<Long, Double>> parameterIdToRts = new HashMap<String, SortedMap<Long, Double>>();

		List<ResponseTimeRecord> responseTimeRecords = this.responseTimeRepository.getResponseTimes(internalActionId,
				resourceId);
		
		for (ResponseTimeRecord responseTimeRecord : responseTimeRecords) {
			ServiceParameters parameters = this.serviceCallRepository
					.getParametersOfServiceCall(responseTimeRecord.getServiceExecutionId());

			String rtId = parametersToId.get(parameters);
			if (rtId == null) {
				rtId = internalActionId + "#" + resourceId + "#" + distinctParameterId;
				distinctParameterId++;
				parametersToId.put(parameters, rtId);
				ResourceDemandInfo rdInfo = new ResourceDemandInfo(parameters, internalActionId, resourceId);
				this.idToServiceParameters.put(rtId, rdInfo);
			}

			SortedMap<Long, Double> rts = parameterIdToRts.get(rtId);
			if (rts == null) {
				rts = new TreeMap<Long, Double>();
				parameterIdToRts.put(rtId, rts);
			}

			double reponseTime = (responseTimeRecord.getStopTime() - responseTimeRecord.getStartTime()) / 1.0e9;

			rts.put(responseTimeRecord.getStartTime(), reponseTime);
		}

		for (Entry<String, SortedMap<Long, Double>> responseTimes : parameterIdToRts.entrySet()) {
			this.addResourceDemand(resourceId, responseTimes.getKey(), responseTimes.getValue());
		}
	}

	private void addResourceDemand(String resourceId, String resourceDemandId, SortedMap<Long, Double> responseTimes) {

		Service service = configurationFactory.createService();
		service.setName(resourceDemandId);

		ResourceDemand d = configurationFactory.createResourceDemand();
		d.setName(resourceDemandId);
		d.setResource(this.idToResource.get(resourceId));
		service.getTasks().add(d);

		TimeSeries timeSeries = buildTimeSeries(responseTimes, this.responseTimeRepository::timeToSeconds);
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

	private void buildLibredeConfig() {
		ValidatorConfiguration validatorConfiguration = configurationFactory.createValidatorConfiguration();
		validatorConfiguration.setType(ResponseTimeValidator.class.getCanonicalName());

		ValidationSpecification validationSpecification = configurationFactory.createValidationSpecification();
		validationSpecification.setValidateEstimates(true);
		validationSpecification.setValidationFolds(3);
		validationSpecification.getValidators().add(validatorConfiguration);

		OutputSpecification outputSpecification = configurationFactory.createOutputSpecification();

		EstimationApproachConfiguration estimationConf = configurationFactory.createEstimationApproachConfiguration();
		estimationConf.setType(ESTIMATION_APPROACH.getCanonicalName());

		EstimationSpecification estimationSpecification = configurationFactory.createEstimationSpecification();
		estimationSpecification.getApproaches().add(estimationConf);

		UnitsFactory unitsFactory = UnitsFactory.eINSTANCE;

		Quantity<Time> startTime = unitsFactory.createQuantity(this.responseTimeRepository.getEarliestEntry() / 1.0e9,
				Time.SECONDS);
		Quantity<Time> endTime = unitsFactory.createQuantity(this.responseTimeRepository.getLatestEntry() / 1.0e9,
				Time.SECONDS);

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

	public void saveConfig(String filePath) {
		try {
			this.internSaveConfig(filePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void internSaveConfig(String filePath) throws IOException {
		ResourceSet resourceSet = new ResourceSetImpl();

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				org.eclipse.emf.ecore.resource.Resource.Factory.Registry.DEFAULT_EXTENSION,
				new XMIResourceFactoryImpl());

		URI filePathUri = org.eclipse.emf.common.util.URI.createFileURI(filePath);
		org.eclipse.emf.ecore.resource.Resource resource = resourceSet.createResource(filePathUri);
		resource.getContents().add(this.libredeConfig);
		resource.save(Collections.EMPTY_MAP);
	}
}
