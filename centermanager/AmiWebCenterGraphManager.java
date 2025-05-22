package com.f1.ami.web.centermanager;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.f1.ami.amicommon.AmiCenterDefinition;
import com.f1.ami.amicommon.AmiConsts;
import com.f1.ami.web.AmiWebCenterEntityListener;
import com.f1.ami.web.AmiWebCenterGraphListener;
import com.f1.ami.web.AmiWebManager;
import com.f1.ami.web.AmiWebObject;
import com.f1.ami.web.AmiWebObjectFields;
import com.f1.ami.web.AmiWebObjects;
import com.f1.ami.web.AmiWebRealtimeObjectListener;
import com.f1.ami.web.AmiWebRealtimeObjectManager;
import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.graph.AmiCenterGraphNode;
import com.f1.ami.web.graph.AmiCenterGraphNode_Dbo;
import com.f1.ami.web.graph.AmiCenterGraphNode_Index;
import com.f1.ami.web.graph.AmiCenterGraphNode_Method;
import com.f1.ami.web.graph.AmiCenterGraphNode_Procedure;
import com.f1.ami.web.graph.AmiCenterGraphNode_Table;
import com.f1.ami.web.graph.AmiCenterGraphNode_Timer;
import com.f1.ami.web.graph.AmiCenterGraphNode_Trigger;
import com.f1.utils.AH;
import com.f1.utils.CH;
import com.f1.utils.LH;
import com.f1.utils.OH;
import com.f1.utils.OneToOne;
import com.f1.utils.SH;
import com.f1.utils.structs.Tuple2;

public class AmiWebCenterGraphManager implements AmiWebCenterEntityListener, AmiWebRealtimeObjectListener {
	private static final Logger log = LH.get();
	public static final Set<String> INTERESTED_TYPES = CH.s(AmiConsts.TYPE_TABLE, AmiConsts.TYPE_DBO, AmiConsts.TYPE_INDEX, AmiConsts.TYPE_TRIGGER, AmiConsts.TYPE_TIMER,
			AmiConsts.TYPE_PROCEDURE, AmiConsts.TYPE_CENTER);
	private AmiWebService service;
	static private final OneToOne<String, String> DATATYPES2TYPES = new OneToOne<String, String>();
	private static AmiWebCenterGraphListener[] EMPTY = new AmiWebCenterGraphListener[0];
	private AmiWebCenterGraphListener listeners[] = EMPTY;
	private Map<String, AmiCenterGraphNode_Table> tableNodes = new HashMap<String, AmiCenterGraphNode_Table>();
	private Map<String, AmiCenterGraphNode_Dbo> dboNodes = new HashMap<String, AmiCenterGraphNode_Dbo>();
	private Map<String, AmiCenterGraphNode_Index> indexNodes = new HashMap<String, AmiCenterGraphNode_Index>();
	private Map<String, AmiCenterGraphNode_Method> methodNodes = new HashMap<String, AmiCenterGraphNode_Method>();
	private Map<String, AmiCenterGraphNode_Procedure> procedureNodes = new HashMap<String, AmiCenterGraphNode_Procedure>();
	private Map<String, AmiCenterGraphNode_Trigger> triggerNodes = new HashMap<String, AmiCenterGraphNode_Trigger>();
	private Map<String, AmiCenterGraphNode_Timer> timerNodes = new HashMap<String, AmiCenterGraphNode_Timer>();
	private long nextUid = 1;

	public static final Comparator<? super AmiCenterGraphNode> COMPARATOR_ID = new Comparator<AmiCenterGraphNode>() {
		@Override
		public int compare(AmiCenterGraphNode o1, AmiCenterGraphNode o2) {
			return OH.compare(o1.getLabel(), o2.getLabel(), false, SH.COMPARATOR_CASEINSENSITIVE_STRING);
		}
	};

	private long nextUid() {
		return this.nextUid++;
	}

	public void debug() {
		for (AmiCenterGraphNode_Table t : this.tableNodes.values()) {
			StringBuilder sb = new StringBuilder();
			sb.append(t.getLabel()).append("target indexes").append(": ");
			sb.append(t.getTargetIndexes());
			System.out.println(sb);
		}

	}

