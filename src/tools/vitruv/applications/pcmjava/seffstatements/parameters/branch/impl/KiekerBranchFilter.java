package tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.branch.BranchDataSet;
import tools.vitruv.applications.pcmjava.seffstatements.parameters.monitoring.records.BranchRecord;

@Plugin(description = "A filter for loop iteration records.")
public final class KiekerBranchFilter extends AbstractFilterPlugin implements BranchDataSet {
	
	private final Map<String, List<BranchRecord>> branchIdToRecord;
	
	@Override
	public Set<String> getBranchIds() {
		return this.branchIdToRecord.keySet();
	}
	
	@Override
	public List<BranchRecord> getBranchRecords(String branchId) {
		return this.branchIdToRecord.get(branchId);
	}

	public KiekerBranchFilter(Configuration configuration, IProjectContext projectContext) {
		super(configuration, projectContext);
		this.branchIdToRecord = 
				new HashMap<String, List<BranchRecord>>();
	}

	/**
	 * The name of the input port for incoming events. 
	 */
	public static final String INPUT_PORT_NAME_EVENTS = "inputEvent";
	
	@InputPort(
			name = INPUT_PORT_NAME_EVENTS, 
			description = "Input for loop iteration records.", 
			eventTypes = { BranchRecord.class })
	public final void inputEvent(final BranchRecord record) {
		String branchId = record.getBranchId();
		List<BranchRecord> branchRecords = this.branchIdToRecord.get(branchId);
		if (branchRecords == null) {
			branchRecords = new ArrayList<BranchRecord>();
			this.branchIdToRecord.put(branchId, branchRecords);
		}
		branchRecords.add(record);
	}

	@Override
	public Configuration getCurrentConfiguration() {
		return new Configuration();
	}

	@Override
	public String getBranchNotExecutedId() {
		return BranchRecord.EXECUTED_BRANCH_ID;
	}
}