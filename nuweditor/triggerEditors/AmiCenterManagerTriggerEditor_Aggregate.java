package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;

public class AmiCenterManagerTriggerEditor_Aggregate extends GridPortlet {
	final private FormPortlet form;

	final private FormPortletCheckboxField allowExternalUpdateField;

	public AmiCenterManagerTriggerEditor_Aggregate(PortletConfig config) {
		super(config);
		form = new FormPortlet(generateConfig());
		allowExternalUpdateField = form.addField(new FormPortletCheckboxField("allowExternalUpdates"));
		addChild(form, 0, 0);
		addChild(new FormPortlet(generateConfig()), 0, 1);
	}

}
