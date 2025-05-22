package com.f1.ami.web.centermanager.editor;

import java.util.Map;

import com.f1.ami.amicommon.msg.AmiCenterQueryDsRequest;

public interface AmiCenterManagerEditorTemplate {

	public void initTemplate();

	public void resetTemplate();

	//script = preUseClause + useClause(with proper warning "MISSING REQUIRED FIELDS")
	public String previewScript();

	public void submitScript();

	public void diffScript();

	//preUseClause = object schema
	public String prepareUseClause();

	//useClause = object config
	public String preparePreUseClause();

	public boolean validateScript(String script);

	public boolean validateFields();//this should be made abstract

	public void readFromConfig(Map config);

	public String getPendingRequiredFields();

	public void sendQueryToBackend(String query);

	public AmiCenterQueryDsRequest prepareRequest();

	public short getGroupCode();
}
