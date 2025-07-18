package com.f1.ami.web.centermanager.editor;

import java.util.Map;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.ConfirmDialog;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletButton;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.SH;
import com.f1.utils.casters.Caster_Boolean;

//create timer mytimer oftype amiscript on "100" use script="...";
public class AmiCenterManagerAddTimerPortlet extends AmiCenterManagerAbstractAddObjectPortlet {

	private AmiCenterEntityOptionField timerTypeField;
	private AmiCenterEntityOptionField timerOnField;
	private AmiCenterEntityOptionField timerPriorityField;
	private AmiCenterEntityOptionField timerScriptField;

	private AmiCenterEntityOptionField timerLoggingField;
	private AmiCenterEntityOptionField timerVarsField;
	private AmiCenterEntityOptionField timerOnStartupScriptField;
	private AmiCenterEntityOptionField timerTimeoutField;
	private AmiCenterEntityOptionField timerLimitField;

	public static byte GROUP_CODE_TIMER = AmiCenterGraphNode.TYPE_TIMER;

	public AmiCenterManagerAddTimerPortlet(PortletConfig config) {
		super(config, GROUP_CODE_TIMER);
	}

	public AmiCenterManagerAddTimerPortlet(PortletConfig config, Map<String, String> objectConfig, byte mode) {
		super(config, GROUP_CODE_TIMER, objectConfig, mode);
	}

