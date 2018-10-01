package rde.analysis.branch;

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
import monitoring.records.BranchRecord;

@Plugin(description = "A filter for loop iteration records.")
public final class KiekerBranchExecutionFilter extends AbstractFilterPlugin {
	
	private final Map<String, List<BranchRecord>> branchIdToRecord;
	
	public Set<String> getBranchIds() {
		return this.branchIdToRecord.keySet();
	}
	
	public List<BranchRecord> getBranchRecords(String branchId) {
		return this.branchIdToRecord.get(branchId);
	}

	public KiekerBranchExecutionFilter(Configuration configuration, IProjectContext projectContext) {
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
}