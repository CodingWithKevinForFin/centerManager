package com.f1.ami.web.centermanager.autocomplete;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.f1.ami.amicommon.AmiUtils;
import com.f1.ami.web.AmiWebConsts;
import com.f1.ami.web.AmiWebService;
import com.f1.ami.web.AmiWebUtils;
import com.f1.suite.web.JsFunction;
import com.f1.suite.web.portal.PortletManager;
import com.f1.suite.web.portal.impl.form.FormPortlet;
import com.f1.suite.web.portal.impl.form.FormPortletField;
import com.f1.suite.web.portal.impl.form.FormPortletTextEditField;
import com.f1.utils.CH;
import com.f1.utils.OH;
import com.f1.utils.SH;
import com.f1.utils.casters.Caster_Integer;
import com.f1.utils.casters.Caster_String;
import com.f1.utils.structs.table.derived.ParamsDefinition;
import com.f1.utils.structs.table.stack.BasicCalcTypes;

public class AmiAbstractFormPortletAmiScriptField extends FormPortletTextEditField implements AutoCompleteListener {
	public static final String JSNAME = "AmiCodeField";
	public static final Set<String> AVAILABLE_FLASH_COLORS = new HashSet<String>();
	public static final Set<String> AVAILABLE_ANNOTATION_TYPES = new HashSet<String>();
	static {
		// color names should be all lowercase.
		AVAILABLE_FLASH_COLORS.add("yellow");
		AVAILABLE_FLASH_COLORS.add("red");
		AVAILABLE_FLASH_COLORS.add("orange");

		AVAILABLE_ANNOTATION_TYPES.add("error");
		AVAILABLE_ANNOTATION_TYPES.add("info");
		AVAILABLE_ANNOTATION_TYPES.add("warning");
	}
	private int cursorPos = -1;
	private boolean cursonPositionMoved;
	private int pageX, pageY;
	private int scrollTop, scrollLeft;
	private String mode = "amiscript";

	private String amiEditorKeyboard;
	private StringBuilder stringBuff;
	private HashSet<String> usedVariableNames = new HashSet<String>();
	private BasicCalcTypes varMapping;
	private HashSet<Integer> breakpoints = new HashSet<Integer>(); // line numbers indicating where the breakpoints will be (0 index based)
	// highlighting
	private int curHighlightedRow = -1;
	// scrolling
	private int curScrolledRow = -1;
	private boolean shouldScroll = false;
	// flashing
	private int flashRowStart = -1, flashRowEnd = -1;
	private String flashRowColor = null;
	private boolean shouldFlash = false;
	// annotating
	private int annotationRow = -1;
	private boolean clearAnnotation = false;
	private boolean annotate = false;
	private String annotationType = null;
	private String annotationMessage = null;

	//need service to init amiEditorKeyboard
	protected final AmiWebService service;
	//specific
	protected AmiAbstractScriptAutoCompletion autoCompletion;
	//private AmiWebScriptManagerForLayout scriptManager; //declared in sub-class

	//add
	private String autoCompleteMethodName;
	private Class[] autoCompleteVarType;

	public AmiAbstractFormPortletAmiScriptField(String title, PortletManager manager) {
		super(String.class, title);
		this.service = AmiWebUtils.getService(manager);
		this.amiEditorKeyboard = service.getVarsManager().getSetting(AmiWebConsts.USER_SETTING_AMI_EDITOR_KEYBOARD);
		this.stringBuff = new StringBuilder();
		//specific, for now use empty mapping, for center script field, const map is the dbo
		//this.varMapping = new com.f1.utils.structs.table.stack.BasicCalcTypes(scriptManager.getConstsMap(thiz));
		this.varMapping = new com.f1.utils.structs.table.stack.BasicCalcTypes();
		for (String s : this.varMapping.getVarKeys())
			this.usedVariableNames.add(s);
		this.setValueNoFire("");
	}

	@Override
	public FormPortletTextEditField moveCursor(int i) {
		if (i == this.cursorPos)
			return this;
		setCursorPosition(i);
		cursonPositionMoved = true;
		fireConfigChanged();
		return this;
	}

