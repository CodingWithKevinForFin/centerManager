package com.f1.ami.web.centermanager.nuweditor;

import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.AmiWebUtils;
import com.f1.ami.web.centermanger.AmiCenterManagerUtils;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.PortletManager;
import com.f1.suite.web.portal.impl.ConfirmDialogPortlet;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletButton;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuFactory;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuListener;
import com.f1.suite.web.portal.impl.form.FormPortletListener;
import com.f1.suite.web.portal.style.PortletStyleManager_Dialog;
import com.f1.utils.SH;

public abstract class AmiCenterManagerAbstractEditCenterObjectPortlet extends GridPortlet
		implements FormPortletContextMenuFactory, FormPortletContextMenuListener, FormPortletListener {
	public static final int DEFAULT_ROWHEIGHT = 25;
	public static final int DEFAULT_LEFTPOS = 75; //164
	public static final int DEFAULT_Y_SPACING = 10;
	public static final int DEFAULT_X_SPACING = 65;
	public static final int DEFAULT_TOPPOS = DEFAULT_Y_SPACING;

	//Width consts
	public static final int NAME_WIDTH = 110;
	public static final int TYPE_WIDTH = 100;
	public static final int PRIORITY_WIDTH = 100;
	public static final int ON_WIDTH = 150;
	public static final int TIMEOUT_WIDTH = 100;
	public static final int LIMIT_WIDTH = 100;
	public static final int LOGGING_WIDTH = 80;
	public static final int VARS_WIDTH = 700;

	//height const
	public static final int OPTION_FORM_HEIGHT = 120;
	public static final int AMISCRIPT_FORM_HEIGHT = 600;

	//padding
	public static final int AMISCRIPT_FORM_PADDING = 0;

	protected final AmiWebService service;
	protected boolean isAdd = false;

	//buttons
	final protected FormPortlet buttonsFp;
	private final FormPortletButton submitButton;
	private final FormPortletButton cancelButton;
	private final FormPortletButton previewButton;
	private final FormPortletButton diffButton;
	private final FormPortletButton importExportButton;

	public AmiCenterManagerAbstractEditCenterObjectPortlet(PortletConfig config, boolean isAdd) {
		super(config);
		this.isAdd = isAdd;
		this.service = AmiWebUtils.getService(config.getPortletManager());

		this.buttonsFp = new FormPortlet(generateConfig());
		this.buttonsFp.getFormPortletStyle().setLabelsWidth(200);
		this.buttonsFp.setMenuFactory(this);
		this.buttonsFp.addMenuListener(this);
		this.buttonsFp.addFormPortletListener(this);

		this.submitButton = buttonsFp.addButton(new FormPortletButton("Submit"));
		this.cancelButton = buttonsFp.addButton(new FormPortletButton("Cancel"));
		this.previewButton = buttonsFp.addButton(new FormPortletButton("Preview"));
		this.diffButton = buttonsFp.addButton(new FormPortletButton("Diff"));
		this.importExportButton = buttonsFp.addButton(new FormPortletButton("Import/Export"));
	}

	@Override
	public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {
		if (button == this.cancelButton) {
			close();
			return;
		} else if (button == this.previewButton) {
			PortletStyleManager_Dialog dp = service.getPortletManager().getStyleManager().getDialogStyle();
			final PortletManager portletManager = service.getPortletManager();
			ConfirmDialogPortlet cdp = new ConfirmDialogPortlet(portletManager.generateConfig(), AmiCenterManagerUtils.formatPreviewScript(previewScript()),
					ConfirmDialogPortlet.TYPE_MESSAGE);
			portletManager.showDialog("Script", cdp, dp.getDialogWidth(), dp.getDialogHeight());
		}
	}

	public String previewScript() {
		if (SH.is(prepareUseClause()))
			return preparePreUseClause() + " USE " + prepareUseClause();
		return preparePreUseClause();
	}

	abstract public String prepareUseClause();

	abstract public String preparePreUseClause();

}
