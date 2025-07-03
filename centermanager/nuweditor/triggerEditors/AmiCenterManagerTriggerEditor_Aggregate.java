package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.utils.SH;

public class AmiCenterManagerTriggerEditor_Aggregate extends AmiCenterManagerAbstractTriggerEditor {
	private static final int FORM_LEFT_POSITION = 120;
	private static final int FORM_WIDTH = 550;
	private static final int FORM_HEIGHT = 300;

	final private FormPortletCheckboxField allowExternalUpdateField;
	final private AmiWebFormPortletAmiScriptField groupBysField;
	final private AmiWebFormPortletAmiScriptField selectsField;

	public AmiCenterManagerTriggerEditor_Aggregate(PortletConfig config) {
		super(config);

		allowExternalUpdateField = form.addField(new FormPortletCheckboxField("allowExternalUpdates"));
		allowExternalUpdateField.setHelp("Optional. Value is either true or false (false by default)." + "<br>"
				+ "If true, then other processes (i.e triggers, UPDATEs) are allowed to perform UPDATEs on the target table." + "<br>"
				+ " Please use precaution when using this feature, since updating cells controlled by the aggregate trigger will result into an undesirable state.");

		groupBysField = form.addField(new AmiWebFormPortletAmiScriptField(AmiCenterManagerUtils.formatRequiredField("groupBys"), getManager(), ""));
		groupBysField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		groupBysField.setLeftPosPx(FORM_LEFT_POSITION).setWidth(FORM_WIDTH).setHeight(FORM_HEIGHT).setTopPosPx(50);
		groupBysField.setHelp("A comma delimited list of expressions to group rows by, each expression being of the form:" + "<br>"
				+ "<b><i style=\"color:blue\">targetTableColumn = expression_on_sourceTableColumns [,targetTableColumn = expression_on_sourceTableColumns ...]</i></b>");

		//		DividerPortlet div = new DividerPortlet(getManager(), true, groupBysField, selectsField);

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
		if (SH.is(groupBysField.getValue()))
			sb.append(" groupBys = ").append(SH.doubleQuote(groupBysField.getValue()));
		else
			sb.append(" groupBys = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (SH.is(selectsField.getValue()))
			sb.append(" selects = ").append(SH.doubleQuote(selectsField.getValue()));
		else
			sb.append(" selects = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (allowExternalUpdateField.getBooleanValue())
			sb.append(" allowExternalUpdate = ").append(SH.doubleQuote("true"));
		return sb.toString();
	}

	@Override
	public FormPortletField<?> getFieldByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enableEdit(boolean enable) {
		// TODO Auto-generated method stub

	}

}
