package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import java.util.Set;

import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletField;

public abstract class AmiCenterManagerAbstractTriggerEditor extends GridPortlet {
	final protected FormPortlet form;

	public AmiCenterManagerAbstractTriggerEditor(PortletConfig config) {
		super(config);
		form = new FormPortlet(generateConfig());
	}

	public FormPortlet getForm() {
		return this.form;
	}

	abstract public String getKeyValuePairs();

	abstract public FormPortletField<?> getFieldByName(String name);

	abstract public void enableEdit(boolean enable);

	abstract public Set<? extends FormPortlet> getSmartEditors();

}
