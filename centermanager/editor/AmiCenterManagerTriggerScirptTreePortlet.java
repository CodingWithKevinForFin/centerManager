package com.f1.ami.web.centermanager.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.f1.ami.amicommon.msg.AmiCenterQueryDsRequest;
import com.f1.ami.amicommon.msg.AmiCenterQueryDsResponse;
import com.f1.ami.web.AmiWebAmiScriptCallback;
import com.f1.ami.web.AmiWebCompilerListener;
import com.f1.ami.web.AmiWebConsts;
import com.f1.ami.web.AmiWebDomObject;
import com.f1.ami.web.AmiWebDomObjectDependency;
import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.AmiWebFormula;
import com.f1.ami.web.AmiWebLayoutHelper;
import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.AmiWebUtils;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.ami.web.graph.AmiCenterGraphNode;
import com.f1.ami.web.graph.AmiCenterGraphNode_Trigger;
import com.f1.base.Action;
import com.f1.base.Row;
import com.f1.base.Table;
import com.f1.container.ResultMessage;
import com.f1.suite.web.fastwebcolumns.FastWebColumns;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.PortletManager;
import com.f1.suite.web.portal.impl.ConfirmDialogPortlet;
import com.f1.suite.web.portal.impl.DividerPortlet;
import com.f1.suite.web.portal.impl.FastTreePortlet;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.TreeStateCopierIdGetter;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletButton;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuFactory;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuListener;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletListener;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.suite.web.portal.style.PortletStyleManager_Dialog;
import com.f1.suite.web.tree.WebTreeContextMenuListener;
import com.f1.suite.web.tree.WebTreeNode;
import com.f1.suite.web.tree.impl.FastWebTree;
import com.f1.suite.web.tree.impl.FastWebTreeColumn;
import com.f1.utils.CH;
import com.f1.utils.OH;
import com.f1.utils.SH;
import com.f1.utils.casters.Caster_Boolean;
import com.f1.utils.casters.Caster_String;
import com.f1.utils.concurrent.IdentityHashSet;
import com.f1.utils.structs.LongKeyMap;
import com.f1.utils.structs.Tuple2;
import com.f1.utils.structs.table.derived.DerivedCellCalculator;

