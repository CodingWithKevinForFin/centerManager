package com.f1.ami.web.centermanager.graph.nodes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.f1.ami.web.centermanager.AmiWebCenterGraphManager;

public class AmiCenterGraphNode_Table extends AmiCenterGraphAbstractNode {
	public static final byte CODE = AmiCenterGraphNode.TYPE_TABLE;

	private Map<String, AmiCenterGraphNode_Trigger> targetTriggers = new HashMap<String, AmiCenterGraphNode_Trigger>();
	private Map<String, AmiCenterGraphNode_Index> targetIndexes = new HashMap<String, AmiCenterGraphNode_Index>();

	//(inbound)A sink trigger directs the data into the table. The arrow should point from the trigger to the table
	private Set<AmiCenterGraphNode_Trigger> sinkTriggers = new HashSet<AmiCenterGraphNode_Trigger>();

	//(outbound)A source trigger moves the data out of the table. The arrow should point from the table to the trigger
	private Set<AmiCenterGraphNode_Trigger> sourceTriggers = new HashSet<AmiCenterGraphNode_Trigger>();

	public AmiCenterGraphNode_Table(AmiWebCenterGraphManager manager, long uid, String label) {
		super(manager, uid, label);
	}

	public AmiCenterGraphNode_Table(AmiWebCenterGraphManager manager, long uid, String label, boolean readOnly) {
		this(manager, uid, label);
		this.readOnly = readOnly;
	}

	@Override
	public byte getType() {
		return CODE;
	}

	public Map<String, AmiCenterGraphNode_Trigger> getTargetTriggers() {
		return this.targetTriggers;
	}

	public Map<String, AmiCenterGraphNode_Index> getTargetIndexes() {
		return this.targetIndexes;
	}

	public void bindTargetTrigger(String id, AmiCenterGraphNode_Trigger trigger) {
		this.targetTriggers.put(id, trigger);
	}
	public void bindTargetIndex(String id, AmiCenterGraphNode_Index index) {
		this.targetIndexes.put(id, index);
	}

	public void unbindTargetTrigger(String id, AmiCenterGraphNode_Trigger trigger) {
		this.targetTriggers.remove(id, trigger);
	}
	public void unbindTargetIndex(String id, AmiCenterGraphNode_Index index) {
		this.targetIndexes.remove(id, index);
	}

	public boolean hasIndex() {
		return !this.targetIndexes.isEmpty();
	}

	public boolean hasTrigger() {
		return !this.targetTriggers.isEmpty();
	}

	public void addSourceTrigger(AmiCenterGraphNode_Trigger source) {
		this.sourceTriggers.add(source);
	}

	public void addSinkTrigger(AmiCenterGraphNode_Trigger sink) {
		this.sinkTriggers.add(sink);
	}
}
