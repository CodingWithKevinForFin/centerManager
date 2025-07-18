package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.utils.SH;

public class AmiCenterManagerTriggerEditor_Projection extends AmiCenterManagerAbstractTriggerEditor {
	private static final int FORM_LEFT_POSITION = 120;
	private static final int FORM_WIDTH = 550;
	private static final int FORM_HEIGHT = 300;

	final private FormPortletCheckboxField allowExternalUpdateField;
	final private AmiWebFormPortletAmiScriptField wheresField;
	final private AmiWebFormPortletAmiScriptField selectsField;

	public AmiCenterManagerTriggerEditor_Projection(PortletConfig config) {
		super(config);

		allowExternalUpdateField = form.addField(new FormPortletCheckboxField("allowExternalUpdates"));
		allowExternalUpdateField.setHelp("Optional. Value is either true or false (false by default)." + "<br>"
				+ "If true, then other processes (i.e triggers, UPDATEs) are allowed to perform UPDATEs on the target table." + "<br>"
				+ " Please use precaution when using this feature, since updating cells controlled by the aggregate trigger will result into an undesirable state.");

		wheresField = form.addField(new AmiWebFormPortletAmiScriptField(AmiCenterManagerUtils.formatRequiredField("wheres"), getManager(), ""));
		wheresField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		wheresField.setLeftPosPx(FORM_LEFT_POSITION).setWidth(FORM_WIDTH).setHeight(FORM_HEIGHT).setTopPosPx(50);
		wheresField.setHelp("A comma-delimited list of boolean expressions that must all be true on a source table's row in order for it to be projected into the target table:"
				+ "<br>" + "<b><i style=\"color:blue\">expression_on_sourceTableColumns,[ expression_on_sourceTableColumns ...]</i></b>");

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
		if (SH.is(wheresField.getValue()))
			sb.append(" wheres = ").append(SH.doubleQuote(wheresField.getValue()));
		else
			sb.append(" wheres = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

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
