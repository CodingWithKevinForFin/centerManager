package com.f1.ami.web.centermanager.editor;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.f1.ami.amicommon.AmiConsts;
import com.f1.ami.amicommon.AmiUtils;
import com.f1.ami.amicommon.msg.AmiCenterQueryDsRequest;
import com.f1.ami.amicommon.msg.AmiCenterQueryDsResponse;
import com.f1.ami.amicommon.msg.AmiDatasourceColumn;
import com.f1.ami.portlets.AmiWebHeaderPortlet;
import com.f1.ami.web.AmiWebFormatterManager;
import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.AmiWebUtils;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.centermanager.AmiCenterManagerUtils;
import com.f1.base.Action;
import com.f1.base.Row;
import com.f1.base.Table;
import com.f1.container.ResultMessage;
import com.f1.suite.web.fastwebcolumns.FastWebColumns;
import com.f1.suite.web.menu.WebMenu;
import com.f1.suite.web.menu.impl.BasicWebMenu;
import com.f1.suite.web.menu.impl.BasicWebMenuLink;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.PortletManager;
import com.f1.suite.web.portal.impl.DividerPortlet;
import com.f1.suite.web.portal.impl.FastTablePortlet;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.HtmlPortlet;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;
import com.f1.suite.web.portal.impl.form.FormPortletSelectField;
import com.f1.suite.web.portal.impl.form.FormPortletTextField;
import com.f1.suite.web.table.WebColumn;
import com.f1.suite.web.table.WebContextMenuFactory;
import com.f1.suite.web.table.WebContextMenuListener;
import com.f1.suite.web.table.WebTable;
import com.f1.suite.web.table.fast.FastWebTable;
import com.f1.utils.SH;
import com.f1.utils.casters.Caster_Boolean;
import com.f1.utils.casters.Caster_String;
import com.f1.utils.concurrent.HasherMap;
import com.f1.utils.impl.CaseInsensitiveHasher;
import com.f1.utils.structs.Tuple2;
import com.f1.utils.structs.table.BasicTable;
import com.f1.utils.structs.table.SmartTable;

public class AmiCenterManagerEditColumnPortlet extends GridPortlet implements WebContextMenuListener, WebContextMenuFactory {

	final private AmiWebService service;
	private AmiWebHeaderPortlet header;
	private HtmlPortlet tableIcon;
	private FormPortlet tableInfoPortlet;
	private FastTablePortlet columnMetadata;
	private String tableName;

	private AmiCenterManagerColumnMetaDataEditForm columnMetaDataEditForm;

	private static final String BG_GREY = "_bg=#4c4c4c";

