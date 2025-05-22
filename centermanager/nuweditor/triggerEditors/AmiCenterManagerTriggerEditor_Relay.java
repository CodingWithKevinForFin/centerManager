package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.ami.web.centermanager.nuweditor.AmiCenterManagerAbstractEditCenterObjectPortlet;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.SH;

public class AmiCenterManagerTriggerEditor_Relay extends AmiCenterManagerAbstractTriggerEditor {
	public static final int DEFAULT_ROWHEIGHT = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_ROWHEIGHT;
	public static final int DEFAULT_LEFTPOS = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_LEFTPOS; //164
	public static final int DEFAULT_Y_SPACING = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_Y_SPACING;
	public static final int DEFAULT_X_SPACING = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_X_SPACING;
	public static final int DEFAULT_TOPPOS = DEFAULT_Y_SPACING + 10;

	public static final int OPTION_FORM_HEIGHT = 50;
	public static final int AMISCRIPT_FORM_HEIGHT = 600;
	public static final int AMISCRIPT_FORM_PADDING = 0;

	private static final int FORM_LEFT_POSITION = 120;
	private static final int FORM_WIDTH = 650;
	private static final int FORM_HEIGHT = 200;

	final private FormPortletTextField hostField;
	final private FormPortletTextField portField;
	final private FormPortletTextField loginField;
	final private FormPortletTextField keystoreFileField;
	final private FormPortletTextField keystorePassField;
	final private FormPortletTextField targetField;

	final private FormPortletTextField insertField;
	final private FormPortletTextField updateField;
	final private FormPortletTextField deleteField;

	final private AmiWebFormPortletAmiScriptField derivedValuesField;
	final private AmiWebFormPortletAmiScriptField whereField;

	public AmiCenterManagerTriggerEditor_Relay(PortletConfig config) {
		super(config);

		//row 1
		hostField = form.addField(new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("host")));
		hostField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		hostField.setWidth(180).setLeftPosPx(DEFAULT_LEFTPOS).setTopPosPx(DEFAULT_TOPPOS).setHeight(DEFAULT_ROWHEIGHT);
		hostField.setHelp("hostname of the relay instance");

