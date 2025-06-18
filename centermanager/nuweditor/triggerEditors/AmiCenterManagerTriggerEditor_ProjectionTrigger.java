
package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import java.util.Map;
import java.util.Set;

import com.f1.ami.web.AmiWebMenuUtils;
import com.f1.ami.web.AmiWebUtils;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.ami.web.centermanager.nuweditor.triggerEditors.smarteditors.AmiCenterManagerTriggerEditor_ProjectionSelectEditor;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.menu.impl.BasicWebMenu;
import com.f1.suite.web.menu.impl.BasicWebMenuLink;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletButton;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuFactory;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuListener;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletListener;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.utils.CH;
import com.f1.utils.SH;

public class AmiCenterManagerTriggerEditor_ProjectionTrigger extends AmiCenterManagerAbstractTriggerEditor
		implements FormPortletListener, FormPortletContextMenuFactory, FormPortletContextMenuListener {
	private static final int FORM_LEFT_POSITION = 120;
	private static final int FORM_WIDTH = 550;
	private static final int FORM_HEIGHT = 300;

	final private FormPortletCheckboxField allowExternalUpdateField;
	final private FormPortletTextField wheresField;
	final private AmiCenterManagerTriggerEditor_ProjectionSelectEditor selectsEditor;

	public AmiCenterManagerTriggerEditor_ProjectionTrigger(PortletConfig config) {
		super(config);

		allowExternalUpdateField = form.addField(new FormPortletCheckboxField("allowExternalUpdates"));
		allowExternalUpdateField.setHelp("Optional. Value is either true or false (false by default)." + "<br>"
				+ "If true, then other processes (i.e triggers, UPDATEs) are allowed to perform UPDATEs on the target table." + "<br>"
				+ " Please use precaution when using this feature, since updating cells controlled by the aggregate trigger will result into an undesirable state.");

		wheresField = form.addField(new FormPortletTextField(AmiCenterManagerUtils.formatRequiredField("wheres")));
		wheresField.setGroupName(AmiCenterEntityConsts.GROUP_NAME_REQUIRED_FIELD);
		wheresField.setLeftPosPx(FORM_LEFT_POSITION).setWidth(600).setHeight(25).setTopPosPx(50);
		wheresField.setHelp("A comma-delimited list of boolean expressions that must all be true on a source table's row in order for it to be projected into the target table:"
				+ "<br>" + "<b><i style=\"color:blue\">expression_on_sourceTableColumns,[ expression_on_sourceTableColumns ...]</i></b>");
		wheresField.setHasButton(true);
		wheresField.setCorrelationData(new Formula() {

			@Override
			public void onContextMenu(FormPortletField field, String action) {
				AmiWebMenuUtils.processContextMenuAction(AmiWebUtils.getService(getManager()), action, field);

			}

			@Override
			public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition) {
				BasicWebMenu r = new BasicWebMenu();
				AmiWebMenuUtils.createOperatorsMenu(r, AmiWebUtils.getService(getManager()), "");
				WebMenu variables = createVariablesMenu("Variables", CH.s("account", "region", "qty", "px"));
				r.add(variables);
				return r;
			}
		});

		selectsEditor = new AmiCenterManagerTriggerEditor_ProjectionSelectEditor(generateConfig());
		addChild(form, 0, 0, 1, 1);
		addChild(selectsEditor, 0, 1, 1, 1);
		setRowSize(0, 150);

		this.form.addFormPortletListener(this);
		this.form.addMenuListener(this);
		this.form.setMenuFactory(this);
	}

	public static interface Formula {
		public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition);
		public void onContextMenu(FormPortletField field, String action);
	}

	//add
	public static WebMenu createVariablesMenu(String menuName, Set<String> columns) {
		WebMenu variables = new BasicWebMenu(menuName, true);

		for (String column : CH.sort(columns, SH.COMPARATOR_CASEINSENSITIVE_STRING)) {
			variables.add(new BasicWebMenuLink(column, true, "var_" + column).setAutoclose(false).setCssStyle("_fm=courier"));
		}
		return variables;
	}

	@Override
	public String getKeyValuePairs() {
		StringBuilder sb = new StringBuilder();
		if (SH.is(wheresField.getValue()))
			sb.append(" wheres = ").append(SH.doubleQuote(wheresField.getValue()));
		else
			sb.append(" wheres = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (SH.is(selectsEditor.getOutput()))
			sb.append(" selects = ").append(SH.doubleQuote(selectsEditor.getOutput()));
		else
			sb.append(" selects = ").append(AmiCenterEntityConsts.REQUIRED_FEILD_WARNING);

		if (allowExternalUpdateField.getBooleanValue())
			sb.append(" allowExternalUpdate = ").append(SH.doubleQuote("true"));
		return sb.toString();
	}

	@Override
	public void onContextMenu(FormPortlet portlet, String action, FormPortletField field) {
		if (field == wheresField) {
			Formula cb = (Formula) field.getCorrelationData();
			cb.onContextMenu(field, action);
		}

	}

	@Override
	public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition) {
		Formula t = (Formula) field.getCorrelationData();
		return t.createMenu(formPortlet, field, cursorPosition);
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
