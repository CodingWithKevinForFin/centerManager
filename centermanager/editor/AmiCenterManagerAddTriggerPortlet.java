package com.f1.ami.web.centermanager.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.f1.ami.amicommon.msg.AmiCenterQueryDsRequest;
import com.f1.ami.amicommon.msg.AmiCenterQueryDsResponse;
import com.f1.ami.portlets.AmiWebHeaderSearchHandler;
import com.f1.ami.web.AmiWebFormPortletAmiScriptField;
import com.f1.ami.web.AmiWebLayoutHelper;
import com.f1.ami.web.AmiWebLockedPermissiblePortlet;
import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.AmiWebUtils;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.ami.web.graph.AmiCenterGraphNode;
import com.f1.base.Action;
import com.f1.base.Table;
import com.f1.container.ResultMessage;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.PortletManager;
import com.f1.suite.web.portal.impl.ConfirmDialog;
import com.f1.suite.web.portal.impl.ConfirmDialogListener;
import com.f1.suite.web.portal.impl.ConfirmDialogPortlet;
import com.f1.suite.web.portal.impl.RootPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletButton;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletListener;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.suite.web.portal.impl.form.FormPortletTitleField;
import com.f1.suite.web.portal.style.PortletStyleManager_Dialog;
import com.f1.utils.CH;
import com.f1.utils.MH;
import com.f1.utils.OH;
import com.f1.utils.SH;
import com.f1.utils.casters.Caster_Boolean;
import com.f1.utils.casters.Caster_Integer;
import com.f1.utils.casters.Caster_String;
import com.f1.utils.structs.MapInMap;
import com.f1.utils.structs.Tuple2;

