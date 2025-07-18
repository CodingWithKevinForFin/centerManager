package com.f1.ami.web.centermanager.editor;

import java.util.Map;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.ConfirmDialog;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.SH;
import com.f1.utils.casters.Caster_Boolean;

public class AmiCenterManagerAddProcedurePortlet extends AmiCenterManagerAbstractAddObjectPortlet {
	public static final byte GROUP_CODE_PROCEDURE = AmiCenterGraphNode.TYPE_PROCEDURE;
	private AmiCenterEntityOptionField procedureTypeField;
	private AmiCenterEntityOptionField procedureArgumentsField;
	private AmiCenterEntityOptionField procedureScriptField;
	private AmiCenterEntityOptionField procedureOnStartupScriptField;
	private AmiCenterEntityOptionField procedureLoggingField;

	public AmiCenterManagerAddProcedurePortlet(PortletConfig config) {
		super(config, GROUP_CODE_PROCEDURE);
	}

	public AmiCenterManagerAddProcedurePortlet(PortletConfig config, Map<String, String> objectConfig, byte mode) {
		super(config, GROUP_CODE_PROCEDURE, objectConfig, mode);

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
	public short getGroupCode() {
		return GROUP_CODE_PROCEDURE;
	}

	@Override
	public void readFromConfig(Map config) {
		String procedureType = (String) config.get("procedureType");
		String procedureName = (String) config.get("procedureName");
		procedureTypeField.setValue(AmiCenterManagerUtils.centerObjectTypeToCode(AmiCenterGraphNode.TYPE_PROCEDURE, procedureType));
		nameField.setValue(procedureName);
		//cache the common property values
		valueCache.put(procedureTypeField, procedureType);
		//valueCache.put(nameField, procedureName);
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
		//disallow the user to change trigger type(too complicated)
		this.procedureTypeField.setDisabled(true);

	}

	@Override
	public boolean validateFields() {
		// TODO; add procedure specific field checking
		return true;
	}

	@Override
	public void initTemplate() {
		FormPortletSelectField procTypeField = new FormPortletSelectField(short.class, AmiCenterManagerUtils.formatRequiredField("Procedure Type"));
		procTypeField.setHelp("OFTYPE");
		nameField.setHelp("name of the procedure to be created, each procedure's name must be unique within the database");
		procedureTypeField = new AmiCenterEntityOptionField(this, procTypeField, true, false, "procedureType", "Procedure Type", GROUP_CODE_PROCEDURE);
		procedureTypeField.addOption(AmiCenterEntityConsts.TIMER_TYPE_CODE_AMISCRIPT, AmiCenterEntityConsts.PROCEDURE_TYPE_AMISCRIPT);
		FormPortletTextField argumentsField = new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("Arguments"));
		argumentsField.setHelp("arguments=\"type varname, ...\"");
		procedureArgumentsField = new AmiCenterEntityOptionField(this, argumentsField, true, false, "arguments", "arguments", GROUP_CODE_PROCEDURE);
		procedureArgumentsField.setHelp("In the form of <b style=\"color:blue\"><i>type name, type name ...</b></i> ");

		procedureScriptField = new AmiCenterEntityOptionField(this, new AmiWebFormPortletAmiScriptField(AmiCenterManagerUtils.formatRequiredField("script"), getManager(), ""),
				true, false, "script", "script", GROUP_CODE_PROCEDURE);
		procedureScriptField.getInner().setWidthPct(0.60);
		procedureScriptField.getInner().setHeightPct(0.25);
		procedureScriptField.setHelp("AmiScript to run when procedure is called");

		procedureLoggingField = new AmiCenterEntityOptionField(this, new FormPortletSelectField(short.class, "logging"), false, false, "logging", "logging", GROUP_CODE_PROCEDURE);
		procedureLoggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_OFF, AmiCenterEntityConsts.LOGGING_LEVEL_OFF);
		procedureLoggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_ON, AmiCenterEntityConsts.LOGGING_LEVEL_ON);
		procedureLoggingField.addOption(AmiCenterEntityConsts.LOGGING_LEVEL_CODE_VERBOSE, AmiCenterEntityConsts.LOGGING_LEVEL_VERBOSE);
		procedureLoggingField.setHelp("set the logging level when the timer gets called: The following Logging options are supported:<br>"
				+ "<ul><li>(1). <b style=\"color:blue\"><i>off</i></b> (default): no logging<br></li>"
				+ "<li>(2). <b style=\"color:blue\"><i>on</i></b>: logs the time when the timer is called and when it completes.<br></li>"
				+ "<li>(3). <b style=\"color:blue\"><i>verbose</i></b>: equivalent of using <b><i>show_plan=ON</i></b> in AMIDB. Logs the time that a timer starts and finishes and also each query step.</li></ul>");

		procedureOnStartupScriptField = new AmiCenterEntityOptionField(this, new AmiWebFormPortletAmiScriptField("onStartupScript", getManager(), ""), false, false,
				"onStartupScript", "onStartupScript", GROUP_CODE_PROCEDURE);
		procedureOnStartupScriptField.getInner().setWidthPct(0.60);
		procedureOnStartupScriptField.getInner().setHeightPct(0.25);
		procedureOnStartupScriptField.setHelp("AmiScript to run When the procedure is created");

		this.schemaFields.add(procedureTypeField);
		this.configFields.add(procedureArgumentsField);
		this.configFields.add(procedureScriptField);
		this.configFields.add(procedureLoggingField);
		this.configFields.add(procedureOnStartupScriptField);
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
	public boolean validateScript(String script) {
		return super.validateScript(script) && validateFields();
	}
	@Override
	public void sendQueryToBackend(String query) {
		// TODO Auto-generated method stub

	}

}
