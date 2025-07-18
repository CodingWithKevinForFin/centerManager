package com.f1.ami.web.centermanager.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;

import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.centermanager.AmiWebCenterGraphManager;
import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode;
import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode_Dbo;
import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode_Index;
import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode_Method;
import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode_Procedure;
import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode_Table;
import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode_Timer;
import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode_Trigger;
import com.f1.ami.web.centermanager.portlets.AmiWebCenterManagerPortlet;
import com.f1.ami.web.graph.AmiWebGraphNode_Link;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.portal.impl.WebMenuListener;
import com.f1.suite.web.portal.impl.visual.GraphContextMenuFactory;
import com.f1.suite.web.portal.impl.visual.GraphListener;
import com.f1.suite.web.portal.impl.visual.GraphPortlet;
import com.f1.suite.web.portal.impl.visual.GraphPortlet.Edge;
import com.f1.suite.web.portal.impl.visual.GraphPortlet.Node;
import com.f1.utils.CH;
import com.f1.utils.MH;
import com.f1.utils.OH;
import com.f1.utils.concurrent.IdentityHashSet;
import com.f1.utils.structs.LongKeyMap;

public class AmiCenterManagerEntityRelationGraph implements GraphListener, GraphContextMenuFactory, WebMenuListener {
	private static final String STYLE_TABLE = "_cna=graph_node_table";//"_cna=graph_node_rt_chartpanel";//"_cna=graph_node_table";
	private static final String STYLE_TRIGGER = "_cna=graph_node_trigger";
	private static final String STYLE_TIMER = "_cna=graph_node_timer";

	private static final String STYLE_PROCEDURE = "_cna=graph_node_procedure";
	private static final String STYLE_METHOD = "_cna=graph_node_method";
	private static final String STYLE_DBO = "_cna=graph_node_dbo";
	private static final String STYLE_INDEX = "_cna=graph_node_index";

	private static final int LEFT_PADDING = 140;
	private static final int TOP_PADDING = 100;
	private static final int X_SPACING = 120;
	private static final int Y_SPACING = 120;
	private static final int NODE_WIDTH = 100;
	private static final int NODE_HEIGHT = 60;

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
	private static byte WALK_INBOUND_TRIGGER = 1;
	private static byte WALK_OUTBOUND_TRIGGER = 2;
	private static byte WALK_INBOUND_TRIGGER_SOURCE = 4;
	private static byte WALK_INBOUND_TRIGGER_TARGET = 8;
	private static byte WALK_OUTBOUND_TRIGGER_SOURCE = 16;
	private static byte WALK_OUTBOUND_TRIGGER_TARGET = 32;

	private final Comparator<AmiCenterGraphNode> comparator = new Comparator<AmiCenterGraphNode>() {

		@Override
		public int compare(AmiCenterGraphNode o1, AmiCenterGraphNode o2) {
			Integer d1 = depths.get(o1.getUid());
			Integer d2 = depths.get(o2.getUid());
			int n = OH.compare(d1, d2, true);
			return n == 0 ? AmiWebCenterGraphManager.COMPARATOR_ID.compare(o1, o2) : n;
		}
	};

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

	public void buildGraph(IdentityHashSet<AmiCenterGraphNode> origNodes, IdentityHashSet<AmiCenterGraphNode> toSelect) {
		if (this.origNodes.equals(origNodes) && this.toSelect.equals(toSelect))
			return;
		this.origNodes.clear();
		this.origNodes.addAll(origNodes);
		this.toSelect.clear();
		this.toSelect.addAll(toSelect);
		this.owner.onGraphNeedsRebuild();
	}

