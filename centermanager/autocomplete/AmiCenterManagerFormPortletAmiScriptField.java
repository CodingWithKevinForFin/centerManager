package com.f1.ami.web.centermanager.autocomplete;

import com.f1.suite.web.portal.PortletManager;
import com.f1.utils.structs.table.derived.BasicMethodFactory;
import com.f1.utils.structs.table.derived.MethodFactory;

public class AmiCenterManagerFormPortletAmiScriptField extends AmiAbstractFormPortletAmiScriptField {

	public AmiCenterManagerFormPortletAmiScriptField(String title, PortletManager manager) {
		super(title, manager);
		this.autoCompletion = new AmiCenterManagerScriptAutoCompletion(service);
	}

	public BasicMethodFactory getMethodFactory() {
		return this.autoCompletion.getMethodFactory();
	}

	public void addMethodFactory(MethodFactory toAdd) {
		this.autoCompletion.getMethodFactory().addFactory(toAdd);
	}

}