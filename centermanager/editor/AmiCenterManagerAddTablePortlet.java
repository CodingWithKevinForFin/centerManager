package com.f1.ami.web.centermanager.editor;

import java.util.List;
import java.util.Map;

import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.ami.web.graph.AmiCenterGraphNode;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.ConfirmDialog;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextAreaField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.CH;
import com.f1.utils.SH;
import com.f1.utils.casters.Caster_Boolean;

/**
 * CREATE PUBLIC TABLE [IF NOT EXISTS] tblname (col1 column_type [, col2 column_type ...]) <br>
 * [USE [PersistEngine="[FAST|TEXT|custom]"]] <br>
 * [PersistOptions="custom_options"] <br>
 * [Broadcast="[true|false]"] <br>
 * [RefreshPeriodMs="duration_millis"] <br>
 * [OnUndefColumn="REJECT|IGNORE|ADD"] <br>
 * [InitialCapacity="number_of_rows_min_1"] <br>
 * 
 *
 */
public class AmiCenterManagerAddTablePortlet extends AmiCenterManagerAbstractAddObjectPortlet {
	public static final byte GROUP_CODE_TABLE = AmiCenterGraphNode.TYPE_TABLE;

	private AmiCenterEntityOptionField tableSchemaField;
	private AmiCenterEntityOptionField tablePersistEngineIsCustomField;
	private AmiCenterEntityOptionField tableCustomPersistOptionField;
	private AmiCenterEntityOptionField tablePersistEngineField;
	private AmiCenterEntityOptionField tableBroadCastField;
	private AmiCenterEntityOptionField tableRefreshPeriodMsField;
	private AmiCenterEntityOptionField tableOnUndefColumnField;
	private AmiCenterEntityOptionField tableInitialCapacityField;

	public AmiCenterManagerAddTablePortlet(PortletConfig config) {
		super(config, GROUP_CODE_TABLE);
	}