	public void rebuild() {
		IdentityHashMap<AmiCenterGraphNode, Byte> allNodes = new IdentityHashMap<AmiCenterGraphNode, Byte>();
		for (AmiCenterGraphNode i : this.origNodes)
			walkNodes(i, allNodes, (byte) (WALK_INBOUND_TRIGGER | WALK_OUTBOUND_TRIGGER | WALK_INBOUND_TRIGGER_SOURCE | WALK_INBOUND_TRIGGER_TARGET | WALK_OUTBOUND_TRIGGER_SOURCE
					| WALK_OUTBOUND_TRIGGER_TARGET));
		System.out.println(allNodes);
		this.allNodes.clear();
		for (AmiCenterGraphNode i : allNodes.keySet())
			this.allNodes.put(i.getUid(), i);
		this.graphNodes.clear();
		this.erGraph.clear();
		this.depths.clear();
		this.remaining.clear();
		for (AmiCenterGraphNode i : this.allNodes.values())
			this.remaining.add(i);

		//TODO:REMOVE this.remaining? remaining should be identical to this.allNodes
		int depth = 0;

		List<AmiCenterGraphNode_Table> tables = new ArrayList<AmiCenterGraphNode_Table>();
		List<AmiCenterGraphNode_Trigger> triggers = new ArrayList<AmiCenterGraphNode_Trigger>();
		List<AmiCenterGraphNode_Index> indexes = new ArrayList<AmiCenterGraphNode_Index>();

		List<AmiCenterGraphNode_Procedure> procedures = new ArrayList<AmiCenterGraphNode_Procedure>();
		List<AmiCenterGraphNode_Timer> timers = new ArrayList<AmiCenterGraphNode_Timer>();
		List<AmiCenterGraphNode_Dbo> dbos = new ArrayList<AmiCenterGraphNode_Dbo>();
		List<AmiCenterGraphNode_Method> methods = new ArrayList<AmiCenterGraphNode_Method>();

		for (AmiCenterGraphNode i : this.allNodes.values()) {
			depth = Math.max(depth, determineDepth(i));
			if (i.getType() == AmiCenterGraphNode.TYPE_TABLE)
				tables.add((AmiCenterGraphNode_Table) i);
			else if (i.getType() == AmiCenterGraphNode.TYPE_TRIGGER)
				triggers.add((AmiCenterGraphNode_Trigger) i);
			else if (i.getType() == AmiCenterGraphNode.TYPE_INDEX)
				indexes.add((AmiCenterGraphNode_Index) i);
			else if (i.getType() == AmiCenterGraphNode.TYPE_PROCEDURE)
				procedures.add((AmiCenterGraphNode_Procedure) i);
			else if (i.getType() == AmiCenterGraphNode.TYPE_TIMER)
				timers.add((AmiCenterGraphNode_Timer) i);
			else if (i.getType() == AmiCenterGraphNode.TYPE_METHOD)
				methods.add((AmiCenterGraphNode_Method) i);
			else if (i.getType() == AmiCenterGraphNode.TYPE_DBO)
				dbos.add((AmiCenterGraphNode_Dbo) i);
		}

		this.maxDepth = depth;
		minXPos = new int[depth + 1];
		//Start with tables
		for (AmiCenterGraphNode_Table i : sort(tables)) {
			if (i.getTargetIndexes().isEmpty() && i.getTargetTriggers().isEmpty())
				continue;
			Node node = addNode(0, i);
			if (node == null)
				continue;
		}

		//then for tables without indexes or triggers
		for (AmiCenterGraphNode i : CH.l(remaining)) {
			addNode(0, i);
		}

		//build links
		for (AmiCenterGraphNode i : allNodes.keySet()) {
			Node sourceNode = graphNodes.get(i.getUid());
			switch (i.getType()) {
				case AmiCenterGraphNode.TYPE_TABLE:
					AmiCenterGraphNode_Table t = (AmiCenterGraphNode_Table) i;
					//build links for triggers
					for (AmiCenterGraphNode_Trigger j : t.getTargetTriggers().values()) {
						Node targetNode = graphNodes.get(j.getUid());
						if (targetNode != null) {
							addDataEdge(sourceNode, targetNode);
						}
					}
					//build links for indexes
					for (AmiCenterGraphNode_Index j : t.getTargetIndexes().values()) {
						Node targetNode = graphNodes.get(j.getUid());
						if (targetNode != null) {
							addDataEdge(sourceNode, targetNode);
						}
					}
					break;
			}
		}
	}

	private Node addNode(int minx, AmiCenterGraphNode i) {
		if (!this.remaining.remove(i))
			return null;
		int y = depths.get(i.getUid());
		String style;
		String name = i.getLabel();
		//String description = i.getDescription();
		//		if (SH.is(description))
		//			name = name + "<BR>(" + description + ")";
		switch (i.getType()) {
			case AmiCenterGraphNode.TYPE_TABLE:
				style = STYLE_TABLE;
				break;
			case AmiCenterGraphNode.TYPE_TIMER: {
				style = STYLE_TIMER;
				break;
			}
			case AmiCenterGraphNode.TYPE_TRIGGER: {
				style = STYLE_TRIGGER;
				break;
			}
			case AmiCenterGraphNode.TYPE_PROCEDURE: {
				style = STYLE_PROCEDURE;
				break;
			}
			case AmiCenterGraphNode.TYPE_INDEX: {
				style = STYLE_INDEX;
				break;
			}
			case AmiCenterGraphNode.TYPE_DBO: {
				style = STYLE_DBO;
				break;
			}
			case AmiCenterGraphNode.TYPE_METHOD:
				style = STYLE_METHOD;
				break;
			default:
				style = "";
		}
		//TODO:ADD undefined icon 
		//		if (i.getInner() == null && i.getType() != AmiWebGraphNode.TYPE_FEED)
		//			name += "<BR><span style='background:red;color:white;pointer-events:none'>Not Defined</span>";
		int x = Math.max(minx, minXPos[y]);
		minXPos[y] = x + X_SPACING;
		Node node = this.erGraph.addNode(LEFT_PADDING + x, TOP_PADDING + (this.maxDepth - y) * Y_SPACING, NODE_WIDTH, NODE_HEIGHT, name, style);
		node.setData(i);
		graphNodes.put(i.getUid(), node);
		return node;
	}

