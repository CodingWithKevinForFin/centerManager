package com.f1.ami.web.centermanager.nuweditor.triggerEditors;

import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.AmiWebUtils;
import com.f1.ami.web.centermanager.nuweditor.triggerEditors.smarteditors.AmiCenterManagerTriggerEditor_AggregateGroupByEditor;
import com.f1.ami.web.centermanager.nuweditor.triggerEditors.smarteditors.AmiCenterManagerTriggerEditor_AggregateSelectEditor;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.form.FormPortletCheckboxField;

public class AmiCenterManagerTriggerEditor_AggregateTrigger extends AmiCenterManagerAbstractTriggerEditor {
	/*strcture:
	 * <GridPortlet*formGrid:
	 *  1. FormPortlet*form1: nameField,onField<br>
	 *  2. groupbyForm*form2 indexConfigForm<br>
	 *  3. selectForm*form3 <br>
	 * 
	 * */

	final private AmiWebService service;

	final private FormPortletCheckboxField allowExternalUpdateField;

	final private AmiCenterManagerTriggerEditor_AggregateGroupByEditor groupByEditor;
	final private AmiCenterManagerTriggerEditor_AggregateSelectEditor selectEditor;

	public AmiCenterManagerTriggerEditor_AggregateTrigger(PortletConfig config) {
		super(config);
		this.service = AmiWebUtils.getService(getManager());

		allowExternalUpdateField = form.addField(new FormPortletCheckboxField("allowExternalUpdates"));
		allowExternalUpdateField.setHelp("Optional. Value is either true or false (false by default)." + "<br>"
				+ "If true, then other processes (i.e triggers, UPDATEs) are allowed to perform UPDATEs on the target table." + "<br>"
				+ " Please use precaution when using this feature, since updating cells controlled by the aggregate trigger will result into an undesirable state.");

		this.groupByEditor = new AmiCenterManagerTriggerEditor_AggregateGroupByEditor(generateConfig());
		this.selectEditor = new AmiCenterManagerTriggerEditor_AggregateSelectEditor(generateConfig());

		addChild(form, 0, 0);
		addChild(groupByEditor, 0, 1, 1, 1);
		addChild(selectEditor, 0, 2, 1, 1);
		setRowSize(0, 100);
		//addChild(new FormPortlet(generateConfig()), 0, 1, 1, 1);
		//addChild(this.groupByEditor, 0, 1, 1, 1);
		//
		//addChild(this.selectEditor, 0, 2, 1, 1);

		//addChild(groupByEditor, 0, 1);
		//		GridPortlet smartEditorGrid = new GridPortlet(generateConfig());
		//		smartEditorGrid.addChild(groupByEditor, 0, 0);
		//		smartEditorGrid.addChild(selectEditor, 0, 1);

	}

	@Override
	public String getKeyValuePairs() {
		// TODO Auto-generated method stub
		return null;
	}

}
