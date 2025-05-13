package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanager.nuweditor.AmiCenterManagerAbstractEditCenterObjectPortlet;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.TabPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.SH;

public class AmiCenterManagerTriggerEditor_Amiscirpt extends AmiCenterManagerAbstractTriggerEditor {
	public static final int DEFAULT_ROWHEIGHT = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_ROWHEIGHT;
	public static final int DEFAULT_LEFTPOS = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_LEFTPOS; //164
	public static final int DEFAULT_Y_SPACING = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_Y_SPACING;
	public static final int DEFAULT_X_SPACING = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_X_SPACING;
	public static final int DEFAULT_TOPPOS = DEFAULT_Y_SPACING;

	public static final int OPTION_FORM_HEIGHT = 80;
	public static final int AMISCRIPT_FORM_HEIGHT = 600;
	public static final int AMISCRIPT_FORM_PADDING = 0;

	final private TabPortlet scriptTabs;

	//option fields
	final private FormPortletCheckboxField canMutateRowField;
	final private FormPortletTextField rowVarField;
	final private AmiWebFormPortletAmiScriptField varsField;
	//script fields
	final private GridPortlet onInsertingScriptGrid;
	final private FormPortlet onInsertingScriptForm;
	final private AmiWebFormPortletAmiScriptField onInsertingScriptField;

	final private GridPortlet onUpdatingScriptGrid;
	final private FormPortlet onUpdatingScriptForm;
	final private AmiWebFormPortletAmiScriptField onUpdatingScriptField;

	final private GridPortlet onDeletingScriptGrid;
	final private FormPortlet onDeletingScriptForm;
	final private AmiWebFormPortletAmiScriptField onDeletingScriptField;

	final private GridPortlet onInsertedScriptGrid;
	final private FormPortlet onInsertedScriptForm;
	final private AmiWebFormPortletAmiScriptField onInsertedScriptField;

	final private GridPortlet onUpdatedScriptGrid;
	final private FormPortlet onUpdatedScriptForm;
	final private AmiWebFormPortletAmiScriptField onUpdatedScriptField;

	final private GridPortlet onStartupScriptGrid;
	final private FormPortlet onStartupScriptForm;
	final private AmiWebFormPortletAmiScriptField onStartupScriptField;

