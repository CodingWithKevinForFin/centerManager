package com.f1.ami.web.centermanager.nuweditor.triggerEditors.smarteditors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.menu.impl.BasicWebMenu;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletButton;
import com.f1.suite.web.portal.impl.form.FormPortletButtonField;
import com.f1.suite.web.portal.impl.form.FormPortletContextMenuFactory;
import com.f1.suite.web.portal.impl.form.FormPortletDivField;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletListener;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextAreaField;
import com.f1.suite.web.portal.impl.form.FormPortletTitleField;

public class AmiCenterManagerTriggerEditor_AggregateGroupByEditor extends FormPortlet implements FormPortletListener, FormPortletContextMenuFactory {

	private FormPortletTitleField groupByTitleField;

	public StringBuilder groupByOutput = new StringBuilder();

	final private FormPortletSelectField<String> targetColumnField;
	final private FormPortletSelectField<String> sourceColumnField;

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

	public AmiCenterManagerTriggerEditor_AggregateGroupByEditor(PortletConfig config) {
		super(config);

		this.targetColumns = new ArrayList<FormPortletSelectField<String>>();
		this.sourceColumns = new ArrayList<FormPortletSelectField<String>>();
		this.groupByTitleField = this.addField(new FormPortletTitleField("groupBy"));
		this.groupByTitleField.setLeftPosPx(400);
		this.groupByTitleField.setHelp("A comma delimited list of expressions to group rows by, each expression being of the form:" + "<br>"
				+ "<b><i style=\"color:blue\">targetTableColumn = expression_on_sourceTableColumns [,targetTableColumn = expression_on_sourceTableColumns ...]</i></b>");

		FormPortletDivField sourceTitleDiv = this.addField(new FormPortletDivField("<b> Source </b>"));
		FormPortletDivField targetTitleDiv = this.addField(new FormPortletDivField("<b> Target </b>"));

		targetTitleDiv.setLeftPosPx(DEFAULT_LEFTPOS + 45).setTopPosPx(DEFAULT_TOPPOS).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(200);
		sourceTitleDiv.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS + 48).setTopPosPx(DEFAULT_TOPPOS).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(200);

		targetColumnField = this.addField(new FormPortletSelectField(String.class, ""));
		targetColumnField.addOption("act", "act");
		targetColumnField.addOption("region", "region");
		targetColumnField.addOption("cnt", "cnt");
		targetColumnField.addOption("value", "value");

		sourceColumnField = this.addField(new FormPortletSelectField(String.class, "&nbsp&nbsp<b>=</b>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp "));
		sourceColumnField.addOption("account", "account");
		sourceColumnField.addOption("region", "region");
		sourceColumnField.addOption("qty", "qty");
		sourceColumnField.addOption("px", "px");

		targetColumnField.setWidthPx(COLNAME_WIDTH);
		targetColumnField.setHeightPx(DEFAULT_ROWHEIGHT);
		targetColumnField.setLeftPosPx(DEFAULT_LEFTPOS);
		targetColumnField.setTopPosPx(DEFAULT_TOPPOS * 2);

		sourceColumnField.setWidthPx(COLNAME_WIDTH);
		sourceColumnField.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS);
		sourceColumnField.setTopPosPx(DEFAULT_TOPPOS * 2);
		//		sourceColumnField.setRightPosPx(300);
		sourceColumnField.setHeightPx(DEFAULT_ROWHEIGHT);

		this.addButton = this.addField(new FormPortletButtonField("").setValue("Add"));
		this.addButton.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS + 230).setTopPosPx(DEFAULT_TOPPOS * 2).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(80);
		this.addButton.setCssStyle("_fm=bold|_fg=#FFFFFF|_bg=#FFA500|style.borderRadius=5px");

		this.clearButton = this.addField(new FormPortletButtonField("").setValue("Clear"));
		this.clearButton.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS + 320).setTopPosPx(DEFAULT_TOPPOS * 2).setHeightPx(DEFAULT_ROWHEIGHT).setWidthPx(80);

		this.outputField = this.addField(new FormPortletTextAreaField("Output"));
		this.outputField.setLeftPosPx(DEFAULT_LEFTPOS).setTopPosPx(DEFAULT_TOPPOS * 2 + DEFAULT_ROWHEIGHT * 2).setHeightPx(DEFAULT_ROWHEIGHT * 3).setWidthPx(600);

		this.addFormPortletListener(this);

	}

	public void addGroupByFieldAtPos(int pos) {
		FormPortletSelectField<String> targetColumnField = new FormPortletSelectField(String.class, "");
		targetColumnField.addOption("test", "test");
		targetColumnField.addOption("test1", "test1");
		targetColumnField.addOption("test2", "test2");

		FormPortletSelectField<String> sourceColumnField = new FormPortletSelectField(String.class, "&nbsp&nbsp<b>=</b>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp ");
		sourceColumnField.addOption("test", "test");
		sourceColumnField.addOption("test1", "test1");
		sourceColumnField.addOption("test2", "test2");

		targetColumns.add(pos, targetColumnField);

		sourceColumns.add(pos, sourceColumnField);

		//Order of fields being added changes if you can select a field.
		this.addField(targetColumnField);
		this.addField(sourceColumnField);

	}

	private void repositionAtPosition(int position) {
		FormPortletSelectField targetColumnField = this.targetColumns.get(position);
		FormPortletSelectField sourceColumnField = this.sourceColumns.get(position);

		targetColumnField.setWidthPx(COLNAME_WIDTH);
		targetColumnField.setHeightPx(DEFAULT_ROWHEIGHT);
		targetColumnField.setLeftPosPx(DEFAULT_LEFTPOS);
		targetColumnField.setTopPosPx(DEFAULT_TOPPOS * 2 + (DEFAULT_ROWHEIGHT + DEFAULT_SPACING) * position);

		sourceColumnField.setWidthPx(COLNAME_WIDTH);
		sourceColumnField.setLeftPosPx(DEFAULT_LEFTPOS + COLNAME_WIDTH + DEFAULT_LEFTPOS);
		sourceColumnField.setTopPosPx(DEFAULT_TOPPOS * 2 + (DEFAULT_ROWHEIGHT + DEFAULT_SPACING) * position);
		sourceColumnField.setRightPosPx(50);
		sourceColumnField.setHeightPx(DEFAULT_ROWHEIGHT);
	}

	@Override
	public void onButtonPressed(FormPortlet portlet, FormPortletButton button) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		if (field == this.addButton) {
			addGroupByClause();
		} else if (field == this.clearButton) {
			clearGroupByClause();
		} else if (field == this.outputField) {
			this.groupByOutput.setLength(0);
			this.groupByOutput.append(field.getValue());
		}

	}

	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public WebMenu createMenu(FormPortlet formPortlet, FormPortletField<?> field, int cursorPosition) {
		BasicWebMenu r = new BasicWebMenu();

		return r;
	}

	public void addGroupByClause() {
		String targetColumn = this.targetColumnField.getValue();
		String sourceColumn = this.sourceColumnField.getValue();
		if (this.groupByOutput.length() == 0)
			this.groupByOutput.append(targetColumn).append(" = ").append(sourceColumn);
		else
			this.groupByOutput.append(',').append(targetColumn).append(" = ").append(sourceColumn);
		this.outputField.setValue(this.groupByOutput.toString());

	}

	public void clearGroupByClause() {
		this.groupByOutput.setLength(0);
		this.outputField.setValue("");
	}

}
