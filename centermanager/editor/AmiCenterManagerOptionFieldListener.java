package com.f1.ami.web.centermanager.editor;

import com.f1.suite.web.portal.impl.form.FormPortletField;

public interface AmiCenterManagerOptionFieldListener {
	public void onOptionFieldAdded();
	public void onOptionFieldRemoved();
	public void onOptionFieldEdited(FormPortletField<?> field);
}