	@Override
	public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {
		super.onButtonPressed(portlet, button);
	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		super.onFieldValueChanged(portlet, field, attributes);

	}

	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onButton(ConfirmDialog source, String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doSearch() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSearchNext() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSearchPrevious() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean validateScript(String script) {
		return super.validateScript(script) && validateFields();
	}

	//only checking for compile timer errors
	@Override
	public boolean validateFields() {
		//TODO:
		return true;
	}

	@Override
	public void sendQueryToBackend(String query) {

	}

	@Override
	public void readFromConfig(Map config) {
		String timerType = (String) config.get("timerType");
		String timerOn = (String) config.get("timerOn");
		String timerName = (String) config.get("timerName");
		String timerPriority = (String) config.get("timerPriority");
		timerTypeField.setValue(AmiCenterManagerUtils.centerObjectTypeToCode(AmiCenterGraphNode.TYPE_TIMER, timerType));
		timerOnField.setValue(timerOn);
		nameField.setValue(timerName);
		timerPriorityField.setValue(timerPriority);
		//cache the common property values
		valueCache.put(timerTypeField, timerType);
		valueCache.put(timerOnField, timerOn);
		//valueCache.put(nameField, timerName);
		valueCache.put(timerPriorityField, timerPriority);
		//loop over each field and fill in the value from config
		for (AmiCenterEntityOptionField of : this.configFields) {
			String key = of.getId();
			String value = (String) config.get(key);
			if (SH.is(value)) {
				FormPortletField<?> fpf = of.getInner();
				Class<?> clzz = fpf.getType();
				if (clzz == String.class) {
					FormPortletField<String> stringField = (FormPortletField<String>) fpf;
					stringField.setValue(value);
				} else if (clzz == Boolean.class) {
					FormPortletField<Boolean> stringField = (FormPortletField<Boolean>) fpf;
					stringField.setValue(Caster_Boolean.INSTANCE.cast(value));
				} else if (clzz == short.class) {
					FormPortletSelectField<Short> selectField = (FormPortletSelectField<Short>) fpf;
					short loggingCode = AmiCenterManagerUtils.toLoggingTypeCode(value);
					selectField.setValue(loggingCode);
				}
				//stores the last value for all non empty fields
				valueCache.put(of, value);
			}
		}
		//**post config read
		this.nameField.setDisabled(false);
		this.timerOnField.setDisabled(false);
		//disallow the user to change trigger type(too complicated)
		this.timerTypeField.setDisabled(true);

	}

	@Override
	public void initTemplate() {
		nameField.setHelp("name of the timer to create, each timer's name must be unique within the database");
		timerTypeField = new AmiCenterEntityOptionField(this, new FormPortletSelectField(short.class, AmiCenterManagerUtils.formatRequiredField("Timer Type")), true, false,
				"timerType", "Timer Type", GROUP_CODE_TIMER);
		//timerTypeField.addOption(AmiCenterEntityTypeConsts.ENTITY_TYPE_CODE_NULL, AmiCenterEntityTypeConsts.ENTITY_TYPE_NULL);
		timerTypeField.addOption(AmiCenterEntityConsts.TIMER_TYPE_CODE_AMISCRIPT, AmiCenterEntityConsts.TIMER_TYPE_AMISCRIPT);

		timerOnField = new AmiCenterEntityOptionField(this, new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("ON")), true, false, "timerOn", "ON",
				GROUP_CODE_TIMER);
		timerOnField.setHelp("either:<br>" + "<ul><li>(1). A positive number defining the period in milliseconds between timer executions.</li>"
				+ "  <li>(2). Empty string (\"\") to never run timer, useful for timers that should just run at startup, see <i style=\"color:blue\">onStartupScript</i></li>"
				+ "  <li>(3). Crontab style entry declaring the schedule of when the timer should be execute:</li></ul>");

		timerPriorityField = new AmiCenterEntityOptionField(this, new FormPortletTextField("PRIORITY"), false, false, "timerPriority", "PRIORITY", GROUP_CODE_TIMER);
		timerPriorityField.setHelp("a number, timers with lowest value are executed first. Only considered when two or more timers have the same exact scheduled time");

		timerScriptField = new AmiCenterEntityOptionField(this, new AmiWebFormPortletAmiScriptField(AmiCenterManagerUtils.formatRequiredField("script"), getManager(), ""), true,
				false, "script", "script", GROUP_CODE_TIMER);
		timerScriptField.getInner().setWidthPct(0.60);
		timerScriptField.getInner().setHeightPct(0.25);
		timerScriptField.setHelp("AmiScript to run when timer is executed");

		timerLoggingField = new AmiCenterEntityOptionField(this, new FormPortletSelectField(short.class, "logging"), false, false, "logging", "logging", GROUP_CODE_TIMER);
		timerLoggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_OFF, AmiCenterEntityConsts.LOGGING_LEVEL_OFF);
		timerLoggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_ON, AmiCenterEntityConsts.LOGGING_LEVEL_ON);
		timerLoggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_VERBOSE, AmiCenterEntityConsts.LOGGING_LEVEL_VERBOSE);
		timerLoggingField.setHelp("set the logging level when the timer gets called: The following Logging options are supported:<br>"
				+ "<ul><li>(1). <b style=\"color:blue\"><i>off</i></b> (default): no logging<br></li>"
				+ "<li>(2). <b style=\"color:blue\"><i>on</i></b>: logs the time when the timer is called and when it completes.<br></li>"
				+ "<li>(3). <b style=\"color:blue\"><i>verbose</i></b>: equivalent of using <b><i>show_plan=ON</i></b> in AMIDB. Logs the time that a timer starts and finishes and also each query step.</li></ul>");

		timerVarsField = new AmiCenterEntityOptionField(this, new FormPortletTextField("vars"), false, false, "vars", "vars", GROUP_CODE_TIMER);
		timerVarsField.setHelp("Variables shared by the timer, a comma delimited list of type varname");

		timerOnStartupScriptField = new AmiCenterEntityOptionField(this, new AmiWebFormPortletAmiScriptField("onStartupScript", getManager(), ""), false, false, "onStartupScript",
				"onStartupScript", GROUP_CODE_TIMER);
		timerOnStartupScriptField.getInner().setWidthPct(0.60);
		timerOnStartupScriptField.getInner().setHeightPct(0.25);
		timerOnStartupScriptField.setHelp("AmiScript to run when the timer is created");

		timerTimeoutField = new AmiCenterEntityOptionField(this, new FormPortletTextField("timeout"), false, false, "timeout", "timeout", GROUP_CODE_TIMER);
		timerTimeoutField.setHelp("Timeout in milliseconds, default is 100000 (100 seconds)");

		timerLimitField = new AmiCenterEntityOptionField(this, new FormPortletTextField("limit"), false, false, "limit", "limit", GROUP_CODE_TIMER);
		timerLimitField.setHelp("Row limit for queries, default is 10000");

		this.schemaFields.add(timerTypeField);
		this.schemaFields.add(timerOnField);
		this.schemaFields.add(timerPriorityField);
		this.configFields.add(timerScriptField);
		this.configFields.add(timerLoggingField);
		this.configFields.add(timerVarsField);
		this.configFields.add(timerOnStartupScriptField);
		this.configFields.add(timerTimeoutField);
		this.configFields.add(timerLimitField);

	}

	@Override
	public void resetTemplate() {
		// TODO Auto-generated method stub
	}

	@Override
	public void submitScript() {
		// TODO Auto-generated method stub

	}

	@Override
	public void diffScript() {
		// TODO Auto-generated method stub

	}

	@Override
	public short getGroupCode() {
		return GROUP_CODE_TIMER;
	}

}