	public AmiCenterManagerTriggerEditor_Amiscirpt(PortletConfig config) {
		super(config);

		canMutateRowField = form.addField(new FormPortletCheckboxField("canMutateRow"));
		canMutateRowField.setHelp(" If true, then any values of the row changed inside the onInsertingScript will reflect back on the row to be inserted." + "<br>"
				+ "For onUpdatingScript, any changes to the new_varname values will reflect on the row to be updated." + "<br>"
				+ "Note, this only applies to the onInsertingScript and onUpdatingScript options, has no effect on onInsertedScript, onUpdatedScript, and onDeletingScript. ");
		canMutateRowField.setLeftPosPx(DEFAULT_LEFTPOS + 63).setWidthPx(40).setTopPosPx(DEFAULT_TOPPOS + 10);

		rowVarField = form.addField(new FormPortletTextField("rowVar"));
		rowVarField.setLeftPosPx(DEFAULT_LEFTPOS + 250).setWidth(180).setHeightPx(DEFAULT_ROWHEIGHT).setTopPosPx(DEFAULT_TOPPOS + 5);
		rowVarField.setHelp("a placeholder (can be any custom variable name) that contains the map that reflects the row change in the table (either insert, update or delete)."
				+ "<br>" + " Note that rowVar is a read-only map and the available methods include:" + "<br>"
				+ "<b><i style=\"color:blue\"> boolean containsValue(), boolean containsKey().</i></b>" + "<br>"
				+ " For onUpdatingScript, you must add the <b><i style=\"color:blue\">new_</i></b> or <b><i style=\"color:blue\">old_</i></b> prefix to the rowVar to use it. ");

		varsField = form.addField(new AmiWebFormPortletAmiScriptField("vars", getManager(), ""));
		varsField.setLeftPosPx(DEFAULT_LEFTPOS).setWidth(700).setTopPosPx(DEFAULT_TOPPOS + 40).setHeight(20);
		varsField.setHelp("Variables shared by the trigger, a comma delimited list of type varname");

		onInsertingScriptGrid = new GridPortlet(generateConfig());
		onInsertingScriptForm = new FormPortlet(generateConfig());
		onInsertingScriptGrid.addChild(onInsertingScriptForm);
		onInsertingScriptField = onInsertingScriptForm.addField(new AmiWebFormPortletAmiScriptField("", getManager(), ""));
		onInsertingScriptField.setLeftPosPx(AMISCRIPT_FORM_PADDING).setRightPosPx(AMISCRIPT_FORM_PADDING).setBottomPosPx(AMISCRIPT_FORM_PADDING);
		onInsertingScriptField.setWidthPx(400).setTopPosPx(DEFAULT_TOPPOS);
		onInsertingScriptField.setHeightPx(AMISCRIPT_FORM_HEIGHT);

		onUpdatingScriptGrid = new GridPortlet(generateConfig());
		onUpdatingScriptForm = new FormPortlet(generateConfig());
		onUpdatingScriptGrid.addChild(onUpdatingScriptForm);
		onUpdatingScriptField = onUpdatingScriptForm.addField(new AmiWebFormPortletAmiScriptField("", getManager(), ""));
		onUpdatingScriptField.setLeftPosPx(AMISCRIPT_FORM_PADDING).setRightPosPx(AMISCRIPT_FORM_PADDING).setBottomPosPx(AMISCRIPT_FORM_PADDING);
		onUpdatingScriptField.setWidthPx(400).setTopPosPx(DEFAULT_TOPPOS);
		onUpdatingScriptField.setHeightPx(AMISCRIPT_FORM_HEIGHT);

		onDeletingScriptGrid = new GridPortlet(generateConfig());
		onDeletingScriptForm = new FormPortlet(generateConfig());
		onDeletingScriptGrid.addChild(onDeletingScriptForm);
		onDeletingScriptField = onDeletingScriptForm.addField(new AmiWebFormPortletAmiScriptField("", getManager(), ""));
		onDeletingScriptField.setLeftPosPx(AMISCRIPT_FORM_PADDING).setRightPosPx(AMISCRIPT_FORM_PADDING).setBottomPosPx(AMISCRIPT_FORM_PADDING);
		onDeletingScriptField.setWidthPx(400).setTopPosPx(DEFAULT_TOPPOS);
		onDeletingScriptField.setHeightPx(AMISCRIPT_FORM_HEIGHT);

		onInsertedScriptGrid = new GridPortlet(generateConfig());
		onInsertedScriptForm = new FormPortlet(generateConfig());
		onInsertedScriptGrid.addChild(onInsertedScriptForm);
		onInsertedScriptField = onInsertedScriptForm.addField(new AmiWebFormPortletAmiScriptField("", getManager(), ""));
		onInsertedScriptField.setLeftPosPx(AMISCRIPT_FORM_PADDING).setRightPosPx(AMISCRIPT_FORM_PADDING).setBottomPosPx(AMISCRIPT_FORM_PADDING);
		onInsertedScriptField.setWidthPx(400).setTopPosPx(DEFAULT_TOPPOS);
		onInsertedScriptField.setHeightPx(AMISCRIPT_FORM_HEIGHT);

		onUpdatedScriptGrid = new GridPortlet(generateConfig());
		onUpdatedScriptForm = new FormPortlet(generateConfig());
		onUpdatedScriptGrid.addChild(onUpdatedScriptForm);
		onUpdatedScriptField = onUpdatedScriptForm.addField(new AmiWebFormPortletAmiScriptField("", getManager(), ""));
		onUpdatedScriptField.setLeftPosPx(AMISCRIPT_FORM_PADDING).setRightPosPx(AMISCRIPT_FORM_PADDING).setBottomPosPx(AMISCRIPT_FORM_PADDING);
		onUpdatedScriptField.setWidthPx(400).setTopPosPx(DEFAULT_TOPPOS);
		onUpdatedScriptField.setHeightPx(AMISCRIPT_FORM_HEIGHT);

		onStartupScriptGrid = new GridPortlet(generateConfig());
		onStartupScriptForm = new FormPortlet(generateConfig());
		onStartupScriptGrid.addChild(onStartupScriptForm);
		onStartupScriptField = onStartupScriptForm.addField(new AmiWebFormPortletAmiScriptField("", getManager(), ""));
		onStartupScriptField.setLeftPosPx(AMISCRIPT_FORM_PADDING).setRightPosPx(AMISCRIPT_FORM_PADDING).setBottomPosPx(AMISCRIPT_FORM_PADDING);
		onStartupScriptField.setWidthPx(400).setTopPosPx(DEFAULT_TOPPOS);
		onStartupScriptField.setHeightPx(AMISCRIPT_FORM_HEIGHT);

		this.scriptTabs = new TabPortlet(generateConfig());
		this.scriptTabs.setIsCustomizable(false);
		this.scriptTabs.addChild("onInsertingScript", onInsertingScriptGrid);
		this.scriptTabs.addChild("onUpdatingScript", onUpdatingScriptGrid);
		this.scriptTabs.addChild("onDeletingScript", onDeletingScriptGrid);
		this.scriptTabs.addChild("onInsertedScript", onInsertedScriptGrid);
		this.scriptTabs.addChild("onUpdatedScript", onUpdatedScriptGrid);
		this.scriptTabs.addChild("onStartupScript", onStartupScriptGrid);

		addChild(form, 0, 0);
		addChild(scriptTabs, 0, 1);
		setRowSize(0, OPTION_FORM_HEIGHT);

	}

	@Override
	public String getKeyValuePairs() {
		StringBuilder sb = new StringBuilder();
		if (SH.is(varsField.getValue()))
			sb.append(" vars = ").append(SH.doubleQuote(varsField.getValue()));

		if (SH.is(onStartupScriptField.getValue()))
			sb.append(" onStartupScript = ").append(SH.doubleQuote(onStartupScriptField.getValue()));

		if (SH.is(onInsertingScriptField.getValue()))
			sb.append(" onInsertingScript = ").append(SH.doubleQuote(onInsertingScriptField.getValue()));

		if (SH.is(onInsertedScriptField.getValue()))
			sb.append(" onInsertedScript = ").append(SH.doubleQuote(onInsertedScriptField.getValue()));

		if (SH.is(onUpdatingScriptField.getValue()))
			sb.append(" onUpdatingScript = ").append(SH.doubleQuote(onUpdatingScriptField.getValue()));

		if (SH.is(onUpdatedScriptField.getValue()))
			sb.append(" onUpdatedScript = ").append(SH.doubleQuote(onUpdatedScriptField.getValue()));

		if (SH.is(onDeletingScriptField.getValue()))
			sb.append(" onDeletingScript = ").append(SH.doubleQuote(onDeletingScriptField.getValue()));

		if (canMutateRowField.getBooleanValue())
			sb.append(" canMutateRow = ").append(SH.doubleQuote("true"));

		if (SH.is(rowVarField.getValue()))
			sb.append(" rowVar = ").append(SH.doubleQuote(rowVarField.getValue()));
		return sb.toString();
	}

}
