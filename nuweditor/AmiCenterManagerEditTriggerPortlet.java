package com.f1.ami.web.centermanager.nuweditor;

import java.util.Map;

import com.f1.ami.web.centermanager.nuweditor.triggerEditors.AmiCenterManagerTriggerEditor_Aggregate;
import com.f1.ami.web.centermanager.nuweditor.triggerEditors.AmiCenterManagerTriggerEditor_Amiscirpt;
import com.f1.ami.web.centermanger.AmiCenterEntityConsts;
import com.f1.ami.web.centermanger.AmiCenterManagerUtils;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.HtmlPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;

public class AmiCenterManagerEditTriggerPortlet extends AmiCenterManagerAbstractEditCenterObjectPortlet {
	//height const
	private static final int OPTION_FORM_HEIGHT = 40;//common option form height

	//option fields
	final private FormPortlet form;

	final private GridPortlet formAndTriggerConfigGrid;

	final private FormPortletTextField triggerNameField;
	final private FormPortletSelectField<Short> triggerTypeField;

	final private FormPortletTextField triggerOnField;
	final private FormPortletTextField triggerPriorityField;

	//trigger editors
	final private AmiCenterManagerTriggerEditor_Amiscirpt amiscriptEditor;
	final private AmiCenterManagerTriggerEditor_Aggregate aggEditor;

	//trigger-type-specific editor
	private InnerPortlet editorPanel;//all the type-specific fields excluding amiscript fields

	public AmiCenterManagerEditTriggerPortlet(PortletConfig config, boolean isAdd) {
		super(config, isAdd);
		this.form = new FormPortlet(generateConfig());
		amiscriptEditor = new AmiCenterManagerTriggerEditor_Amiscirpt(generateConfig());
		this.getManager().onPortletAdded(amiscriptEditor);
		aggEditor = new AmiCenterManagerTriggerEditor_Aggregate(generateConfig());
		this.getManager().onPortletAdded(aggEditor);

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
		//		canMutateRowField = this.configForm.addField(new FormPortletCheckboxField("canMutateRow"));

		//by default
		editorPanel.setPortlet(amiscriptEditor);

		this.addChild(formAndTriggerConfigGrid, 0, 0);
		this.addChild(buttonsFp, 0, 1);

		setRowSize(1, buttonsFp.getButtonPanelHeight());
		this.form.addFormPortletListener(this);
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
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AGGREGATE:
				editorPanel.setPortlet(aggEditor);
				break;
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
		if (field == this.triggerTypeField) {
			short type = this.triggerTypeField.getValue();
			updateTriggerTemplate(type);
		}

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
