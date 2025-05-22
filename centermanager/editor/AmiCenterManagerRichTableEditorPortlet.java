package com.f1.ami.web.centermanager.editor;

import java.util.Map;

import com.f1.ami.portlets.AmiWebHeaderPortlet;
import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.AmiWebUtils;
import com.f1.ami.web.centermanager.AmiCenterEntityConsts;
import com.f1.ami.web.graph.AmiCenterGraphNode_Index;
import com.f1.ami.web.graph.AmiCenterGraphNode_Trigger;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.PortletManager;
import com.f1.suite.web.portal.impl.GridPortlet;
import com.f1.suite.web.portal.impl.TabPortlet;

public class AmiCenterManagerRichTableEditorPortlet extends GridPortlet {
	private static final String BG_GREY = "_bg=#4c4c4c";
	private static final String TABLE_FORM_FIELD_STYLE = BG_GREY + "|_fm=courier,bold|_fg=#ffffff|_fs=18px|style.border=0px";

	final private AmiWebService service;
	private AmiWebHeaderPortlet header;

	private TabPortlet tableEditorTabsPortlet;//contains triggers,indexes,columns

	public AmiCenterManagerRichTableEditorPortlet(PortletConfig config, Map<String, String> tableConfig, Map<String, AmiCenterGraphNode_Trigger> triggerBinding,
			Map<String, AmiCenterGraphNode_Index> indexBinding) {
		super(config);
		this.service = AmiWebUtils.getService(getManager());

		this.header = new AmiWebHeaderPortlet(generateConfig());
		this.header.updateBlurbPortletLayout("Rich Table Editor", null);
		this.header.setShowSearch(false);
		this.addChild(header, 0, 0, 1, 1);

		PortletManager manager = service.getPortletManager();

		this.tableEditorTabsPortlet = new TabPortlet(generateConfig());
		this.tableEditorTabsPortlet.getTabPortletStyle().setBackgroundColor("#4c4c4c");
		this.tableEditorTabsPortlet.addChild("Info", new AmiCenterManagerAddTablePortlet(manager.generateConfig(), tableConfig, AmiCenterEntityConsts.EDIT));
		this.tableEditorTabsPortlet.addChild("Columns", new AmiCenterManagerEditColumnPortlet(manager.generateConfig(), tableConfig));
		this.tableEditorTabsPortlet.addChild("Triggers", new AmiCenterManagerTriggerScirptTreePortlet(manager.generateConfig(), triggerBinding));//, new HashMap<String, String>(), AmiCenterEntityTypeConsts.EDIT));
		this.tableEditorTabsPortlet.addChild("Indexes", new AmiCenterManagerIndexScirptTreePortlet(manager.generateConfig(), indexBinding, this.tableEditorTabsPortlet));
		this.tableEditorTabsPortlet.setIsCustomizable(false);
		this.addChild(this.tableEditorTabsPortlet, 0, 1, 1, 1);

	}

}
