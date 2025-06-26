package com.f1.ami.web.centermanager.nuweditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.f1.ami.amicommon.msg.AmiCenterQueryDsRequest;
import com.f1.ami.amicommon.msg.AmiCenterQueryDsResponse;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.ami.web.centermanager.editor.AmiCenterManagerSubmitEditScriptPortlet;
import com.f1.ami.web.centermanager.nuweditor.triggerEditors.AmiCenterManagerAbstractTriggerEditor;
import com.f1.ami.web.centermanager.nuweditor.triggerEditors.AmiCenterManagerTriggerEditor_AggregateTrigger;
import com.f1.ami.web.centermanager.nuweditor.triggerEditors.AmiCenterManagerTriggerEditor_Amiscirpt;
import com.f1.ami.web.centermanager.nuweditor.triggerEditors.AmiCenterManagerTriggerEditor_Decorate;
import com.f1.ami.web.centermanager.nuweditor.triggerEditors.AmiCenterManagerTriggerEditor_JoinTrigger;
import com.f1.ami.web.centermanager.nuweditor.triggerEditors.AmiCenterManagerTriggerEditor_ProjectionTrigger;
import com.f1.ami.web.centermanager.nuweditor.triggerEditors.AmiCenterManagerTriggerEditor_Relay;
import com.f1.ami.web.graph.AmiCenterGraphNode;
import com.f1.base.Action;
import com.f1.base.Row;
import com.f1.base.Table;
import com.f1.container.ResultMessage;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.HtmlPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletMultiCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.CH;
import com.f1.utils.SH;
import com.f1.utils.string.sqlnode.AdminNode;

public class AmiCenterManagerEditTriggerPortlet extends AmiCenterManagerAbstractEditCenterObjectPortlet {
	//height const
	private static final int OPTION_FORM_HEIGHT = 90;//common option form height

	//option fields
	final private FormPortlet form;

	final private GridPortlet formAndTriggerConfigGrid;

	final private FormPortletTextField triggerNameField;
	final private FormPortletSelectField<Short> triggerTypeField;

	final private FormPortletMultiCheckboxField<String> triggerOnField;
	final private FormPortletTextField triggerPriorityField;
	private AmiCenterManagerAbstractTriggerEditor curEditor;

	//trigger editors
	final private AmiCenterManagerTriggerEditor_Amiscirpt amiscriptEditor;
	final private AmiCenterManagerTriggerEditor_AggregateTrigger aggEditor;
	final private AmiCenterManagerTriggerEditor_ProjectionTrigger projectionEditor;
	final private AmiCenterManagerTriggerEditor_JoinTrigger joinEditor;
	final private AmiCenterManagerTriggerEditor_Decorate decorateEditor;
	final private AmiCenterManagerTriggerEditor_Relay relayEditor;

	//trigger-type-specific editor
	private InnerPortlet editorPanel;//all the type-specific fields excluding amiscript fields

	public AmiCenterManagerEditTriggerPortlet(PortletConfig config, boolean isAdd) {
		super(config, isAdd);
		this.form = new FormPortlet(generateConfig());
		amiscriptEditor = new AmiCenterManagerTriggerEditor_Amiscirpt(generateConfig());
		this.getManager().onPortletAdded(amiscriptEditor);
		aggEditor = new AmiCenterManagerTriggerEditor_AggregateTrigger(generateConfig());
		this.getManager().onPortletAdded(aggEditor);
		projectionEditor = new AmiCenterManagerTriggerEditor_ProjectionTrigger(generateConfig());
		this.getManager().onPortletAdded(projectionEditor);
		joinEditor = new AmiCenterManagerTriggerEditor_JoinTrigger(generateConfig());
		this.getManager().onPortletAdded(joinEditor);
		decorateEditor = new AmiCenterManagerTriggerEditor_Decorate(generateConfig());
		this.getManager().onPortletAdded(decorateEditor);
		relayEditor = new AmiCenterManagerTriggerEditor_Relay(generateConfig());
		this.getManager().onPortletAdded(relayEditor);

		formAndTriggerConfigGrid = new GridPortlet(generateConfig());
		formAndTriggerConfigGrid.addChild(form, 0, 0);
		formAndTriggerConfigGrid.setRowSize(0, OPTION_FORM_HEIGHT);
		this.editorPanel = formAndTriggerConfigGrid.addChild(new HtmlPortlet(generateConfig()).setCssStyle("_bg=#e2e2e2"), 0, 1, 1, 1);

		triggerNameField = this.form.addField(new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("Name")));
		triggerNameField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		triggerNameField.setWidth(NAME_WIDTH).setHeightPx(DEFAULT_ROWHEIGHT).setLeftPosPx(DEFAULT_LEFTPOS).setTopPosPx(DEFAULT_TOPPOS);
		triggerNameField.setHelp("Name of the trigger to create, must be unique within the database");

