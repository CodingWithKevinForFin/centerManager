package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanger.AmiCenterEntityConsts;
import com.f1.ami.web.centermanger.AmiCenterManagerUtils;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.utils.SH;

public class AmiCenterManagerTriggerEditor_Decorate extends AmiCenterManagerAbstractTriggerEditor {
	private static final int FORM_LEFT_POSITION = 120;
	private static final int FORM_WIDTH = 550;
	private static final int FORM_HEIGHT = 300;

	final private FormPortletCheckboxField keysChangeField;
	final private AmiWebFormPortletAmiScriptField onField;
	final private AmiWebFormPortletAmiScriptField selectsField;

	public AmiCenterManagerTriggerEditor_Decorate(PortletConfig config) {
		super(config);
		keysChangeField = form.addField(new FormPortletCheckboxField("keysChange"));
		keysChangeField.setHelp("Either true or false. Default is false." + "<br>" + "If it's expected that columns participating in the ON clause can change, then set to true."
				+ "<br>" + "Note that setting to true adds additional overhead.");
		onField = form.addField(new AmiWebFormPortletAmiScriptField(AmiCenterManagerUtils.formatRequiredField("on"), getManager(), ""));
		onField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
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
		if (SH.is(onField.getValue()))
			sb.append(" on = ").append(SH.doubleQuote(onField.getValue()));
		else
			sb.append(" on = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (SH.is(selectsField.getValue()))
			sb.append(" selects = ").append(SH.doubleQuote(selectsField.getValue()));
		else
			sb.append(" selects = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (keysChangeField.getBooleanValue())
			sb.append(" keysChange = ").append(SH.doubleQuote("true"));
		return sb.toString();
	}

}