		portField = form.addField(new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("port")));
		portField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		portField.setWidth(100).setHeightPx(DEFAULT_ROWHEIGHT).setLeftPosPx(DEFAULT_LEFTPOS + 260).setTopPosPx(DEFAULT_TOPPOS);
		portField.setHelp("port for the relay instance defined by the property <b><i style=\"color:blue\">ami.port</i></b>");

		loginField = form.addField(new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("login")));
		loginField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		loginField.setWidth(100).setHeightPx(DEFAULT_ROWHEIGHT).setLeftPosPx(DEFAULT_LEFTPOS + 450).setTopPosPx(DEFAULT_TOPPOS);
		loginField.setHelp("the unique id to identify the process/application");

		//row2
		keystoreFileField = form.addField(new FormPortletTextField("keystoreFile"));
		keystoreFileField.setWidth(160).setLeftPosPx(DEFAULT_LEFTPOS + 10).setTopPosPx(DEFAULT_Y_SPACING * 2 + DEFAULT_ROWHEIGHT + 10).setHeight(DEFAULT_ROWHEIGHT);
		keystoreFileField.setHelp("optional, location of a keystore file");

		keystorePassField = form.addField(new FormPortletTextField("keystorePass"));
		keystorePassField.setWidth(160).setLeftPosPx(DEFAULT_LEFTPOS + 220 + DEFAULT_X_SPACING).setTopPosPx(DEFAULT_Y_SPACING * 2 + DEFAULT_ROWHEIGHT + 10)
				.setHeight(DEFAULT_ROWHEIGHT);
		keystorePassField.setHelp(" optional, the keystore password, this will be encrypted using the <b><i style=\"color:blue\">strEncrypt</i></b> method first");

		targetField = form.addField(new FormPortletTextField("target"));
		targetField.setWidth(110).setLeftPosPx(DEFAULT_LEFTPOS + 450 + DEFAULT_X_SPACING).setTopPosPx(DEFAULT_Y_SPACING * 2 + DEFAULT_ROWHEIGHT + 10).setHeight(DEFAULT_ROWHEIGHT);
		targetField.setHelp("The name of the target table, if not defined assumes the same name as the source");

		//each of the following field occupies one row
		insertField = form.addField(new FormPortletTextField("insert"));
		insertField.setWidth(FORM_WIDTH).setLeftPosPx(DEFAULT_LEFTPOS).setTopPosPx(DEFAULT_Y_SPACING * 3 + DEFAULT_ROWHEIGHT * 2 + 10).setHeight(DEFAULT_ROWHEIGHT);
		insertField.setHelp(" comma delimited list of target columns to be sent on an onInserted event on the source table." + "<br>"
				+ " If your target table has a unique constraint, in most cases you will want to add that column(s) to this list");

		updateField = form.addField(new FormPortletTextField("update"));
		updateField.setWidth(FORM_WIDTH).setLeftPosPx(DEFAULT_LEFTPOS).setTopPosPx(DEFAULT_Y_SPACING * 4 + DEFAULT_ROWHEIGHT * 3 + 10).setHeight(DEFAULT_ROWHEIGHT);
		updateField.setHelp("comma delimited list of target columns to be sent on an onUpdated event on the source table. " + "<br>"
				+ "If your target table has a unique constraint, a unique identifier column(s) needs to be in this list");

		deleteField = form.addField(new FormPortletTextField("delete"));
		deleteField.setWidth(FORM_WIDTH).setLeftPosPx(DEFAULT_LEFTPOS).setTopPosPx(DEFAULT_Y_SPACING * 5 + DEFAULT_ROWHEIGHT * 4 + 10).setHeight(DEFAULT_ROWHEIGHT);
		deleteField.setHelp(" comma delimited list of target columns to be sent on an onDeleted event on the source table." + "<br>"
				+ "If your target table has a unique constraint, a unique identifier column(s) needs to be in this list");

		//amiscript form
		derivedValuesField = form.addField(new AmiWebFormPortletAmiScriptField("derivedValues", getManager(), ""));
		derivedValuesField.setLeftPosPx(FORM_LEFT_POSITION).setWidth(FORM_WIDTH).setHeight(FORM_HEIGHT).setTopPosPx(DEFAULT_Y_SPACING * 6 + DEFAULT_ROWHEIGHT * 5 + 20);
		derivedValuesField.setHelp("<b><i style=\"color:blue\">key=expression,...[key=expression]</i></b> pattern to map source columns to target columns." + "<br>"
				+ "If the option is omitted, all source columns will map to the target columns of the same given name." + "<br>"
				+ "If target columns are omitted, it will map the target column to a source column of the same given name");

		whereField = form.addField(new AmiWebFormPortletAmiScriptField("where", getManager(), ""));
		whereField.setLeftPosPx(FORM_LEFT_POSITION).setWidth(FORM_WIDTH).setHeight(FORM_HEIGHT).setTopPosPx(DEFAULT_Y_SPACING * 6 + DEFAULT_ROWHEIGHT * 5 + FORM_HEIGHT + 40);
		whereField.setHelp("a conditional statement which needs to evaluate to a boolean expression on the source rows," + "<br>"
				+ " filters what messages should be sent to the target table," + "<br>" + " false indicates the message will be skipped.");
		addChild(form, 0, 0);
	}

	@Override
	public String getKeyValuePairs() {
		StringBuilder sb = new StringBuilder();

		if (SH.is(hostField.getValue()))
			sb.append(" host = ").append(SH.doubleQuote(hostField.getValue()));
		else
			sb.append(" host = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (SH.is(portField.getValue()))
			sb.append(" port = ").append(SH.doubleQuote(portField.getValue()));
		else
			sb.append(" port = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (SH.is(loginField.getValue()))
			sb.append(" login = ").append(SH.doubleQuote(loginField.getValue()));
		else
			sb.append(" login = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (SH.is(targetField.getValue()))
			sb.append(" target = ").append(SH.doubleQuote(targetField.getValue()));

		if (SH.is(derivedValuesField.getValue()))
			sb.append(" derivedValues = ").append(SH.doubleQuote(derivedValuesField.getValue()));

		if (SH.is(insertField.getValue()))
			sb.append(" insert = ").append(SH.doubleQuote(insertField.getValue()));

		if (SH.is(updateField.getValue()))
			sb.append(" update = ").append(SH.doubleQuote(updateField.getValue()));

		if (SH.is(deleteField.getValue()))
			sb.append(" delete = ").append(SH.doubleQuote(deleteField.getValue()));

		if (SH.is(whereField.getValue()))
			sb.append(" where = ").append(SH.doubleQuote(whereField.getValue()));

		return sb.toString();
	}

}