		//trigger type
		triggerTypeField = this.form.addField(new FormPortletSelectField(short.class, AmiCenterManagerUtils.formatRequiredField("Type")));
		initTriggerTypes();
		triggerTypeField.setWidth(TYPE_WIDTH).setHeightPx(DEFAULT_ROWHEIGHT).setLeftPosPx(DEFAULT_LEFTPOS + NAME_WIDTH + DEFAULT_X_SPACING).setTopPosPx(DEFAULT_TOPPOS);

		triggerOnField = this.form.addField(new FormPortletMultiCheckboxField(String.class, AmiCenterManagerUtils.formatRequiredField("ON")));
		triggerOnField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		triggerOnField.setHelp("Name of the table(s) that will cause the trigger to execute");
		//		triggerOnField.setWidth(ON_WIDTH).setHeightPx(DEFAULT_ROWHEIGHT).setLeftPosPx(DEFAULT_LEFTPOS + NAME_WIDTH + TYPE_WIDTH + DEFAULT_X_SPACING * 2)
		//				.setTopPosPx(DEFAULT_TOPPOS);
		triggerOnField.setWidth(ON_WIDTH).setHeightPx(DEFAULT_ROWHEIGHT).setLeftPosPx(DEFAULT_LEFTPOS).setTopPosPx(DEFAULT_TOPPOS + DEFAULT_ROWHEIGHT * 2);
		triggerOnField.setBgColor("#ffffff");
		triggerOnField.setBorderColor("00FFFFFF");

		triggerPriorityField = this.form.addField(new FormPortletTextField("PRIORITY"));
		triggerPriorityField.setHelp("a number, timers with lowest value are executed first. Only considered when two or more timers have the same exact scheduled time");
		triggerPriorityField.setWidth(PRIORITY_WIDTH).setHeightPx(DEFAULT_ROWHEIGHT).setLeftPosPx(DEFAULT_LEFTPOS + NAME_WIDTH + TYPE_WIDTH + (int) (DEFAULT_X_SPACING * 3.5))
				.setTopPosPx(DEFAULT_TOPPOS);

		//by default
		editorPanel.setPortlet(amiscriptEditor);
		curEditor = amiscriptEditor;

		this.addChild(formAndTriggerConfigGrid, 0, 0);
		this.addChild(buttonsFp, 0, 1);

