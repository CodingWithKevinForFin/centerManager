package com.f1.ami.web.centermanager.nuweditor.triggerEditors.smarteditors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.f1.ami.web.AmiWebMenuUtils;
import com.f1.ami.web.AmiWebUtils;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.menu.impl.BasicWebMenu;
import com.f1.suite.web.menu.impl.BasicWebMenuLink;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletButton;
import com.f1.suite.web.portal.impl.form.FormPortletButtonField;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuFactory;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuListener;
import com.f1.suite.web.portal.impl.form.FormPortletDivField;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletListener;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextAreaField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.suite.web.portal.impl.form.FormPortletTitleField;
import com.f1.utils.CH;
import com.f1.utils.SH;

public class AmiCenterManagerTriggerEditor_ProjectionSelectEditor extends FormPortlet
		implements FormPortletListener, FormPortletContextMenuFactory, FormPortletContextMenuListener {
	private FormPortletTitleField selectTitleField;

	public StringBuilder selectOutput = new StringBuilder();

	final private FormPortletSelectField<String> targetColumnField;

	final private FormPortletTextField selectExpressionField;

	final private FormPortletTextAreaField outputField;
	final private FormPortletButtonField addButton;
	final private FormPortletButtonField clearButton;

	private List<FormPortletSelectField<String>> targetColumns;
	private List<FormPortletSelectField<String>> sourceColumns;

	private static final int COLNAME_WIDTH = 200; //60
	private static final int DEFAULT_ROWHEIGHT = 25;
	private static final int DEFAULT_LEFTPOS = 80; //164
	private static final int DEFAULT_SPACING = 10;
	private static final int DEFAULT_TITLEHEIGHT = 27;
	private static final int DEFAULT_TOPPOS = DEFAULT_SPACING + DEFAULT_TITLEHEIGHT;

	public AmiCenterManagerTriggerEditor_ProjectionSelectEditor(PortletConfig config) {
		super(config);

		this.targetColumns = new ArrayList<FormPortletSelectField<String>>();
		this.sourceColumns = new ArrayList<FormPortletSelectField<String>>();
		this.selectTitleField = this.addField(new FormPortletTitleField("selects"));
		this.selectTitleField.setLeftPosPx(400);
		this.selectTitleField.setHelp(" A comma delimited list of expressions on how to populate target columns from source columns." + "<br>"
				+ "<b><i style=\"color:blue\">targetTableColumn = aggregate_on_sourceTableColumns [,targetTableColumn = aggregate_on_sourceTableColumns ...]</i></b>");

		FormPortletDivField sourceTitleDiv = this.addField(new FormPortletDivField("<b> Source </b>"));
		FormPortletDivField targetTitleDiv = this.addField(new FormPortletDivField("<b> Target </b>"));

		targetTitleDiv.setLeftPosPx(DEFAULT_LEFTPOS + 45).setTopPosPx(DEFAULT_TOPPOS).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(200);
		sourceTitleDiv.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS + 48).setTopPosPx(DEFAULT_TOPPOS).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(200);

		targetColumnField = this.addField(new FormPortletSelectField(String.class, ""));
		targetColumnField.addOption("act", "act");
		targetColumnField.addOption("region", "region");
		targetColumnField.addOption("cnt", "cnt");
		targetColumnField.addOption("value", "value");

		targetColumnField.setWidthPx(COLNAME_WIDTH);
		targetColumnField.setHeightPx(DEFAULT_ROWHEIGHT);
		targetColumnField.setLeftPosPx(DEFAULT_LEFTPOS);
		targetColumnField.setTopPosPx(DEFAULT_TOPPOS * 2);

		selectExpressionField = this.addField(new FormPortletTextField(""));
		selectExpressionField.setHasButton(true);
		selectExpressionField.setCorrelationData(new AmiTriggerSelectFormula() {

			@Override
			public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition) {
				BasicWebMenu r = new BasicWebMenu();
				AmiWebMenuUtils.createOperatorsMenu(r, AmiWebUtils.getService(getManager()), "");
				//TODO:sub this with real values
				WebMenu variables = createVariablesMenu("Variables", CH.s("account", "region", "qty", "px"));
				r.add(variables);
				WebMenu tableNames = createVariablesMenu("Table Names", CH.s("Accounts1", "Accounts2"));
				r.add(tableNames);
				return r;
			}

			@Override
			public void onContextMenu(FormPortletField field, String action) {
				AmiWebMenuUtils.processContextMenuAction(AmiWebUtils.getService(getManager()), action, field);

			}
		});
		selectExpressionField.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS).setTopPosPx(DEFAULT_TOPPOS * 2).setHeightPx(DEFAULT_ROWHEIGHT)
				.setWidthPx(COLNAME_WIDTH);

		this.addButton = this.addField(new FormPortletButtonField("").setValue("Add"));
		this.addButton.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS + 250).setTopPosPx(DEFAULT_TOPPOS * 2).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(80);
		this.addButton.setCssStyle("_fm=bold|_fg=#FFFFFF|_bg=#FFA500|style.borderRadius=5px");

		this.clearButton = this.addField(new FormPortletButtonField("").setValue("Clear"));
		this.clearButton.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS + 340).setTopPosPx(DEFAULT_TOPPOS * 2).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(80);

		this.outputField = this.addField(new FormPortletTextAreaField("Output"));
		this.outputField.setLeftPosPx(DEFAULT_LEFTPOS).setTopPosPx(DEFAULT_TOPPOS * 2 + DEFAULT_ROWHEIGHT * 2).setHeightPx(DEFAULT_ROWHEIGHT * 3).setWidthPx(600);

		this.addFormPortletListener(this);
		this.addMenuListener(this);
		this.setMenuFactory(this);

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
	public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		if (field == this.addButton) {
			addSelectClause();
		} else if (field == this.clearButton) {
			clearSelectClause();
		} else if (field == this.outputField) {
			this.selectOutput.setLength(0);
			this.selectOutput.append(field.getValue());
		}

	}

	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		// TODO Auto-generated method stub

	}

	public static interface AmiTriggerSelectFormula {
		public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition);
		public void onContextMenu(FormPortletField field, String action);
	}

	@Override
	public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition) {
		AmiTriggerSelectFormula t = (AmiTriggerSelectFormula) field.getCorrelationData();
		return t.createMenu(formPortlet, field, cursorPosition);
	}

	@Override
	public void onContextMenu(FormPortlet portlet, String action, FormPortletField field) {
		if (field == selectExpressionField) {
			AmiTriggerSelectFormula cb = (AmiTriggerSelectFormula) field.getCorrelationData();
			cb.onContextMenu(field, action);
		}
	}

	public void addSelectClause() {
		String targetColumn = this.targetColumnField.getValue();
		String sourceExpression = this.selectExpressionField.getValue();
		if (this.selectOutput.length() == 0)
			this.selectOutput.append(targetColumn).append(" = ").append(sourceExpression);
		else
			this.selectOutput.append(',').append(targetColumn).append(" = ").append(sourceExpression);
		this.outputField.setValue(this.selectOutput.toString());
	}

	public void clearSelectClause() {
		this.selectOutput.setLength(0);
		this.outputField.setValue("");
	}

	public String getOutput() {
		return this.outputField.getValue();
	}
}
