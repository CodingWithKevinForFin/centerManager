package com.f1.ami.web.centermanager.editor;

import java.util.Map;

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

/**
 * 
 * CREATE INDEX [IF NOT EXISTS] idx_name <br>
 * ON tbl_name(col_name [HASH|SORT|SERIES] [,col_name [HASH|SORT|SERIES] ...]) <br>
 * [USE CONSTRAINT="[NONE|UNIQUE|PRIMARY]"] <br>
 *
 */
public class AmiCenterManagerAddIndexPortlet extends AmiCenterManagerAbstractAddObjectPortlet {
	public static final byte GROUP_CODE_INDEX = AmiCenterGraphNode.TYPE_INDEX;
	private AmiCenterEntityOptionField indexOnField;
	private AmiCenterEntityOptionField indexConstraintField;
	private AmiCenterEntityOptionField indexAutogenField = null;//only not null when constraint="primary"

	public AmiCenterManagerAddIndexPortlet(PortletConfig config) {
		super(config, GROUP_CODE_INDEX);
	}

	public AmiCenterManagerAddIndexPortlet(PortletConfig config, Map<String, String> objectConfig, byte mode) {
		super(config, GROUP_CODE_INDEX, objectConfig, mode);

	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		super.onFieldValueChanged(portlet, field, attributes);
		if (field == this.indexConstraintField.getInner()) {
			if ((short) this.indexConstraintField.getInner().getValue() == AmiCenterEntityConsts.INDEX_CONSTRAINT_TYPE_CODE_PRIMARY) {
				indexAutogenField = new AmiCenterEntityOptionField(this, new FormPortletSelectField(short.class, "AUTOGEN"), false, false, "AUTOGEN", "AUTOGEN", GROUP_CODE_INDEX);
				indexAutogenField.addOption(AmiCenterEntityConsts.AUTOGEN_TYPE_CODE_NONE, AmiCenterEntityConsts.AUTOGEN_TYPE_NONE);
				indexAutogenField.addOption(AmiCenterEntityConsts.AUTOGEN_TYPE_CODE_RAND, AmiCenterEntityConsts.AUTOGEN_TYPE_RAND);
				indexAutogenField.addOption(AmiCenterEntityConsts.AUTOGEN_TYPE_CODE_INC, AmiCenterEntityConsts.AUTOGEN_TYPE_INC);
				indexAutogenField.setHelp("Primary indexes can also be automatically generated on a particular column using AUTOGEN, where two options are available:<br>"
						+ "<ul><li>(1). <b style=\"color:blue\"><i>RAND</i></b>:A random UID is assigned to the column with a unique value for each row.<br></li>"
						+ "<li>(2). <b style=\"color:blue\"><i>INC</i></b>:An auto-incrementing UID is assigned to the column with a unique value for each row, starting at 0 for the first row, 1 for the second row and etc. Note that this option is only supported for <b><i>INT</i></b> and <b><i>LONG</i></b> columns.</li></ul>");
				this.configFields.add(this.indexAutogenField);
			} else if (this.indexAutogenField != null && this.hasField(this.indexAutogenField.getInner())) {
				this.removeField(this.indexAutogenField.getInner());
				this.configFields.remove(this.indexAutogenField);
			}
		}

	}

	@Override
	public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {
		super.onButtonPressed(portlet, button);
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
		return GROUP_CODE_INDEX;
	}

	@Override
	public void readFromConfig(Map config) {
		String indexOn = (String) config.get("indexOn");
		String indexConfig = (String) config.get("indexConfig");
		String indexConstraint = (String) config.get("CONSRAINT");
		this.indexOnField.setValue(indexOn);
		//this.indexConfigField.setValue(indexConfig);
		this.indexConstraintField.setValue(indexConstraint);
		valueCache.put(this.indexOnField, indexOn);
		//valueCache.put(this.indexConfigField, indexConfig);
		for (AmiCenterEntityOptionField of : this.configFields) {
			String key = of.getId();
			String value = (String) config.get(key);
			if (SH.is(value)) {
				FormPortletField<?> fpf = of.getInner();
				Class<?> clzz = fpf.getType();
				if (clzz == short.class) {
					FormPortletSelectField<Short> selectField = (FormPortletSelectField<Short>) fpf;
					short loggingCode = AmiCenterManagerUtils.toIndexConstraintCode(value);
					selectField.setValue(loggingCode);
				}
			}
		}

	}

	@Override
	public boolean validateScript(String script) {
		return super.validateScript(script) && validateFields();
	}

	@Override
	public boolean validateFields() {
		//TODO:
		return true;
	}

	@Override
	public void initTemplate() {
		this.nameField.setHelp("Name of the index, each index's name must be unique for the table");
		this.indexOnField = new AmiCenterEntityOptionField(this, new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("ON")), true, false, "indexOn", "ON",
				GROUP_CODE_INDEX);
		this.indexOnField.setHelp("Name of the table to add the index to");
		//this.indexConfigField = new AmiCenterEntityOptionField(this, new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("Index Configuration")), true, false,
		//"indexConfig", "Index Configuration", GROUP_CODE_INDEX);
		this.indexConstraintField = new AmiCenterEntityOptionField(this, new FormPortletSelectField(short.class, "CONSTRAINT"), false, false, "CONSTRAINT", "CONSTRAINT",
				GROUP_CODE_INDEX);
		this.indexConstraintField.addOption(AmiCenterEntityConsts.INDEX_CONSTRAINT_TYPE_CODE_NONE, AmiCenterEntityConsts.INDEX_CONSTRAINT_TYPE_NONE);
		this.indexConstraintField.addOption(AmiCenterEntityConsts.INDEX_CONSTRAINT_TYPE_CODE_UNIQUE, AmiCenterEntityConsts.INDEX_CONSTRAINT_TYPE_UNIQUE);
		this.indexConstraintField.addOption(AmiCenterEntityConsts.INDEX_CONSTRAINT_TYPE_CODE_PRIMARY, AmiCenterEntityConsts.INDEX_CONSTRAINT_TYPE_PRIMARY);
		this.indexConstraintField
				.setHelp("Constraints can be added to an index to determine the outcome of a key collision. Three different types of constraints are supported:<br>"
						+ "<ul><li>(1). <b style=\"color:blue\"><i>NONE</i></b> (default): If a constraint is not supplied, this is the default. There is no restriction on having multiple rows with the same key.<br></li>"
						+ "<li>(2). <b style=\"color:blue\"><i>UNIQUE</i></b>: An attempt to insert (or update) a row such that two rows in the table will have the same key will fail.<br></li>"
						+ "<li>(3). <b style=\"color:blue\"><i>PRIMARY</i></b>: An attempt to insert a row with the same key as an existing row will cause the existing row to be updated instead of a new row being inserted (specifically, those cells specified and not participating in the index will be updated). This can be thought of as an \"UPSERT\" in other popular databases. An attempt to update a row such that two rows in the table will have the same key will fail. Each table can have at most one PRIMARY index.</li></ul>");
		this.schemaFields.add(this.indexOnField);
		//this.schemaFields.add(this.indexConfigField);
		this.configFields.add(this.indexConstraintField);

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

}