	public AmiCenterManagerAddTablePortlet(PortletConfig config, Map<String, String> objectConfig, byte mode) {
		super(config, GROUP_CODE_TABLE, objectConfig, mode);
		this.tablePersistEngineIsCustomField.setDisabled(true);

	}

	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		if (field == this.tablePersistEngineIsCustomField.getInner()) {
			if ((boolean) this.tablePersistEngineIsCustomField.getValue()) {
				//remove the old peristengnine field
				this.removeField(this.tablePersistEngineField.getInner());

				this.tablePersistEngineField = new AmiCenterEntityOptionField(this, new FormPortletTextField("PersistEngine"), false, false, "PersistEngine", "PersistEngine",
						GROUP_CODE_TABLE, false);
				this.addFieldAfter(this.tablePersistEngineIsCustomField.getInner(), this.tablePersistEngineField.getInner());
				this.tableCustomPersistOptionField = new AmiCenterEntityOptionField(this, new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("PersistOptions")),
						true, false, "PersistOptions", "PersistOptions", GROUP_CODE_TABLE, false);
				this.addFieldBefore(this.tableBroadCastField.getInner(), this.tableCustomPersistOptionField.getInner());
			} else {
				this.removeField(this.tablePersistEngineField.getInner());
				this.removeField(this.tableCustomPersistOptionField.getInner());
				this.tablePersistEngineField = new AmiCenterEntityOptionField(this, new FormPortletSelectField(short.class, "PersistEngine"), false, false, "PersistEngine",
						"PersistEngine", GROUP_CODE_TABLE, false);
				this.tablePersistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_NONE, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_NONE);
				this.tablePersistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_FAST, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_FAST);
				this.tablePersistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_HISTORICAL, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_HISTORICAL);
				this.tablePersistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_TEXT, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_TEXT);
				this.addFieldBefore(this.tableBroadCastField.getInner(), this.tablePersistEngineField.getInner());
			}
		} else if (field == this.tablePersistEngineField.getInner()) {
			//remove all non-hdb option fields
			if ((short) this.tablePersistEngineField.getValue() == AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_HISTORICAL) {
				for (AmiCenterEntityOptionField f : getNonHDBConfigFields())
					this.removeField(f.getInner());
			} else {
				for (AmiCenterEntityOptionField f : getNonHDBConfigFields()) {
					if (!this.hasField(f.getInner()))
						this.addField(f.getInner());
				}

			}

		}
		super.onFieldValueChanged(portlet, field, attributes);
	}

	private List<AmiCenterEntityOptionField> getNonHDBConfigFields() {
		return CH.l(this.tableBroadCastField, this.tableRefreshPeriodMsField, this.tableOnUndefColumnField, this.tableInitialCapacityField);
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
		return GROUP_CODE_TABLE;
	}

	@Override
	public void readFromConfig(Map config) {
		String tableName = (String) config.get("name");
		String tableSchema = (String) config.get("tableSchema");
		String isCustom = (String) config.get("custom");
		this.nameField.setValue(tableName);
		this.tableSchemaField.setValue(tableSchema);
		//valueCache.put(this.nameField, tableName);
		valueCache.put(this.tableSchemaField, tableSchema);
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
					short code = -1;
					if ("OnUndefColumn".equals(key))
						code = AmiCenterManagerUtils.toUnDefColumnCode(value);
					else if ("PersistEngine".equals(key))
						code = AmiCenterManagerUtils.toTablePersistEngineCode(value);
					selectField.setValue(code);
				}
				//stores the last value for all non empty fields
				valueCache.put(of, value);
			}
		}

	}

	@Override
	public boolean validateFields() {
		//TODO:
		return true;
	}

	@Override
	public void initTemplate() {
		this.nameField.setHelp("String name of the table to create");
		this.tableSchemaField = new AmiCenterEntityOptionField(this, new FormPortletTextAreaField(AmiCenterManagerUtils.formatRequiredField("Schema")), true, false, "tableSchema",
				"Schema", GROUP_CODE_TABLE);
		this.tableSchemaField.getInner().setWidthPct(0.7d);
		this.tableSchemaField.getInner().setHeightPct(0.3d);
		this.tableSchemaField.setHelp("In the format of <b style=\"color:blue\"><i>(col1 column_type,col2 column_type,...)</b></i>");
		this.tablePersistEngineIsCustomField = new AmiCenterEntityOptionField(this, new FormPortletCheckboxField("Custom"), false, false, "custom", "custom", GROUP_CODE_TABLE);
		((FormPortletCheckboxField) (this.tablePersistEngineIsCustomField.getInner())).setDefaultValue(false);
		this.tablePersistEngineIsCustomField.setHelp("When enabled, a custom user-designed persist engine will be used");

		this.tablePersistEngineField = new AmiCenterEntityOptionField(this, new FormPortletSelectField(short.class, "PersistEngine"), false, false, "PersistEngine",
				"PersistEngine", GROUP_CODE_TABLE);
		this.tablePersistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_NONE, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_NONE);
		this.tablePersistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_FAST, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_FAST);
		this.tablePersistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_HISTORICAL, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_HISTORICAL);
		this.tablePersistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_TEXT, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_TEXT);
		this.tablePersistEngineField.setHelp(
				"The persist engine takes the following inputs:<br> <ul><ui>(1). <b style=\"color:blue\"><i>\"FAST\"</i></b>: The table will be persisted to disk using a fast, binary protocol.<br>"
						+ "<ui>(2). <b style=\"color:blue\"><i>\"TEXT\"</b></i>: The table will be persisted to disk using a slow, but easy to read text file</ui><br>"
						+ "<ui>(3). <b style=\"color:blue\"><i>\"HISTORICAL\"</b></i>: The table will be persisted as a historical table using a disk-based historical engine. Optimized for storing large volumes of data and querying on demand.</ui><ul>"

		);

		this.tableBroadCastField = new AmiCenterEntityOptionField(this, new FormPortletCheckboxField("BroadCast"), false, false, "BroadCast", "BroadCast", GROUP_CODE_TABLE);
		((FormPortletCheckboxField) (this.tableBroadCastField.getInner())).setDefaultValue(true);
		this.tableBroadCastField.setHelp("Broadcast=\"true\": Front end visualizations & external listeners will be notified as data is updated in the table.");

		this.tableRefreshPeriodMsField = new AmiCenterEntityOptionField(this, new FormPortletTextField("RefreshPeriodMs"), false, false, "RefreshPeriodMs", "RefreshPeriodMs",
				GROUP_CODE_TABLE);
		this.tableRefreshPeriodMsField
				.setHelp("RefreshPeriodMs=\"*duration_millis*\": The period that the table will conflate and broadcast changes to front end at. For example:<br>"
						+ "if a cells' value changes 10 times in one second and the refresh period is 500ms, then only ~2 updates will be broadcast out (the other 8 will be conflated).");

		this.tableOnUndefColumnField = new AmiCenterEntityOptionField(this, new FormPortletSelectField(short.class, "OnUndefColumn"), false, false, "OnUndefColumn",
				"OnUndefColumn", GROUP_CODE_TABLE);
		this.tableOnUndefColumnField.addOption(AmiCenterEntityConsts.ON_UNDEF_COLUMN_OPTION_CODE_REJECT, AmiCenterEntityConsts.ON_UNDEF_COLUMN_OPTION_REJECT);
		this.tableOnUndefColumnField.addOption(AmiCenterEntityConsts.ON_UNDEF_COLUMN_OPTION_CODE_IGNORE, AmiCenterEntityConsts.ON_UNDEF_COLUMN_OPTION_IGNORE);
		this.tableOnUndefColumnField.addOption(AmiCenterEntityConsts.ON_UNDEF_COLUMN_OPTION_CODE_ADD, AmiCenterEntityConsts.ON_UNDEF_COLUMN_OPTION_ADD);
		this.tableOnUndefColumnField.setHelp(
				"Behaviour when a realtime Object record contains an undefined column:<br> <ul><ui>(1). <b style=\"color:blue\"><i>\"REJECT\"</i></b>: The record will be rejected. This is the default.<br>"
						+ "<ui>(2). <b style=\"color:blue\"><i>\"IGNORE\"</b></i>: The record will be inserted, but the undefined values will be ignored.</ui><br>"
						+ "<ui>(3). <b style=\"color:blue\"><i>\"ADD\"</b></i>: The table will automatically have the column added.</ui><ul>"

		);

		this.tableInitialCapacityField = new AmiCenterEntityOptionField(this, new FormPortletTextField("InitialCapacity"), false, false, "InitialCapacity", "InitialCapacity",
				GROUP_CODE_TABLE);
		((FormPortletTextField) (this.tableInitialCapacityField.getInner())).setDefaultValue("1000");
		this.tableInitialCapacityField.setHelp(
				"InitialCapacity=\"number_of_rows\": The number of rows to allocate memory for when the table is created. Must be at least 1.<br> The default initial capacity is 1,000 rows.");

		this.schemaFields.add(tableSchemaField);
		this.configFields.add(tablePersistEngineField);
		this.configFields.add(tableBroadCastField);
		this.configFields.add(tableRefreshPeriodMsField);
		this.configFields.add(tableOnUndefColumnField);
		this.configFields.add(tableInitialCapacityField);

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
