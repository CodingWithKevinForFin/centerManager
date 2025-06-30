package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.utils.SH;

public class AmiCenterManagerTriggerEditor_Join extends AmiCenterManagerAbstractTriggerEditor {
	private static final int FORM_LEFT_POSITION = 120;
	private static final int FORM_WIDTH = 550;
	private static final int FORM_HEIGHT = 300;

	final private FormPortletSelectField<Short> typeField;
	final private AmiWebFormPortletAmiScriptField onField;
	final private AmiWebFormPortletAmiScriptField selectsField;

	public AmiCenterManagerTriggerEditor_Join(PortletConfig config) {
		super(config);

		typeField = form.addField(new FormPortletSelectField<Short>(short.class, AmiCenterManagerUtils.formatRequiredField("type")));
		typeField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_CODE_INNER, AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_INNER);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_CODE_LEFT, AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_LEFT);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_CODE_RIGHT, AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_RIGHT);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_CODE_OUTER, AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_OUTER);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_CODE_LEFT_ONLY, AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_LEFT_ONLY);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_CODE_RIGHT_ONLY, AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_RIGHT_ONLY);
		typeField.addOption(AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_CODE_OUTER_ONLY, AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_OUTER_ONLY);
		typeField.setDefaultValue(AmiCenterEntityConsts.TRIGGER_JOIN_TYPE_CODE_INNER);
		typeField.setHelp("How to join the left and right tables");
		typeField.setLeftPosPx(60).setWidth(200).setHeight(20).setTopPosPx(20);

		onField = form.addField(new AmiWebFormPortletAmiScriptField(AmiCenterManagerUtils.formatRequiredField("groupBys"), getManager(), ""));
		onField.setLeftPosPx(FORM_LEFT_POSITION).setWidth(FORM_WIDTH).setHeight(FORM_HEIGHT).setTopPosPx(50);
		onField.setHelp("An expression for how to relate the two tables in the form:" + "<br>"
				+ "<b><i style=\"color:blue\">leftColumn == rightColumn [ && leftColumn == rightColumn ... ]</i></b>");

		selectsField = form.addField(new AmiWebFormPortletAmiScriptField(AmiCenterManagerUtils.formatRequiredField("selects"), getManager(), ""));
		selectsField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		selectsField.setLeftPosPx(FORM_LEFT_POSITION).setWidth(FORM_WIDTH).setHeight(FORM_HEIGHT).setTopPosPx(380);
		selectsField.setHelp(" A comma delimited list of expressions on how to populate target columns from source columns." + "<br>"
				+ "<b><i style=\"color:blue\">targetTableColumn = aggregate_on_sourceTableColumns [,targetTableColumn = aggregate_on_sourceTableColumns ...]</i></b>");
		addChild(form, 0, 0);
	}

	@Override
	public String getKeyValuePairs() {
		StringBuilder sb = new StringBuilder();
		sb.append(" type = ").append(AmiCenterManagerUtils.toJoinTriggerType(typeField.getValue()));

		if (SH.is(onField.getValue()))
			sb.append(" on = ").append(SH.doubleQuote(onField.getValue()));
		else
			sb.append(" on = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (SH.is(selectsField.getValue()))
			sb.append(" selects = ").append(SH.doubleQuote(selectsField.getValue()));
		else
			sb.append(" selects = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
		return sb.toString();
	}

	@Override
	public FormPortletField<?> getFieldByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
