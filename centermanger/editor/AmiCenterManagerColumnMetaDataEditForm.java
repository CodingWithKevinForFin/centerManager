package com.f1.ami.web.centermanger.editor;

import java.util.HashMap;
import java.util.Map;

import com.f1.ami.amicommon.AmiConsts;
import com.f1.ami.amicommon.msg.AmiDatasourceColumn;
import com.f1.ami.web.centermanger.AmiCenterEntityConsts;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletButton;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletListener;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.OH;
import com.f1.utils.OneToOne;

public class AmiCenterManagerColumnMetaDataEditForm extends FormPortlet implements FormPortletListener {
	public static HashMap<String, String> KEY_TO_NAME_MAP = new HashMap<String, String>();

	public static OneToOne<String, String> KEY_TO_NAME_MAPPING;
	static {
		KEY_TO_NAME_MAP.put("columnName", "Column Name");
		KEY_TO_NAME_MAP.put("dataType", "Data Type");
		KEY_TO_NAME_MAPPING = new OneToOne(KEY_TO_NAME_MAP);
	}
	private FormPortletTextField columnNameEditField;
	private FormPortletSelectField<Byte> dataTypeEditField;
	private FormPortletCheckboxField noNullEditField;
	private FormPortletCheckboxField noBroadcastEditField;

	private FormPortletCheckboxField isEnumField;
	private FormPortletCheckboxField isCacheField;
	private FormPortletTextField cacheValueField;
	private FormPortletSelectField<Byte> cacheUnitField;
	private FormPortletCheckboxField isCompactField;
	private FormPortletCheckboxField isAsciiField;
	private FormPortletCheckboxField isBitmapField;
	private FormPortletCheckboxField isOndiskField;

	//this cache is set when the row from the table is clicked
	private Map<String, String> columnCache = new HashMap<String, String>();

