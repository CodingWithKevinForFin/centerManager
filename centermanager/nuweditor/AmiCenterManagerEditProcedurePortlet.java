package com.f1.ami.web.centermanager.nuweditor;

import java.util.Map;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.TabPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.SH;

public class AmiCenterManagerEditProcedurePortlet extends AmiCenterManagerAbstractEditCenterObjectPortlet {
	private static final int DEFAULT_ROWHEIGHT = 25;
	private static final int DEFAULT_LEFTPOS = 75; //164
	private static final int DEFAULT_Y_SPACING = 10;
	private static final int DEFAULT_X_SPACING = 65;
	private static final int DEFAULT_TOPPOS = DEFAULT_Y_SPACING;

	//Width consts
	private static final int NAME_WIDTH = 110;
	private static final int TYPE_WIDTH = 80;
	private static final int LOGGING_WIDTH = 80;
	private static final int ARGS_WIDTH = 700;
	//height const
	private static final int OPTION_FORM_HEIGHT = 120;
	private static final int AMISCRIPT_FORM_HEIGHT = 600;

	//padding
	private static final int AMISCRIPT_FORM_PADDING = 0;

	final private FormPortlet form;
	final private TabPortlet scriptTabs;
	final private FormPortletTextField nameField;
	final private FormPortletSelectField<Short> typeField;
	final private AmiWebFormPortletAmiScriptField argsField;
	final private FormPortletSelectField<Short> loggingField;

	final private GridPortlet scriptGrid;
	final private FormPortlet scriptForm;

	final private GridPortlet onStartupScriptGrid;
	final private FormPortlet onStartupScriptForm;
	final private AmiWebFormPortletAmiScriptField scriptField;
	final private AmiWebFormPortletAmiScriptField onStartupScriptField;

	public AmiCenterManagerEditProcedurePortlet(PortletConfig config, boolean isAdd) {
		super(config, isAdd);

		this.form = new FormPortlet(generateConfig());

		//The top row
		this.nameField = this.form.addField(new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("Name")));
		this.nameField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		this.nameField.setWidth(NAME_WIDTH);
		this.nameField.setHeightPx(DEFAULT_ROWHEIGHT);
		this.nameField.setLeftPosPx(DEFAULT_LEFTPOS);
		this.nameField.setTopPosPx(DEFAULT_TOPPOS);

		this.typeField = this.form.addField(new FormPortletSelectField<Short>(short.class, AmiCenterManagerUtils.formatRequiredField("Type")));
		this.typeField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		//only one type for timer, so disable
		this.typeField.setDisabled(true);
		this.typeField.addOption(AmiCenterEntityConsts.TIMER_TYPE_CODE_AMISCRIPT, AmiCenterEntityConsts.TIMER_TYPE_AMISCRIPT);
		this.typeField.setLeftPosPx(DEFAULT_LEFTPOS + NAME_WIDTH + DEFAULT_X_SPACING).setTopPosPx(DEFAULT_TOPPOS).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(100);