public class AmiCenterManagerTriggerScirptTreePortlet extends GridPortlet implements Comparator<WebTreeNode>, WebTreeContextMenuListener, FormPortletContextMenuFactory,
		FormPortletListener, FormPortletContextMenuListener, AmiWebCompilerListener, TreeStateCopierIdGetter, AmiWebDomObjectDependency {
	public static final String DEFAULT_DS_NAME = "AMI";
	public static final byte DEFAULT_PERMISSION = (byte) 15;
	//Backend config
	public static final int DEFAULT_LIMIT = 10000;
	public static final int DEFAULT_TIMEOUT = 60000;
	private long sessionId = -1;

	private LongKeyMap<List<WebTreeNode>> nodesByGraphId = new LongKeyMap<List<WebTreeNode>>();

	final private DividerPortlet divider;
	final private FastTreePortlet tree;

	//form
	final private FormPortlet form;
	//common fields
	private FormPortletTextField nameField;
	private FormPortletSelectField<Short> typeField;
	private FormPortletTextField onField;
	private FormPortletTextField priorityField;

	final private AmiWebService service;
	//buttons
	final private FormPortletButton testButton;
	final private FormPortletButton resetButton;
	final private FormPortletButton diffButton;
	final private FormPortletButton previewButton;
	private boolean changed;
	private short lastTriggerType = -1;
	private Map<String, String> fieldCache = new HashMap<String, String>();

	//edited fields
	final private IdentityHashSet<FormPortletTextField> editedFields = new IdentityHashSet<FormPortletTextField>();
	final private IdentityHashSet<AmiWebFormPortletAmiScriptField> editedScripts = new IdentityHashSet<AmiWebFormPortletAmiScriptField>();
	final private IdentityHashSet<FormPortletSelectField> editedSelectFields = new IdentityHashSet<FormPortletSelectField>();
	final private IdentityHashSet<FormPortletCheckboxField> editedCheckboxFields = new IdentityHashSet<FormPortletCheckboxField>();

	private WebTreeNode treeNodeTriggers;

	private Map<String, AmiCenterGraphNode_Trigger> triggerBinding;

	public AmiCenterManagerTriggerScirptTreePortlet(PortletConfig config, Map<String, AmiCenterGraphNode_Trigger> triggerBinding) {
		super(config);
		this.service = AmiWebUtils.getService(getManager());
		this.triggerBinding = triggerBinding;
		this.divider = new DividerPortlet(generateConfig(), true);
		this.divider.setOffsetFromTopPx(300);
		this.addChild(divider);

		this.tree = new FastTreePortlet(generateConfig());
		this.tree.getTree().setComparator(this);
		this.tree.getTree().addMenuContextListener(this);
		//add default form and dialog style for this.tree
		this.tree.setFormStyle(AmiWebUtils.getService(getManager()).getUserFormStyleManager());
		this.tree.setDialogStyle(AmiWebUtils.getService(getManager()).getUserDialogStyleManager());
		this.tree.getTree().setRootLevelVisible(false);
		buildTree(triggerBinding);

		//form
		this.form = new FormPortlet(generateConfig());
		this.form.setMenuFactory(this);
		this.form.addMenuListener(this);
		this.form.addFormPortletListener(this);

		//fields
		this.nameField = new FormPortletTextField(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_NAME + AmiCenterEntityConsts.REQUIRED_FIELD_ANNOTATION_HTML);
		this.nameField.setName(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_NAME);
		this.typeField = new FormPortletSelectField(short.class, AmiCenterEntityConsts.OPTION_NAME_TRIGGER_TYPE + AmiCenterEntityConsts.REQUIRED_FIELD_ANNOTATION_HTML);
		this.typeField.setName(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_TYPE);
		this.typeField.setDisabled(true);//not allowing changing types
		initTriggerTypes();
		this.onField = new FormPortletTextField(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_ON + AmiCenterEntityConsts.REQUIRED_FIELD_ANNOTATION_HTML);
		this.onField.setName(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_ON);
		this.priorityField = new FormPortletTextField(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_PRIORITY);
		this.priorityField.setName(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_PRIORITY);

		//buttons
		this.testButton = new FormPortletButton("Test");
		this.resetButton = new FormPortletButton("Reset");
		this.diffButton = new FormPortletButton("Diff");
		this.previewButton = new FormPortletButton("Preview");
		this.testButton.setEnabled(false);
		this.resetButton.setEnabled(false);
		this.diffButton.setEnabled(false);
		this.previewButton.setEnabled(false);

		form.addButton(testButton);
		form.addButton(resetButton);
		form.addButton(diffButton);
		form.addButton(previewButton);

		this.divider.addChild(this.tree);
		this.divider.addChild(this.form);
		this.service.addCompilerListener(this);
		this.service.getDomObjectsManager().addGlobalListener(this);

		sendAuth();
	}

	private void addCommonOptionFields() {
		if (!this.form.hasField(this.typeField))
			form.addField(this.typeField);
		if (!this.form.hasField(this.nameField))
			form.addField(this.nameField);
		if (!this.form.hasField(this.onField))
			form.addField(this.onField);
		if (!this.form.hasField(this.priorityField))
			form.addField(this.priorityField);
	}

	private void initTriggerTypes() {
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_NULL, AmiCenterEntityConsts.ENTITY_TYPE_NULL);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AMISCRIPT, AmiCenterEntityConsts.TRIGGER_TYPE_AMISCRIPT);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AGGREGATE, AmiCenterEntityConsts.TRIGGER_TYPE_AGGREGATE);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_PROJECTION, AmiCenterEntityConsts.TRIGGER_TYPE_PROJECTION);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_JOIN, AmiCenterEntityConsts.TRIGGER_TYPE_JOIN);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_DECORATE, AmiCenterEntityConsts.TRIGGER_TYPE_DECORATE);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_RELAY, AmiCenterEntityConsts.TRIGGER_TYPE_RELAY);
	}

	@Override
	public void onClosed() {
		this.service.removeCompilerListener(this);
		this.service.getDomObjectsManager().removeGlobalListener(this);
		super.onClosed();
	}

	private void insertOptionField(String option, Class<?> fieldType, boolean required) {
		if (required)
			insertOptionField(option, option + AmiCenterEntityConsts.REQUIRED_FIELD_ANNOTATION_HTML, fieldType);
		else
			insertOptionField(option, option, fieldType);
	}

	private void insertOptionField(String option, String formattedOption, Class<?> fieldType) {
		FormPortletField<?> fpf = null;
		if (fieldType == FormPortletCheckboxField.class) {
			fpf = new FormPortletCheckboxField(formattedOption);
		} else if (fieldType == FormPortletTextField.class) {
			fpf = new FormPortletTextField(formattedOption);
		} else if (fieldType == AmiWebFormPortletAmiScriptField.class) {
			fpf = new AmiWebFormPortletAmiScriptField(formattedOption, getManager(), "");
			fpf.setWidthPct(0.60);
			fpf.setHeightPct(0.25);
		} else
			throw new RuntimeException("Unknown fieldType" + fieldType);
		if (fpf != null) {
			this.form.addField(fpf);
			fpf.setName(option);
		}

	}

	private void insertOptionField(String option, List<String> optionValues, boolean required) {
		String formattedOption = null;
		if (required)
			formattedOption = option + AmiCenterEntityConsts.REQUIRED_FIELD_ANNOTATION_HTML;
		else
			formattedOption = option;
		FormPortletSelectField<String> fpf = new FormPortletSelectField<String>(String.class, formattedOption);
		for (String v : optionValues)
			fpf.addOption(v, v);
		if (fpf != null) {
			form.addField(fpf);
			fpf.setName(option);
		}

	}

	private void generateTemplate(short type) {
		switch (type) {
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AMISCRIPT:
				insertOptionField("canMutateRow", FormPortletCheckboxField.class, false);
				insertOptionField("runOnStartup", FormPortletCheckboxField.class, false);
				insertOptionField("onInsertingScript", AmiWebFormPortletAmiScriptField.class, false);
				insertOptionField("onUpdatingScript", AmiWebFormPortletAmiScriptField.class, false);
				insertOptionField("onDeletingScript", AmiWebFormPortletAmiScriptField.class, false);
				insertOptionField("onInsertedScript", AmiWebFormPortletAmiScriptField.class, false);
				insertOptionField("onUpdatedScript", AmiWebFormPortletAmiScriptField.class, false);
				insertOptionField("rowVar", FormPortletTextField.class, false);
				insertOptionField("onStartupScript", AmiWebFormPortletAmiScriptField.class, false);
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AGGREGATE:
				insertOptionField("groupBys", FormPortletTextField.class, true);
				insertOptionField("selects", FormPortletTextField.class, true);
				insertOptionField("allowExternalUpdates", FormPortletCheckboxField.class, false);
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_DECORATE:
				insertOptionField("on", FormPortletTextField.class, true);
				insertOptionField("selects", FormPortletTextField.class, true);
				insertOptionField("keysChange", FormPortletCheckboxField.class, false);
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_JOIN:
				insertOptionField("type", CH.l("INNER", "LEFT", "RIGHT", "OUTER", "LEFT ONLY", "RIGHT ONLY", "OUTER ONLY"), false);
				insertOptionField("on", FormPortletTextField.class, true);
				insertOptionField("selects", FormPortletTextField.class, true);
				insertOptionField("wheres", FormPortletTextField.class, false);
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_PROJECTION:
				insertOptionField("selects", FormPortletTextField.class, true);
				insertOptionField("wheres", FormPortletTextField.class, false);
				insertOptionField("allowExternalUpdates", FormPortletCheckboxField.class, false);
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_RELAY:
				insertOptionField("hosts", FormPortletTextField.class, true);
				insertOptionField("port", FormPortletTextField.class, true);
				insertOptionField("login", FormPortletTextField.class, true);
				insertOptionField("keystoreFile", FormPortletTextField.class, false);
				insertOptionField("keystorePass", FormPortletTextField.class, false);
				insertOptionField("derivedValues", FormPortletTextField.class, false);
				insertOptionField("inserts", FormPortletTextField.class, false);
				insertOptionField("updates", FormPortletTextField.class, false);
				insertOptionField("deletes", FormPortletTextField.class, false);
				insertOptionField("target", FormPortletTextField.class, false);
				insertOptionField("where", FormPortletTextField.class, false);
				break;
			default:
				throw new RuntimeException("Unknow trigger type code: " + type);

		}
	}

	private void buildTree(Map<String, AmiCenterGraphNode_Trigger> triggerBinding) {
		this.tree.clear();
		this.nodesByGraphId.clear();
		this.treeNodeTriggers = createNode(this.tree.getRoot(), "Triggers", AmiWebConsts.CENTER_GRAPH_NODE_TRIGGER, null);
		for (Entry<String, AmiCenterGraphNode_Trigger> e : triggerBinding.entrySet()) {
			String triggerName = e.getKey();
			AmiCenterGraphNode_Trigger trigger = e.getValue();
			createNode(this.treeNodeTriggers, trigger);
		}
	}

	private WebTreeNode createNode(WebTreeNode parent, String title, String icon, Object data) {
		WebTreeNode r = this.tree.createNode(title, parent, false, data);
		r.setIconCssStyle(icon == null ? null : "_bgi=url('" + icon + "')");
		return r;
	}

	public static String getIcon(AmiCenterGraphNode node) {
		switch (node.getType()) {
			case AmiCenterGraphNode.TYPE_TABLE:
				return AmiWebConsts.CENTER_GRAPH_NODE_TABLE;
			case AmiCenterGraphNode.TYPE_TRIGGER:
				return AmiWebConsts.CENTER_GRAPH_NODE_TRIGGER;
			case AmiCenterGraphNode.TYPE_TIMER:
				return AmiWebConsts.CENTER_GRAPH_NODE_TIMER;
			case AmiCenterGraphNode.TYPE_PROCEDURE:
				return AmiWebConsts.CENTER_GRAPH_NODE_PROCEDURE;
			case AmiCenterGraphNode.TYPE_INDEX:
				return AmiWebConsts.CENTER_GRAPH_NODE_INDEX;
			case AmiCenterGraphNode.TYPE_DBO:
				return AmiWebConsts.CENTER_GRAPH_NODE_DBO;
			case AmiCenterGraphNode.TYPE_METHOD:
				return AmiWebConsts.CENTER_GRAPH_NODE_METHOD;
		}
		return null;
	}

	private WebTreeNode createNode(WebTreeNode parent, AmiCenterGraphNode node) {
		String icon = getIcon(node);
		String label = node.getLabel();
		WebTreeNode r = parent.getTreeManager().createNode(label, parent, false, node);
		r.setIconCssStyle(icon == null ? null : "_bgi=url('" + icon + "')");
		LongKeyMap.Node<List<WebTreeNode>> entry = this.nodesByGraphId.getNodeOrCreate(node.getUid());
		if (entry.getValue() == null)
			entry.setValue(new ArrayList<WebTreeNode>());
		entry.getValue().add(r);

		return r;
	}

	@Override
	public void onUserDblclick(FastWebColumns columns, String action, Map<String, String> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onContextMenu(FastWebTree tree, String action) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNodeClicked(FastWebTree tree, WebTreeNode node) {
		if (!this.editedFields.isEmpty() || !this.editedScripts.isEmpty() || !this.editedSelectFields.isEmpty() || !this.editedCheckboxFields.isEmpty()) {
			getManager().showAlert("You are in the middle of an edit, please <B>Test</B> or <B>Reset</B> changes first");
			return;
		}
		if (node == null || node == this.treeNodeTriggers)
			return;
		//enable preview button
		this.previewButton.setEnabled(true);
		AmiCenterGraphNode_Trigger target = (AmiCenterGraphNode_Trigger) node.getData();
		//query the backend to init the editor
		prepareRequestToBackend("SHOW FULL TRIGGERS WHERE TriggerName==\"" + target.getLabel() + "\";");
	}

	@Override
	public void onCellMousedown(FastWebTree tree, WebTreeNode start, FastWebTreeColumn col) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNodeSelectionChanged(FastWebTree fastWebTree, WebTreeNode node) {
	}

	@Override
	public Object getId(WebTreeNode node) {
		return node.getName();
	}

	@Override
	public int compare(WebTreeNode o1, WebTreeNode o2) {
		// TODO Auto-generated method stub
		return 0;
	}

	private AmiCenterQueryDsRequest prepareRequest() {
		AmiCenterQueryDsRequest request = getManager().getTools().nw(AmiCenterQueryDsRequest.class);

		int timeout = 60;
		try {
			timeout = (int) (timeout * 1000);
		} catch (Exception e) {
			getManager().showAlert("Timeout is not in a valid format");
			return null;
		}

		request.setTimeoutMs(timeout);
		request.setQuerySessionKeepAlive(true);
		request.setInvokedBy(service.getUserName());
		request.setSessionVariableTypes(null);
		request.setSessionVariables(null);
		request.setAllowSqlInjection(false);//String template, dflt to false
		request.setPermissions(DEFAULT_PERMISSION);
		request.setType(AmiCenterQueryDsRequest.TYPE_QUERY);
		request.setOriginType(AmiCenterQueryDsRequest.ORIGIN_FRONTEND_SHELL);
		request.setDatasourceName(DEFAULT_DS_NAME);
		request.setLimit(-1);
		return request;
	}

	@Override
	public void onBackendResponse(ResultMessage<Action> result) {
		if (result.getError() != null) {
			getManager().showAlert("Internal Error:" + result.getError().getMessage(), result.getError());
			return;
		}
		AmiCenterQueryDsResponse response = (AmiCenterQueryDsResponse) result.getAction();
		if (sessionId == -1) {
			this.sessionId = response.getQuerySessionId();
		}
		if (response.getOk() && response.getTables() != null && response.getTables().size() == 1) {
			List<Table> tables = response.getTables();
			Table t = tables.get(0);
			Row r = t.getRow(0);
			String triggerName = (String) r.get("TriggerName");
			String triggerOn = (String) r.get("TableName");
			String triggerType = (String) r.get("TriggerType");
			String triggerPriority = (String) r.get("Priority");
			String triggerOptions = (String) r.get("Options");
			//parse trigger options
			Map<String, String> m = AmiCenterManagerUtils.parseUseOptions(triggerOptions);
			HashMap<String, String> colToKey = new HashMap<String, String>();
			colToKey.put(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_NAME, "TriggerName");
			colToKey.put(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_ON, "TableName");
			colToKey.put(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_TYPE, "TriggerType");
			colToKey.put(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_PRIORITY, "Priority");
			short type = AmiCenterManagerUtils.centerObjectTypeToCode(AmiCenterGraphNode.TYPE_TRIGGER, triggerType);

			//build fields in forms
			//only reset the form if trigger type has changed
			if (this.lastTriggerType != type) {
				this.form.clearFields();
				addCommonOptionFields();//add if not exists
				generateTemplate(type);
			}
			this.lastTriggerType = type;
			//set field value
			//1. set common value
			for (String name : AmiCenterEntityConsts.TRIGGER_CONFIG_OPTIONS) {
				FormPortletField f = form.getFieldByName(name);
				fieldCache.put(name, (String) r.get(colToKey.get(name)));
				if (f instanceof FormPortletTextField)
					f.setValue(r.get(colToKey.get(name)));
				else if (f instanceof FormPortletSelectField)
					((FormPortletSelectField) f)
							.setValue(AmiCenterManagerUtils.centerObjectTypeToCode(AmiCenterGraphNode.TYPE_TRIGGER, OH.cast(r.get(colToKey.get(name)), String.class)));
			}
			//2. set option value
			for (Entry<String, String> e : m.entrySet()) {
				String key = e.getKey();
				String value = e.getValue();
				FormPortletField f = form.getFieldByName(key);
				f.setValue(value);
				fieldCache.put(key, value);
			}
		}
	}

	private void prepareRequestToBackend(String query) {
		AmiCenterQueryDsRequest request = prepareRequest();
		request.setQuery(query);
		request.setQuerySessionId(this.sessionId);
		service.sendRequestToBackend(this, request);
	}

	private void sendAuth() {
		AmiCenterQueryDsRequest request = prepareRequest();
		if (request == null)
			return;
		service.sendRequestToBackend(this, request);
	}

	@Override
	public void initLinkedVariables() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDomObjectAriChanged(AmiWebDomObject target, String oldAri) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDomObjectEvent(AmiWebDomObject object, byte eventType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDomObjectRemoved(AmiWebDomObject object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDomObjectAdded(AmiWebDomObject object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFormulaChanged(AmiWebFormula formula, DerivedCellCalculator old, DerivedCellCalculator nuw) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCallbackChanged(AmiWebAmiScriptCallback callback, DerivedCellCalculator old, DerivedCellCalculator nuw) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRecompiled() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onContextMenu(FormPortlet portlet, String action, FormPortletField node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {
		if (this.resetButton == button) {
			IdentityHashSet<FormPortletField> allEditedFields = new IdentityHashSet<FormPortletField>();
			for (FormPortletField ff : this.editedFields)
				allEditedFields.add(ff);
			for (FormPortletField ff : this.editedScripts)
				allEditedFields.add(ff);
			for (FormPortletField ff : this.editedSelectFields)
				allEditedFields.add(ff);
			for (FormPortletField ff : this.editedCheckboxFields)
				allEditedFields.add(ff);
			//loop over the edited fields and revert each of them
			for (FormPortletField f : allEditedFields) {
				String orig = this.fieldCache.get(f.getName());
				if (orig == null) {//the field is not configured prior to edit
					if (f instanceof FormPortletCheckboxField)
						f.setValue(false);
					else
						f.setValue("");//revert to empty string
					onFieldChanged(f);
				}
				if (f instanceof FormPortletSelectField) {
					FormPortletSelectField sf = (FormPortletSelectField) f;
					if (AmiCenterEntityConsts.OPTION_NAME_TRIGGER_TYPE.equals(sf.getName())) {
						short origTypeCode = AmiCenterManagerUtils.centerObjectTypeToCode(AmiCenterGraphNode.TYPE_TRIGGER, orig);
						sf.setValue(origTypeCode);
						onFieldChanged(f);
					} else if ("type".equals(sf.getName())) {
						sf.setValue(orig);
						onFieldChanged(f);
					}
				} else if (f instanceof FormPortletTextField) {
					FormPortletTextField tf = (FormPortletTextField) f;
					tf.setValue(orig);
					onFieldChanged(f);
				} else if (f instanceof AmiWebFormPortletAmiScriptField) {
					AmiWebFormPortletAmiScriptField wf = (AmiWebFormPortletAmiScriptField) f;
					wf.setValue(orig);
					onFieldChanged(f);
				} else if (f instanceof FormPortletCheckboxField) {
					FormPortletCheckboxField cf = (FormPortletCheckboxField) f;
					if (orig == null)
						orig = "false";
					cf.setValue(Caster_Boolean.INSTANCE.cast(orig));
					onFieldChanged(f);
				}
			}
		} else if (this.testButton == button) {
			String query = null;
			//if only the name has changed
			if (this.editedFields.size() == 1 && this.editedFields.contains(this.nameField) && this.editedScripts.isEmpty() && this.editedSelectFields.isEmpty()
					&& this.editedSelectFields.isEmpty()) {
				query = "RENAME TRIGGER " + this.fieldCache.get(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_NAME) + " TO " + this.nameField.getValue();
				getManager().showDialog("Submit Trigger", new AmiCenterManagerSubmitEditScriptPortlet(this.service, generateConfig(), query),
						AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_WIDTH, AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_HEIGHT);
			} else {
				query = "DROP TRIGGER " + this.fieldCache.get(AmiCenterEntityConsts.OPTION_NAME_TRIGGER_NAME) + ";" + previewScript();
				getManager().showDialog("Submit Trigger", new AmiCenterManagerSubmitEditScriptPortlet(this.service, generateConfig(), query),
						AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_WIDTH, AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_HEIGHT);
			}
		} else if (this.previewButton == button) {
			String text = AmiCenterManagerUtils.formatPreviewScript(previewScript());
			PortletStyleManager_Dialog dp = service.getPortletManager().getStyleManager().getDialogStyle();
			final PortletManager portletManager = service.getPortletManager();
			ConfirmDialogPortlet cdp = new ConfirmDialogPortlet(portletManager.generateConfig(), text, ConfirmDialogPortlet.TYPE_MESSAGE);
			int w = dp.getDialogWidth();
			int h = dp.getDialogHeight();
			portletManager.showDialog("Trigger Script", cdp, w + 200, h);
		} else if (this.diffButton == button) {
			Tuple2<Map<String, Object>, Map<String, Object>> diffs = getJsonDiff();
			LinkedHashMap a = new LinkedHashMap<String, Map>();
			a.put("Configuration", diffs.getA());
			LinkedHashMap b = new LinkedHashMap<String, Map>();
			b.put("Configuration", diffs.getB());
			String oldConfig = AmiWebLayoutHelper.toJson(a, service);
			String newConfig = AmiWebLayoutHelper.toJson(b, service);
			AmiWebUtils.diffConfigurations(service, oldConfig, newConfig, "Orginal Script", "New Script", null);
		}

	}

	private Tuple2<Map<String, Object>, Map<String, Object>> getJsonDiff() {
		LinkedHashMap<String, Object> newConfig = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> oldConfig = new LinkedHashMap<String, Object>();
		IdentityHashSet<FormPortletField> allEditedFields = new IdentityHashSet<FormPortletField>();
		for (FormPortletField ff : this.editedFields)
			allEditedFields.add(ff);
		for (FormPortletField ff : this.editedScripts)
			allEditedFields.add(ff);
		for (FormPortletField ff : this.editedSelectFields)
			allEditedFields.add(ff);
		for (FormPortletField ff : this.editedCheckboxFields)
			allEditedFields.add(ff);
		for (FormPortletField f : allEditedFields) {
			String orig = this.fieldCache.get(f.getName());
			if (orig == null) {//the field is not configured prior to edit
				newConfig.put(f.getName(), f.getValue());
			} else {
				newConfig.put(f.getName(), f.getValue());
				oldConfig.put(f.getName(), orig);
			}
		}
		return new Tuple2<Map<String, Object>, Map<String, Object>>(oldConfig, newConfig);
	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		onFieldChanged(field);
	}

	private void onFieldChanged(FormPortletField field) {
		boolean hadNoChanges = this.editedFields.isEmpty() && this.editedScripts.isEmpty() && this.editedSelectFields.isEmpty() && this.editedCheckboxFields.isEmpty();
		String value = null;
		Object rawValue = field.getValue();
		if (rawValue instanceof String)
			value = SH.trim((String) rawValue);
		else if (rawValue instanceof Short && AmiCenterEntityConsts.OPTION_NAME_TRIGGER_TYPE.equals(field.getName())) {
			short typeCode = (short) rawValue;
			FormPortletSelectField<Short> sf = (FormPortletSelectField) field;
			value = sf.getOption(typeCode).getName();
		} else if (rawValue instanceof Boolean) {
			value = Caster_String.INSTANCE.cast(rawValue);
		}

		String orig = fieldCache.get(field.getName());
		if (field instanceof FormPortletTextField) {
			FormPortletTextField tf = (FormPortletTextField) field;
			if (OH.eq(value, orig) || (SH.isnt(field.getValue()) && orig == null)) {
				this.editedFields.remove(tf);
				formatFieldTitle(tf, false);
			} else {
				this.editedFields.add(tf);
				formatFieldTitle(tf, true);
			}
		} else if (field instanceof AmiWebFormPortletAmiScriptField) {
			AmiWebFormPortletAmiScriptField af = (AmiWebFormPortletAmiScriptField) field;
			if (OH.eq(value, orig) || (SH.isnt(field.getValue()) && orig == null)) {
				this.editedScripts.remove(af);
				formatFieldTitle(af, false);
			} else {
				this.editedScripts.add(af);
				formatFieldTitle(af, true);
			}
		} else if (field instanceof FormPortletSelectField) {
			FormPortletSelectField psf = (FormPortletSelectField) field;
			if (OH.eq(value, orig) || (SH.isnt(field.getValue()) && orig == null)) {
				this.editedSelectFields.remove(psf);
				formatFieldTitle(psf, false);
			} else {
				this.editedSelectFields.add(psf);
				formatFieldTitle(psf, true);
			}
		} else if (field instanceof FormPortletCheckboxField) {
			FormPortletCheckboxField cf = (FormPortletCheckboxField) field;
			if (orig == null)
				orig = "false";
			if (OH.eq(value, orig) || (SH.isnt(field.getValue()) && orig == null)) {
				this.editedCheckboxFields.remove(cf);
				formatFieldTitle(cf, false);
			} else {
				this.editedCheckboxFields.add(cf);
				formatFieldTitle(cf, true);
			}
		}
		boolean hasNoChanges = this.editedFields.isEmpty() && this.editedScripts.isEmpty() && this.editedSelectFields.isEmpty() && this.editedCheckboxFields.isEmpty();
		if (hasNoChanges != hadNoChanges) {
			this.testButton.setEnabled(!hasNoChanges);
			this.resetButton.setEnabled(!hasNoChanges);
			this.diffButton.setEnabled(!hasNoChanges);
		}
	}

	private static void formatFieldTitle(FormPortletField<?> field, boolean hasChanged) {
		if (hasChanged)
			field.setTitle(AmiCenterEntityConsts.CHANGED_FIELD_ANNOTATION_HTML + field.getTitle());
		else
			field.setTitle(field.getTitle().replace(AmiCenterEntityConsts.CHANGED_FIELD_ANNOTATION_HTML, ""));

	}

	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	public String previewScript() {
		StringBuilder sb = new StringBuilder("CREATE TRIGGER ");
		sb.append(parseValueForRequiredField(nameField));
		sb.append(" OFTYPE ").append(SH.noNull(OH.cast(this.typeField, FormPortletSelectField.class).getOption(this.typeField.getValue()).getName()));
		sb.append(" ON ").append(parseValueForRequiredField(this.onField));
		if (SH.is(this.priorityField.getValue()))
			sb.append(" PRIORITY ").append(this.priorityField.getValue());
		sb.append(" USE ").append(prepareUseClause());
		sb.append(";");
		return sb.toString();
	}

	//only non-empty options will be appended to the use clause
	private String prepareUseClause() {
		StringBuilder script = new StringBuilder();
		List<FormPortletField<?>> configFields = getConfigFields();
		//contains nonEmpty fields + required fields
		List<FormPortletField<?>> nonEmptyAndRequiredFields = new ArrayList<FormPortletField<?>>();
		for (FormPortletField<?> fpf : configFields) {
			if (fpf instanceof FormPortletCheckboxField) {
				FormPortletCheckboxField cf = (FormPortletCheckboxField) fpf;
				if (cf.getValue() != null) //null is false
					nonEmptyAndRequiredFields.add(fpf);
			} else if (SH.is(fpf.getValue()) || isRequiredField(fpf))
				nonEmptyAndRequiredFields.add(fpf);
		}
		for (FormPortletField<?> f : nonEmptyAndRequiredFields) {
			if (f instanceof AmiWebFormPortletAmiScriptField)
				script.append(f.getName()).append("=").append(SH.doubleQuote((String) f.getValue())).append("").append(" ");
			else if (f instanceof FormPortletCheckboxField) {
				FormPortletCheckboxField cf = (FormPortletCheckboxField) f;
				script.append(f.getName()).append("=").append(SH.doubleQuote(Caster_String.INSTANCE.cast(cf.getBooleanValue()))).append(" ");

			} else if (isRequiredField(f) && !SH.is(f.getValue())) //an empty required field
				script.append(f.getName()).append("=").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
			else {//non-empty field
				script.append(f.getName()).append("=\"").append(f.getValue()).append("\"").append(" ");
			}
		}

		return script.toString();
	}

	//config fields are fields after the keyword "USE"
	public List<FormPortletField<?>> getConfigFields() {
		//all the config fields are after the "PRIORITY" field
		int priorityFieldlocation = this.form.getFieldLocation(this.priorityField);
		List<FormPortletField<?>> configFields = new ArrayList<FormPortletField<?>>();
		for (int i = priorityFieldlocation + 1; i < this.form.getFieldsCount(); i++) {
			configFields.add(this.form.getFieldAt(i));
		}
		return configFields;
	}

	private static boolean isRequiredField(FormPortletField<?> f) {
		return f.getTitle().contains("*");
	}

	private static String parseValueForRequiredField(FormPortletField<?> f) {
		if (SH.isnt(f.getValue()))
			return AmiCenterEntityConsts.REQUIRED_FEILD_WARNING;
		return Caster_String.INSTANCE.cast(f.getValue());

	}

}
