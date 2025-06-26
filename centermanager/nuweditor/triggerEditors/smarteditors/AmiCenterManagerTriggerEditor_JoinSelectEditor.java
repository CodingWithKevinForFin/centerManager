package com.f1.ami.web.centermanager.nuweditor.triggerEditors.smarteditors;

import java.util.Map;
import java.util.Set;

import com.f1.ami.web.AmiWebMenuUtils;
import com.f1.ami.web.AmiWebService;
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
import com.f1.suite.web.portal.impl.form.FormPortletTitleField;
import com.f1.utils.CH;
import com.f1.utils.SH;

public class AmiCenterManagerTriggerEditor_JoinSelectEditor extends FormPortlet implements FormPortletListener, FormPortletContextMenuFactory, FormPortletContextMenuListener {
	final private FormPortletTitleField selectTitleField;
	final private FormPortletSelectField<String> targetColumnField;

	final private FormPortletTextAreaField sourceColumnField;

	final private FormPortletTextAreaField outputField;
	final private FormPortletButtonField addButton;
	final private FormPortletButtonField clearButton;
	public StringBuilder output = new StringBuilder();

	final private AmiWebService service;

	private String leftTable;
	private String rightTable;

	private Set<String> leftTableColumns;
	private Set<String> rightTableColumns;

	private static final int COLNAME_WIDTH = 150; //60
	private static final int DEFAULT_ROWHEIGHT = 25;
	private static final int DEFAULT_LEFTPOS = 80; //164
	private static final int DEFAULT_SPACING = 10;
	private static final int DEFAULT_TITLEHEIGHT = 27;
	private static final int DEFAULT_TOPPOS = DEFAULT_SPACING + DEFAULT_TITLEHEIGHT;

	public AmiCenterManagerTriggerEditor_JoinSelectEditor(PortletConfig config) {
		super(config);
		this.service = AmiWebUtils.getService(getManager());
		this.selectTitleField = this.addField(new FormPortletTitleField("selects"));
		this.selectTitleField.setLeftPosPx(400);
		this.selectTitleField.setHelp(("An expression for how to relate the two tables in the form:" + "<br>"
				+ "<b><i style=\"color:blue\">leftColumn == rightColumn [ && leftColumn == rightColumn ... ]</i></b>"));

		FormPortletDivField sourceTitleDiv = this.addField(new FormPortletDivField("<b> Source </b>"));
		FormPortletDivField targetTitleDiv = this.addField(new FormPortletDivField("<b> Target </b>"));

		targetTitleDiv.setLeftPosPx(DEFAULT_LEFTPOS + 45).setTopPosPx(DEFAULT_TOPPOS).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(200);
		sourceTitleDiv.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS + 48).setTopPosPx(DEFAULT_TOPPOS).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(200);

		targetColumnField = this.addField(new FormPortletSelectField(String.class, ""));
		targetColumnField.setWidthPx(COLNAME_WIDTH);
		targetColumnField.setHeightPx(DEFAULT_ROWHEIGHT);
		targetColumnField.setLeftPosPx(DEFAULT_LEFTPOS);
		targetColumnField.setTopPosPx(DEFAULT_TOPPOS * 2);

