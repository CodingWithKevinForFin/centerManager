package com.f1.ami.web.centermanager.editor;

import java.util.Map;

import com.f1.ami.web.centermanager.graph.nodes.AmiCenterGraphNode;
import com.f1.suite.web.portal.PortletConfig;
import com.f1.suite.web.portal.impl.ConfirmDialog;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletField;

public class AmiCenterManagerAddDboPortlet extends AmiCenterManagerAbstractAddObjectPortlet {
	public static final byte GROUP_CODE_DBO = AmiCenterGraphNode.TYPE_DBO;

	public AmiCenterManagerAddDboPortlet(PortletConfig config) {
		super(config, GROUP_CODE_DBO);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onFieldValueChanged(FormPortlet portlet, FormPortletField<?> field, Map<String, String> attributes) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onButton(ConfirmDialog source, String id) {
		// TODO Auto-generated method stub
		return false;
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
	public short getGroupCode() {
		return GROUP_CODE_DBO;
	}

	@Override
	public void readFromConfig(Map config) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean validateFields() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initTemplate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetTemplate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void submitScript() {
		// TODO Auto-generated method stub

	}

	@Override
	public void diffScript() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendQueryToBackend(String query) {
		// TODO Auto-generated method stub

	}

}