	public AmiWebCenterGraphManager(AmiWebService service) {
		this.service = service;
		init();
	}

	public void init() {
		for (AmiCenterDefinition d : this.service.getCenterIds()) {
			byte centerId = d.getId();
			AmiWebManager m = this.service.getWebManagers().getWebManager(centerId);
			for (AmiWebObjects i : m.getAmiObjectsByTypes(INTERESTED_TYPES)) {
				i.addAmiListener(this);
			}
		}

	}

	public void addListener(AmiWebCenterGraphListener listener) {
		this.listeners = AH.append(this.listeners, listener);
	}
	public boolean removeListener(AmiWebCenterGraphListener listener) {
		int i = AH.indexOf(listener, this.listeners);
		if (i == -1)
			return false;
		this.listeners = AH.remove(this.listeners, i);
		return true;
	}

	@Override
	public void onAmiCenterEntityAdded(String nodeType, String nodeName, Object correlationData, boolean readonly) {
		if (nodeType == null) {
			throw new IllegalArgumentException("unknown nodeType: " + nodeType);
		}
		AmiCenterGraphNode n = null;
		switch (nodeType) {
			case AmiConsts.TYPE_TABLE:
				getOrCreateTable(nodeName, readonly);
				break;
			case AmiConsts.TYPE_PROCEDURE:
				getOrCreateProcedure(nodeName, readonly);
				break;
			case AmiConsts.TYPE_TIMER:
				getOrCreateTimer(nodeName, readonly);
				break;
			case AmiConsts.TYPE_TRIGGER:
				getOrCreateTrigger(nodeName, correlationData, readonly);
				break;
			case AmiConsts.TYPE_INDEX:
				getOrCreateIndex(nodeName, correlationData, readonly);
				break;
			case AmiConsts.TYPE_DBO:
				getOrCreateDbo(nodeName, readonly);
				break;
			//TODO:MISSING METHODS
		}
	}

	public AmiCenterGraphNode_Dbo getDbo(String nodeName) {
		AmiCenterGraphNode_Dbo n = this.dboNodes.get(nodeName);
		if (n == null) {
			throw new NullPointerException();
		}
		return n;
	}
	public AmiCenterGraphNode_Trigger getTrigger(String nodeName) {
		AmiCenterGraphNode_Trigger n = this.triggerNodes.get(nodeName);
		if (n == null) {
			throw new NullPointerException();
		}
		return n;
	}

	public AmiCenterGraphNode_Table getTable(String nodeName) {
		AmiCenterGraphNode_Table n = this.tableNodes.get(nodeName);
		if (n == null) {
			throw new NullPointerException();
		}
		return n;
	}

	public AmiCenterGraphNode_Procedure getProcedure(String nodeName) {
		AmiCenterGraphNode_Procedure n = this.procedureNodes.get(nodeName);
		if (n == null) {
			throw new NullPointerException();
		}
		return n;
	}

	public AmiCenterGraphNode_Index getIndex(String nodeName) {
		AmiCenterGraphNode_Index n = this.indexNodes.get(nodeName);
		if (n == null) {
			throw new NullPointerException();
		}
		return n;
	}

	public AmiCenterGraphNode_Timer getTimer(String nodeName) {
		AmiCenterGraphNode_Timer n = this.timerNodes.get(nodeName);
		if (n == null) {
			throw new NullPointerException();
		}
		return n;
	}

	public AmiCenterGraphNode_Method getMethod(String nodeName) {
		AmiCenterGraphNode_Method n = this.methodNodes.get(nodeName);
		if (n == null) {
			throw new NullPointerException();
		}
		return n;
	}

	private AmiCenterGraphNode_Dbo getOrCreateDbo(String nodeName, boolean readonly) {
		AmiCenterGraphNode_Dbo n = this.dboNodes.get(nodeName);
		if (n == null) {
			this.dboNodes.put(nodeName, n = new AmiCenterGraphNode_Dbo(this, nextUid(), nodeName, readonly));
			fireAdded(n);
		}
		return n;
	}