	public AmiCenterManagerEditColumnPortlet(PortletConfig config, Map<String, String> tableConfig) {
		super(config);
		this.service = AmiWebUtils.getService(getManager());
		PortletManager manager = service.getPortletManager();
		//tableInfo:
		String tableName = tableConfig.get("name");
		this.tableName = tableName;
		String persistengine = tableConfig.get("PersistEngine");

		//init portlets(tableForm and header)
		//TODO:Should I use amiwebheaderportlet?
		this.header = new AmiWebHeaderPortlet(generateConfig());
		this.header.setShowSearch(false);
		StringBuilder legendHtml = new StringBuilder();
		legendHtml.append("<div style=\"height:100%; width:100%;>");
		String legendIconPrefix = "<div style=\"display:inline-flex; position:relative; \"><div style=\"margin:auto; position:relative;\"><div class=\"";
		String legendIconMiddle = "\"></div><div style=\"width:100%; color:white; text-align:center;\">";
		String legendIconSuffix = "</div></div></div>";
		legendHtml.append(legendIconPrefix + "ami_datamodeler_ds" + legendIconMiddle + "Datasource" + legendIconSuffix);

		this.tableIcon = new HtmlPortlet(generateConfig());
		this.tableIcon.setHtml(legendHtml.toString());

		this.header.setLegendWidth(300);
		this.header.updateLegendPortletLayout(legendHtml.toString());

		//init grid
		tableInfoPortlet = new FormPortlet(manager.generateConfig());
		FormPortletTextField tableNameField = new FormPortletTextField("Table Name");
		FormPortletSelectField<Short> persistEngineField = new FormPortletSelectField<Short>(short.class, "PersistEngine");
		tableNameField.setBorderColor("4c4c4c");
		tableNameField.setValue(tableName);
		tableNameField.setDisabled(true);
		persistEngineField.setBorderColor("4c4c4c");
		persistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_NONE, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_NONE);
		persistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_FAST, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_FAST);
		persistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_HISTORICAL, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_HISTORICAL);
		persistEngineField.addOption(AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_CODE_TEXT, AmiCenterEntityConsts.PERSIST_ENGINE_TYPE_TEXT);
		persistEngineField.setValue(AmiCenterManagerUtils.toTablePersistEngineCode(persistengine));
		persistEngineField.setDisabled(true);
		tableInfoPortlet.addField(tableNameField);
		tableInfoPortlet.addField(persistEngineField);
		tableInfoPortlet.getFormPortletStyle().setCssStyle("_bg=#ffffff");

		//init table
		this.columnMetadata = new FastTablePortlet(generateConfig(), new BasicTable(new String[] { "columnName", "dataType", "options", "noNull", "position" }),
				"Column Configuration");
		AmiWebFormatterManager fm = service.getFormatterManager();
		this.columnMetadata.getTable().addColumn(true, "Column Name", "columnName", fm.getBasicFormatter()).setWidth(150);
		this.columnMetadata.getTable().addColumn(true, "Data Type", "dataType", fm.getBasicFormatter());
		this.columnMetadata.getTable().addColumn(true, "Options", "options", fm.getBasicFormatter());
		this.columnMetadata.getTable().addColumn(true, "NoNull", "noNull", fm.getBasicFormatter());
		this.columnMetadata.getTable().addColumn(true, "Position", "position", fm.getIntegerWebCellFormatter());
		this.columnMetadata.getTable().sortRows("position", true, true, false);
		this.columnMetadata.setDialogStyle(AmiWebUtils.getService(getManager()).getUserDialogStyleManager());
		this.columnMetadata.addOption(FastTablePortlet.OPTION_TITLE_BAR_COLOR, "#6f6f6f");
		this.columnMetadata.addOption(FastTablePortlet.OPTION_TITLE_DIVIDER_HIDDEN, true);
		//add listener
		this.columnMetadata.getTable().addMenuListener(this);
		//have the ability to create and respond to menu items
		this.columnMetadata.getTable().setMenuFactory(this);
		this.columnMetaDataEditForm = new AmiCenterManagerColumnMetaDataEditForm(generateConfig(), tableName, AmiCenterManagerColumnMetaDataEditForm.MODE_EDIT);

		DividerPortlet div = new DividerPortlet(generateConfig(), true, this.columnMetadata, this.columnMetaDataEditForm);

		this.addChild(tableIcon, 0, 0, 1, 1).setPadding(0, 0, 20, 20);
		this.addChild(tableInfoPortlet, 1, 0, 1, 1);
		//		this.addChild(columnMetadata, 0, 1, 1, 1);
		//		this.addChild(columnMetaDataEditForm, 1, 1, 1, 1);
		this.addChild(div, 0, 1, 2, 4);
		sendAuth();
		initColumnMetadata(tableName);

	}

	public SmartTable getColumnTable() {
		return this.columnMetadata.getTable().getTable();
	}

	private static Map<String, String> parseOptions(String option) {
		HasherMap<String, String> m = new HasherMap<String, String>(CaseInsensitiveHasher.INSTANCE);
		List<String> l = SH.splitToList(" ", option);
		for (String s : l) {
			String key, value = null;
			if (s.contains("=")) {
				key = SH.beforeFirst(s, "=");
				value = SH.afterFirst(s, "=");
			} else {
				key = s;
				value = "true";
			}
			m.put(key, value);
		}
		return m;
	}

	private void sendAuth() {
		AmiCenterQueryDsRequest request = prepareRequest();
		if (request == null)
			return;
		service.sendRequestToBackend(this, request);
	}

	private void prepareRequestToBackend(String query) {
		AmiCenterQueryDsRequest request = prepareRequest();
		request.setQuery(query);
		service.sendRequestToBackend(this, request);
	}

	private AmiCenterQueryDsRequest prepareRequest() {
		AmiCenterQueryDsRequest request = getManager().getTools().nw(AmiCenterQueryDsRequest.class);

		int timeout = 60;
		try {
			timeout = (int) (timeout * 1000);
		} catch (Exception e) {
			getManager().showAlert("Timeout is not in a valid format");
			return null;
		}

		request.setTimeoutMs(timeout);
		request.setQuerySessionKeepAlive(true);
		request.setInvokedBy(service.getUserName());
		request.setSessionVariableTypes(null);
		request.setSessionVariables(null);
		request.setAllowSqlInjection(false);//String template, dflt to false
		request.setPermissions(AmiCenterQueryDsRequest.PERMISSIONS_FULL);
		request.setType(AmiCenterQueryDsRequest.TYPE_QUERY);
		request.setOriginType(AmiCenterQueryDsRequest.ORIGIN_FRONTEND_SHELL);
		request.setDatasourceName("AMI");
		request.setLimit(-1);
		return request;
	}

	@Override
	public void onBackendResponse(ResultMessage<Action> result) {
		if (result.getError() != null) {
			getManager().showAlert("Internal Error:" + result.getError().getMessage(), result.getError());
			return;
		}
		AmiCenterQueryDsResponse response = (AmiCenterQueryDsResponse) result.getAction();
		List<Table> tables = response.getTables();
		if (response.getOk() && tables != null && tables.size() == 1) {
			Table t = tables.get(0);
			for (Row r : t.getRows()) {
				String columnName = (String) r.get("ColumnName");
				String dataType = (String) r.get("DataType");
				String options = (String) r.get("Options");
				Boolean noNull = (Boolean) r.get("NoNull");
				Integer position = (Integer) r.get("Position");
				this.columnMetadata.addRow(columnName, dataType, options, noNull, position);
			}
		}

	}

	public void initColumnMetadata(String t) {
		this.columnMetadata.clearRows();
		prepareRequestToBackend("SHOW COLUMNS WHERE TableName ==" + "\"" + t + "\";");
	}

	@Override
	public void onUserDblclick(FastWebColumns columns, String action, Map<String, String> properties) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onContextMenu(WebTable table, String action) {
		String targetColumnName = null;
		byte actionMode = -1;
		String dialogTitle = null;
		if (SH.startsWith(action, "add_column_")) {
			String temp = SH.afterFirst(action, "add_column_");
			if (temp.startsWith("before")) {
				actionMode = AmiCenterManagerColumnMetaDataEditForm.ACTION_ADD_BEFORE;
				targetColumnName = SH.afterFirst(temp, "before_");
			} else if (temp.startsWith("after")) {
				actionMode = AmiCenterManagerColumnMetaDataEditForm.ACTION_ADD_AFTER;
				targetColumnName = SH.afterFirst(temp, "after_");
			}
			dialogTitle = "Add Column";
		} else if (SH.startsWith(action, "drop_column_")) {
			actionMode = AmiCenterManagerColumnMetaDataEditForm.ACTION_DROP;
			targetColumnName = SH.afterFirst(action, "drop_column_");
			dialogTitle = "Drop Column";
			String query = "ALTER TABLE " + this.tableName + " DROP " + targetColumnName;
			getManager().showDialog("Drop Column", new AmiCenterManagerSubmitEditScriptPortlet(this.service, generateConfig(), query),
					AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_WIDTH, AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_HEIGHT);
			return;
		}

		getManager().showDialog(dialogTitle,
				new AmiCenterManagerColumnMetaDataEditForm(generateConfig(), this.tableName, AmiCenterManagerColumnMetaDataEditForm.MODE_ADD, targetColumnName, actionMode),
				AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_WIDTH, AmiCenterManagerSubmitEditScriptPortlet.DEFAULT_PORTLET_HEIGHT);

	}

	@Override
	public void onCellClicked(WebTable table, Row row, WebColumn col) {

	}

	@Override
	public void onCellMousedown(WebTable table, Row row, WebColumn col) {
		this.columnMetaDataEditForm.resetForm();
		String dataType = (String) row.get("dataType");
		String columnName = (String) row.get("columnName");
		Boolean noNull = (Boolean) row.get("noNull");
		Integer position = (Integer) row.get("position");
		String options = (String) row.get("options");
		FormPortletTextField f1 = (FormPortletTextField) this.columnMetaDataEditForm.getForm().getFieldByName("columnName");
		f1.setValue(columnName);
		f1.setDisabled(false);
		FormPortletSelectField f2 = (FormPortletSelectField) this.columnMetaDataEditForm.getForm().getFieldByName(AmiCenterManagerColumnMetaDataEditForm.VARNAME_COLUMN_DATA_TYPE);
		f2.setValue(AmiUtils.parseTypeName(dataType));
		f2.setDisabled(false);
		FormPortletCheckboxField f3 = (FormPortletCheckboxField) this.columnMetaDataEditForm.getForm().getFieldByName(AmiConsts.NONULL);
		f3.setValue(noNull);
		f3.setDisabled(false);
		Map<String, String> m = parseOptions(options);

		//set the column cache
		this.columnMetaDataEditForm.setColumnCache(AmiCenterManagerColumnMetaDataEditForm.VARNAME_COLUMN_DATA_TYPE, dataType);
		this.columnMetaDataEditForm.setColumnCache(AmiCenterManagerColumnMetaDataEditForm.VARNAME_COLUMN_NAME, columnName);
		this.columnMetaDataEditForm.setColumnCache(AmiCenterManagerColumnMetaDataEditForm.VARNAME_NONULL, Caster_String.INSTANCE.cast(noNull));
		for (Entry<String, String> e : m.entrySet()) {
			this.columnMetaDataEditForm.setColumnCache(e.getKey(), e.getValue());
		}

		switch (AmiUtils.parseTypeName(dataType)) {
			case AmiDatasourceColumn.TYPE_BIGDEC:
			case AmiDatasourceColumn.TYPE_BIGINT:
			case AmiDatasourceColumn.TYPE_BOOLEAN:
			case AmiDatasourceColumn.TYPE_BYTE:
			case AmiDatasourceColumn.TYPE_CHAR:
			case AmiDatasourceColumn.TYPE_COMPLEX:
			case AmiDatasourceColumn.TYPE_DOUBLE:
			case AmiDatasourceColumn.TYPE_FLOAT:
			case AmiDatasourceColumn.TYPE_INT:
			case AmiDatasourceColumn.TYPE_LONG:
			case AmiDatasourceColumn.TYPE_SHORT:
			case AmiDatasourceColumn.TYPE_UTC:
			case AmiDatasourceColumn.TYPE_UTCN:
			case AmiDatasourceColumn.TYPE_UUID:
				setNoBroadCast(m);
				//enable common options
				this.columnMetaDataEditForm.disableCommonOptions(false);
				break;
			case AmiDatasourceColumn.TYPE_STRING:
				setNoBroadCast(m);
				//enable common options
				this.columnMetaDataEditForm.disableCommonOptions(false);

				Boolean isCompact = Caster_Boolean.INSTANCE.cast(m.get(AmiConsts.COMPACT));
				Boolean isAscii = Caster_Boolean.INSTANCE.cast(m.get(AmiConsts.ASCII));
				Boolean isBitmap = Caster_Boolean.INSTANCE.cast(m.get(AmiConsts.BITMAP));
				Boolean isOndisk = Caster_Boolean.INSTANCE.cast(m.get(AmiConsts.ONDISK));
				Boolean isEnum = Caster_Boolean.INSTANCE.cast(m.get(AmiConsts.TYPE_NAME_ENUM));
				boolean isCache = m.get(AmiConsts.CACHE) != null;
				if (isCache) {
					getColumnOptionEditField(AmiConsts.CACHE).setValue(true).setDisabled(false);
					String rawCacheValue = (String) m.get(AmiConsts.CACHE);
					int cacheValue = parseCacheValue(rawCacheValue).getA();
					String cacheUnit = parseCacheValue(rawCacheValue).getB();
					if (SH.isnt(cacheUnit))
						cacheUnit = AmiConsts.CACHE_UNIT_DEFAULT_BYTE;
					byte cacheUnitByte = AmiCenterManagerUtils.toCacheUnitCode(cacheUnit);
					this.columnMetaDataEditForm.getCacheValueField().setValue(SH.toString(cacheValue));
					this.columnMetaDataEditForm.getCacheUnitField().setValue(cacheUnitByte);
				}
				//enable edit for all string options
				this.columnMetaDataEditForm.disableStringOptions(false);
				if (isCompact != null && Boolean.TRUE.equals(isCompact)) {
					getColumnOptionEditField(AmiConsts.COMPACT).setValue(true).setDisabled(false);
					getColumnOptionEditField(AmiConsts.BITMAP).setDisabled(true);
				}
				if (isAscii != null && Boolean.TRUE.equals(isAscii)) {
					getColumnOptionEditField(AmiConsts.ASCII).setValue(true).setDisabled(false);
				}
				if (isBitmap != null && Boolean.TRUE.equals(isBitmap)) {
					getColumnOptionEditField(AmiConsts.BITMAP).setValue(true).setDisabled(false);
					getColumnOptionEditField(AmiConsts.COMPACT).setDisabled(true);

				}
				if (isOndisk != null && Boolean.TRUE.equals(isOndisk)) {
					getColumnOptionEditField(AmiConsts.ONDISK).setValue(true).setDisabled(false);
				}
				if (isEnum != null && Boolean.TRUE.equals(isEnum)) {
					getColumnOptionEditField(AmiConsts.TYPE_NAME_ENUM).setValue(true).setDisabled(false);
				}

				break;
			case AmiDatasourceColumn.TYPE_BINARY:
				setNoBroadCast(m);
				//enable common options
				this.columnMetaDataEditForm.disableCommonOptions(false);
				break;
			default:
				throw new NullPointerException();
		}

	}

	public static Tuple2<Integer, String> parseCacheValue(String s) {
		Tuple2<Integer, String> cacheValue = new Tuple2<Integer, String>();
		StringBuilder digitBuilder = new StringBuilder();
		StringBuilder unitBuilder = new StringBuilder();
		for (char c : s.toCharArray()) {
			if (c == '"')
				continue;
			if (Character.isDigit(c))
				digitBuilder.append(c);
			else
				unitBuilder.append(c);
		}
		cacheValue.setA(Integer.parseInt(digitBuilder.toString()));
		cacheValue.setB(unitBuilder.toString());
		return cacheValue;
	}

	private void setNoBroadCast(Map<String, String> m) {
		FormPortletCheckboxField noBroadcastEditField = (FormPortletCheckboxField) this.columnMetaDataEditForm.getForm().getFieldByName("NoBroadcast");
		Boolean noBroadcast = Caster_Boolean.INSTANCE.cast(m.get(AmiConsts.NOBROADCAST));
		noBroadcastEditField.setValue(noBroadcast);
		noBroadcastEditField.setDisabled(false);
	}

	private FormPortletCheckboxField getColumnOptionEditField(String name) {
		return (FormPortletCheckboxField) this.columnMetaDataEditForm.getForm().getFieldByName(name);
	}

	@Override
	public void onSelectedChanged(FastWebTable fastWebTable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNoSelectedChanged(FastWebTable fastWebTable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(int viewTop, int viewPortHeight, long contentWidth, long contentHeight) {
		// TODO Auto-generated method stub

	}

	@Override
	public WebMenu createMenu(WebTable table) {
		FastWebTable ftw = (FastWebTable) table;
		int origRowPos = ftw.getActiveRow().getLocation();
		String origColumnName = (String) ftw.getActiveRow().get("columnName");
		BasicWebMenu m = new BasicWebMenu();
		m.add(new BasicWebMenuLink("Add Column Before " + origColumnName, true, "add_column_before_" + origColumnName));
		m.add(new BasicWebMenuLink("Add Column After " + origColumnName, true, "add_column_after_" + origColumnName));
		m.add(new BasicWebMenuLink("Drop Column", true, "drop_column_" + origColumnName));

		return m;
	}

}
