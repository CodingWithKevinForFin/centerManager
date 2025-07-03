package com.f1.ami.web.centermanager.nuweditor;

import com.f1.ami.amicommon.msg.AmiCenterQueryDsRequest;
import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.AmiWebUtils;
import com.f1.ami.web.centermanager.editor.AmiCenterManagerSubmitEditScriptPortlet;
import com.f1.base.Action;
import com.f1.container.ResultMessage;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletButton;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuFactory;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuListener;
import com.f1.suite.web.portal.impl.form.FormPortletListener;
import com.f1.utils.SH;
import com.f1.utils.converter.json2.ObjectToJsonConverter;

public abstract class AmiCenterManagerAbstractEditCenterObjectPortlet extends GridPortlet
		implements FormPortletContextMenuFactory, FormPortletContextMenuListener, FormPortletListener {
	public static final int DEFAULT_ROWHEIGHT = 25;
	public static final int DEFAULT_LEFTPOS = 75; //164
	public static final int DEFAULT_Y_SPACING = 10;
	public static final int DEFAULT_X_SPACING = 45;
	public static final int DEFAULT_TOPPOS = DEFAULT_Y_SPACING;

	//Width consts
	public static final int NAME_WIDTH = 250;
	public static final int TYPE_WIDTH = 100;
	public static final int PRIORITY_WIDTH = 50;
	public static final int ON_WIDTH = 700;
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
	protected final FormPortletButton submitButton;
	protected final FormPortletButton cancelButton;
	//private final FormPortletButton previewButton;
	protected final FormPortletButton diffButton;
	protected final FormPortletButton importExportButton;
	protected final static ObjectToJsonConverter JSON_CONVERTER = new ObjectToJsonConverter();
	static {
		JSON_CONVERTER.setIgnoreUnconvertable(true);
		JSON_CONVERTER.setStrictValidation(true);
		JSON_CONVERTER.setTreatNanAsNull(false);
		JSON_CONVERTER.setCompactMode(false);
	}

	//fields needed to query the backend
	protected long sessionId = -1;

	//enable editing
	protected final FormPortletCheckboxField enableEditingCheckbox;

	public AmiCenterManagerAbstractEditCenterObjectPortlet(PortletConfig config, boolean isAdd) {
		super(config);
		this.isAdd = isAdd;
		this.enableEditingCheckbox = isAdd ? null : new FormPortletCheckboxField("<i>Enable Editing</i>");
		if (!isAdd) {
			this.enableEditingCheckbox.setDefaultValue(false);
			this.enableEditingCheckbox.setBgColor("#bab0b0");
		}

		this.service = AmiWebUtils.getService(config.getPortletManager());

		this.buttonsFp = new FormPortlet(generateConfig());
		this.buttonsFp.getFormPortletStyle().setLabelsWidth(200);
		this.buttonsFp.setMenuFactory(this);
		this.buttonsFp.addMenuListener(this);
		this.buttonsFp.addFormPortletListener(this);

		this.submitButton = buttonsFp.addButton(new FormPortletButton("Submit"));
		this.cancelButton = buttonsFp.addButton(new FormPortletButton("Cancel"));
		//this.previewButton = buttonsFp.addButton(new FormPortletButton("Preview"));
		this.diffButton = buttonsFp.addButton(new FormPortletButton("Diff"));
		this.importExportButton = buttonsFp.addButton(new FormPortletButton("Import/Export"));
	}

	@Override
	public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {
		if (button == this.cancelButton) {
			close();
			return;
		} else if (button == this.importExportButton) {
			getManager().showDialog("Export/Import Editor Script", new AmiCenterManagerScriptExportPortlet(generateConfig(), this));
		} else if (button == this.submitButton) {
			//getManager().showDialog("Export/Import Editor Script", new AmiCenterManagerTriggerEditor_SelectEditor(generateConfig()), 800, 750);
		}
		//		else if (button == this.previewButton) {
		//			PortletStyleManager_Dialog dp = service.getPortletManager().getStyleManager().getDialogStyle();
		//			final PortletManager portletManager = service.getPortletManager();
		//			ConfirmDialogPortlet cdp = new ConfirmDialogPortlet(portletManager.generateConfig(), AmiCenterManagerUtils.formatPreviewScript(previewScript()),
		//					ConfirmDialogPortlet.TYPE_MESSAGE);
		//			portletManager.showDialog("Script", cdp, dp.getDialogWidth(), dp.getDialogHeight());
		//		} 

	}

	public String previewScript() {
		if (SH.is(prepareUseClause()))
			return preparePreUseClause() + " USE " + prepareUseClause();
		return preparePreUseClause();
	}

	//The abilities to query the backend
	@Override
	public void onBackendResponse(ResultMessage<Action> result) {
		if (result.getError() != null) {
			getManager().showAlert("Internal Error:" + result.getError().getMessage(), result.getError());
			return;
		}
	}

	public AmiCenterQueryDsRequest prepareRequest() {
		AmiCenterQueryDsRequest request = getManager().getTools().nw(AmiCenterQueryDsRequest.class);

		request.setLimit(AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_LIMIT);
		request.setTimeoutMs(AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_TIMEOUT);
		request.setQuerySessionKeepAlive(true);
		request.setIsTest(false);
		request.setAllowSqlInjection(AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_ALLOW_SQL_INJECTION);
		request.setInvokedBy(service.getUserName());
		request.setSessionVariableTypes(null);
		request.setSessionVariables(null);
		request.setPermissions(AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PERMISSION);
		request.setType(AmiCenterQueryDsRequest.TYPE_QUERY);
		request.setOriginType(AmiCenterQueryDsRequest.ORIGIN_FRONTEND_SHELL);
		request.setDatasourceName(AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_DS_NAME);
		return request;
	}

	protected void sendQueryToBackend(String query) {
		if (SH.isnt(query))
			return;
		AmiCenterQueryDsRequest request = prepareRequest();
		if (request == null)
			return;
		request.setQuery(query);
		request.setQuerySessionId(this.sessionId);
		service.sendRequestToBackend(this, request);
	}

	abstract public String prepareUseClause();

	abstract public String preparePreUseClause();

	abstract public String exportToText();

	abstract public void importFromText(String text, StringBuilder sink);

	//abstract public void enableEdit(boolean enable);

}