	private AmiCenterGraphNode_Index getOrCreateIndex(String nodeName, Object correlationData, boolean readonly) {
		AmiCenterGraphNode_Index n = this.indexNodes.get(nodeName);
		if (n == null) {
			this.indexNodes.put(nodeName, n = new AmiCenterGraphNode_Index(this, nextUid(), nodeName, readonly));
			if (correlationData instanceof String) {
				String tableName = (String) correlationData;
				AmiCenterGraphNode_Table owner = getOrCreateTable(tableName);
				owner.bindTargetIndex(nodeName, n);
				n.setBindingTable(owner);
			}
			fireAdded(n);
		}
		return n;
	}

	private AmiCenterGraphNode_Trigger getOrCreateTrigger(String nodeName, Object correlationData, boolean readonly) {
		AmiCenterGraphNode_Trigger n = this.triggerNodes.get(nodeName);
		if (n == null) {
			this.triggerNodes.put(nodeName, n = new AmiCenterGraphNode_Trigger(this, nextUid(), nodeName, readonly));
			if (correlationData instanceof String) {
				String tables = (String) correlationData;
				String[] tableNames = null;
				tableNames = SH.split(',', tables);
				for (String name : tableNames) {
					AmiCenterGraphNode_Table owner = getOrCreateTable(name);
					owner.bindTargetTrigger(nodeName, n);
					n.setBindingTable(owner);
				}

			}
			fireAdded(n);
		}
		return n;
	}

	private AmiCenterGraphNode_Timer getOrCreateTimer(String nodeName, boolean readonly) {
		AmiCenterGraphNode_Timer n = this.timerNodes.get(nodeName);
		if (n == null) {
			this.timerNodes.put(nodeName, n = new AmiCenterGraphNode_Timer(this, nextUid(), nodeName, readonly));
			fireAdded(n);
		}
		return n;
	}

	private AmiCenterGraphNode_Procedure getOrCreateProcedure(String nodeName, boolean readonly) {
		AmiCenterGraphNode_Procedure n = this.procedureNodes.get(nodeName);
		if (n == null) {
			this.procedureNodes.put(nodeName, n = new AmiCenterGraphNode_Procedure(this, nextUid(), nodeName, readonly));
			fireAdded(n);
		}
		return n;
	}

	private AmiCenterGraphNode_Table getOrCreateTable(String nodeName, boolean readonly) {
		AmiCenterGraphNode_Table n = this.tableNodes.get(nodeName);
		if (n == null) {
			this.tableNodes.put(nodeName, n = new AmiCenterGraphNode_Table(this, nextUid(), nodeName, readonly));
			fireAdded(n);
		}
		return n;
	}

	//TODO: A better way to do it? Only method nodes are allowed to be created from outside of this class(in AmiWebCenterManagerPortlet by querying the backend)
	public AmiCenterGraphNode_Method getOrCreateMethod(String nodeName, boolean readonly) {
		AmiCenterGraphNode_Method n = this.methodNodes.get(nodeName);
		if (n == null) {
			this.methodNodes.put(nodeName, n = new AmiCenterGraphNode_Method(this, nextUid(), nodeName, readonly));
			fireAdded(n);
		}
		return n;
	}

	private AmiCenterGraphNode_Dbo getOrCreateDbo(String nodeName) {
		AmiCenterGraphNode_Dbo n = this.dboNodes.get(nodeName);
		if (n == null) {
			this.dboNodes.put(nodeName, n = new AmiCenterGraphNode_Dbo(this, nextUid(), nodeName));
			fireAdded(n);
		}
		return n;
	}

	private AmiCenterGraphNode_Index getOrCreateIndex(String nodeName) {
		AmiCenterGraphNode_Index n = this.indexNodes.get(nodeName);
		if (n == null) {
			this.indexNodes.put(nodeName, n = new AmiCenterGraphNode_Index(this, nextUid(), nodeName));
			fireAdded(n);
		}
		return n;
	}

	private AmiCenterGraphNode_Trigger getOrCreateTrigger(String nodeName) {
		AmiCenterGraphNode_Trigger n = this.triggerNodes.get(nodeName);
		if (n == null) {
			this.triggerNodes.put(nodeName, n = new AmiCenterGraphNode_Trigger(this, nextUid(), nodeName));
			fireAdded(n);
		}
		return n;
	}

