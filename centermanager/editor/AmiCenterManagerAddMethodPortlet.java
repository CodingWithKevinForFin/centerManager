package com.f1.ami.web.centermanager.editor;

import java.util.Map;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.ami.web.graph.AmiCenterGraphNode;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.ConfirmDialog;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletTextAreaField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.SH;

/**
 * CREATE METHOD return_type method_name (data_type arg1, data_type arg2) <br>
 * { //code goes here; return result; };
 *
 * -schemaFields:<br>
 * this.schemaFields.add(nameField);<br>
 * this.schemaFields.add(methodReturnTypeField);<br>
 * this.schemaFields.add(methodArgumentField);<br>
 * 
 * -configFields:<br>
 * this.configFields.add(methodImplField);<br>
 */
public class AmiCenterManagerAddMethodPortlet extends AmiCenterManagerAbstractAddObjectPortlet {
	public static final byte GROUP_CODE_METHOD = AmiCenterGraphNode.TYPE_METHOD;
	//for methods, [name,returntype,args,impl]
	private AmiCenterEntityOptionField methodReturnTypeField;
	private AmiCenterEntityOptionField methodArgumentField;
	private AmiCenterEntityOptionField methodImplField;

	public AmiCenterManagerAddMethodPortlet(PortletConfig config) {
		super(config, GROUP_CODE_METHOD);
		// TODO Auto-generated constructor stub
	}

	public AmiCenterManagerAddMethodPortlet(PortletConfig config, Map<String, String> objectConfig, byte mode) {
		super(config, GROUP_CODE_METHOD, objectConfig, mode);
		// TODO Auto-generated constructor stub
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
		return GROUP_CODE_METHOD;
	}

	@Override
	public void readFromConfig(Map config) {
		String name = (String) config.get("methodName");
		String args = (String) config.get("methodArguments");
		String returnType = (String) config.get("methodReturnType");
		String code = (String) config.get("methodImplementation");
		this.nameField.setValue(name);//ok
		this.methodArgumentField.setValue(args);
		this.methodReturnTypeField.setValue(returnType);//ok
		this.methodImplField.setValue(code);
		//valueCache.put(this.nameField, name);
		valueCache.put(this.methodArgumentField, args);
		valueCache.put(this.methodReturnTypeField, returnType);
		valueCache.put(this.methodImplField, code);
	}

	@Override
	public boolean validateFields() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initTemplate() {
		methodReturnTypeField = new AmiCenterEntityOptionField(this, new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("Return Type")), true, false,
				"methodReturnType", "Return Type", GROUP_CODE_METHOD);
		methodArgumentField = new AmiCenterEntityOptionField(this, new FormPortletTextAreaField(AmiCenterManagerUtils.formatRequiredField("methodArgument")), true, false,
				"methodArgument", "methodArgument", GROUP_CODE_METHOD);
		methodArgumentField.setHelp("In the format of <b style=\"color:blue\"><i>(data_type arg1, data_type arg2,...)</b></i>");
		methodImplField = new AmiCenterEntityOptionField(this,
				new AmiWebFormPortletAmiScriptField(AmiCenterManagerUtils.formatRequiredField("Implementation(Code Block)"), getManager(), ""), true, false, "methodImplementation",
				"Implementation(Code Block)", GROUP_CODE_METHOD);
		methodImplField.getInner().setWidthPct(0.60);
		methodImplField.getInner().setHeightPct(0.25);
		methodImplField.setHelp(" {<br>" + "&nbsp&nbsp&nbsp//code goes here;<br>" + "&nbsp&nbsp&nbspreturn ...;<br>" + "};");
		this.schemaFields.add(methodReturnTypeField);
		this.schemaFields.add(methodArgumentField);
		this.configFields.add(methodImplField);

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
	public void sendQueryToBackend(String query) {
		// TODO Auto-generated method stub

	}

	@Override
	public String previewScript() {
		//there is no use clause for methods
		return preparePreUseClause();
	}

	@Override
	public String preparePreUseClause() {
		StringBuilder script = new StringBuilder();
		script.append("CREATE METHOD ");
		//check return type
		String returnType = (String) this.methodReturnTypeField.getValue();
		if (SH.is(returnType))
			script.append(this.methodReturnTypeField.getValue()).append(" ");
		else
			script.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING).append(" ");

		//check name
		String name = (String) this.nameField.getValue();
		if (SH.is(name))
			script.append(name);
		else
			script.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		String args = SH.trim((String) this.methodArgumentField.getValue());
		//checking "()"
		if (SH.is(args)) {
			boolean hasParenthesis = false;
			if (args.charAt(0) == '(' && args.charAt(args.length() - 1) == ')')
				hasParenthesis = true;
			if (hasParenthesis)
				script.append(this.methodArgumentField.getValue());
			else
				script.append('(').append(this.methodArgumentField.getValue()).append(')');
		} else
			script.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		//checking "{}"
		String code = SH.trim((String) this.methodImplField.getValue());
		if (SH.is(code)) {
			boolean hasCurlybrace = false;
			if (code.charAt(0) == '{' && code.charAt(code.length() - 1) == '}')
				hasCurlybrace = true;
			if (hasCurlybrace)
				script.append(this.methodImplField.getValue());
			else
				script.append('{').append(this.methodImplField.getValue()).append('}');
		} else
			script.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		return script.toString();
	}

}
