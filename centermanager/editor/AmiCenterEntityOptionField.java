package com.f1.ami.web.centermanager.editor;

import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextAreaField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;

public class AmiCenterEntityOptionField {
	private FormPortlet owner;
	private FormPortletField<?> inner;
	private boolean isRequired;
	private boolean hasChanged;
	public String id;//e.g triggerName
	public String title;//e.g. Trigger Name
	public short groupCode;//trigger,table,timer,procedure?

	public AmiCenterEntityOptionField(FormPortlet owner, FormPortletField<?> inner, boolean isRequired, boolean hasChanged, String id, String title, short groupCode) {
		this.owner = owner;
		this.inner = inner;
		this.isRequired = isRequired;
		this.hasChanged = hasChanged;
		this.id = id;
		this.title = title;
		this.isRequired = isRequired;
		this.groupCode = groupCode;
		this.owner.addField(this.inner);
	}

	public AmiCenterEntityOptionField(FormPortlet owner, FormPortletField<?> inner, boolean isRequired, boolean hasChanged, String id, String title, short groupCode,
			boolean addToOwner) {
		this.owner = owner;
		this.inner = inner;
		this.isRequired = isRequired;
		this.hasChanged = hasChanged;
		this.id = id;
		this.title = title;
		this.isRequired = isRequired;
		this.groupCode = groupCode;
		if (addToOwner)
			this.owner.addField(this.inner);
	}

	public boolean getIsRequired() {
		return this.isRequired;
	}

	public boolean getHasChanged() {
		return this.hasChanged;
	}

	public String getId() {
		return this.id;
	}
	public String getFormattedTitle() {
		return this.getFormattedTitle();
	}
	public String title() {
		return this.title;
	}

	public FormPortletSelectField<Short> addOption(short key, String name) {
		if (!(this.inner instanceof FormPortletSelectField))
			throw new UnsupportedOperationException();
		return ((FormPortletSelectField) this.inner).addOption(key, name);
	}
	public void setDisabled(boolean disabled) {
		this.inner.setDisabled(disabled);
	}
	public FormPortletField<?> getInner() {
		return this.inner;
	}

	public void setHelp(String help) {
		this.inner.setHelp(help);
	}

	public Object getValue() {
		if (this.inner instanceof FormPortletSelectField)
			return ((FormPortletSelectField<Short>) this.inner).getValue();
		else if (this.inner instanceof FormPortletTextField)
			return ((FormPortletTextField) this.inner).getValue();
		else if (this.inner instanceof FormPortletTextAreaField)
			return ((FormPortletTextAreaField) this.inner).getValue();
		else if (this.inner instanceof AmiWebFormPortletAmiScriptField)
			return ((AmiWebFormPortletAmiScriptField) this.inner).getValue();
		else if (this.inner instanceof FormPortletCheckboxField)
			return ((FormPortletCheckboxField) this.inner).getValue();
		return null;
	}

	public void setValue(Object value) {
		if (this.inner instanceof FormPortletSelectField)
			((FormPortletSelectField) this.inner).setValue(value);
		else if (this.inner instanceof FormPortletTextField)
			((FormPortletTextField) this.inner).setValue((String) value);
		else if (this.inner instanceof FormPortletTextAreaField)
			((FormPortletTextAreaField) this.inner).setValue((String) value);
		else if (this.inner instanceof AmiWebFormPortletAmiScriptField)
			((AmiWebFormPortletAmiScriptField) this.inner).setValue((String) value);
		else if (this.inner instanceof FormPortletCheckboxField)
			((FormPortletCheckboxField) this.inner).setValue((Boolean) value);
	}

	public void setHasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}

	//return the trigger, timer, procedure code
	public short getGroupCode() {
		return groupCode;
	}

	public String getTitle() {
		return this.title;
	}
}
