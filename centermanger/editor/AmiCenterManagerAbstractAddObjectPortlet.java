package com.f1.ami.web.centermanger.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.f1.ami.amicommon.AmiUtils;
import com.f1.ami.amicommon.msg.AmiCenterQueryDsRequest;
import com.f1.ami.amicommon.msg.AmiCenterQueryDsResponse;
import com.f1.ami.portlets.AmiWebHeaderPortlet;
import com.f1.ami.portlets.AmiWebHeaderSearchHandler;
import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.AmiWebLayoutHelper;
import com.f1.ami.web.AmiWebLockedPermissiblePortlet;
import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.AmiWebSpecialPortlet;
import com.f1.ami.web.AmiWebUtils;
import com.f1.ami.web.centermanger.AmiCenterEntityConsts;
import com.f1.ami.web.centermanger.AmiCenterManagerUtils;
import com.f1.ami.web.graph.AmiCenterGraphNode;
import com.f1.base.Action;
import com.f1.base.Table;
import com.f1.container.ResultMessage;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.PortletManager;
import com.f1.suite.web.portal.impl.ConfirmDialogListener;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.HtmlPortlet;
import com.f1.suite.web.portal.impl.HtmlPortlet.Callback;
import com.f1.suite.web.portal.impl.HtmlPortletListener;
import com.f1.suite.web.portal.impl.MultiDividerPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletButton;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletListener;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.suite.web.util.WebHelper;
import com.f1.utils.CH;
import com.f1.utils.MH;
import com.f1.utils.OH;
import com.f1.utils.SH;
import com.f1.utils.casters.Caster_String;
import com.f1.utils.structs.Tuple2;

