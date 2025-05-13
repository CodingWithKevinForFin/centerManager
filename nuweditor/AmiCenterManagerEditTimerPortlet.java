package com.f1.ami.web.centermanager.nuweditor;

import java.util.Map;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanger.AmiCenterEntityConsts;
import com.f1.ami.web.centermanger.AmiCenterManagerUtils;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.TabPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.SH;

public class AmiCenterManagerEditTimerPortlet extends AmiCenterManagerAbstractEditCenterObjectPortlet {

	final private FormPortlet form;
	final private TabPortlet scriptTabs;
	final private FormPortletTextField nameField;
	final private FormPortletSelectField<Short> timerTypeField;
	final private FormPortletTextField timerOnField;
	final private FormPortletTextField timerPriorityField;
	final private FormPortletTextField timerTimeoutField;
	final private FormPortletTextField timerLimitField;
	final private FormPortletSelectField<Short> timerLoggingField;
	final private AmiWebFormPortletAmiScriptField timerVarsField;

	final private GridPortlet scriptGrid;
	final private FormPortlet scriptForm;

	final private GridPortlet onStartupScriptGrid;
	final private FormPortlet onStartupScriptForm;
	final private AmiWebFormPortletAmiScriptField timerScriptField;
	final private AmiWebFormPortletAmiScriptField timerOnStartupScriptField;

	public AmiCenterManagerEditTimerPortlet(PortletConfig config, boolean isAdd) {
		super(config, isAdd);
		this.form = new FormPortlet(generateConfig());

		//The top row
		this.nameField = this.form.addField(new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("Name")));
		this.nameField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		this.nameField.setWidth(NAME_WIDTH);
		this.nameField.setHeightPx(DEFAULT_ROWHEIGHT);
		this.nameField.setLeftPosPx(DEFAULT_LEFTPOS);
		this.nameField.setTopPosPx(DEFAULT_TOPPOS);

		this.timerTypeField = this.form.addField(new FormPortletSelectField<Short>(short.class, AmiCenterManagerUtils.formatRequiredField("Type")));
		this.timerTypeField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		//only one type for timer, so disable
		this.timerTypeField.setDisabled(true);
		this.timerTypeField.addOption(AmiCenterEntityConsts.TIMER_TYPE_CODE_AMISCRIPT, AmiCenterEntityConsts.TIMER_TYPE_AMISCRIPT);
		this.timerTypeField.setLeftPosPx(DEFAULT_LEFTPOS + NAME_WIDTH + DEFAULT_X_SPACING).setTopPosPx(DEFAULT_TOPPOS).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(100);