	@Override
	public FormPortletTextEditField insertAtCursor(String value) {
		if (value != null) {
			final String t = getValue();
			if (t == null) {
				setValue(value.toString());
				this.cursorPos = getValue().length();
			} else if (value.length() > 0) {
				setValue(SH.splice(t, getCursorPosition(), 0, value));
				moveCursor(getCursorPosition() + value.length());
			}
		}
		return this;
	}

	@Override
	public void setSelection(int start, int end) {
	}

	@Override
	public String getjsClassName() {
		return JSNAME;
	}
	@Override
	public void handleCallback(String action, Map<String, String> attributes) {
		if ("updateCursor".equals(action)) {
			this.cursorPos = CH.getOrThrow(Caster_Integer.INSTANCE, attributes, "pos");
			this.pageX = CH.getOrThrow(Caster_Integer.INSTANCE, attributes, "pageX");
			this.pageY = CH.getOrThrow(Caster_Integer.INSTANCE, attributes, "pageY");
		} else if ("showAC".equals(action)) {
			if (this.autoCompletion.isMenuActive()) {
				this.resetAutoCompletion();
			}
		} else if ("updateScrollTop".equals(action)) {
			this.scrollTop = CH.getOrThrow(Caster_Integer.INSTANCE, attributes, "scrollTop");
		} else if ("updateScrollLeft".equals(action)) {
			this.scrollLeft = CH.getOrThrow(Caster_Integer.INSTANCE, attributes, "scrollLeft");
		} else if ("onGutterMousedown".equals(action)) {
			int row = CH.getOrThrow(Caster_Integer.INSTANCE, attributes, "row");
			setOrUnsetBreakpoint(row);
		} else if ("click".equals(action)) {
			//			int row = CH.getOrThrow(Caster_Integer.INSTANCE, attributes, "row");
			//			Boolean ctrlKey = CH.getOrThrow(Caster_Boolean.INSTANCE, attributes, "ctrlKey");
			//			Boolean shiftKey = CH.getOrThrow(Caster_Boolean.INSTANCE, attributes, "shiftKey");
			//			Boolean altKey = CH.getOrThrow(Caster_Boolean.INSTANCE, attributes, "altKey");
			//			int mask = 0;
			//			if (ctrlKey)
			//				mask |= Form.KEY_CTRL;
			//			if (shiftKey)
			//				mask |= Form.KEY_SHIFT;
			//			if (altKey)
			//				mask |= Form.KEY_ALT;
			//			onSpecialKeyPressed(this.getForm(), this, -1, mask, cursorPos);
		} else
			super.handleCallback(action, attributes);
	};
	@Override
	public boolean onUserValueChanged(Map<String, String> attributes, String objectName, StringBuilder pendingJs) {
		if (this.isDisabled() == false) {
			String type = CH.getOrNoThrow(Caster_String.INSTANCE, attributes, "type", "");
			if (SH.equals(type, "onchange")) {
				int start = CH.getOrThrow(Caster_Integer.INSTANCE, attributes, "s");
				int end = CH.getOrThrow(Caster_Integer.INSTANCE, attributes, "e");
				String change = CH.getOrThrow(Caster_String.INSTANCE, attributes, "c");
				SH.clear(stringBuff);
				stringBuff.append(this.getValue());
				stringBuff.replace(start, end, SH.replaceAll(change, '\r', ""));
				if (SH.equals(stringBuff, getValue()))
					return false;
				this.setValueNoFire(stringBuff.toString());
				if (change.equals(";")) {
					autoCompletion.setMenuActive(false);
				} else if (change.equals(".")) {
					autoCompletion.setMenuActive(true);
				}
			}
		}
		return true;
	}
	@Override
	public FormPortletField<String> setValue(String value) {
		return super.setValue(SH.replaceAll(SH.noNull(value), '\r', ""));
	}

	public void insertTextNoThrow(int position, String text) {
		if (getValue() == null)
			setValue(text);
		else if (position == -1 || position >= getValue().length())
			setValue(getValue() + text);
		else
			setValue(SH.splice(getValue(), position, 0, text));
		setCursorPosition(position + text.length());
	}
	@Override
	public void setCursorPosition(int cursorPosition) {
		this.cursorPos = cursorPosition;
		fireConfigChanged();
	}