public abstract class AmiCenterManagerAbstractAddObjectPortlet extends FormPortlet
		implements FormPortletListener, ConfirmDialogListener, AmiWebLockedPermissiblePortlet, AmiWebHeaderSearchHandler, AmiCenterManagerEditorTemplate {
	protected final AmiWebService service;
	//every center object has a name field
	protected AmiCenterEntityOptionField nameField;
	private byte groupCode;
	private final FormPortletButton submitButton = new FormPortletButton("Submit");
	private final FormPortletButton cancelButton = new FormPortletButton("Cancel");
	private final FormPortletButton previewButton = new FormPortletButton("Preview");
	private FormPortletButton diffButton = null;
	private final PortletManager manager;
	//add/edit mode
	protected byte mode = AmiCenterEntityConsts.ADD;
	//caches the last value prior to edit	, the value is oftype object
	protected Map<AmiCenterEntityOptionField, String> valueCache = new HashMap<AmiCenterEntityOptionField, String>();
	protected long sessionId = -1;
	protected int changes = -1;
	protected int schemaFlags = -1;
	protected byte editPolicy = AmiCenterEntityConsts.EDIT_POLICY_FUZZY;
	protected final List<AmiCenterEntityOptionField> schemaFields = new ArrayList<AmiCenterEntityOptionField>();
	protected final List<AmiCenterEntityOptionField> configFields = new ArrayList<AmiCenterEntityOptionField>();

	/**
	 * For schema fields:<br>
	 * (1). Table: [name, schema] <br>
	 * (2). Trigger: [name, ofype, on, priority] <br>
	 * (3). Timer: [name, oftype, on, priority] <br>
	 * (4). Procedure: [name, oftype] <br>
	 * (5). Index: [name, on] <br>
	 * (6). Method: [returnType, name] * EXCEPTION <br>
	 * (7). DBO: [name, oftype]
	 */

	public AmiCenterManagerAbstractAddObjectPortlet(PortletConfig config, byte groupCode) {
		super(config);
		this.groupCode = groupCode;
		this.schemaFlags = 0;
		String nameId = null;
		switch (this.groupCode) {
			case AmiCenterGraphNode.TYPE_TIMER:
				FlagHasConfigNode(AmiCenterEntityConsts.HAS_OFTYPE);
				FlagHasConfigNode(AmiCenterEntityConsts.HAS_ON);
				FlagHasConfigNode(AmiCenterEntityConsts.HAS_PRIORITY);
				nameId = "timerName";
				break;
			case AmiCenterGraphNode.TYPE_TRIGGER:
				FlagHasConfigNode(AmiCenterEntityConsts.HAS_OFTYPE);
				FlagHasConfigNode(AmiCenterEntityConsts.HAS_ON);
				FlagHasConfigNode(AmiCenterEntityConsts.HAS_PRIORITY);
				nameId = "triggerName";
				break;
			case AmiCenterGraphNode.TYPE_TABLE:
				nameId = "tableName";
				break;
			case AmiCenterGraphNode.TYPE_INDEX:
				FlagHasConfigNode(AmiCenterEntityConsts.HAS_ON);
				nameId = "indexName";
				break;
			case AmiCenterGraphNode.TYPE_METHOD:
				nameId = "methodName";
				break;
			case AmiCenterGraphNode.TYPE_DBO:
				FlagHasConfigNode(AmiCenterEntityConsts.HAS_OFTYPE);
				nameId = "dboName";
				break;
			case AmiCenterGraphNode.TYPE_PROCEDURE:
				FlagHasConfigNode(AmiCenterEntityConsts.HAS_OFTYPE);
				nameId = "dboName";
				break;

		}
		this.nameField = new AmiCenterEntityOptionField(this, new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("Name")), true, false, nameId, "Name", groupCode);
		this.schemaFields.add(nameField);
		this.service = AmiWebUtils.getService(getManager());
		this.manager = getManager();
		this.getFormPortletStyle().setCssStyle("_bg=#e2e2e2");
		initTemplate();
		addButton(this.submitButton);
		addButton(this.cancelButton);
		addButton(this.previewButton);
		addFormPortletListener(this);
	}

	public AmiCenterManagerAbstractAddObjectPortlet(PortletConfig config, byte groupCode, Map objectConfig, byte mode) {
		this(config, groupCode);
		this.mode = mode;
		if (this.mode == AmiCenterEntityConsts.EDIT) {
			this.changes = 0;
			readFromConfig(objectConfig);
			this.diffButton = new FormPortletButton("Diff");
			addButton(this.diffButton);
		}

	}
	private void FlagHasConfigNode(int mask) {
		if (MH.allBits(this.schemaFlags, mask))
			return;
		this.schemaFlags |= mask;
	}

	@Override
	abstract public void readFromConfig(Map config);

	@Override
	public AmiCenterQueryDsRequest prepareRequest() {
		AmiCenterQueryDsRequest request = getManager().getTools().nw(AmiCenterQueryDsRequest.class);

		request.setLimit(AmiCenterManagerSubmitTriggerPortlet.DEFAULT_LIMIT);
		request.setTimeoutMs(AmiCenterManagerSubmitTriggerPortlet.DEFAULT_TIMEOUT);
		request.setQuerySessionKeepAlive(true);
		request.setIsTest(false);
		request.setAllowSqlInjection(AmiCenterManagerSubmitTriggerPortlet.DEFAULT_ALLOW_SQL_INJECTION);
		request.setInvokedBy(service.getUserName());
		request.setSessionVariableTypes(null);
		request.setSessionVariables(null);
		request.setPermissions(AmiCenterManagerSubmitTriggerPortlet.DEFAULT_PERMISSION);
		request.setType(AmiCenterQueryDsRequest.TYPE_QUERY);
		request.setOriginType(AmiCenterQueryDsRequest.ORIGIN_FRONTEND_SHELL);
		request.setDatasourceName(AmiCenterManagerSubmitTriggerPortlet.DEFAULT_DS_NAME);
		return request;
	}

	@Override
	public void onBackendResponse(ResultMessage<Action> result) {
		if (result.getError() != null) {
			getManager().showAlert("Internal Error:" + result.getError().getMessage(), result.getError());
			return;
		}
		AmiCenterQueryDsResponse response = (AmiCenterQueryDsResponse) result.getAction();
		if (sessionId == -1) {
			this.sessionId = response.getQuerySessionId();
		}
	}

	//except for the index, all other objects can use this. Index needs to override
	/**
	 * For schema fields:<br>
	 * (1). Table: [name, schema] <br>
	 * (2). Trigger: [name, ofype, on, priority] <br>
	 * (3). Timer: [name, oftype, on, priority] <br>
	 * (4). Procedure: [name, oftype] <br>
	 * (5). Index: [name, on] <br>
	 * (6). Method: [returnType, name] * EXCEPTION <br>
	 * (7). DBO: [name, oftype]
	 */
	@Override
	public String preparePreUseClause() {
		StringBuilder script = new StringBuilder();
		script.append("CREATE");
		Object toAppend = null;
		Object nameFieldValue = this.nameField.getInner().getValue();
		if (SH.is(nameFieldValue))
			toAppend = nameFieldValue;
		else
			toAppend = AmiCenterEntityConsts.REQUIRED_FEILD_WARNING;

		switch (this.groupCode) {
			case AmiCenterGraphNode.TYPE_TABLE:
				script.append(" PUBLIC TABLE ").append(toAppend);
				break;
			case AmiCenterGraphNode.TYPE_TRIGGER:
				script.append(" TRIGGER ").append(toAppend);
				break;
			case AmiCenterGraphNode.TYPE_TIMER:
				script.append(" TIMER ").append(toAppend);
				break;
			case AmiCenterGraphNode.TYPE_PROCEDURE:
				script.append(" PROCEDURE ").append(toAppend);
				break;
			case AmiCenterGraphNode.TYPE_METHOD:
				throw new UnsupportedOperationException("For timers please override this method");
			case AmiCenterGraphNode.TYPE_INDEX:
				script.append(" INDEX ").append(toAppend);
				break;
			case AmiCenterGraphNode.TYPE_DBO:
				script.append(" DBO ").append(toAppend);
				break;
			default:
				throw new RuntimeException("Unsupported center object type: " + this.groupCode);
		}
		boolean hasOftype = MH.anyBits(this.schemaFlags, AmiCenterEntityConsts.HAS_OFTYPE);
		boolean hasOn = MH.anyBits(this.schemaFlags, AmiCenterEntityConsts.HAS_ON);
		boolean hasPriority = MH.anyBits(this.schemaFlags, AmiCenterEntityConsts.HAS_PRIORITY);

		//for tables, it only has another tableSchema field
		if (this.groupCode == AmiCenterGraphNode.TYPE_TABLE) {
			String schema = (String) this.schemaFields.get(1).getInner().getValue();
			if (SH.is(schema))
				script.append(schema);
			else
				script.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
			return script.toString();
		}
		//for all other objects, exclusing name field(already appended)
		switch (this.schemaFields.size()) {
			case 4://Trigger and timer
				OH.assertTrue(this.schemaFlags == AmiCenterEntityConsts.HAS_ALL_SCHEMA_NODE);
				short type = (short) this.schemaFields.get(1).getInner().getValue();
				String typeStr = SH.noNull(OH.cast(this.schemaFields.get(1).getInner(), FormPortletSelectField.class).getOption(this.schemaFields.get(1).getValue()).getName());
				Object on = this.schemaFields.get(2).getInner().getValue();
				Object priority = this.schemaFields.get(3).getInner().getValue();
				//oftype
				script.append(" OFTYPE ");
				if (SH.is(typeStr))
					script.append(typeStr);
				else
					script.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
				//on
				script.append(" ON ");
				if (SH.is(on)) {
					if (this.groupCode == AmiCenterGraphNode.TYPE_TIMER)
						script.append(SH.doubleQuote((String) on));
					else
						script.append(on);
				} else
					script.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
				//priority
				if (SH.is(priority))
					script.append(" PRIORITY ").append(priority);
				break;
			case 2://procedure, index, dbo;//[name, on] or [name, oftype]
				if (hasOftype) {
					script.append(" OFTYPE ");
					if (SH.is(this.schemaFields.get(1).getInner().getValue())) {
						String typeAsStr = SH
								.noNull(OH.cast(this.schemaFields.get(1).getInner(), FormPortletSelectField.class).getOption(this.schemaFields.get(1).getValue()).getName());
						script.append(typeAsStr);
					}

					else
						script.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
				}
				if (hasOn) {
					script.append(" ON ");
					if (SH.is(this.schemaFields.get(1).getInner().getValue()))
						script.append(this.schemaFields.get(1).getInner().getValue());
					else
						script.append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
				}
				break;
			default:
				throw new RuntimeException("illegal schema fields size: " + schemaFields.size());

		}
		return script.toString();
	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		switch (this.mode) {
			case AmiCenterEntityConsts.ADD:
				break;
			case AmiCenterEntityConsts.EDIT:
				AmiCenterEntityOptionField toFind = null;
				ArrayList<AmiCenterEntityOptionField> existingFields = new ArrayList<AmiCenterEntityOptionField>(this.schemaFields);
				existingFields.addAll(this.configFields);
				for (AmiCenterEntityOptionField of : existingFields) {
					if (of.getInner() == field) {
						toFind = of;
						break;
					}

				}
				String lastVal = valueCache.get(toFind);
				String nuwVal = null;
				if (field instanceof FormPortletSelectField) {
					//in this case, field.getValue() will return short, we need to return the string value
					nuwVal = OH.cast(field, FormPortletSelectField.class).getOption(field.getValue()).getName();
				} else
					nuwVal = Caster_String.INSTANCE.cast(field.getValue());
				if (lastVal != null) {//if the field is configured
					if (!nuwVal.equals(lastVal)) {
						field.setTitle(AmiCenterEntityConsts.CHANGED_FIELD_ANNOTATION_HTML + field.getTitle());
						toFind.setHasChanged(true);
					} else {
						field.setTitle(field.getTitle().replace(AmiCenterEntityConsts.CHANGED_FIELD_ANNOTATION_HTML, ""));
						toFind.setHasChanged(false);
					}

				} else {
					if ("".equals(nuwVal) || "false".equals(nuwVal)) {
						field.setTitle(field.getTitle().replace(AmiCenterEntityConsts.CHANGED_FIELD_ANNOTATION_HTML, ""));
						toFind.setHasChanged(false);
					} else {
						field.setTitle(AmiCenterEntityConsts.CHANGED_FIELD_ANNOTATION_HTML + field.getTitle());
						toFind.setHasChanged(true);
					}
				}
				break;

		}

	}

	private void doReplaceObject() {
		//TODO: shouuld consider this.change, but since this is edit_poicy_fuzzy, we ignore it
		if (this.mode != AmiCenterEntityConsts.EDIT)// || this.changes == 0)
			throw new IllegalStateException();
		String oldName = null;
		if (this.groupCode != AmiCenterGraphNode.TYPE_METHOD)
			oldName = this.valueCache.get(this.nameField);
		else
			oldName = this.valueCache.get(this.nameField) + this.valueCache.get(this.schemaFields.get(2));//name + args

		String dropQuery = "DROP " + AmiCenterManagerUtils.toCenterObjectString(this.groupCode, true) + " " + oldName + ";";
		getManager().showDialog("Submit " + AmiCenterManagerUtils.toCenterObjectString(this.groupCode, false),
				new AmiCenterManagerSubmitObjectPortlet(generateConfig(), dropQuery + previewScript()), AmiCenterManagerSubmitTriggerPortlet.DEFAULT_PORTLET_WIDTH,
				AmiCenterManagerSubmitTriggerPortlet.DEFAULT_PORTLET_HEIGHT);

	}

	@Override
	public boolean validateScript(String script) {
		boolean isValid = !script.contains(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
		if (!isValid) {
			String warning = "Missing required fields: " + "<b>" + getPendingRequiredFields() + "</b>";
			AmiCenterManagerUtils.popDialog(service, warning, "Timer Script Warning");
			return false;
		}
		return true;
	}

	abstract public boolean validateFields();

	@Override
	public String getPendingRequiredFields() {
		StringBuilder sb = new StringBuilder();
		List<AmiCenterEntityOptionField> existingFields = new ArrayList<>(this.schemaFields);
		existingFields.addAll(this.configFields);
		for (AmiCenterEntityOptionField of : existingFields) {
			FormPortletField fpf = of.getInner();
			if (SH.isnt(fpf.getValue()) && of.getIsRequired())
				sb.append(of.getTitle()).append(",");
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	@Override
	public String prepareUseClause() {
		StringBuilder script = new StringBuilder();
		List<AmiCenterEntityOptionField> nonEmptyAndRequiredFields = new ArrayList<AmiCenterEntityOptionField>();
		//contains nonEmpty fields + required fields
		for (AmiCenterEntityOptionField of : configFields) {
			if (!this.hasField(of.getInner()))//continue if the form does not have this field	
				continue;
			FormPortletField fpf = of.getInner();
			if (SH.is(fpf.getValue())) {
				if (fpf instanceof AmiWebFormPortletAmiScriptField)
					script.append(of.getTitle()).append("=").append(SH.doubleQuote((String) fpf.getValue())).append("").append(" ");
				else if (fpf instanceof FormPortletSelectField) {//CHANGE
					String val = OH.cast(fpf, FormPortletSelectField.class).getOption(fpf.getValue()).getName();
					if (!AmiCenterEntityConsts.AUTOGEN_TYPE_NONE.equals(val))
						script.append(of.getTitle()).append("=").append(SH.doubleQuote(val)).append("").append(" ");
				} else
					script.append(of.getTitle()).append("=\"").append(fpf.getValue()).append("\"").append(" ");
			} else if (SH.isnt(fpf.getValue()) && of.getIsRequired()) { //if field is required and empty, append "MISSING REQUIRED FIELD";
				script.append(of.getTitle()).append("=").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
			}
		}
		return script.toString();
	}

	@Override
	abstract public void initTemplate();

	@Override
	abstract public void resetTemplate();

	@Override
	public String previewScript() {
		return preparePreUseClause() + " USE " + prepareUseClause();
	}

	@Override
	abstract public void submitScript();

	@Override
	abstract public void diffScript();

	@Override
	abstract public void sendQueryToBackend(String query);

	@Override
	public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {
		if (button == this.cancelButton) {
			close();
		} else if (button == this.previewButton) {
			String text = null;
			if (hasOfType() && (short) (this.schemaFields.get(1).getInner().getValue()) == AmiCenterEntityConsts.ENTITY_TYPE_CODE_NULL)
				text = "Please select a trigger type";
			else
				text = AmiCenterManagerUtils.formatPreviewScript(previewScript());

			AmiCenterManagerUtils.popDialog(service, text, "Preview Script");
		} else if (button == this.submitButton) {
			prepareRequest();
			String triggerScript = previewScript();
			switch (this.mode) {
				case AmiCenterEntityConsts.ADD:
					if (triggerScript != null) {//triggerScript == null means the type is <NULL>
						boolean isTriggerComplete = validateScript(triggerScript);
						if (isTriggerComplete) {
							getManager().showDialog("Submit " + AmiCenterManagerUtils.toCenterObjectString(this.groupCode, false),
									new AmiCenterManagerSubmitObjectPortlet(generateConfig(), previewScript()), AmiCenterManagerSubmitTriggerPortlet.DEFAULT_PORTLET_WIDTH,
									AmiCenterManagerSubmitTriggerPortlet.DEFAULT_PORTLET_HEIGHT);
						}
					} else {//if trigger type is null, throw alert
						AmiCenterManagerUtils.popDialog(this.service, "Please select a trigger type", "Trigger Script");
					}
					break;
				case AmiCenterEntityConsts.EDIT:
					if (this.editPolicy == AmiCenterEntityConsts.EDIT_POLICY_FUZZY) {
						doReplaceObject();
					}
					break;
			}
		} else if (button == this.diffButton) {
			if (this.mode != AmiCenterEntityConsts.EDIT)
				throw new IllegalStateException("Cannot diff scripts in mode: " + this.mode);
			Tuple2<Map<String, Object>, Map<String, Object>> diffs = getJsonDiff();
			LinkedHashMap a = new LinkedHashMap<String, Map>();
			a.put("Configuration", diffs.getA());
			LinkedHashMap b = new LinkedHashMap<String, Map>();
			b.put("Configuration", diffs.getB());
			String oldConfig = AmiWebLayoutHelper.toJson(a, service);
			String newConfig = AmiWebLayoutHelper.toJson(b, service);
			AmiWebUtils.diffConfigurations(service, oldConfig, newConfig, "Orginal Script", "New Script", null);
		}

	}

	//returns the old,new config
	private Tuple2<Map<String, Object>, Map<String, Object>> getJsonDiff() {
		LinkedHashMap<String, Object> newConfig = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> oldConfig = new LinkedHashMap<String, Object>();
		List<AmiCenterEntityOptionField> existingFields = new ArrayList<>(this.schemaFields);
		existingFields.addAll(this.configFields);
		for (AmiCenterEntityOptionField of : existingFields) {
			if (of.getHasChanged()) {

				if (of.getInner() instanceof FormPortletSelectField) {
					String val = OH.cast(of.getInner(), FormPortletSelectField.class).getOption(of.getInner().getValue()).getName();
					newConfig.put(of.getId(), val);
				} else
					newConfig.put(of.getId(), of.getValue());
				//get the old value for the field that has changed
				//case1: the old field is empty, do nothing
				//case2: the old field value has changed
				String oldValue = this.valueCache.get(of);
				if (SH.is(oldValue))
					oldConfig.put(of.getId(), oldValue);
			}
		}
		return new Tuple2<Map<String, Object>, Map<String, Object>>(oldConfig, newConfig);
	}

	public boolean hasOfType() {
		boolean hasOftype = MH.anyBits(this.schemaFlags, AmiCenterEntityConsts.HAS_OFTYPE);
		return hasOftype;
	}

	public boolean hasOn() {
		boolean hasOn = MH.anyBits(this.schemaFlags, AmiCenterEntityConsts.HAS_ON);
		return hasOn;
	}

	public boolean hasPriority() {
		boolean hasPriority = MH.anyBits(this.schemaFlags, AmiCenterEntityConsts.HAS_PRIORITY);
		return hasPriority;
	}

	private void sendAuth() {
		AmiCenterQueryDsRequest request = prepareRequest();
		if (request == null)
			return;
		service.sendRequestToBackend(this, request);
	}

	public class AmiCenterManagerSubmitObjectPortlet extends GridPortlet implements AmiWebSpecialPortlet, FormPortletListener, HtmlPortletListener {
		final protected FormPortlet form;//contains all the buttons
		final protected MultiDividerPortlet div;

		//buttons
		protected FormPortletButton confirmTriggerButton;
		protected FormPortletButton undoButton;

		//div children
		//child1
		final protected FormPortlet previewForm;
		final protected FormPortletField<String> previewField;
		protected long sessionId = -1;

		//child2
		final private HtmlPortlet resultForm;

		public AmiCenterManagerSubmitObjectPortlet(PortletConfig config, String previewScript) {
			super(config);
			this.div = new MultiDividerPortlet(generateConfig(), false);
			this.form = new FormPortlet(generateConfig());

			AmiWebHeaderPortlet header = new AmiWebHeaderPortlet(generateConfig());
			header.setShowSearch(false);
			header.updateBlurbPortletLayout("Submit Trigger", "");
			header.setShowLegend(false);
			header.setInformationHeaderHeight(80);
			header.getBarFormPortlet().addFormPortletListener(this);
			this.confirmTriggerButton = header.getBarFormPortlet().addButton(new FormPortletButton("Confirm").setCssStyle("_bg=#4edd49|_fg=#000000"));
			this.undoButton = header.getBarFormPortlet().addButton(new FormPortletButton("Back").setCssStyle("_bg=#b3e5fc|_fg=#000000"));
			addChild(header, 0, 0);
			//div children init
			this.previewForm = new FormPortlet(generateConfig());
			this.previewField = this.previewForm.addField(new AmiWebFormPortletAmiScriptField("Script", getManager(), ""));

			this.previewField.setLeftTopWidthHeightPx(60, 20, (int) (AmiCenterEntityConsts.DEFAULT_PORTLET_WIDTH * 0.85), 0);
			this.previewField.setHeightPct(0.8d);
			this.previewField.setValue(previewScript);

			this.resultForm = new HtmlPortlet(generateConfig());
			this.resultForm.addListener(this);
			this.resultForm.setJavascript("scrollToBottom()");

			this.resultForm.setCssStyle("style.fontFamily=courier|_bg=#000000|_fg=#44FF44|style.overflow=scroll");

			this.div.addChild(this.previewForm);
			this.div.addChild(resultForm);
			this.div.setWeights(new double[] { 2, 1 });
			this.div.setThickness(2);
			this.div.setColor("#00000");

			this.addChild(this.div, 0, 1);

			this.previewForm.addFormPortletListener(this);
			this.sendAuth();
		}

		private void sendAuth() {
			AmiCenterQueryDsRequest request = prepareRequest();
			if (request == null)
				return;
			service.sendRequestToBackend(this, request);
		}

		@Override
		public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {
			if (button == this.confirmTriggerButton) {
				this.previewField.setTopPosPx(0);
				String query = this.previewField.getValue();
				if (SH.isnt(query))
					return;
				AmiCenterQueryDsRequest request = prepareRequest();
				if (request == null)
					return;
				request.setQuery(query);
				request.setQuerySessionId(this.sessionId);
				service.sendRequestToBackend(this, request);
				appendOutput("#44ff44", "\n" + SH.trim(query));
			} else if (button == this.undoButton)
				this.close();

		}

		private void appendOutput(String color, String txt) {
			if (SH.isnt(txt))
				return;
			StringBuilder sb = new StringBuilder();
			sb.append("<span style='color:").append(color).append("'>");
			WebHelper.escapeHtmlNewLineToBr(txt, sb);
			sb.append("</span>");
			this.resultForm.appendHtml(sb.toString());
			//		this.outputField.setValue(sb.toString());
		}

		@Override
		public void onBackendResponse(ResultMessage<Action> result) {
			if (result.getError() != null) {
				getManager().showAlert("Internal Error:" + result.getError().getMessage(), result.getError());
				return;
			}
			AmiCenterQueryDsResponse response = (AmiCenterQueryDsResponse) result.getAction();
			if (sessionId == -1) {
				this.sessionId = response.getQuerySessionId();
				if (this.sessionId >= 0) {
					appendOutput("#ffffff", "Successfully connected to Center, Session ID " + this.sessionId);
				}
			}
			StringBuilder sb = new StringBuilder();

			if (response.getOk()) {
				sb.append("<BR>");
				List<Table> tables = response.getTables();
				if (CH.isntEmpty(tables))
					throw new RuntimeException("Only CREATE Clause is allowed");

				this.resultForm.appendHtml(sb.toString());
				sb.setLength(0);
				if (SH.is(response.getMessage()))
					sb.append(response.getMessage()).append('\n');
				AmiUtils.toMessage(response, service.getFormatterManager().getTimeMillisFormatter().getInner(), sb);
				appendOutput("#ffffff", sb.toString());

				//NOTE This code should be VERY similar to  AmiCenterConsolCmd_Sql
				Class<?> returnType = response.getReturnType();
				boolean hasReturnValue = returnType != null && returnType != Void.class;
				if (hasReturnValue) {
					sb.setLength(0);
					Object returnValue = AmiUtils.getReturnValue(response);
					if (returnValue != null)
						returnType = returnValue.getClass();
					sb.append("(").append(AmiCenterManagerAbstractAddObjectPortlet.this.service.getScriptManager("").forType(returnType));
					sb.append(")");
					String s = AmiUtils.sJson(returnValue);
					if (s != null && s.indexOf('\n') != -1)
						sb.append('\n');
					sb.append(s);
					appendOutput("#FFAAFF", sb.toString());
				}
			} else {
				sb.setLength(0);
				appendOutput("#ff4444", "\n" + response.getMessage() + "\n");
				sb.setLength(0);
				AmiUtils.toMessage(response, service.getFormatterManager().getTimeMillisFormatter().getInner(), sb);
				appendOutput("#ffffff", sb.toString());
			}

			//setIsRunning(false);
		}

		@Override
		public void onUserClick(HtmlPortlet portlet) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onUserCallback(HtmlPortlet htmlPortlet, String id, int mouseX, int mouseY, Callback cb) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onHtmlChanged(String old, String nuw) {
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

}