//Various work flows
//(1) select triggerParent in CM -> select 'add trigger' ->                                 
//(2) select individual trigger in CM -> select 'edit trigger' ->                                                              
public class AmiCenterManagerAddTriggerPortlet extends FormPortlet
		implements FormPortletListener, ConfirmDialogListener, AmiWebLockedPermissiblePortlet, AmiWebHeaderSearchHandler {
	final private AmiWebService service;
	private final FormPortletButton submitButton = new FormPortletButton("Submit");
	private final FormPortletButton cancelButton = new FormPortletButton("Cancel");
	private final FormPortletButton previewButton = new FormPortletButton("Preview");
	private FormPortletButton diffButton = null;
	private final PortletManager manager;
	//add/edit mode
	private byte mode = AmiCenterEntityConsts.ADD;
	//caches the last value prior to edit
	private Map<AmiCenterManagerOptionField, String> valueCache = new HashMap<AmiCenterManagerOptionField, String>();
	private long sessionId = -1;
	private byte editPolicy = AmiCenterEntityConsts.EDIT_POLICY_STRICT;
	private int changes = CHANGED_UNINIT;
	private Stack<AmiCenterManagerOptionField> existingFields = new Stack<AmiCenterManagerOptionField>();

	private static final int CHANGED_NAME = 1 << 0;
	private static final int CHANGED_PRIORTY = 1 << 1;
	private static final int CHANGED_AMISCRIPT_BLOCK = 1 << 2;
	private static final int CHANGED_OTHER = 1 << 3;
	private static final int CHANGED_UNINIT = -1;

	//option fields
	private AmiCenterManagerOptionField triggerTypeField;
	private AmiCenterManagerOptionField triggerNameField;
	private AmiCenterManagerOptionField triggerOnField;
	private AmiCenterManagerOptionField triggerPriorityField;
	private AmiCenterManagerOptionField configTitleField;

	public AmiCenterManagerAddTriggerPortlet(PortletConfig config) {
		super(config);
		this.service = AmiWebUtils.getService(getManager());
		RootPortlet root = (RootPortlet) this.service.getPortletManager().getRoot();
		this.manager = getManager();
		this.getFormPortletStyle().setCssStyle("_bg=#e2e2e2");

		//trigger type
		triggerTypeField = new AmiCenterManagerOptionField(new FormPortletSelectField(short.class, "Trigger Type" + AmiCenterEntityConsts.REQUIRED_FIELD_ANNOTATION_HTML), true,
				false, "triggerType", "Trigger Type", AmiCenterManagerUtils.formatRequiredField("Trigger Type"));

		initTriggerTypes();

		//trigger name
		triggerNameField = new AmiCenterManagerOptionField(new FormPortletTextField("Trigger Name" + AmiCenterEntityConsts.REQUIRED_FIELD_ANNOTATION_HTML), true, false,
				"triggerName", "Trigger Name", AmiCenterManagerUtils.formatRequiredField("Trigger Name"));

		triggerOnField = new AmiCenterManagerOptionField(new FormPortletTextField("ON" + AmiCenterEntityConsts.REQUIRED_FIELD_ANNOTATION_HTML), true, false, "triggerOn", "ON",
				AmiCenterManagerUtils.formatRequiredField("ON"));

		triggerPriorityField = new AmiCenterManagerOptionField(new FormPortletTextField("PRIORITY"), false, false, "triggerPriority", "PRIORITY", "PRIORITY");

		//enable it when the user selects a trigger type	
		triggerNameField.setDisabled(true);
		triggerOnField.setDisabled(true);

		//configuration
		configTitleField = new AmiCenterManagerOptionField(new FormPortletTitleField("CONFIGURATION (* is a REQUIRED field)"), false, false,
				"CONFIGURATION (* is a REQUIRED field)", "CONFIGURATION (* is a REQUIRED field)", "CONFIGURATION (* is a REQUIRED field)");

		existingFields.addAll(CH.l(triggerTypeField, triggerNameField, triggerOnField, triggerPriorityField, configTitleField));
		addButton(this.submitButton);
		addButton(this.cancelButton);
		addButton(this.previewButton);
		addFormPortletListener(this);
	}

	//Constructor for edit portlet
	public AmiCenterManagerAddTriggerPortlet(PortletConfig config, MapInMap<String, String, String> triggerConfig, byte mode) {
		this(config);
		this.mode = mode;
		if (this.mode == AmiCenterEntityConsts.EDIT) {
			this.changes = 0;//init the changes to 0(Nothing has changed in the beginning)
			String triggerType = triggerConfig.getMulti("COMMON_CONFIG", "triggerType");
			String triggerOn = triggerConfig.getMulti("COMMON_CONFIG", "triggerOn");
			String triggerName = triggerConfig.getMulti("COMMON_CONFIG", "triggerName");
			String triggerPriority = triggerConfig.getMulti("COMMON_CONFIG", "triggerPriority");
			triggerTypeField.setValue(AmiCenterManagerUtils.centerObjectTypeToCode(AmiCenterGraphNode.TYPE_TRIGGER, triggerType));
			triggerOnField.setValue(triggerOn);
			triggerNameField.setValue(triggerName);
			triggerPriorityField.setValue(triggerPriority);
			//cache the common property values
			valueCache.put(triggerTypeField, triggerType);
			valueCache.put(triggerOnField, triggerOn);
			valueCache.put(triggerNameField, triggerName);
			valueCache.put(triggerPriorityField, triggerPriority);
			generateTemplate((short) triggerTypeField.getValue());
			//loop over each field and fill in the value from config
			Map<String, String> mapConfig = triggerConfig.get(triggerType);
			for (AmiCenterManagerOptionField of : this.existingFields) {
				String key = of.getId();
				String value = mapConfig.get(key);
				if (SH.is(value)) {
					FormPortletField<?> fpf = of.getInner();
					Class<?> clzz = fpf.getType();
					if (clzz == String.class) {
						FormPortletField<String> stringField = (FormPortletField<String>) fpf;
						stringField.setValue(value);
					} else if (clzz == Boolean.class) {
						FormPortletField<Boolean> stringField = (FormPortletField<Boolean>) fpf;
						stringField.setValue(Caster_Boolean.INSTANCE.cast(value));
					}
					//stores the last value for all non empty fields
					valueCache.put(of, value);
				}
			}
			//**post config read
			this.triggerNameField.setDisabled(false);
			this.triggerOnField.setDisabled(false);
			//disallow the user to change trigger type(too complicated)
			this.triggerTypeField.setDisabled(true);
			this.diffButton = new FormPortletButton("Diff");
			addButton(this.diffButton);
		}
	}

	private void initTriggerTypes() {
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_NULL, AmiCenterEntityConsts.ENTITY_TYPE_NULL);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AMISCRIPT, AmiCenterEntityConsts.TRIGGER_TYPE_AMISCRIPT);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AGGREGATE, AmiCenterEntityConsts.TRIGGER_TYPE_AGGREGATE);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_PROJECTION, AmiCenterEntityConsts.TRIGGER_TYPE_PROJECTION);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_JOIN, AmiCenterEntityConsts.TRIGGER_TYPE_JOIN);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_DECORATE, AmiCenterEntityConsts.TRIGGER_TYPE_DECORATE);
		triggerTypeField.addOption(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_RELAY, AmiCenterEntityConsts.TRIGGER_TYPE_RELAY);
	}
	@Override
	public void doSearch() {
		// TODO Auto-generated method stub

	}
	@Override
	public void doSearchNext() {
		// TODO Auto-generated method stub

	}
	@Override
	public void doSearchPrevious() {
		// TODO Auto-generated method stub

	}
	@Override
	public boolean onButton(ConfirmDialog source, String id) {
		return false;
	}
	@Override
	public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {
		if (button == this.cancelButton) {
			close();
		} else if (button == this.previewButton) {
			String text = null;
			if ((short) (this.triggerTypeField.getValue()) == AmiCenterEntityConsts.TRIGGER_TYPE_CODE_NULL)
				text = "Please select a trigger type";
			else {
				text = AmiCenterManagerUtils.formatPreviewScript(previewTriggerScript());
			}
			PortletStyleManager_Dialog dp = service.getPortletManager().getStyleManager().getDialogStyle();
			final PortletManager portletManager = service.getPortletManager();
			ConfirmDialogPortlet cdp = new ConfirmDialogPortlet(portletManager.generateConfig(), text, ConfirmDialogPortlet.TYPE_MESSAGE);
			int w = dp.getDialogWidth();
			int h = dp.getDialogHeight();
			portletManager.showDialog("Trigger Script", cdp, w + 200, h);
		} else if (button == this.submitButton) {
			prepareRequest();
			String triggerScript = previewTriggerScript();
			PortletStyleManager_Dialog dp = service.getPortletManager().getStyleManager().getDialogStyle();
			final PortletManager portletManager = service.getPortletManager();
			switch (this.mode) {
				case AmiCenterEntityConsts.ADD:
					if (triggerScript != null) {//triggerScript == null means the type is <NULL>
						boolean isTriggerComplete = validateTrigger(triggerScript);
						if (isTriggerComplete) {
							getManager().showDialog("Submit Trigger", new AmiCenterManagerSubmitEditScriptPortlet(this.service, generateConfig(), previewTriggerScript()),
									AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_WIDTH, AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_HEIGHT);
						}
					} else {//if trigger type is null, throw alert
						throwAlertDialogue("Please select a trigger type", "Trigger Script");
					}
					break;
				case AmiCenterEntityConsts.EDIT:
					//step0: shortCircuit for only change on PIORITY
					switch (this.editPolicy) {
						case AmiCenterEntityConsts.EDIT_POLICY_STRICT:
							boolean canShortCircuit = canShortCircuit();
							if (canShortCircuit) {
								doReplaceTrigger();
							}
							if (this.changes == 0) {
								String warning = "No trigger changes detected";
								throwAlertDialogue(warning, "Trigger Edit Warning");
								return;
							}
							//step1: handle more complex edit cases

							break;
						case AmiCenterEntityConsts.EDIT_POLICY_FUZZY:
							doReplaceTrigger();
							break;
						default:
							throw new RuntimeException("Unknown edit policy: " + this.editPolicy);

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
	private void doReplaceTrigger() {
		if (this.mode != AmiCenterEntityConsts.EDIT || this.changes == 0)
			throw new IllegalStateException();
		String query = null;
		if (this.changes == CHANGED_NAME) {
			query = "RENAME TRIGGER " + this.valueCache.get(this.triggerNameField) + " TO " + Caster_String.INSTANCE.cast(this.triggerNameField.getValue());
		} else
			query = "DROP TRIGGER " + this.valueCache.get(this.triggerNameField) + ";" + previewTriggerScript();
		getManager().showDialog("Submit Trigger", new AmiCenterManagerSubmitEditScriptPortlet(this.service, generateConfig(), query),
				AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_WIDTH, AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_HEIGHT);
	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		switch (this.mode) {
			case AmiCenterEntityConsts.ADD:
				if (field == triggerTypeField.getInner() && !field.getValue().equals(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_NULL)) {
					triggerNameField.setDisabled(false);
					triggerOnField.setDisabled(false);
					resetTemplate();
					generateTemplate((short) triggerTypeField.getValue());

				} else if (field == triggerTypeField.getInner() && field.getValue().equals(AmiCenterEntityConsts.TRIGGER_TYPE_CODE_NULL)) {
					triggerNameField.setDisabled(true);
					triggerOnField.setDisabled(true);
					resetTemplate();
				}
				break;
			case AmiCenterEntityConsts.EDIT:
				AmiCenterManagerOptionField toFind = null;
				for (AmiCenterManagerOptionField of : this.existingFields) {
					if (of.getInner() == field)
						toFind = of;
				}
				String lastVal = valueCache.get(toFind);
				String nuwVal = Caster_String.INSTANCE.cast(field.getValue());
				if (lastVal != null) {//if the field is configured
					if (!nuwVal.equals(lastVal)) {
						field.setTitle(AmiCenterEntityConsts.CHANGED_FIELD_ANNOTATION_HTML + field.getTitle());
						toFind.setHasChanged(true);
						switch (toFind.getId()) {
							case "triggerName":
								changed(CHANGED_NAME);
								break;
							case "triggerPriority":
								changed(CHANGED_PRIORTY);
								break;
							default:
								changed(CHANGED_OTHER);
								break;
						}
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
	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {

	}

	private void resetTemplate() {
		while (existingFields.peek() != configTitleField) {
			FormPortletField toRemove = existingFields.pop().getInner();
			removeField(toRemove);
		}
	}

	private void generateTemplate(short type) {
		switch (type) {
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AMISCRIPT:
				insertOptionField("canMutateRow", FormPortletCheckboxField.class, false);
				insertOptionField("runOnStartup", FormPortletCheckboxField.class, false);
				insertOptionField("onInsertingScript", AmiWebFormPortletAmiScriptField.class, false);
				insertOptionField("onUpdatingScript", AmiWebFormPortletAmiScriptField.class, false);
				insertOptionField("onDeletingScript", AmiWebFormPortletAmiScriptField.class, false);
				insertOptionField("onInsertedScript", AmiWebFormPortletAmiScriptField.class, false);
				insertOptionField("onUpdatedScript", AmiWebFormPortletAmiScriptField.class, false);
				insertOptionField("rowVar", FormPortletTextField.class, false);
				insertOptionField("onStartupScript", AmiWebFormPortletAmiScriptField.class, false);
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_AGGREGATE:
				insertOptionField("groupBys", FormPortletTextField.class, true);
				insertOptionField("selects", FormPortletTextField.class, true);
				insertOptionField("allowExternalUpdates", FormPortletCheckboxField.class, false);
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_DECORATE:
				insertOptionField("on", FormPortletTextField.class, true);
				insertOptionField("selects", FormPortletTextField.class, true);
				insertOptionField("keysChange", FormPortletCheckboxField.class, false);
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_JOIN:
				insertOptionField("type", CH.l("INNER", "LEFT", "RIGHT", "OUTER", "LEFT ONLY", "RIGHT ONLY", "OUTER ONLY"), false);
				insertOptionField("on", FormPortletTextField.class, true);
				insertOptionField("selects", FormPortletTextField.class, true);
				insertOptionField("wheres", FormPortletTextField.class, false);
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_PROJECTION:
				insertOptionField("selects", FormPortletTextField.class, true);
				insertOptionField("wheres", FormPortletTextField.class, false);
				insertOptionField("allowExternalUpdates", FormPortletCheckboxField.class, false);
				break;
			case AmiCenterEntityConsts.TRIGGER_TYPE_CODE_RELAY:
				insertOptionField("hosts", FormPortletTextField.class, true);
				insertOptionField("port", FormPortletTextField.class, true);
				insertOptionField("login", FormPortletTextField.class, true);
				insertOptionField("keystoreFile", FormPortletTextField.class, false);
				insertOptionField("keystorePass", FormPortletTextField.class, false);
				insertOptionField("derivedValues", FormPortletTextField.class, false);
				insertOptionField("inserts", FormPortletTextField.class, false);
				insertOptionField("updates", FormPortletTextField.class, false);
				insertOptionField("deletes", FormPortletTextField.class, false);
				insertOptionField("target", FormPortletTextField.class, false);
				insertOptionField("where", FormPortletTextField.class, false);
				break;
			default:
				throw new RuntimeException("Unknow trigger type code: " + type);

		}
	}

	private void insertOptionField(String option, Class<?> fieldType, boolean required) {
		if (required)
			insertOptionField(option, option + AmiCenterEntityConsts.REQUIRED_FIELD_ANNOTATION_HTML, fieldType, true);
		else
			insertOptionField(option, option, fieldType, false);
	}

	private void insertOptionField(String option, String formattedOption, Class<?> fieldType, boolean isRequired) {
		FormPortletField fpf = null;
		if (fieldType == FormPortletCheckboxField.class) {
			fpf = new FormPortletCheckboxField(formattedOption);
		} else if (fieldType == FormPortletTextField.class) {
			fpf = new FormPortletTextField(formattedOption);
		} else if (fieldType == AmiWebFormPortletAmiScriptField.class) {
			fpf = new AmiWebFormPortletAmiScriptField(formattedOption, getManager(), "");
			fpf.setWidthPct(0.60);
			fpf.setHeightPct(0.25);
		} else
			throw new RuntimeException("Unknown fieldType" + fieldType);
		if (fpf != null)
			existingFields.add(new AmiCenterManagerOptionField(fpf, isRequired, false, option, option, formattedOption));
	}

	//option + AmiCenterEntityTypeConsts.REQUIRED_FIELD_ANNOTATION_HTML
	private void insertOptionField(String option, List<String> optionValues, boolean required) {
		String formattedOption = null;
		if (required)
			formattedOption = option + AmiCenterEntityConsts.REQUIRED_FIELD_ANNOTATION_HTML;
		else
			formattedOption = option;
		FormPortletSelectField<String> fpf = new FormPortletSelectField<String>(String.class, formattedOption);
		for (String v : optionValues)
			fpf.addOption(v, v);
		if (fpf != null)
			existingFields.add(new AmiCenterManagerOptionField(fpf, required, false, option, option, formattedOption));

	}

	private String previewTriggerScript() {
		if ((short) (this.triggerTypeField.getValue()) == AmiCenterEntityConsts.TRIGGER_TYPE_CODE_NULL)
			return null;
		StringBuilder sb = new StringBuilder("CREATE TRIGGER ");
		sb.append(parseValueForRequiredField(this.triggerNameField.getInner()));
		sb.append(" OFTYPE ").append(SH.noNull(OH.cast(this.triggerTypeField.getInner(), FormPortletSelectField.class).getOption(this.triggerTypeField.getValue()).getName()));
		sb.append(" ON ").append(parseValueForRequiredField(this.triggerOnField.getInner()));
		if (SH.is(this.triggerPriorityField.getValue()))
			sb.append(" PRIORITY ").append(this.triggerPriorityField.getValue());
		sb.append(" USE ").append(prepareUseClause());
		sb.append(";");
		return sb.toString();
	}

	//only non-empty options will be appended to the use clause
	private String prepareUseClause() {
		StringBuilder script = new StringBuilder();
		List<AmiCenterManagerOptionField> configFields = new ArrayList<AmiCenterManagerOptionField>(this.existingFields).subList(5, this.existingFields.size());
		//contains nonEmpty fields + required fields
		List<AmiCenterManagerOptionField> nonEmptyAndRequiredFields = new ArrayList<AmiCenterManagerOptionField>();
		for (AmiCenterManagerOptionField of : configFields) {
			FormPortletField fpf = of.getInner();
			if (SH.is(fpf.getValue()) || of.getIsRequired())
				nonEmptyAndRequiredFields.add(of);
		}
		for (AmiCenterManagerOptionField of : nonEmptyAndRequiredFields) {
			FormPortletField fpf = of.getInner();
			if (fpf instanceof AmiWebFormPortletAmiScriptField)
				script.append(of.getId()).append("=").append(SH.doubleQuote((String) fpf.getValue())).append("").append(" ");
			else if (of.getIsRequired() && !SH.is(fpf.getValue())) //an empty required field
				script.append(of.getId()).append("=").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
			else {//non-empty field
				if (of.getIsRequired())
					script.append(of.getId()).append("=\"").append(fpf.getValue()).append("\"").append(" ");
				else
					script.append(fpf.getTitle()).append("=\"").append(fpf.getValue()).append("\"").append(" ");
			}
		}

		return script.toString();
	}

	@Deprecated
	private static boolean isRequiredField(FormPortletField<?> f) {
		return f.getTitle().contains("*");
	}

	private static String parseValueForRequiredField(FormPortletField<?> f) {
		if (SH.isnt(f.getValue()))
			return AmiCenterEntityConsts.REQUIRED_FEILD_WARNING;
		return Caster_String.INSTANCE.cast(f.getValue());

	}

	private String getPendingRequiredFields() {
		StringBuilder sb = new StringBuilder();
		for (AmiCenterManagerOptionField of : this.existingFields) {
			FormPortletField fpf = of.getInner();
			if (SH.isnt(fpf.getValue()) && of.getIsRequired())
				sb.append(SH.beforeLast(fpf.getTitle(), AmiCenterEntityConsts.REQUIRED_FIELD_ANNOTATION_HTML)).append(",");
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	private boolean validateTrigger(String script) {
		boolean isValid = !script.contains(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);
		if (!isValid) {
			PortletStyleManager_Dialog dp = service.getPortletManager().getStyleManager().getDialogStyle();
			final PortletManager portletManager = service.getPortletManager();
			String warning = "Missing required fields: " + "<b>" + getPendingRequiredFields() + "</b>";
			ConfirmDialogPortlet cdp = new ConfirmDialogPortlet(portletManager.generateConfig(), warning, ConfirmDialogPortlet.TYPE_MESSAGE);
			int w = dp.getDialogWidth();
			int h = dp.getDialogHeight();
			portletManager.showDialog("Trigger Script", cdp, w + 200, h);
			return false;
		} else
			return true;
	}

	/**
	 * step1: validate all the entries that have changed step2: evaluate shortcircuit(priority and name): (1). if only the name has changed, check if the new name collides with an
	 * existing name (2). if the priority has changed, shortcircuit to drop + recreate trigger (3). if the amiscript block has changed, evaluate the amiscript block
	 * 
	 * @return
	 */
	private boolean canShortCircuit() {
		if (this.mode != AmiCenterEntityConsts.EDIT)
			throw new IllegalStateException("validateReplace() is only used in edit mode");
		//validate entry that has changed(only check priority and name)
		if (this.changes == CHANGED_PRIORTY) {
			Integer nuw_priority = Caster_Integer.INSTANCE.castNoThrow(this.triggerPriorityField.getValue());
			if (SH.is(nuw_priority))
				return true;
			return false;
		} else if (this.changes == CHANGED_NAME) {
			String nuwName = (String) this.triggerNameField.getValue();
			if (SH.is(nuwName)) {
				return true;
			}
			throw new RuntimeException("Invalid table name:" + nuwName);
		}
		return false;
	}

	private List<FormPortletField<?>> getNonEmptyFields() {
		List<FormPortletField<?>> flds = new ArrayList<FormPortletField<?>>();
		for (AmiCenterManagerOptionField of : this.existingFields) {
			FormPortletField<?> fpf = of.getInner();
			if (SH.is(Caster_String.INSTANCE.cast(fpf.getValue())))
				flds.add(fpf);
		}
		return flds;
	}

	//returns the old,new config
	private Tuple2<Map<String, Object>, Map<String, Object>> getJsonDiff() {
		LinkedHashMap<String, Object> newConfig = new LinkedHashMap<String, Object>();
		LinkedHashMap<String, Object> oldConfig = new LinkedHashMap<String, Object>();
		for (AmiCenterManagerOptionField of : this.existingFields) {
			if (of.getHasChanged()) {
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

	private void changed(int mask) {
		if (MH.allBits(changes, mask))
			return;
		changes |= mask;
	}

	private boolean hasChange(int mask) {
		return MH.anyBits(changes, mask);
	}

	private void sendQueryToBackend(String query) {
		if (SH.isnt(query))
			return;
		AmiCenterQueryDsRequest request = prepareRequest();
		if (request == null)
			return;
		request.setQuery(query);
		request.setQuerySessionId(this.sessionId);
		service.sendRequestToBackend(this, request);
	}

	private AmiCenterQueryDsRequest prepareRequest() {
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

	@Override
	public void onBackendResponse(ResultMessage<Action> result) {
		if (result.getError() != null) {
			getManager().showAlert("Internal Error:" + result.getError().getMessage(), result.getError());
			return;
		}
		result.getRequestMessage().getFuture();
		AmiCenterQueryDsResponse response = (AmiCenterQueryDsResponse) result.getAction();
		if (sessionId == -1) {
			this.sessionId = response.getQuerySessionId();
		}

		if (response.getOk()) {
			List<Table> tables = response.getTables();
			//validate if rename trigger is successful(whether the new trigger name collides with existing names)
			//if (tables.size() == 1 && "TRIGGERS".equals(tables.get(0).getTitle())) {
			//				if (tables.get(0).getRows().isEmpty()) //if there is no matching name, rename sucuessful
			//					doReplaceTrigger();
			//				else {
			//					throwAlertDialogue("Trigger Name Already Exists: " + triggerNameField.getValue(), "Trigger Edit Warning");
			//				}
		}
	}

	public void throwAlertDialogue(String warning, String dialogueTitle) {
		PortletStyleManager_Dialog dp = service.getPortletManager().getStyleManager().getDialogStyle();
		final PortletManager portletManager = service.getPortletManager();
		ConfirmDialogPortlet cdp = new ConfirmDialogPortlet(portletManager.generateConfig(), warning, ConfirmDialogPortlet.TYPE_MESSAGE);
		int w = dp.getDialogWidth();
		int h = dp.getDialogHeight();
		portletManager.showDialog(dialogueTitle, cdp, w + 200, h);
	}

	public class AmiCenterManagerOptionField {
		FormPortletField<?> inner;
		boolean isRequired;
		boolean hasChanged;
		String id;//e.g triggerName
		String title;//e.g. Trigger Name
		String formattedTitle;//e.g <span style:color=red;> trigger Name <span>

		public AmiCenterManagerOptionField(FormPortletField<?> inner, boolean isRequired, boolean hasChanged, String id, String title, String formattedTitle) {
			this.inner = inner;
			this.isRequired = isRequired;
			this.hasChanged = hasChanged;
			this.id = id;
			this.title = title;
			if (this.isRequired) {
				this.formattedTitle = AmiCenterManagerUtils.formatRequiredField(this.title);
			} else
				this.formattedTitle = this.title;
			addMe();
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
		public FormPortletField<?> addMe() {
			return AmiCenterManagerAddTriggerPortlet.this.addField(this.inner);
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

		public Object getValue() {
			if (this.inner instanceof FormPortletSelectField)
				return ((FormPortletSelectField<Short>) this.inner).getValue();
			return this.inner.getValue();
		}

		public void setValue(Object value) {
			if (this.inner instanceof FormPortletSelectField)
				((FormPortletSelectField) this.inner).setValue(value);
			else if (this.inner instanceof FormPortletTextField)
				((FormPortletTextField) this.inner).setValue((String) value);
		}

		public void setHasChanged(boolean hasChanged) {
			this.hasChanged = hasChanged;
		}

	}
}