		loggingField = this.form.addField(new FormPortletSelectField(short.class, "logging"));
		loggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_OFF, AmiCenterEntityConsts.LOGGING_LEVEL_OFF);
		loggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_ON, AmiCenterEntityConsts.LOGGING_LEVEL_ON);
		loggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_VERBOSE, AmiCenterEntityConsts.LOGGING_LEVEL_VERBOSE);
		loggingField.setHelp("set the logging level when the timer gets called: The following Logging options are supported:<br>"
				+ "<ul><li>(1). <b style=\"color:blue\"><i>off</i></b> (default): no logging<br></li>"
				+ "<li>(2). <b style=\"color:blue\"><i>on</i></b>: logs the time when the timer is called and when it completes.<br></li>"
				+ "<li>(3). <b style=\"color:blue\"><i>verbose</i></b>: equivalent of using <b><i>show_plan=ON</i></b> in AMIDB. Logs the time that a timer starts and finishes and also each query step.</li></ul>");
		loggingField.setWidthPx(LOGGING_WIDTH);
		loggingField.setHeightPx(DEFAULT_ROWHEIGHT);
		loggingField.setLeftPosPx(DEFAULT_LEFTPOS + NAME_WIDTH + TYPE_WIDTH + DEFAULT_X_SPACING * 3);
		loggingField.setTopPosPx(DEFAULT_TOPPOS);

		argsField = this.form.addField(new AmiWebFormPortletAmiScriptField("args", getManager(), ""));
		argsField.setHelp("Variables shared by the timer, a comma delimited list of type varname");
		argsField.setWidthPx(ARGS_WIDTH);
		argsField.setHeightPx(DEFAULT_ROWHEIGHT - 5);
		argsField.setLeftPosPx(DEFAULT_LEFTPOS);
		argsField.setTopPosPx(DEFAULT_TOPPOS + (DEFAULT_ROWHEIGHT + DEFAULT_Y_SPACING) * 1);

		onStartupScriptGrid = new GridPortlet(generateConfig());
		onStartupScriptForm = new FormPortlet(generateConfig());
		onStartupScriptGrid.addChild(onStartupScriptForm);
		onStartupScriptField = onStartupScriptForm.addField(new AmiWebFormPortletAmiScriptField("", getManager(), ""));
		onStartupScriptField.setHelp("AmiScript to run when the timer is created");
		onStartupScriptField.setLeftPosPx(AMISCRIPT_FORM_PADDING).setRightPosPx(AMISCRIPT_FORM_PADDING).setBottomPosPx(AMISCRIPT_FORM_PADDING);
		onStartupScriptField.setWidthPx(400);
		onStartupScriptField.setHeightPx(AMISCRIPT_FORM_HEIGHT);
		onStartupScriptField.setTopPosPx(DEFAULT_TOPPOS);

		scriptGrid = new GridPortlet(generateConfig());
		scriptForm = new FormPortlet(generateConfig());
		scriptGrid.addChild(scriptForm);
		scriptField = scriptForm.addField(new AmiWebFormPortletAmiScriptField("", getManager(), ""));
		scriptField.setHelp("AmiScript to run when timer is executed");
		scriptField.setLeftPosPx(AMISCRIPT_FORM_PADDING).setRightPosPx(AMISCRIPT_FORM_PADDING).setBottomPosPx(AMISCRIPT_FORM_PADDING);
		scriptField.setWidthPx(400);
		scriptField.setHeightPx(AMISCRIPT_FORM_HEIGHT);
		scriptField.setTopPosPx(DEFAULT_TOPPOS);

		this.scriptTabs = new TabPortlet(generateConfig());
		this.scriptTabs.setIsCustomizable(false);
		this.scriptTabs.addChild("Script", scriptGrid);
		this.scriptTabs.addChild("onStartupScript", onStartupScriptGrid);

		GridPortlet grid = new GridPortlet(generateConfig());
		grid.addChild(form, 0, 0);
		grid.addChild(scriptTabs, 0, 1);
		grid.setRowSize(0, OPTION_FORM_HEIGHT);

		this.addChild(grid, 0, 0);
		this.addChild(buttonsFp, 0, 1);

		setRowSize(1, buttonsFp.getButtonPanelHeight());
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
		StringBuilder sb = new StringBuilder();
		//requried field
		if (SH.is(argsField.getValue()))
			sb.append(" arguments = ").append(SH.doubleQuote(argsField.getValue()));
		else
			sb.append(" arguments = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
		if (SH.is(scriptField.getValue()))
			sb.append(" script= ").append(SH.doubleQuote(scriptField.getValue()));
		else
			sb.append(" script= ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
		if (loggingField.getValue() != AmiCenterEntityConsts.LOGGING_LEVEL_CODE_OFF)
			sb.append(" logging = ").append(SH.doubleQuote(loggingField.getOption(loggingField.getValue()).getName()));

		if (SH.is(onStartupScriptField.getValue()))
			sb.append(" onStartupScript = ").append(SH.doubleQuote(onStartupScriptField.getValue()));

		return sb.toString();
	}

	@Override
	public String preparePreUseClause() {
		StringBuilder sb = new StringBuilder("CREATE PROCEDURE ");
		if (SH.is(nameField.getValue()))
			sb.append(SH.doubleQuote(nameField.getValue()));
		else
			sb.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
		sb.append(" OFTYPE ").append(typeField.getOption(typeField.getValue()).getName());
		return sb.toString();
	}

	@Override
	public String exportToText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void importFromText(String text, StringBuilder sink) {
		// TODO Auto-generated method stub
		return;
	}

}
