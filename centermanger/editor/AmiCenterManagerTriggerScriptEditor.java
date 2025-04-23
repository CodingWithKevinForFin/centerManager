package com.f1.ami.web.centermanger.editor;

import java.util.Map;
import java.util.logging.Logger;

import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.AmiWebUtils;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.ConfirmDialog;
import com.f1.suite.web.portal.impl.ConfirmDialogListener;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletButton;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuFactory;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuListener;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletListener;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.LH;

public class AmiCenterManagerTriggerScriptEditor extends GridPortlet
		implements FormPortletListener, ConfirmDialogListener, FormPortletContextMenuFactory, FormPortletContextMenuListener {
	private static final Logger log = LH.get();
	private FormPortlet bodyForm;
	private AmiWebService service;

	public AmiCenterManagerTriggerScriptEditor(PortletConfig config, AmiCenterManagerAddTriggerPortlet inner) {
		super(config);
		this.service = AmiWebUtils.getService(getManager());
		this.bodyForm = new FormPortlet(generateConfig());
		FormPortletTextField f = new FormPortletTextField("test");
		f.setValue("test");
		this.bodyForm.addField(f);
		this.bodyForm.addFormPortletListener(this);
		this.bodyForm.setMenuFactory(this);
		this.bodyForm.addMenuListener(this);
	}

	@Override
	public void onContextMenu(FormPortlet portlet, String action, FormPortletField node) {
		// TODO Auto-generated method stub

	}

	@Override
	public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onButton(ConfirmDialog source, String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		// TODO Auto-generated method stub

	}

}