	private AmiCenterGraphNode_Timer getOrCreateTimer(String nodeName) {
		AmiCenterGraphNode_Timer n = this.timerNodes.get(nodeName);
		if (n == null) {
			this.timerNodes.put(nodeName, n = new AmiCenterGraphNode_Timer(this, nextUid(), nodeName));
			fireAdded(n);
		}
		return n;
	}

	private AmiCenterGraphNode_Procedure getOrCreateProcedure(String nodeName) {
		AmiCenterGraphNode_Procedure n = this.procedureNodes.get(nodeName);
		if (n == null) {
			this.procedureNodes.put(nodeName, n = new AmiCenterGraphNode_Procedure(this, nextUid(), nodeName));
			fireAdded(n);
		}
		return n;
	}

	private AmiCenterGraphNode_Table getOrCreateTable(String nodeName) {
		AmiCenterGraphNode_Table n = this.tableNodes.get(nodeName);
		if (n == null) {
			this.tableNodes.put(nodeName, n = new AmiCenterGraphNode_Table(this, nextUid(), nodeName));
			fireAdded(n);
		}
		return n;
	}

	@Override
	public void onAmiCenterEntityRemoved(String nodeType, String nodeName, Object correlatioData) {
		if (nodeType == null) {
			throw new IllegalArgumentException("unknown nodeType: " + nodeType);
		}
		AmiCenterGraphNode n = null;
		switch (nodeType) {
			case AmiConsts.TYPE_TABLE:
				n = this.tableNodes.get(nodeName);
				if (n != null) {
					AmiCenterGraphNode_Table nta = this.tableNodes.remove(nodeName);
					//when you drop a table, you should also remove its index from the this.indexNodes
					//Not true for triggers, bc you cannot drop a table that has triggers sitting on it
					for (Entry<String, AmiCenterGraphNode_Index> e : nta.getTargetIndexes().entrySet()) {
						this.indexNodes.remove(e.getKey());
					}
					fireRemoved(n);
				}
				break;
			case AmiConsts.TYPE_PROCEDURE:
				n = this.procedureNodes.get(nodeName);
				if (n != null) {
					this.procedureNodes.remove(nodeName);
					fireRemoved(n);
				}
				break;
			case AmiConsts.TYPE_TIMER:
				n = this.timerNodes.get(nodeName);
				if (n != null) {
					this.timerNodes.remove(nodeName);
					fireRemoved(n);
				}
				break;
			case AmiConsts.TYPE_TRIGGER:
				n = this.triggerNodes.get(nodeName);
				if (n != null) {
					AmiCenterGraphNode_Trigger nt = this.triggerNodes.remove(nodeName);
					//when a trigger is removed, we also need to update its parent(s)(table(s)) to remove its binding trigger
					for (AmiCenterGraphNode_Table t : nt.getBindingTables()) {
						t.unbindTargetTrigger(nt.getLabel(), nt);
					}
					fireRemoved(n);
				}
				break;
			case AmiConsts.TYPE_INDEX:
				n = this.indexNodes.get(nodeName);
				if (n != null) {
					AmiCenterGraphNode_Index ni = this.indexNodes.remove(nodeName);
					//when an index is removed, we also need to update its parent(table) to remove its binding index
					ni.getBindingTable().unbindTargetIndex(ni.getLabel(), ni);
					fireRemoved(n);
				}
				break;
			case AmiConsts.TYPE_DBO:
				n = this.dboNodes.get(nodeName);
				if (n != null) {
					this.dboNodes.remove(nodeName);
					fireRemoved(n);
				}
				break;
		}
	}

	private void fireAdded(AmiCenterGraphNode node) {
		for (AmiWebCenterGraphListener i : this.listeners) {
			i.onCenterNodeAdded(node);
		}
	}
	private void fireRemoved(AmiCenterGraphNode node) {
		for (AmiWebCenterGraphListener i : this.listeners) {
			i.onCenterNodeRemoved(node);
		}
	}