	private <T extends AmiCenterGraphNode> Collection<T> sort(Collection<T> in) {
		if (in.isEmpty())
			return Collections.EMPTY_LIST;
		ArrayList<T> r = new ArrayList<T>();
		for (T i : in)
			if (this.depths.containsKey(i.getUid()) || i instanceof AmiWebGraphNode_Link)
				r.add(i);
		Collections.sort(r, comparator);
		return r;
	}

	private void addDataEdge(Node sourceNode, Node targetNode) {
		Edge edge = this.erGraph.addEdge(sourceNode.getId(), targetNode.getId());
		edge.setColor("#AEAEAE");
		edge.setDirection(GraphPortlet.DIRECTION_FORWARD);
	}

	//NOT SURE how walkNodes() and determineDepth() works, TODO: 
	private int determineDepth(AmiCenterGraphNode i) {
		Integer r = null;
		if (r == null) {
			switch (i.getType()) {
				case AmiCenterGraphNode.TYPE_INDEX:
					r = 0;
					break;
				case AmiCenterGraphNode.TYPE_TRIGGER:
					r = 2;
					break;
				case AmiCenterGraphNode.TYPE_TABLE:
					r = 1;
					break;
				default://all other standalone objects will have depth of 0(timer,procedure,method,dbo)
					r = 0;
			}
			depths.put(i.getUid(), r);
		}
		return r;

	}

	private void walkNodes(AmiCenterGraphNode t2, IdentityHashMap<AmiCenterGraphNode, Byte> nodes, byte walkMask) {
		if (t2 == null)
			return;
		Byte existing = nodes.get(t2);
		if (existing == null) {
			existing = 0;
			nodes.put(t2, walkMask);
		} else {
			nodes.put(t2, (byte) (walkMask | existing));
		}
		byte remaining = (byte) (walkMask & (~existing));
		if (remaining == 0)
			return;
		//		nodes.add(t2);
		final boolean walkInboundTrigger = MH.allBits(remaining, WALK_INBOUND_TRIGGER);
		final boolean walkOutboundTrigger = MH.allBits(remaining, WALK_OUTBOUND_TRIGGER);
		final boolean walkInboundTriggerSource = MH.allBits(remaining, WALK_INBOUND_TRIGGER_SOURCE);
		final boolean walkInboundTriggerTarget = MH.allBits(remaining, WALK_INBOUND_TRIGGER_TARGET);
		final boolean walkOutboundTriggerSource = MH.allBits(remaining, WALK_OUTBOUND_TRIGGER_SOURCE);
		final boolean walkOutboundTriggerTarget = MH.allBits(remaining, WALK_OUTBOUND_TRIGGER_TARGET);
		//walkNodes(,nodes,1);
		String style = null;
		switch (t2.getType()) {
			case AmiCenterGraphNode.TYPE_TABLE:
				AmiCenterGraphNode_Table tableNode = (AmiCenterGraphNode_Table) t2;
				if (walkOutboundTrigger) {//outbound
					for (AmiCenterGraphNode_Trigger triggerNode : tableNode.getOutboundTriggers())
						walkNodes(triggerNode, nodes, (byte) (WALK_OUTBOUND_TRIGGER_SOURCE | WALK_OUTBOUND_TRIGGER_TARGET));
				}
				if (walkInboundTrigger) {//inbound
					for (AmiCenterGraphNode_Trigger triggerNode : tableNode.getInboundTriggers())
						walkNodes(triggerNode, nodes, (byte) (WALK_INBOUND_TRIGGER_SOURCE | WALK_INBOUND_TRIGGER_TARGET));
				}
				if (walkInboundTriggerSource || walkInboundTriggerTarget || walkOutboundTriggerSource || walkOutboundTriggerTarget)
					System.out.println("Will only walk up to trigger source/target tables");
				style = STYLE_TABLE;
				break;
			case AmiCenterGraphNode.TYPE_TRIGGER: {
				AmiCenterGraphNode_Trigger triggerNode = (AmiCenterGraphNode_Trigger) t2;
				if (walkInboundTriggerSource) {
					for (AmiCenterGraphNode_Table i : triggerNode.getSourceTables())
						walkNodes(i, nodes, WALK_INBOUND_TRIGGER_SOURCE);
				}
				if (walkInboundTriggerTarget) {
					for (AmiCenterGraphNode_Table i : triggerNode.getSinkTables())
						walkNodes(i, nodes, WALK_INBOUND_TRIGGER_SOURCE);
				}
				if (walkOutboundTriggerSource) {
					for (AmiCenterGraphNode_Table i : triggerNode.getSourceTables())
						walkNodes(i, nodes, WALK_OUTBOUND_TRIGGER_SOURCE);
				}
				if (walkOutboundTriggerTarget) {
					for (AmiCenterGraphNode_Table i : triggerNode.getSinkTables())
						walkNodes(i, nodes, WALK_OUTBOUND_TRIGGER_SOURCE);
				}
				style = STYLE_TRIGGER;
				break;
			}

		}
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