		sourceColumnField = this.addField(new FormPortletTextAreaField("=&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp"));
		sourceColumnField.setWidthPx(COLNAME_WIDTH * 3);
		sourceColumnField.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS);
		sourceColumnField.setTopPosPx(DEFAULT_TOPPOS * 2);
		sourceColumnField.setHeightPx(DEFAULT_ROWHEIGHT * 4);

		sourceColumnField.setHasButton(true);
		sourceColumnField.setCorrelationData(new Formula() {
			@Override
			public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition) {
				BasicWebMenu r = new BasicWebMenu();
				AmiWebMenuUtils.createOperatorsMenu(r, AmiWebUtils.getService(getManager()), "");
				if (leftTable != null && rightTable != null) {
					//table names [left, right]
					WebMenu tableNames = new BasicWebMenu("Table Names", true);
					String leftTableHTML = leftTable + "&nbsp;&nbsp;&nbsp;<i>Left Table</i>";
					String rightTableHTML = rightTable + "&nbsp;&nbsp;&nbsp;<i>Right Table</i>";
					tableNames.add(new BasicWebMenuLink(leftTableHTML, true, "var_" + leftTable).setAutoclose(false).setCssStyle("_fm=courier"));
					tableNames.add(new BasicWebMenuLink(rightTableHTML, true, "var_" + rightTable).setAutoclose(false).setCssStyle("_fm=courier"));
					r.add(tableNames);
				}

				if (leftTableColumns != null) {
					//table columns [leftColumn, rightColumn]
					WebMenu leftColumns = new BasicWebMenu("Left Table Columns", true);
					for (String c : CH.sort(leftTableColumns, SH.COMPARATOR_CASEINSENSITIVE_STRING))
						leftColumns.add(new BasicWebMenuLink(c, true, "var_" + c).setAutoclose(false).setCssStyle("_fm=courier"));
					r.add(leftColumns);
				}
				if (rightTableColumns != null) {
					WebMenu rightColumns = new BasicWebMenu("Right Table Columns", true);
					for (String c : CH.sort(rightTableColumns, SH.COMPARATOR_CASEINSENSITIVE_STRING))
						rightColumns.add(new BasicWebMenuLink(c, true, "var_" + c).setAutoclose(false).setCssStyle("_fm=courier"));
					r.add(rightColumns);
				}

				return r;
			}

			@Override
			public void onContextMenu(FormPortletField field, String action) {
				AmiWebMenuUtils.processContextMenuAction(AmiWebUtils.getService(getManager()), action, field);

			}
		});

		this.addButton = this.addField(new FormPortletButtonField("").setValue("Add"));
		this.addButton.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS + 230).setTopPosPx(DEFAULT_TOPPOS * 5).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(80);
		this.addButton.setCssStyle("_fm=bold|_fg=#FFFFFF|_bg=#FFA500|style.borderRadius=5px");

		this.clearButton = this.addField(new FormPortletButtonField("").setValue("Clear"));
		this.clearButton.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS + 320).setTopPosPx(DEFAULT_TOPPOS * 5).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(80);

		this.outputField = this.addField(new FormPortletTextAreaField("Output"));
		this.outputField.setLeftPosPx(DEFAULT_LEFTPOS).setTopPosPx(DEFAULT_TOPPOS * 4 + DEFAULT_ROWHEIGHT * 4).setHeightPx(DEFAULT_ROWHEIGHT * 5).setWidthPx(600);

		this.addFormPortletListener(this);
		this.addMenuListener(this);
		this.setMenuFactory(this);
	}

	@Override
	public void onContextMenu(FormPortlet portlet, String action, FormPortletField node) {
		if (node == this.sourceColumnField) {
			Formula f = (Formula) this.sourceColumnField.getCorrelationData();
			f.onContextMenu(node, action);
		}

	}

	@Override
	public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition) {
		if (field == this.sourceColumnField) {
			Formula f = (Formula) this.sourceColumnField.getCorrelationData();
			return f.createMenu(formPortlet, field, cursorPosition);
		}
		return null;
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
			this.output.setLength(0);
			this.output.append(field.getValue());
		}

	}

	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		// TODO Auto-generated method stub

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

	public void setResultTableColumns(Set<String> result) {
		targetColumnField.clearOptions();
		for (String col : result)
			targetColumnField.addOption(col, col);
	}

	public void setLeftTableColumns(Set<String> cols) {
		this.leftTableColumns = cols;
	}

	public void setRightTableColumns(Set<String> cols) {
		this.rightTableColumns = cols;
	}

	public void setLeft(String left) {
		this.leftTable = left;
	}

	public void setRight(String right) {
		this.rightTable = right;
	}

	public void addSelectClause() {
		String targetColumn = this.targetColumnField.getValue();
		String sourceColumn = this.sourceColumnField.getValue();
		if (this.output.length() == 0)
			this.output.append(targetColumn).append(" = ").append(sourceColumn);
		else
			this.output.append(',').append(targetColumn).append(" = ").append(sourceColumn);
		this.outputField.setValue(this.output.toString());
	}

	public void clearSelectClause() {
		this.output.setLength(0);
		this.outputField.setValue("");
	}

	public String getOutput() {
		return outputField.getValue();
	}

}
