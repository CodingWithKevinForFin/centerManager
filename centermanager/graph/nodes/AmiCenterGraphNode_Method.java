package com.f1.ami.web.centermanager.graph.nodes;

import com.f1.ami.web.centermanager.AmiWebCenterGraphManager;

public class AmiCenterGraphNode_Method extends AmiCenterGraphAbstractNode {
	public static final byte CODE = AmiCenterGraphNode.TYPE_METHOD;

	public AmiCenterGraphNode_Method(AmiWebCenterGraphManager manager, long uid, String label) {
		super(manager, uid, label);
	}

	public AmiCenterGraphNode_Method(AmiWebCenterGraphManager manager, long uid, String label, boolean readOnly) {
		this(manager, uid, label);
		this.readOnly = readOnly;
	}

	@Override
	public byte getType() {
		return CODE;
	}

}
