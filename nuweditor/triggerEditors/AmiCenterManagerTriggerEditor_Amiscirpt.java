package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanager.nuweditor.AmiCenterManagerAbstractEditCenterObjectPortlet;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.TabPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;

public class AmiCenterManagerTriggerEditor_Amiscirpt extends GridPortlet {
	public static final int DEFAULT_ROWHEIGHT = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_ROWHEIGHT;
	public static final int DEFAULT_LEFTPOS = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_LEFTPOS; //164
	public static final int DEFAULT_Y_SPACING = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_Y_SPACING;
	public static final int DEFAULT_X_SPACING = AmiCenterManagerAbstractEditCenterObjectPortlet.DEFAULT_X_SPACING;
	public static final int DEFAULT_TOPPOS = DEFAULT_Y_SPACING;

	public static final int OPTION_FORM_HEIGHT = 60;
	public static final int AMISCRIPT_FORM_HEIGHT = 600;
	public static final int AMISCRIPT_FORM_PADDING = 0;

	final private FormPortlet form;
	final private TabPortlet scriptTabs;

	//option fields
	final private FormPortletCheckboxField canMutateRowField;
	final private FormPortletCheckboxField runOnStartupField;
	final private FormPortletTextField rowVarField;
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
		form = new FormPortlet(generateConfig());

		canMutateRowField = form.addField(new FormPortletCheckboxField("canMutateRow"));
		canMutateRowField.setLeftPosPx(DEFAULT_LEFTPOS + 80).setWidthPx(40).setTopPosPx(DEFAULT_TOPPOS + 10);

		runOnStartupField = form.addField(new FormPortletCheckboxField("runOnStartup"));
		runOnStartupField.setLeftPosPx(DEFAULT_LEFTPOS + 250).setWidthPx(40).setTopPosPx(DEFAULT_TOPPOS + 10);

		rowVarField = form.addField(new FormPortletTextField("rowVar"));
		rowVarField.setLeftPosPx(DEFAULT_LEFTPOS + 370).setWidth(180).setHeightPx(DEFAULT_ROWHEIGHT).setTopPosPx(DEFAULT_TOPPOS + 5);

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

}