		setRowSize(1, buttonsFp.getButtonPanelHeight());
		this.form.addFormPortletListener(this);
		sendQueryToBackend("SELECT TableName FROM SHOW TABLES;");
	}

	private void initTriggerTypes() {
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AMISCRIPT, AmiCenterEntityConsts.TRIGGER_TYPE_AMISCRIPT);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AGGREGATE, AmiCenterEntityConsts.TRIGGER_TYPE_AGGREGATE);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_PROJECTION, AmiCenterEntityConsts.TRIGGER_TYPE_PROJECTION);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_JOIN, AmiCenterEntityConsts.TRIGGER_TYPE_JOIN);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_DECORATE, AmiCenterEntityConsts.TRIGGER_TYPE_DECORATE);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_RELAY, AmiCenterEntityConsts.TRIGGER_TYPE_RELAY);
	}

	private void updateTriggerTemplate(short type) {
		switch (type) {
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AMISCRIPT:
				editorPanel.setPortlet(amiscriptEditor);
				curEditor = amiscriptEditor;
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AGGREGATE:
				editorPanel.setPortlet(aggEditor);
				curEditor = aggEditor;
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_DECORATE:
				editorPanel.setPortlet(decorateEditor);
				curEditor = decorateEditor;
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_JOIN:
				editorPanel.setPortlet(joinEditor);
				curEditor = joinEditor;
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_PROJECTION:
				editorPanel.setPortlet(projectionEditor);
				curEditor = projectionEditor;
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_RELAY:
				editorPanel.setPortlet(relayEditor);
				curEditor = relayEditor;
				break;
			default:
				throw new RuntimeException("Unknow trigger type code: " + type);

		}
	}

	@Override
	public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onContextMenu(FormPortlet portlet, String action, FormPortletField node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		if (field == this.triggerTypeField) {
			short type = this.triggerTypeField.getValue();
			updateTriggerTemplate(type);
		} else if (field == this.triggerOnField) {
			LinkedHashSet<String> onNames = ((FormPortletMultiCheckboxField) field).getValue();
			String[] namesArr = onNames.toArray(new String[0]);
			switch (this.triggerTypeField.getValue()) {
				case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_JOIN:
					if (namesArr.length == 3) {
						String leftTable = namesArr[0];
						String rightTable = namesArr[1];
						String resultTable = namesArr[2];
						joinEditor.setLeftTable(leftTable);
						joinEditor.setRightTable(rightTable);
						joinEditor.setResultTable(resultTable);
					} else
						joinEditor.resetDependency();
					break;
				case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AGGREGATE:
					if (namesArr.length == 2) {
						String sourceTable = namesArr[0];
						String targetTable = namesArr[1];
						aggEditor.setSourceTable(sourceTable);
						aggEditor.setTargetTable(targetTable);
					} else
						aggEditor.resetDependency();
				case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_PROJECTION:
					if (namesArr.length >= 2) {
						List<String> sourceTables = new ArrayList<>(Arrays.asList(namesArr).subList(0, namesArr.length - 1));
						String targetTable = (String) namesArr[namesArr.length - 1];
						projectionEditor.setSourceTable(sourceTables);
						projectionEditor.setTargetTable(targetTable);
					} else
						projectionEditor.resetDependency();
			}
		}

	}

	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public String prepareUseClause() {
		return curEditor.getKeyValuePairs();
	}

	@Override
	public String preparePreUseClause() {
		StringBuilder sb = new StringBuilder("CREATE TRIGGER ");
		if (SH.is(triggerNameField.getValue()))
			sb.append(triggerNameField.getValue());
		else
			sb.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
		sb.append(" OFTYPE ").append(triggerTypeField.getOption(triggerTypeField.getValue()).getName());

		if (SH.is(triggerOnField.getValue()))
			sb.append(" ON ").append(triggerOnField.getValue());
		else
			sb.append(" ON ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (SH.is(triggerPriorityField.getValue()))
			sb.append(" PRIORITY ").append(triggerPriorityField.getValue());
		return sb.toString();
	}

	@Override
	public String exportToText() {
		return previewScript();
	}

	@Override
	public void importFromText(String text, StringBuilder sink) {
		AdminNode an = AmiCenterManagerUtils.scriptToAdminNode(text);
		Map<String, String> triggerConfig = AmiCenterManagerUtils.parseAdminNode_Trigger(an);
		for (Entry<String, String> e : triggerConfig.entrySet()) {
			String key = e.getKey();
			String value = e.getValue();
			if ("triggerName".equals(key))
				this.triggerNameField.setValue(value);
			else if ("triggerType".equals(key)) {
				this.triggerTypeField.setValue(AmiCenterManagerUtils.centerObjectTypeToCode(AmiCenterGraphNode.TYPE_TRIGGER, value));
				this.onFieldValueChanged(this.form, this.triggerTypeField, null);
			} else if ("triggerOn".equals(key)) {
				this.triggerOnField.setValue(CH.s(value));
			} else if ("triggerPriority".equals(key))
				this.triggerPriorityField.setValue(value);
			else {//TODO:all the use options go here

			}
		}
		return;
	}

	//The abilities to query the backend
	@Override
	public void onBackendResponse(ResultMessage<Action> result) {
		if (result.getError() != null) {
			getManager().showAlert("Internal Error:" + result.getError().getMessage(), result.getError());
			return;
		}
		AmiCenterQueryDsResponse response = (AmiCenterQueryDsResponse) result.getAction();
		if (response.getOk() && response.getTables().size() == 1) {
			Table t = response.getTables().get(0);
			//populate table names into the ON field options
			for (Row r : t.getRows()) {
				String name = (String) r.get("TableName");
				this.triggerOnField.addOption(name, name);
			}
		}
	}

	public AmiCenterQueryDsRequest prepareRequest() {
		AmiCenterQueryDsRequest request = getManager().getTools().nw(AmiCenterQueryDsRequest.class);

		request.setLimit(AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_LIMIT);
		request.setTimeoutMs(AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_TIMEOUT);
		request.setQuerySessionKeepAlive(true);
		request.setIsTest(false);
		request.setAllowSqlInjection(AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_ALLOW_SQL_INJECTION);
		request.setInvokedBy(service.getUserName());
		request.setSessionVariableTypes(null);
		request.setSessionVariables(null);
		request.setPermissions(AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PERMISSION);
		request.setType(AmiCenterQueryDsRequest.TYPE_QUERY);
		request.setOriginType(AmiCenterQueryDsRequest.ORIGIN_FRONTEND_SHELL);
		request.setDatasourceName(AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_DS_NAME);
		return request;
	}

	protected void sendQueryToBackend(String query) {
		if (SH.isnt(query))
			return;
		AmiCenterQueryDsRequest request = prepareRequest();
		if (request == null)
			return;
		request.setQuery(query);
		request.setQuerySessionId(this.sessionId);
		service.sendRequestToBackend(this, request);
	}

}