	@Override
	public void onAmiEntitiesReset(AmiWebRealtimeObjectManager manager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAmiEntityAdded(AmiWebRealtimeObjectManager manager, AmiWebObject entity) {
		String amiDataType = entity.getTypeName();
		String definedBy = (String) entity.getValue("DefinedBy");
		String name = null;
		boolean readonly = !"USER".equals(definedBy);
		if (AmiConsts.TYPE_TABLE.equals(amiDataType)) {
			name = (String) entity.getParam("TableName");
			onAmiCenterEntityAdded(AmiConsts.TYPE_TABLE, name, null, readonly);
		} else if (AmiConsts.TYPE_TRIGGER.equals(amiDataType)) {
			name = (String) entity.getParam("TriggerName");
			String tableName = (String) entity.getParam("TableName");
			onAmiCenterEntityAdded(AmiConsts.TYPE_TRIGGER, name, tableName, readonly);
		} else if (AmiConsts.TYPE_TIMER.equals(amiDataType)) {
			name = (String) entity.getParam("TimerName");
			onAmiCenterEntityAdded(AmiConsts.TYPE_TIMER, name, null, readonly);
		} else if (AmiConsts.TYPE_PROCEDURE.equals(amiDataType)) {
			name = (String) entity.getParam("ProcedureName");
			onAmiCenterEntityAdded(AmiConsts.TYPE_PROCEDURE, name, null, readonly);
		} else if (AmiConsts.TYPE_DBO.equals(amiDataType)) {
			name = (String) entity.getParam("DboName");
			onAmiCenterEntityAdded(AmiConsts.TYPE_DBO, name, null, readonly);
		} else if (AmiConsts.TYPE_INDEX.equals(amiDataType)) {
			name = (String) entity.getParam("IndexName");
			String ownerTable = (String) entity.getParam("TableName");
			String formattedName = AmiCenterManagerUtils.formatIndexNames(ownerTable, name);
			String tableName = (String) entity.getParam("TableName");
			onAmiCenterEntityAdded(AmiConsts.TYPE_INDEX, formattedName, tableName, readonly);
		}
		//TODO:missing methods

	}

	@Override
	public void onAmiEntityUpdated(AmiWebRealtimeObjectManager manager, AmiWebObjectFields fields, AmiWebObject entity) {
	}

	@Override
	public void onAmiEntityRemoved(AmiWebRealtimeObjectManager manager, AmiWebObject entity) {
		String amiDataType = entity.getTypeName();
		String name = null;
		if (AmiConsts.TYPE_TABLE.equals(amiDataType)) {
			name = (String) entity.getParam("TableName");
			onAmiCenterEntityRemoved(AmiConsts.TYPE_TABLE, name, null);
		} else if (AmiConsts.TYPE_TRIGGER.equals(amiDataType)) {
			name = (String) entity.getParam("TriggerName");
			onAmiCenterEntityRemoved(AmiConsts.TYPE_TRIGGER, name, null);
		} else if (AmiConsts.TYPE_TIMER.equals(amiDataType)) {
			name = (String) entity.getParam("TimerName");
			onAmiCenterEntityRemoved(AmiConsts.TYPE_TIMER, name, null);
		} else if (AmiConsts.TYPE_PROCEDURE.equals(amiDataType)) {
			name = (String) entity.getParam("ProcedureName");
			onAmiCenterEntityRemoved(AmiConsts.TYPE_PROCEDURE, name, null);
		} else if (AmiConsts.TYPE_DBO.equals(amiDataType)) {
			name = (String) entity.getParam("DboName");
			onAmiCenterEntityRemoved(AmiConsts.TYPE_DBO, name, null);
		} else if (AmiConsts.TYPE_INDEX.equals(amiDataType)) {
			name = (String) entity.getParam("IndexName");
			String ownerTable = (String) entity.getParam("TableName");
			String formattedName = AmiCenterManagerUtils.formatIndexNames(ownerTable, name);
			onAmiCenterEntityRemoved(AmiConsts.TYPE_INDEX, formattedName, null);
		}

	}

	@Override
	public void onLowerAriChanged(AmiWebRealtimeObjectManager manager, String oldAri, String newAri) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSchemaChanged(AmiWebRealtimeObjectManager manager, byte schemaStatus, Map<String, Tuple2<Class, Class>> columns) {
		// TODO Auto-generated method stub

	}
}
