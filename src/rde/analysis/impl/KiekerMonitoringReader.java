package rde.analysis.impl;

import kieker.analysis.AnalysisController;
import kieker.analysis.IAnalysisController;
import kieker.analysis.plugin.reader.filesystem.FSReader;
import kieker.common.configuration.Configuration;
import rde.analysis.ServiceCallDataSet;
import rde.analysis.branch.KiekerBranchExecutionFilter;
import rde.analysis.loop.KiekerLoopIterationFilter;
import rde.analysis.rd.ResponseTimeDataSet;
import rde.analysis.rd.impl.KiekerCpuUtilizationFilter;
import rde.analysis.rd.impl.KiekerResponseTimeFilter;

public class KiekerMonitoringReader {

	private KiekerResponseTimeFilter responseTimeFilter;
	private KiekerServiceCallRecordFilter callRecordFilter;
	private KiekerCpuUtilizationFilter cpuFilter;
	private KiekerLoopIterationFilter loopFilter;
	private KiekerBranchExecutionFilter branchFilter;

	public KiekerMonitoringReader(String kiekerRecordsDirectoryPath) {
		this.read(kiekerRecordsDirectoryPath);
	}

	public ResponseTimeDataSet getResponseTimeRepository() {
		return responseTimeFilter;
	}

	public ServiceCallDataSet getCallRecordRepository() {
		return callRecordFilter;
	}

	public KiekerCpuUtilizationFilter getCpuRepository() {
		return cpuFilter;
	}

	public KiekerLoopIterationFilter getLoopRepository() {
		return loopFilter;
	}

	public KiekerBranchExecutionFilter getBranchRepository() {
		return branchFilter;
	}

	private void read(String kiekerRecordsDirectoryPath) {
		try {
			this.internRead(kiekerRecordsDirectoryPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void internRead(String kiekerRecordsDirectoryPath) throws Exception {
		// Create Kieker.Analysis instance
		final IAnalysisController analysisInstance = new AnalysisController();

		// Set filesystem monitoring log input directory for our analysis
		final Configuration fsReaderConfig = new Configuration();
		fsReaderConfig.setProperty(FSReader.CONFIG_PROPERTY_NAME_INPUTDIRS, kiekerRecordsDirectoryPath);
		final FSReader reader = new FSReader(fsReaderConfig, analysisInstance);

		// record filter
		this.responseTimeFilter = new KiekerResponseTimeFilter(new Configuration(),
				analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, responseTimeFilter,
				KiekerResponseTimeFilter.INPUT_PORT_NAME_EVENTS);

		// record filter
		this.callRecordFilter = new KiekerServiceCallRecordFilter(new Configuration(),
				analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, callRecordFilter,
				KiekerServiceCallRecordFilter.INPUT_PORT_NAME_EVENTS);

		// record filter
		this.cpuFilter = new KiekerCpuUtilizationFilter(new Configuration(), analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, cpuFilter,
				KiekerCpuUtilizationFilter.INPUT_PORT_NAME_EVENTS);

		// record filter
		this.loopFilter = new KiekerLoopIterationFilter(new Configuration(), analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, loopFilter,
				KiekerLoopIterationFilter.INPUT_PORT_NAME_EVENTS);

		// record filter
		this.branchFilter = new KiekerBranchExecutionFilter(new Configuration(),
				analysisInstance);

		// Connect the output of the reader with the input of the filter.
		analysisInstance.connect(reader, FSReader.OUTPUT_PORT_NAME_RECORDS, branchFilter,
				KiekerBranchExecutionFilter.INPUT_PORT_NAME_EVENTS);

		// Start the analysis
		analysisInstance.run();
	}
}