	@Override
	public int getCursorPosition() {
		return cursorPos;
	}
	@Override
	public void rebuildJs(String jsObjectName, StringBuilder pendingJs) {
		super.rebuildJs(jsObjectName, pendingJs);
		new JsFunction(pendingJs, jsObjectName, "moveScrollTop").addParam(this.scrollTop).end();
		new JsFunction(pendingJs, jsObjectName, "moveScrollLeft").addParam(this.scrollLeft).end();
		new JsFunction(pendingJs, jsObjectName, "moveCursor").addParam(cursorPos).end();
		new JsFunction(pendingJs, jsObjectName, "setBreakpoints").addParam(this.breakpoints).end();
		new JsFunction(pendingJs, jsObjectName, "setMode").addParamQuoted(mode).end();
		new JsFunction(pendingJs, jsObjectName, "setKeyboardHandler").addParamQuoted(amiEditorKeyboard).end();
		new JsFunction(pendingJs, jsObjectName, "updateHighlight").addParam(this.curHighlightedRow).end();
		//add
		//********************
		if (this.autoCompleteMethodName != null && this.autoCompleteVarType != null) {
			JsFunction js = new JsFunction(pendingJs, jsObjectName, "registerMethodAutoComplete").addParamQuoted(this.autoCompleteMethodName);//.addParamQuoted(param).end();
			for (Class<?> clzz : this.autoCompleteVarType)
				js.addParamQuoted(clzz.getSimpleName());
			js.end();
		}

		//********************

		cursonPositionMoved = false;
		if (shouldScroll) {
			new JsFunction(pendingJs, jsObjectName, "scrollToLine").addParam(this.curScrolledRow).end();
			this.shouldScroll = false;
			this.curScrolledRow = -1;
		}
		updateAnnotation(jsObjectName, pendingJs);
	}

	private void updateAnnotation(String jsObjectName, StringBuilder pendingJs) {
		if (shouldFlash) {
			new JsFunction(pendingJs, jsObjectName, "flashRows").addParam(this.flashRowStart).addParam(this.flashRowEnd).addParamQuoted(this.flashRowColor).end();
			this.resetFlash();
		}
		if (annotate)
			new JsFunction(pendingJs, jsObjectName, "setAnnotation").addParam(this.annotationRow).addParamQuoted(this.annotationType).addParamQuoted(this.annotationMessage).end();
		if (clearAnnotation) {
			new JsFunction(pendingJs, jsObjectName, "clearAnnotation").end();
			this.clearAnnotation = false;
		}
	}
	@Override
	public void updateJs(String jsObjectName, StringBuilder pendingJs) {
		super.updateJs(jsObjectName, pendingJs);
		updateAnnotation(jsObjectName, pendingJs);
	}
	public HashSet<Integer> getBreakpoints() {
		return this.breakpoints;
	}

	/*
	 * tries to add row to the set. if false then row already exists in the set, hence gets removed
	 */
	public boolean setOrUnsetBreakpoint(int row) {
		if (!this.breakpoints.add(row))
			return this.breakpoints.remove(row);
		return true;
	}
	public void highlightRow(int row) {
		if (row != this.curHighlightedRow && row >= 0) {
			this.curHighlightedRow = row;
			fireConfigChanged();
		}
	}
	public void clearHighlight() {
		if (this.curHighlightedRow >= 0) {
			this.curHighlightedRow = -1;
			fireConfigChanged();
		}
	}
	public int getHighlightedRowNum() {
		return this.curHighlightedRow;
	}
	public int getCusorPageX() {
		return this.pageX;
	}
	public int getCusorPageY() {
		return this.pageY;
	}
	public void scrollToRow(int rowNum) {
		this.curScrolledRow = rowNum;
		this.shouldScroll = true;
		fireConfigChanged();
	}
	@Override
	public AmiAbstractFormPortletAmiScriptField setName(String name) {
		super.setName(name);
		return this;
	}

	public void setMode(String mode) {
		if (OH.eq(this.mode, mode))
			return;
		this.mode = mode;
		this.fireConfigChanged();
	}

