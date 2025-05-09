package com.f1.ami.web.centermanager.nuweditor;

import java.util.Map;

import com.f1.ami.web.centermanger.AmiCenterEntityConsts;
import com.f1.ami.web.centermanger.AmiCenterManagerUtils;
import com.f1.ami.web.centermanger.editor.AmiCenterManagerAddTriggerPortlet.AmiCenterManagerOptionField;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;

public class AmiCenterManagerEditTriggerPortlet extends AmiCenterManagerAbstractEditCenterObjectPortlet {
	private static final int DEFAULT_ROWHEIGHT = 25;
	private static final int DEFAULT_LEFTPOS = 75; //164
	private static final int DEFAULT_Y_SPACING = 10;
	private static final int DEFAULT_X_SPACING = 65;
	private static final int DEFAULT_TOPPOS = DEFAULT_Y_SPACING;

	//Width consts
	private static final int NAME_WIDTH = 110;
	private static final int ON_WIDTH = 110;
	private static final int TYPE_WIDTH = 100;
	private static final int PRIORITY_WIDTH = 70;
	//height const
	private static final int OPTION_FORM_HEIGHT = 120;
	private static final int AMISCRIPT_FORM_HEIGHT = 600;

	//padding
	private static final int AMISCRIPT_FORM_PADDING = 0;
	//option fields
	final private FormPortlet form;
	final private FormPortlet configForm;
	private FormPortletTextField triggerNameField;
	private FormPortletSelectField<Short> triggerTypeField;

	private FormPortletTextField triggerOnField;
	private FormPortletTextField triggerPriorityField;
	private AmiCenterManagerOptionField configTitleField;

	public AmiCenterManagerEditTriggerPortlet(PortletConfig config, boolean isAdd) {
		super(config, isAdd);
		this.form = new FormPortlet(generateConfig());
		this.configForm = new FormPortlet(generateConfig());
		triggerNameField = this.form.addField(new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("Name")));
		triggerNameField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		triggerNameField.setWidth(NAME_WIDTH).setHeightPx(DEFAULT_ROWHEIGHT).setLeftPosPx(DEFAULT_LEFTPOS).setTopPosPx(DEFAULT_TOPPOS);
		triggerNameField.setHelp("Name of the trigger to create, must be unique within the database");

		//trigger type
		triggerTypeField = this.form.addField(new FormPortletSelectField(short.class, AmiCenterManagerUtils.formatRequiredField("Type")));
		initTriggerTypes();
		triggerTypeField.setWidth(TYPE_WIDTH).setHeightPx(DEFAULT_ROWHEIGHT).setLeftPosPx(DEFAULT_LEFTPOS + NAME_WIDTH + DEFAULT_X_SPACING).setTopPosPx(DEFAULT_TOPPOS);

