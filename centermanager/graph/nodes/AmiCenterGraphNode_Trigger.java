package com.f1.ami.web.centermanager.graph.nodes;

import java.util.HashSet;
import java.util.Set;

import com.f1.ami.web.centermanager.AmiWebCenterGraphManager;

public class AmiCenterGraphNode_Trigger extends AmiCenterGraphAbstractNode {

	public static final byte CODE = AmiCenterGraphNode.TYPE_TRIGGER;
	private Set<AmiCenterGraphNode_Table> bindingTables = new HashSet<AmiCenterGraphNode_Table>();

	public AmiCenterGraphNode_Trigger(AmiWebCenterGraphManager manager, long uid, String label) {
		super(manager, uid, label);
	}

	public AmiCenterGraphNode_Trigger(AmiWebCenterGraphManager manager, long uid, String label, boolean readOnly) {
		this(manager, uid, label);
		this.readOnly = readOnly;
	}

	@Override
	public byte getType() {
		return CODE;
	}

	public void setBindingTable(AmiCenterGraphNode_Table t) {
		this.bindingTables.add(t);
	}

	public Set<AmiCenterGraphNode_Table> getBindingTables() {
		return this.bindingTables;
	}

}