	public AmiCenterManagerColumnMetaDataEditForm(PortletConfig config) {
		super(config);
		this.columnNameEditField = new FormPortletTextField("Colummn Name");
		this.columnNameEditField.setName("columnName");
		this.dataTypeEditField = new FormPortletSelectField<Byte>(Byte.class, "Data Type");
		this.dataTypeEditField.setName("dataType");
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_NONE, "<NONE>");
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_STRING, AmiConsts.TYPE_NAME_STRING);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_BINARY, AmiConsts.TYPE_NAME_BINARY);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_BOOLEAN, AmiConsts.TYPE_NAME_BOOLEAN);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_DOUBLE, AmiConsts.TYPE_NAME_DOUBLE);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_FLOAT, AmiConsts.TYPE_NAME_FLOAT);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_UTC, AmiConsts.TYPE_NAME_UTC);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_UTCN, AmiConsts.TYPE_NAME_UTCN);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_LONG, AmiConsts.TYPE_NAME_LONG);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_INT, AmiConsts.TYPE_NAME_INTEGER);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_CHAR, AmiConsts.TYPE_NAME_CHAR);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_SHORT, AmiConsts.TYPE_NAME_SHORT);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_BYTE, AmiConsts.TYPE_NAME_BYTE);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_BIGINT, AmiConsts.TYPE_NAME_BIGINT);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_BIGDEC, AmiConsts.TYPE_NAME_BIGDEC);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_COMPLEX, AmiConsts.TYPE_NAME_COMPLEX);
		dataTypeEditField.addOption(AmiDatasourceColumn.TYPE_UUID, AmiConsts.TYPE_NAME_UUID);
		dataTypeEditField.setDefaultValue(AmiDatasourceColumn.TYPE_NONE);

		//common fields
		this.noNullEditField = new FormPortletCheckboxField(AmiConsts.NONULL);
		this.noNullEditField.setName(AmiConsts.NONULL);
		this.noBroadcastEditField = new FormPortletCheckboxField(AmiConsts.NOBROADCAST);
		this.noBroadcastEditField.setName(AmiConsts.NOBROADCAST);
		columnNameEditField.setDisabled(true);
		dataTypeEditField.setDisabled(true);
		noNullEditField.setDisabled(true);
		noBroadcastEditField.setDisabled(true);
		this.addField(columnNameEditField);
		this.addField(dataTypeEditField);
		this.addField(noNullEditField);
		this.addField(noBroadcastEditField);

		//Data type specific fields
		isEnumField = new FormPortletCheckboxField(AmiConsts.TYPE_NAME_ENUM);
		isEnumField.setName(AmiConsts.TYPE_NAME_ENUM);
		isCacheField = new FormPortletCheckboxField(AmiConsts.CACHE);
		isCacheField.setName(AmiConsts.CACHE);
		cacheValueField = new FormPortletTextField("");
		cacheValueField.setMaxChars(100);
		cacheUnitField = new FormPortletSelectField<Byte>(Byte.class, "Unit");
		cacheUnitField.addOption(AmiConsts.CODE_CACHE_UNIT_DEFAULT_BYTE, AmiConsts.CACHE_UNIT_DEFAULT_BYTE);
		cacheUnitField.addOption(AmiConsts.CODE_CACHE_UNIT_KB, AmiConsts.CACHE_UNIT_KB);
		cacheUnitField.addOption(AmiConsts.CODE_CACHE_UNIT_MB, AmiConsts.CACHE_UNIT_MB);
		cacheUnitField.addOption(AmiConsts.CODE_CACHE_UNIT_GB, AmiConsts.CACHE_UNIT_GB);
		cacheUnitField.addOption(AmiConsts.CODE_CACHE_UNIT_TB, AmiConsts.CACHE_UNIT_TB);
		cacheUnitField.setDefaultValue(AmiConsts.CODE_CACHE_UNIT_DEFAULT_BYTE);

		isCompactField = new FormPortletCheckboxField(AmiConsts.COMPACT);
		isCompactField.setName(AmiConsts.COMPACT);
		isAsciiField = new FormPortletCheckboxField(AmiConsts.ASCII);
		isAsciiField.setName(AmiConsts.ASCII);
		isBitmapField = new FormPortletCheckboxField(AmiConsts.BITMAP);
		isBitmapField.setName(AmiConsts.BITMAP);
		isOndiskField = new FormPortletCheckboxField(AmiConsts.ONDISK);
		isOndiskField.setName(AmiConsts.ONDISK);
		addField(isEnumField);
		addField(isCompactField);
		addField(isAsciiField);
		addField(isBitmapField);
		addField(isOndiskField);
		addField(isCacheField);
		addField(cacheValueField);
		addField(cacheUnitField);
		//TODO:not use hard-coded values
		isCacheField.setLeftPosPx(164).setWidthPx(20).setHeightPx(16).setTopPosPx(220);
		cacheValueField.setLeftPosPx(189).setWidthPx(50).setHeightPx(16).setTopPosPx(220);
		cacheUnitField.setLeftPosPx(274).setWidthPx(140).setHeightPx(16).setTopPosPx(220);

		//disable all the fields on init
		disableCache(true);
		disableAscii(true);
		disableOnDisk(true);
		disableBitmap(true);
		disableCompact(true);
		disableEnum(true);

		//add listener
		addFormPortletListener(this);
	}

	public void setColumnCache(String key, String value) {
		this.columnCache.put(key, value);
	}

	public void resetForm() {
		columnNameEditField.setDisabled(true);
		dataTypeEditField.setDisabled(true);
		dataTypeEditField.setDefaultValue(AmiDatasourceColumn.TYPE_NONE);
		noNullEditField.setDisabled(true).setValue(false);
		noBroadcastEditField.setDisabled(true).setValue(false);
		isCacheField.setDisabled(true).setValue(false);
		cacheValueField.setDisabled(true).setValue("");
		cacheUnitField.setDisabled(true).setValue(AmiConsts.CODE_CACHE_UNIT_DEFAULT_BYTE);
		isOndiskField.setDisabled(true).setValue(false);
		isAsciiField.setDisabled(true).setValue(false);
		isBitmapField.setDisabled(true).setValue(false);
		isCompactField.setDisabled(true).setValue(false);
		isEnumField.setDisabled(true).setValue(false);
	}

	public void disableCache(boolean disabled) {
		isCacheField.setDisabled(disabled);
		cacheValueField.setDisabled(disabled);
		cacheUnitField.setDisabled(disabled);
	}

	public void disableOnDisk(boolean disabled) {
		isOndiskField.setDisabled(disabled);
	}

	public void disableAscii(boolean disabled) {
		isAsciiField.setDisabled(disabled);
	}
	public void disableBitmap(boolean disabled) {
		isBitmapField.setDisabled(disabled);
	}
	public void disableCompact(boolean disabled) {
		isCompactField.setDisabled(disabled);
	}
	public void disableEnum(boolean disabled) {
		isEnumField.setDisabled(disabled);
	}
	public void disableStringOptions(boolean disabled) {
		disableCache(disabled);
		disableOnDisk(disabled);
		disableAscii(disabled);
		disableBitmap(disabled);
		disableCompact(disabled);
		disableEnum(disabled);
	}
	public void disableCommonOptions(boolean disabled) {
		noBroadcastEditField.setDisabled(disabled);
		noNullEditField.setDisabled(disabled);
	}

	@Override
	public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {

	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		//**PART1:Apply rules**
		//RULE1: CACHE must be used in conjunction with ONDISK(triggered when hit apply)
		//RULE2: ONDISK can not be used in conjunction with other supplied directives(triggered when hit apply)
		//		if (field == this.isOndiskField && isOndiskField.getBooleanValue()) {
		//			disableStringOptions(true);
		//			this.isOndiskField.setDisabled(false);
		//		}
		//RULE3: BITMAP and COMPACT directive are mutually exclusive
		if (field == this.isBitmapField && this.isBitmapField.getBooleanValue()) {
			disableCompact(true);
		} else if (field == this.isBitmapField && !this.isBitmapField.getBooleanValue()) {
			disableCompact(false);
		}

		else if (field == this.isCompactField && this.isCompactField.getBooleanValue()) {
			disableBitmap(true);
		} else if (field == this.isCompactField && !this.isCompactField.getBooleanValue()) {
			disableBitmap(false);
		}
		//RULE4: ASCII directive only supported for STRING columns with COMPACT option(triggered when hit apply)

		//**PART2:apply style to title if the field has changed**
		Object oldVal = this.columnCache.get(field.getName());
		Object curVal = field.getValue();
		if (field instanceof FormPortletSelectField) {
			//in this case, field.getValue() will return short, we need to return the string value
			curVal = OH.cast(field, FormPortletSelectField.class).getOption(field.getValue()).getName();
		}
		if (!curVal.equals(oldVal) && curVal instanceof Boolean && (boolean) curVal != false)
			field.setTitle(AmiCenterEntityConsts.CHANGED_FIELD_ANNOTATION_HTML + field.getTitle());
		else
			field.setTitle(field.getTitle().replace(AmiCenterEntityConsts.CHANGED_FIELD_ANNOTATION_HTML, ""));

	}

	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		// TODO Auto-generated method stub

	}

}
