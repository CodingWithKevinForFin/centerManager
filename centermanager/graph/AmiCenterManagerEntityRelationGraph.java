package com.f1.ami.web.centermanager.graph;

import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode;
import com.f1.ami.web.centermanager.portlets.AmiWebCenterManagerPortlet;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.portal.impl.WebMenuListener;
import com.f1.suite.web.portal.impl.visual.GraphContextMenuFactory;
import com.f1.suite.web.portal.impl.visual.GraphListener;
import com.f1.suite.web.portal.impl.visual.GraphPortlet;
import com.f1.suite.web.portal.impl.visual.GraphPortlet.Node;
import com.f1.utils.concurrent.IdentityHashSet;
import com.f1.utils.structs.LongKeyMap;

public class AmiCenterManagerEntityRelationGraph implements GraphListener, GraphContextMenuFactory, WebMenuListener {
	private GraphPortlet erGraph;
	private AmiWebService service;
	private final LongKeyMap<Node> graphNodes = new LongKeyMap<Node>();
	private int minXPos[];
	private final LongKeyMap<Integer> depths = new LongKeyMap<Integer>();
	private final IdentityHashSet<AmiCenterGraphNode> remaining = new IdentityHashSet<AmiCenterGraphNode>();
	private int maxDepth;
	private LongKeyMap<AmiCenterGraphNode> allNodes = new LongKeyMap<AmiCenterGraphNode>();
	private IdentityHashSet<AmiCenterGraphNode> origNodes = new IdentityHashSet<AmiCenterGraphNode>();
	private IdentityHashSet<AmiCenterGraphNode> toSelect = new IdentityHashSet<AmiCenterGraphNode>();
	private AmiWebCenterManagerPortlet owner;
	private boolean allowModification;
	private static byte WALK_SOURCE = 1;//source is always table
	private static byte WALK_TARGET = 2;//target can be triggers or index

	public AmiCenterManagerEntityRelationGraph(AmiWebCenterManagerPortlet owner, AmiWebService service, GraphPortlet graph, boolean allowModification) {
		this.erGraph = graph;
		this.owner = owner;
		this.allowModification = allowModification;
		this.erGraph.setMenuFactory(this);
		this.service = service;
		this.erGraph.addGraphListener(this);
		this.erGraph.setSnapSize(5);
		this.erGraph.setGridSize(20);
	}

	@Override
	public void onMenuItem(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMenuDismissed() {
		// TODO Auto-generated method stub

	}

	@Override
	public WebMenu createMenu(GraphPortlet graph) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onSelectionChanged(GraphPortlet graphPortlet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onContextMenu(GraphPortlet graphPortlet, String action) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserClick(GraphPortlet graphPortlet, Node nodeOrNull, int button, boolean ctrl, boolean shft) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserDblClick(GraphPortlet graphPortlet, Integer id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onKeyDown(String keyCode, String ctrl) {
		// TODO Auto-generated method stub

	}

}