		this.timerOnField = this.form.addField(new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("ON")));
		this.timerOnField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		this.timerOnField.setHelp("either:<br>" + "<ul><li>(1). A positive number defining the period in milliseconds between timer executions.</li>"
				+ "  <li>(2). Empty string (\"\") to never run timer, useful for timers that should just run at startup, see <i style=\"color:blue\">onStartupScript</i></li>"
				+ "  <li>(3). Crontab style entry declaring the schedule of when the timer should be execute:</li></ul>");
		this.timerOnField.setWidthPx(PRIORITY_WIDTH);
		this.timerOnField.setHeightPx(DEFAULT_ROWHEIGHT);
		this.timerOnField.setLeftPosPx(DEFAULT_LEFTPOS + NAME_WIDTH + TYPE_WIDTH + 2 * DEFAULT_X_SPACING);
		this.timerOnField.setTopPosPx(DEFAULT_TOPPOS);

		this.timerPriorityField = this.form.addField(new FormPortletTextField("PRIORITY"));
		this.timerPriorityField.setHelp("a number, timers with lowest value are executed first. Only considered when two or more timers have the same exact scheduled time");
		this.timerPriorityField.setWidthPx(PRIORITY_WIDTH);
		this.timerPriorityField.setHeightPx(DEFAULT_ROWHEIGHT);
		this.timerPriorityField.setLeftPosPx(DEFAULT_LEFTPOS + NAME_WIDTH + TYPE_WIDTH + ON_WIDTH + 3 * DEFAULT_X_SPACING);
		this.timerPriorityField.setTopPosPx(DEFAULT_TOPPOS);
		this.timerPriorityField.setRightPosPx(80);

		//2nd row
		this.timerTimeoutField = this.form.addField(new FormPortletTextField("timeout"));
		this.timerTimeoutField.setHelp("Timeout in milliseconds, default is 100000 (100 seconds)");
		this.timerTimeoutField.setWidthPx(TIMEOUT_WIDTH);
		this.timerTimeoutField.setHeightPx(DEFAULT_ROWHEIGHT);
		this.timerTimeoutField.setLeftPosPx(DEFAULT_LEFTPOS);
		this.timerTimeoutField.setTopPosPx(DEFAULT_TOPPOS + (DEFAULT_ROWHEIGHT + DEFAULT_Y_SPACING) * 1);

		timerLimitField = this.form.addField(new FormPortletTextField("limit"));
		timerLimitField.setHelp("Row limit for queries, default is 10000");
		this.timerLimitField.setWidthPx(LIMIT_WIDTH);
		this.timerLimitField.setHeightPx(DEFAULT_ROWHEIGHT);
		this.timerLimitField.setLeftPosPx(DEFAULT_LEFTPOS + TIMEOUT_WIDTH + DEFAULT_X_SPACING);
		this.timerLimitField.setTopPosPx(DEFAULT_TOPPOS + (DEFAULT_ROWHEIGHT + DEFAULT_Y_SPACING) * 1);

		timerLoggingField = this.form.addField(new FormPortletSelectField(short.class, "logging"));
		timerLoggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_OFF, AmiCenterEntityConsts.LOGGING_LEVEL_OFF);
		timerLoggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_ON, AmiCenterEntityConsts.LOGGING_LEVEL_ON);
		timerLoggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_VERBOSE, AmiCenterEntityConsts.LOGGING_LEVEL_VERBOSE);
		timerLoggingField.setHelp("set the logging level when the timer gets called: The following Logging options are supported:<br>"
				+ "<ul><li>(1). <b style=\"color:blue\"><i>off</i></b> (default): no logging<br></li>"
				+ "<li>(2). <b style=\"color:blue\"><i>on</i></b>: logs the time when the timer is called and when it completes.<br></li>"
				+ "<li>(3). <b style=\"color:blue\"><i>verbose</i></b>: equivalent of using <b><i>show_plan=ON</i></b> in AMIDB. Logs the time that a timer starts and finishes and also each query step.</li></ul>");
		timerLoggingField.setWidthPx(LOGGING_WIDTH);
		timerLoggingField.setHeightPx(DEFAULT_ROWHEIGHT);
		timerLoggingField.setLeftPosPx(DEFAULT_LEFTPOS + TIMEOUT_WIDTH + LIMIT_WIDTH + DEFAULT_X_SPACING * 2);
		timerLoggingField.setTopPosPx(DEFAULT_TOPPOS + (DEFAULT_ROWHEIGHT + DEFAULT_Y_SPACING) * 1);

		//last row(3rd row)
		timerVarsField = this.form.addField(new AmiWebFormPortletAmiScriptField("vars", getManager(), ""));
		timerVarsField.setHelp("Variables shared by the timer, a comma delimited list of type varname");
		timerVarsField.setWidthPx(VARS_WIDTH);
		timerVarsField.setHeightPx(DEFAULT_ROWHEIGHT - 5);
		timerVarsField.setLeftPosPx(DEFAULT_LEFTPOS);
		timerVarsField.setTopPosPx(DEFAULT_TOPPOS + (DEFAULT_ROWHEIGHT + DEFAULT_Y_SPACING) * 2);

		//script tab
		onStartupScriptGrid = new GridPortlet(generateConfig());
		onStartupScriptForm = new FormPortlet(generateConfig());
		onStartupScriptGrid.addChild(onStartupScriptForm);
		timerOnStartupScriptField = onStartupScriptForm.addField(new AmiWebFormPortletAmiScriptField("", getManager(), ""));
		timerOnStartupScriptField.setHelp("AmiScript to run when the timer is created");
		timerOnStartupScriptField.setLeftPosPx(AMISCRIPT_FORM_PADDING).setRightPosPx(AMISCRIPT_FORM_PADDING).setBottomPosPx(AMISCRIPT_FORM_PADDING);
		timerOnStartupScriptField.setWidthPx(400);
		timerOnStartupScriptField.setHeightPx(AMISCRIPT_FORM_HEIGHT);
		timerOnStartupScriptField.setTopPosPx(DEFAULT_TOPPOS);

		scriptGrid = new GridPortlet(generateConfig());
		scriptForm = new FormPortlet(generateConfig());
		scriptGrid.addChild(scriptForm);
		timerScriptField = scriptForm.addField(new AmiWebFormPortletAmiScriptField("", getManager(), ""));
		timerScriptField.setHelp("AmiScript to run when timer is executed");
		timerScriptField.setLeftPosPx(AMISCRIPT_FORM_PADDING).setRightPosPx(AMISCRIPT_FORM_PADDING).setBottomPosPx(AMISCRIPT_FORM_PADDING);
		timerScriptField.setWidthPx(400);
		timerScriptField.setHeightPx(AMISCRIPT_FORM_HEIGHT);
		timerScriptField.setTopPosPx(DEFAULT_TOPPOS);

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
		if (SH.is(timerScriptField.getValue()))
			sb.append(" script= ").append(SH.doubleQuote(timerScriptField.getValue()));
		else
			sb.append(" script= ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (timerLoggingField.getValue() != AmiCenterEntityConsts.LOGGING_LEVEL_CODE_OFF)
			sb.append(" logging = ").append(SH.doubleQuote(timerLoggingField.getOption(timerLoggingField.getValue()).getName()));
		if (SH.is(timerVarsField.getValue()))
			sb.append(" vars = ").append(SH.doubleQuote(timerVarsField.getValue()));
		if (SH.is(timerOnStartupScriptField.getValue()))
			sb.append(" onStartupScript = ").append(SH.doubleQuote(timerOnStartupScriptField.getValue()));
		if (SH.is(timerTimeoutField.getValue()))
			sb.append(" timeout = ").append(SH.doubleQuote(timerTimeoutField.getValue()));
		if (SH.is(timerLimitField.getValue()))
			sb.append(" limit = ").append(SH.doubleQuote(timerLimitField.getValue()));
		return sb.toString();
	}

	@Override
	public String preparePreUseClause() {
		StringBuilder sb = new StringBuilder("CREATE TIMER ");
		if (SH.is(nameField.getValue()))
			sb.append(nameField.getValue());
		else
			sb.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
		sb.append(" OFTYPE ").append(timerTypeField.getOption(timerTypeField.getValue()).getName());
		sb.append(" ON ").append(SH.doubleQuote(timerOnField.getValue()));
		if (SH.is(timerPriorityField.getValue()))
			sb.append(" PRIORITY ").append(timerPriorityField.getValue());
		return sb.toString();
	}

}
