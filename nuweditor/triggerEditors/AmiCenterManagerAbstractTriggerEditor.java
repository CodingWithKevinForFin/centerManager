package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;

public abstract class AmiCenterManagerAbstractTriggerEditor extends GridPortlet {
	final protected FormPortlet form;

	public AmiCenterManagerAbstractTriggerEditor(PortletConfig config) {
		super(config);
		form = new FormPortlet(generateConfig());
	}

	abstract public String getKeyValuePairs();

}