		triggerOnField = this.form.addField(new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("ON")));
		triggerOnField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		triggerOnField.setHelp("Name of the table(s) that will cause the trigger to execute");
		triggerOnField.setWidth(ON_WIDTH).setHeightPx(DEFAULT_ROWHEIGHT).setLeftPosPx(DEFAULT_LEFTPOS + NAME_WIDTH + TYPE_WIDTH + DEFAULT_X_SPACING * 2)
				.setTopPosPx(DEFAULT_TOPPOS);

		triggerPriorityField = this.form.addField(new FormPortletTextField("PRIORITY"));
		triggerPriorityField.setHelp("a number, timers with lowest value are executed first. Only considered when two or more timers have the same exact scheduled time");
		triggerPriorityField.setWidth(PRIORITY_WIDTH).setHeightPx(DEFAULT_ROWHEIGHT)
				.setLeftPosPx(DEFAULT_LEFTPOS + NAME_WIDTH + TYPE_WIDTH + ON_WIDTH + (int) (DEFAULT_X_SPACING * 3.5)).setTopPosPx(DEFAULT_TOPPOS);

		//starting type-specific fields

		GridPortlet grid = new GridPortlet(generateConfig());
		grid.addChild(form, 0, 0);
		grid.addChild(configForm, 0, 1);

		this.addChild(grid, 0, 0);
		this.addChild(buttonsFp, 0, 1);

		setRowSize(1, buttonsFp.getButtonPanelHeight());
	}

	private void initTriggerTypes() {
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AMISCRIPT, AmiCenterEntityConsts.TRIGGER_TYPE_AMISCRIPT);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AGGREGATE, AmiCenterEntityConsts.TRIGGER_TYPE_AGGREGATE);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_PROJECTION, AmiCenterEntityConsts.TRIGGER_TYPE_PROJECTION);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_JOIN, AmiCenterEntityConsts.TRIGGER_TYPE_JOIN);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_DECORATE, AmiCenterEntityConsts.TRIGGER_TYPE_DECORATE);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_RELAY, AmiCenterEntityConsts.TRIGGER_TYPE_RELAY);
	}

	private void generateTemplate(short type) {
		switch (type) {
			//			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AMISCRIPT:
			//				insertOptionField("canMutateRow", FormPortletCheckboxField.class, false);
			//				insertOptionField("runOnStartup", FormPortletCheckboxField.class, false);
			//				insertOptionField("onInsertingScript", AmiWebFormPortletAmiScriptField.class, false);
			//				insertOptionField("onUpdatingScript", AmiWebFormPortletAmiScriptField.class, false);
			//				insertOptionField("onDeletingScript", AmiWebFormPortletAmiScriptField.class, false);
			//				insertOptionField("onInsertedScript", AmiWebFormPortletAmiScriptField.class, false);
			//				insertOptionField("onUpdatedScript", AmiWebFormPortletAmiScriptField.class, false);
			//				insertOptionField("rowVar", FormPortletTextField.class, false);
			//				insertOptionField("onStartupScript", AmiWebFormPortletAmiScriptField.class, false);
			//				break;
			//			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AGGREGATE:
			//				insertOptionField("groupBys", FormPortletTextField.class, true);
			//				insertOptionField("selects", FormPortletTextField.class, true);
			//				insertOptionField("allowExternalUpdates", FormPortletCheckboxField.class, false);
			//				break;
			//			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_DECORATE:
			//				insertOptionField("on", FormPortletTextField.class, true);
			//				insertOptionField("selects", FormPortletTextField.class, true);
			//				insertOptionField("keysChange", FormPortletCheckboxField.class, false);
			//				break;
			//			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_JOIN:
			//				insertOptionField("type", CH.l("INNER", "LEFT", "RIGHT", "OUTER", "LEFT ONLY", "RIGHT ONLY", "OUTER ONLY"), false);
			//				insertOptionField("on", FormPortletTextField.class, true);
			//				insertOptionField("selects", FormPortletTextField.class, true);
			//				insertOptionField("wheres", FormPortletTextField.class, false);
			//				break;
			//			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_PROJECTION:
			//				insertOptionField("selects", FormPortletTextField.class, true);
			//				insertOptionField("wheres", FormPortletTextField.class, false);
			//				insertOptionField("allowExternalUpdates", FormPortletCheckboxField.class, false);
			//				break;
			//			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_RELAY:
			//				insertOptionField("hosts", FormPortletTextField.class, true);
			//				insertOptionField("port", FormPortletTextField.class, true);
			//				insertOptionField("login", FormPortletTextField.class, true);
			//				insertOptionField("keystoreFile", FormPortletTextField.class, false);
			//				insertOptionField("keystorePass", FormPortletTextField.class, false);
			//				insertOptionField("derivedValues", FormPortletTextField.class, false);
			//				insertOptionField("inserts", FormPortletTextField.class, false);
			//				insertOptionField("updates", FormPortletTextField.class, false);
			//				insertOptionField("deletes", FormPortletTextField.class, false);
			//				insertOptionField("target", FormPortletTextField.class, false);
			//				insertOptionField("where", FormPortletTextField.class, false);
			//				break;
			//			default:
			//				throw new RuntimeException("Unknow trigger type code: " + type);

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
		// TODO Auto-generated method stub

	}

	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public String prepareUseClause() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String preparePreUseClause() {
		// TODO Auto-generated method stub
		return null;
	}

}
