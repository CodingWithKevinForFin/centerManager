package com.f1.ami.web.centermanager.autocomplete;

import java.util.ArrayList;
import java.util.List;

import com.f1.ami.web.AmiWebService;
import com.f1.utils.structs.table.derived.BasicMethodFactory;
import com.f1.utils.structs.table.derived.DerivedCellCalculator;
import com.f1.utils.structs.table.derived.DerivedCellMemberMethod;
import com.f1.utils.structs.table.derived.MethodFactory;

public class AmiCenterManagerScriptAutoCompletion extends AmiAbstractScriptAutoCompletion {

	private AmiCenterManagerImdbScriptManager scriptManager; //specific

	public AmiCenterManagerScriptAutoCompletion(AmiWebService service) {
		super(service);
		this.scriptManager = new AmiCenterManagerImdbScriptManager(service);
		this.factory = scriptManager.getMethodFactory();
		List<DerivedCellMemberMethod<Object>> sink = new ArrayList<DerivedCellMemberMethod<Object>>();
		this.factory.getMemberMethods(null, null, sink);
		for (DerivedCellMemberMethod<Object> i : sink) {
			String name = this.factory.forType(i.getTargetType());
			if (name != null)
				this.types.add(name);
		}
	}

	@Override
	public BasicMethodFactory getMethodFactory() {
		return scriptManager.getMethodFactory();
	}

	public void addMethodFactory(MethodFactory toAdd) {
		scriptManager.getMethodFactory().addFactory(toAdd);
	}

	@Override
	protected Class<?> evaluateType(String prefix) {
		return scriptManager.getMethodFactory().forNameNoThrow(prefix);
	}

	@Override
	public DerivedCellCalculator toCalcNotOptimized() {
		return null;
	}

}