	@Override
	public AmiAbstractFormPortletAmiScriptField setWidth(int width) {
		super.setWidth(width);
		return this;
	}

	@Override
	public AmiAbstractFormPortletAmiScriptField setHeight(int height) {
		super.setHeight(height);
		return this;
	}

	public void onSpecialKeyPressed(FormPortlet formPortlet, FormPortletField<?> field, int keycode, int mask, int cursorPosition) {
		if (mask == FormPortlet.KEY_CTRL && keycode == ' ')
			this.resetAutoCompletion();
	}
	public void resetAutoCompletion() {
		this.autoCompletion.clearGlobalVars();
		this.autoCompletion.addAllVariables(this.varMapping);

		//specific, delayed in sub-class
		//		if (thiz != null) {
		//			CalcFrame constsMap = this.scriptManager.getConstsMap(thiz);
		//			for (String i : constsMap.getVarKeys())
		//				this.autoCompletion.addVariable(i, constsMap.getType(i));
		//		}
		this.autoCompletion.reset(this, this.cursorPos);
	}

	public String getNextVariableName(String suggestedName) {
		return SH.getNextId(AmiUtils.toValidVarName(suggestedName), this.usedVariableNames);
	}
	public void addVariable(String name, Class<?> type) {
		this.varMapping.putType(name, type);
		this.usedVariableNames.add(name);
	}
	public void clearVariables() {
		this.varMapping.clear();
		this.usedVariableNames.clear();

		//specific, delayed in sub-class
		//		CalcFrame constsMap = scriptManager.getConstsMap(thiz);
		//		for (String s : constsMap.getVarKeys())
		//			this.varMapping.putType(s, constsMap.getType(s));
		//		//this.varMapping.putAll((Map) scriptManager.getGlobalVarTypes());
		//		for (String s : this.varMapping.getVarKeys())
		//			this.usedVariableNames.add(s);
	}

	public com.f1.utils.structs.table.stack.BasicCalcTypes getVarTypes() {
		return this.varMapping;
	}

	public void removeVariable(String string) {
		this.varMapping.removeType(string);
		this.usedVariableNames.remove(string);
	}

	public void moveCursor(int position, boolean scrollToRow) {
		this.moveCursor(position);
		if (scrollToRow)
			this.scrollToRow(SH.getLinePosition(this.getValue(), position).getA());
	}
	public void flashRows(int flashRowStart, int flashRowEnd, String flashColorName) {
		if (flashRowStart < 0 || flashRowEnd < flashRowStart || !AVAILABLE_FLASH_COLORS.contains(SH.toLowerCase(flashColorName)))
			return;
		this.flashRowStart = flashRowStart;
		this.flashRowEnd = flashRowEnd;
		this.flashRowColor = flashColorName;
		this.shouldFlash = true;
		//		fireConfigChanged();
		setHasUpdate(true);
	}
	public void resetFlash() {
		this.shouldFlash = false;
		this.flashRowStart = -1;
		this.flashRowEnd = -1;
		this.flashRowColor = null;
	}
	public void setAnnotation(int row, String annotationType, String annotationMessage) {
		if (row < 0 || SH.isnt(annotationMessage) || !AVAILABLE_ANNOTATION_TYPES.contains(SH.toLowerCase(annotationType)))
			return;
		this.clearAnnotation = false;
		this.annotate = true;
		this.annotationRow = row;
		this.annotationType = annotationType;
		this.annotationMessage = annotationMessage;
		setHasUpdate(true);
		//		fireConfigChanged();
	}
	public void clearAnnotation() {
		if (this.clearAnnotation || !this.annotate)
			return;
		this.clearAnnotation = true;
		this.annotate = false;
		this.annotationMessage = null;
		this.annotationRow = -1;
		setHasUpdate(true);
		//		fireConfigChanged();
	}

	@Override
	public void onAutoComplete(ParamsDefinition params) {
		System.out.println("doing autocomplete");
		this.autoCompleteMethodName = params.getMethodName();
		this.autoCompleteVarType = params.getParamTypes();

		fireConfigChanged();

	}
}
