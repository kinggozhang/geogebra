/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

 */

package org.geogebra.web.full.gui.view.algebra;

import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.awt.GColor;
import org.geogebra.common.euclidian.event.PointerEventType;
import org.geogebra.common.gui.SetLabels;
import org.geogebra.common.gui.inputfield.InputHelper;
import org.geogebra.common.gui.view.algebra.AlgebraItem;
import org.geogebra.common.gui.view.algebra.AlgebraView;
import org.geogebra.common.gui.view.algebra.Suggestion;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoPointOnPath;
import org.geogebra.common.kernel.geos.DescriptionMode;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoNumeric;
import org.geogebra.common.kernel.geos.GeoText;
import org.geogebra.common.kernel.geos.HasExtendedAV;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Feature;
import org.geogebra.common.main.GuiManagerInterface.Help;
import org.geogebra.common.main.Localization;
import org.geogebra.common.main.ScreenReader;
import org.geogebra.common.main.error.ErrorHandler;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.common.util.FormatConverterImpl;
import org.geogebra.common.util.IndexHTMLBuilder;
import org.geogebra.common.util.StringUtil;
import org.geogebra.common.util.debug.Log;
import org.geogebra.web.editor.MathFieldProcessing;
import org.geogebra.web.full.css.GuiResources;
import org.geogebra.web.full.gui.inputbar.AlgebraInputW;
import org.geogebra.web.full.gui.inputbar.HasHelpButton;
import org.geogebra.web.full.gui.inputbar.InputBarHelpPanelW;
import org.geogebra.web.full.gui.inputbar.InputBarHelpPopup;
import org.geogebra.web.full.gui.inputbar.WarningErrorHandler;
import org.geogebra.web.full.gui.inputfield.InputSuggestions;
import org.geogebra.web.full.gui.layout.GUITabs;
import org.geogebra.web.full.gui.layout.panels.AlgebraPanelInterface;
import org.geogebra.web.full.gui.util.Resizer;
import org.geogebra.web.full.main.AppWFull;
import org.geogebra.web.html5.gui.GPopupPanel;
import org.geogebra.web.html5.gui.inputfield.AbstractSuggestionDisplay;
import org.geogebra.web.html5.gui.inputfield.AutoCompleteW;
import org.geogebra.web.html5.gui.tooltip.ToolTipManagerW;
import org.geogebra.web.html5.gui.tooltip.ToolTipManagerW.ToolTipLinkType;
import org.geogebra.web.html5.gui.util.CancelEventTimer;
import org.geogebra.web.html5.gui.util.ClickStartHandler;
import org.geogebra.web.html5.gui.util.GPushButton;
import org.geogebra.web.html5.gui.util.LayoutUtilW;
import org.geogebra.web.html5.gui.util.LongTouchManager;
import org.geogebra.web.html5.gui.util.MathKeyboardListener;
import org.geogebra.web.html5.gui.util.NoDragImage;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.html5.main.DrawEquationW;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.himamis.retex.editor.share.serializer.TeXSerializer;
import com.himamis.retex.editor.share.util.Unicode;
import com.himamis.retex.editor.web.MathFieldW;
import com.himamis.retex.renderer.share.platform.FactoryProvider;
import com.himamis.retex.renderer.web.FactoryProviderGWT;

/**
 * main -> marblePanel content controls
 * 
 * content -> plainTextitem | latexItem | c | (definitionPanel outputPanel)
 * 
 * sliderContent -> [sliderPanel minmaxPanel]
 * 
 * plaintextitem -> STRING | (definitionPanel, outputPanel)
 * 
 * outputPanel -> valuePanel
 * 
 * definitionPanel -> c | STRING
 */
public class RadioTreeItem extends AVTreeItem implements MathKeyboardListener,
		AutoCompleteW, RequiresResize, HasHelpButton, SetLabels {

	private static final int DEFINITION_ROW_EDIT_MARGIN = 5;
	private static final int MARGIN_RESIZE = 50;

	static final int BROWSER_SCROLLBAR_WIDTH = 17;

	protected static final int LATEX_MAX_EDIT_LENGHT = 1500;

	Boolean stylebarShown;
	/** Help popup */
	protected InputBarHelpPopup helpPopup;
	/** label "Input..." */
	protected Label dummyLabel;

	/**
	 * The main widget of the tree item containing all others
	 */
	protected FlowPanel main;

	/** Item content after marble */
	protected final FlowPanel content;

	/** Item controls like delete, play, etc */
	protected ItemControls controls;

	protected Canvas canvas;

	String commandError;

	protected String errorMessage;

	protected String lastInput;

	protected GeoElement geo = null;
	protected Kernel kernel;
	protected AppWFull app;
	protected final AlgebraView av;
	protected boolean latex = false;

	private FlowPanel latexItem;
	private FlowPanel plainTextItem;

	// GTextBox tb;
	private boolean needsUpdate;

	/** Clears input only when editing */
	protected GPushButton btnClearInput;

	/**
	 * this panel contains the marble (radio) button
	 */
	protected MarblePanel marblePanel;

	protected FlowPanel definitionPanel;

	protected AlgebraOutputPanel outputPanel;

	protected Localization loc;

	private RadioTreeItemController controller;

	String lastTeX;
	private MathFieldW mf;
	private boolean selectedItem = false;

	// controls must appear on select or not
	private boolean forceControls = false;

	private AsyncOperation<GeoElementND> suggestionCallback;
	protected boolean first = false;
	private String ariaPreview;
	private Label ariaLabel = null;

	public void updateOnNextRepaint() {
		needsUpdate = true;
	}

	/**
	 * Minimal constructor
	 *
	 * @param kernel
	 *            kernel
	 */
	public RadioTreeItem(Kernel kernel) {
		super();
		this.kernel = kernel;
		app = (AppWFull) kernel.getApplication();
		loc = app.getLocalization();
		av = app.getAlgebraView();
		main = new FlowPanel();
		content = new FlowPanel();
		plainTextItem = new FlowPanel();
		setWidget(main);
		setController(createController());

		getController().setLongTouchManager(LongTouchManager.getInstance());
		setDraggable();
	}

	/**
	 * Creates a new RadioTreeItem for displaying/editing an existing GeoElement
	 * 
	 * @param geo0
	 *            the existing GeoElement to display/edit
	 */
	public RadioTreeItem(final GeoElement geo0) {
		this(geo0.getKernel());
		geo = geo0;

		addMarble();

		getPlainTextItem().addStyleName("avPlainText");
		getElement().getStyle().setColor("black");

		updateFont(getPlainTextItem());

		styleContent();

		addControls();

		content.add(getPlainTextItem());
		buildPlainTextItem();
		// if enabled, render with LaTeX
		String ltx = getLatexString(null, true);
		if (ltx != null) {
			if (getAV().isLaTeXLoaded()) {
				doUpdate();
			} else {
				setNeedsUpdate(true);
				av.repaintView();
			}

		}
		createAvexWidget();
		addAVEXWidget(content);
		getWidget().addStyleName("latexEditor");
		if (app.isUnbundled() && geo0.getParentAlgorithm() != null
				&& geo0.getParentAlgorithm() instanceof AlgoPointOnPath) {
			getWidget().getElement().getStyle().setProperty("minHeight", 72,
					Unit.PX);
		}
	}

	protected void addMarble() {
		main.addStyleName("elem");
		main.addStyleName("panelRow");

		marblePanel = new MarblePanel(this);
		main.add(marblePanel);
	}

	protected void styleContent() {
		content.addStyleName("elemText");
	}

	protected void createControls() {
		controls = new ItemControls(this);
	}

	protected void addControls() {
		if (controls != null) {
			return;
		}
		createControls();

		main.add(controls);
		controls.setVisible(true);
	}

	/**
	 *
	 */
	protected void createAvexWidget() {
		// only for checkboxes
	}

	protected final String getLatexString(Integer limit, boolean output) {
		return AlgebraItem.getLatexString(geo, limit, output);
	}

	private void buildPlainTextItem() {
		if (!AlgebraItem.buildPlainTextItemSimple(geo,
				new DOMIndexHTMLBuilder(getPlainTextItem(), app))) {
			buildItemContent();
		} else if (content.getWidgetCount() != 1) {
			// extra content shown (eg. arrow) => rebuild
			rebuildPlaintextContent();
		}
	}

	protected void createDVPanels() {
		if (definitionPanel == null) {
			definitionPanel = new FlowPanel();
		}

		if (outputPanel == null) {
			outputPanel = new AlgebraOutputPanel();
			outputPanel.addStyleName("avOutput");
		}
	}

	private boolean isLatexTrivial() {
		if (!latex) {
			return false;
		}

		String text = getTextForEditing(false, StringTemplate.latexTemplate);
		String[] eq = text.split("=");
		if (eq.length < 2) {
			return false;
		}
		String leftSide = eq[0].trim();
		String rightSide = eq[1].replaceFirst("\\\\left", "")
				.replaceFirst("\\\\right", "").replaceAll(" ", "");

		return leftSide.equals(rightSide);
	}

	private boolean updateDefinitionPanel() {
		if (lastInput != null) {
			definitionFromTeX(lastTeX);
		} else if (latex || AlgebraItem.isGeoFraction(geo)) {
			String text = getTextForEditing(false,
					StringTemplate.latexTemplateHideLHS);
			definitionFromTeX(text);
		} else if (geo != null) {
			IndexHTMLBuilder sb = new DOMIndexHTMLBuilder(definitionPanel, app);
			if (kernel.getAlgebraStyle() == Kernel.ALGEBRA_STYLE_DESCRIPTION) {
				if (AlgebraItem.needsPacking(geo)) {
					IndexHTMLBuilder
							.convertIndicesToHTML(
									geo.getDefinitionDescription(
											StringTemplate.defaultTemplate),
									sb);
				} else {
					geo.addLabelTextOrHTML(geo.getDefinitionDescription(
						StringTemplate.defaultTemplate), sb);
				}
			} else {
				geo.addLabelTextOrHTML(
					geo.getDefinition(StringTemplate.defaultTemplate), sb);
			}
		}
		return true;
	}

	private void definitionFromTeX(String text) {
		definitionPanel.clear();

		canvas = latexToCanvas(text);
		if (canvas != null) {
			canvas.addStyleName("canvasDef");
		}
		if (geo == null) {
			Log.debug("CANVAS to DEF");
		}
		definitionPanel.add(canvas);
	}

	protected boolean updateValuePanel(String text) {
		boolean ret = outputPanel.updateValuePanel(geo, text, latex,
				getFontSize(), app.getActivity());
		if (geo != null && AlgebraItem.shouldShowSymbolicOutputButton(geo)) {
			addControls();
			AlgebraOutputPanel.createSymbolicButton(controls, geo, true,
					app.getActivity());
		}
		return ret;
	}

	private void buildItemContent() {
		if (mayNeedOutput()) {
			if (controller.isEditing() || geo == null) {
				return;
			}
			if ((AlgebraItem.getDescriptionModeForGeo(geo,
					kernel.getAlgebraStyle()) == DescriptionMode.DEFINITION_VALUE
					&& !isLatexTrivial())
					|| lastTeX != null) {
				buildItemWithTwoRows();
				updateItemColor();
			} else {
				buildItemWithSingleRow();
			}

			controls.updateSuggestions(geo);

		} else {
			buildItemWithSingleRow();
		}

		adjustToPanel(content);
	}

	/**
	 * Get the first available suggestion for a geo or null if none available.
	 * 
	 * @param geo1
	 *            geo
	 * @return suggestion or null if none available
	 */
	Suggestion getSuggestion(GeoElement geo1) {
		return app.getArticleElement().getDataParamShowSuggestionButtons()
				&& app.getArticleElement()
						.getDataParamShowAlgebraInput(app.getArticleElement()
								.getDataParamShowMenuBar(false))
				&& controller.isEditing()
						? AlgebraItem
								.getSuggestions(geo1,
										WarningErrorHandler
												.getUndefinedValiables(kernel))
						: null;
	}

	private void buildItemWithTwoRows() {
		createDVPanels();
		String text = getLatexString(LATEX_MAX_EDIT_LENGHT, false);
		latex = text != null;
		if (latex) {
			definitionPanel.addStyleName("avDefinition");
		} else {
			definitionPanel.addStyleName("avDefinitionPlain");
		}

		content.clear();
		if (updateDefinitionPanel()) {
			plainTextItem.clear();
			plainTextItem.add(definitionPanel);
		}

		if (updateValuePanel(geo.getLaTeXDescriptionRHS(true,
				StringTemplate.latexTemplate))) {
			outputPanel.addValuePanel();
			plainTextItem.add(outputPanel);
		}

		content.add(plainTextItem);
	}

	private void clearPreview() {
		content.removeStyleName("avPreview");
		content.addStyleName("noPreview");
		if (outputPanel == null) {
			return;
		}
		ariaPreview = null;
		outputPanel.reset();
	}

	/**
	 * Clear preview and sugggestions
	 */
	public void clearPreviewAndSuggestions() {
		clearPreview();
		clearUndefinedVariables();
		if (controls != null) {
			controls.updateSuggestions(null);
		}
	}

	/**
	 * @param previewGeo
	 *            preview from input bar
	 */
	public void previewValue(GeoElement previewGeo) {
		if (getAV().getActiveTreeItem() != this) {
			return;
		}
		if ((previewGeo
				.needToShowBothRowsInAV() != DescriptionMode.DEFINITION_VALUE
				|| getController().isInputAsText())) {
			clearPreview();

		} else {
			content.removeStyleName("noPreview");
			content.addStyleName("avPreview");
			boolean forceLatex = false;
			if (previewGeo.isGeoFunction() || previewGeo.isGeoFunctionNVar()
					|| previewGeo.isGeoFunctionBoolean()) {
				forceLatex = true;
			}

			InputHelper.updateSymbolicMode(previewGeo);
			ariaPreview = previewGeo.getAuralExpression();

			createDVPanels();
			content.addStyleName("avPreview");
			plainTextItem.clear();
			plainTextItem.add(outputPanel);
			outputPanel.reset();

			IndexHTMLBuilder sb = new IndexHTMLBuilder(false);
			previewGeo.getAlgebraDescriptionTextOrHTMLDefault(sb);
			String plain = sb.toString();

			String text = previewGeo
					.getAlgebraDescription(StringTemplate.latexTemplate)
					.replace("undefined", "").trim();
			if (!StringUtil.empty(text)
					&& (text.charAt(0) == ':' || text.charAt(0) == '=')) {
				text = text.substring(1);
			}
			if (!plain.equals(text) || forceLatex) {
				outputPanel.showLaTeXPreview(text, previewGeo, getFontSize());
			}

			outputPanel.addArrowPrefix(app.getActivity());
			outputPanel.addValuePanel();

			if (content.getWidgetIndex(plainTextItem) == -1) {
				content.add(plainTextItem);
			}
		}
		buildSuggestions(previewGeo);
	}

	/**
	 * Create suggestions for preview
	 * 
	 * @param previewGeo
	 *            preview from input bar
	 */
	public void buildSuggestions(GeoElement previewGeo) {
		if (getSuggestion(previewGeo) != null) {
			addControls();
			controls.reposition();
			controls.updateSuggestions(geo == null ? previewGeo : geo);
		} else if (controls != null) {
			controls.updateSuggestions(geo == null ? previewGeo : geo);
		}
		clearUndefinedVariables();
	}

	protected void buildItemWithSingleRow() {
		// LaTeX
		String text = getLatexString(LATEX_MAX_EDIT_LENGHT,
				geo.needToShowBothRowsInAV() != DescriptionMode.DEFINITION);
		latex = text != null;

		if (latex) {
			if (isInputTreeItem()) {
				text = geo.getLaTeXAlgebraDescription(true,
						StringTemplate.latexTemplate);
			}

			canvas = DrawEquationW.paintOnCanvas(geo, text, canvas,
					getFontSize());
			content.clear();
			if (geo == null) {
				Log.debug("CANVAS to IHTML");
			}
			content.add(canvas);
		} else {
			geo.getAlgebraDescriptionTextOrHTMLDefault(
					new DOMIndexHTMLBuilder(getPlainTextItem(), app));
			updateItemColor();
			rebuildPlaintextContent();
		}
	}

	private void rebuildPlaintextContent() {
		content.clear();
		content.add(getPlainTextItem());
		adjustToPanel(plainTextItem);
		if (geo != null && geo.getParentAlgorithm() != null
				&& geo.getParentAlgorithm().getOutput(0) != geo
				&& mayNeedOutput()) {
			Label prefix = new Label(AlgebraItem.getSymbolicPrefix(kernel));
			content.addStyleName("additionalRow");
			prefix.addStyleName("prefix");
			updateFont(content);

			Image arrow = new NoDragImage(
					app.getActivity().getOutputPrefixIcon(), 24, 24);
			arrow.setStyleName("arrowOutputImg");
			content.insert(arrow, 0);
		}
	}

	protected void updateFont(Widget w) {
		int size = app.getFontSizeWeb() + 2;
		w.getElement().getStyle().setFontSize(size, Unit.PX);
	}

	protected void styleContentPanel() {
		controls.updateAnimPanel();
	}

	/**
	 * @param first
	 *            whether this item is first in AV
	 */
	public void setFirst(boolean first) {
		this.first = first;
		updateButtonPanelPosition();
	}

	/**
	 * Update this item if it was invalidated; update selection
	 */
	public void repaint() {
		if (isNeedsUpdate()) {
			doUpdate();
		}
		// highlight only
		boolean selected = geo.doHighlighting();

		setSelected(selected);

		// select only if it is in selection really.
		selectItem(selected);
	}

	/**
	 * @return whether an item is being edited
	 */
	public boolean commonEditingCheck() {
		return av.isEditItem() || controller.isEditing() || isInputTreeItem()
				|| geo == null;
	}

	protected void doUpdateEnsureNoEditor() {
		doUpdate();
		if (!latex) {
			rebuildPlaintextContent();
		}
	}

	protected void updateTextItems() {
		// check for new LaTeX
		boolean latexAfterEdit = false;

		if (!mayNeedOutput() && outputPanel != null) {
			content.remove(outputPanel);
		}
		if (kernel.getAlgebraStyle() == Kernel.ALGEBRA_STYLE_VALUE
				|| isDefinitionAndValue()) {
			String text = "";
			if (geo != null) {
				text = getLatexString(LATEX_MAX_EDIT_LENGHT, true);
				latexAfterEdit = (text != null);
			} else {
				latexAfterEdit = true;
			}

			if (latex && latexAfterEdit) {
				// Both original and edited text is LaTeX
				if (isInputTreeItem() && geo != null) {
					text = geo.getLaTeXAlgebraDescription(true,
							StringTemplate.latexTemplate);
				}
				updateLaTeX(text);

			} else if (latexAfterEdit) {
				// Edited text is latex now, but the original is not.
				renderLatex(text, getPlainTextItem(), isInputTreeItem());
				latex = true;
			}

		} else if (geo == null) {
			return;
		}
		// edited text is plain
		if (!latexAfterEdit) {

			buildPlainTextItem();
			if (latex) {
				// original text was latex.
				updateItemColor();
				content.clear();
				content.add(getPlainTextItem());
				latex = false;
			}
		}
	}

	private void updateItemColor() {
		if (isDefinitionAndValue() && definitionPanel != null) {
			definitionPanel.getElement().getStyle().setColor("black");
		}
	}

	private Canvas latexToCanvas(String text) {
		return DrawEquationW.paintOnCanvas(geo, text, canvas, getFontSize());
	}

	private void updateLaTeX(String text) {
		if (!isDefinitionAndValue()) {
			content.clear();
			canvas = DrawEquationW.paintOnCanvas(geo, text, canvas,
					getFontSize());

			content.add(canvas);
		}
	}

	protected void adjustToPanel(final FlowPanel panel) {
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
				int width = panel.getOffsetWidth()
						+ marblePanel.getOffsetWidth();
				setAVItemWidths(width);
			}
		});
	}

	void setAVItemWidths(int width) {
		if (getOffsetWidth() < width) {
			getAV().setItemWidth(width);
		}

	}

	/**
	 * @param width
	 *            item width
	 */
	public void setItemWidth(int width) {
		if (getOffsetWidth() != width) {
			if (isInputTreeItem()) {
				Element inputParent = getWidget().getElement()
						.getParentElement();
				Resizer.setPixelWidth(inputParent, width);
			} else {
				setWidth(width + "px");
			}
		}

		onResize();
	}

	protected void replaceToCanvas(String text, Widget old) {
		updateLaTeX(text);
		LayoutUtilW.replace(content, canvas, old);
	}

	/**
	 * @return size for JLM texts. Due to different fonts we need a bit more
	 *         than app.getFontSize(), but +3 looked a bit too big
	 */
	protected int getFontSize() {
		return app.getFontSizeWeb() + 1;
	}

	protected void updateColor(Widget w) {
		if (geo != null) {
			w.getElement().getStyle()
					.setColor(GColor.getColorString(geo.getAlgebraColor()));
		}
	}

	/**
	 * Switches to edit mode
	 * 
	 * @param substituteNumbers
	 *            whether value should be used
	 * @return whether it was successful
	 */
	public boolean enterEditMode(boolean substituteNumbers) {
		content.addStyleName("scrollableTextBox");
		if (isInputTreeItem()) {
			setItemWidth(getAV().getOffsetWidth());
		}

		if (controller.isEditing()) {
			return true;
		}

		if (controls != null) {
			controls.update(true);
		}

		controller.setEditing(true);
		insertHelpToggle();

		if (!onEditStart()) {
			return false;
		}
		if (controls != null) {
			controls.setVisible(true);
			updateButtonPanelPosition();
		}

		return true;
	}

	protected String getTextForEditing(boolean substituteNumbers,
			StringTemplate tpl) {
		if (AlgebraItem.needsPacking(geo) || !geo.isAlgebraLabelVisible()) {
			return geo.getLaTeXDescriptionRHS(substituteNumbers, tpl);
		}
		return geo.getLaTeXAlgebraDescriptionWithFallback(
				substituteNumbers
						|| (geo instanceof GeoNumeric && geo.isSimple()),
				tpl, true);

	}

	protected boolean isDefinitionAndValue() {
		return kernel
				.getAlgebraStyle() == Kernel.ALGEBRA_STYLE_DEFINITION_AND_VALUE;
	}

	protected boolean mayNeedOutput() {
		return kernel
				.getAlgebraStyle() == Kernel.ALGEBRA_STYLE_DEFINITION_AND_VALUE
				|| kernel
						.getAlgebraStyle() == Kernel.ALGEBRA_STYLE_DESCRIPTION;
	}

	private static boolean isMoveablePoint(GeoElement point) {
		return (point.isPointInRegion() || point.isPointOnPath())
				&& point.isPointerChangeable();
	}

	/**
	 * Remove style for scrollable item
	 */
	public void styleEditor() {
		if (!isInputTreeItem()) {
			content.removeStyleName("scrollableTextBox");
		}
	}

	/**
	 * Add style for scrollable item
	 */
	public void styleScrollBox() {
		content.addStyleName("scrollableTextBox");
	}

	/**
	 * @param newValue0
	 *            value after edit
	 * @param callback
	 *            callback
	 * @param allowSliderDialog
	 *            whether to allow slider dialog
	 */
	public final void stopEditing(final String newValue0,
			final AsyncOperation<GeoElementND> callback,
			boolean allowSliderDialog) {
		lastTeX = null;
		lastInput = null;
		onStopEdit();
		styleEditor();

		if (!app.isUnbundled() && stylebarShown != null) {
			getAlgebraDockPanel().showStyleBarPanel(stylebarShown);
			stylebarShown = null;
		}

		removeCloseButton();

		controller.setEditing(false);

		av.cancelEditItem();
		getAV().setLaTeXLoaded();

		if (controls != null) {
			controls.setVisible(true);
		}

		if (newValue0 != null) {
			String rawInput = stopCommon(newValue0);
			String v = app.getKernel().getInputPreviewHelper()
					.getInput(rawInput);
			String newValue = isTextItem() ? "\"" + v + "\"" : v;
			boolean valid = rawInput != null && rawInput.equals(newValue);
			final boolean wasLaTeX = geo instanceof GeoText
					&& ((GeoText) geo).isLaTeX();
			// Formula Hacks ended.
			if (geo != null) {
				boolean redefine = !isMoveablePoint(geo);
				this.lastInput = newValue;
				this.lastTeX = getEditorLatex();
				if (this.lastTeX == null) {
					this.lastInput = null;
				}

				kernel.getAlgebraProcessor().changeGeoElement(geo, newValue,
						redefine, true,
						getErrorHandler(valid, allowSliderDialog),
						new AsyncOperation<GeoElementND>() {

							@Override
							public void callback(GeoElementND geo2) {
								if (geo2 != null) {
									geo = geo2.toGeoElement();
								}
								if (geo instanceof GeoText && wasLaTeX
										&& geo.isIndependent()) {
									((GeoText) geo).setLaTeX(true, false);
								}
								if (marblePanel != null) {
									marblePanel.updateIcons(false);
								}
								lastTeX = null;
								lastInput = null;
								updateAfterRedefine(true);
								if (callback != null) {
									callback.callback(geo2);
								}

							}
						});
				// make sure edting ends: run callback even if not successful
				// TODO maybe prevent running this twice?
				if (!geo.isIndependent()) {
					if (callback != null) {
						callback.callback(geo);
					}
				}

				return;
			}
		} else {
			if (isDefinitionAndValue()) {
				cancelDV();
			}
		}

		// empty new value -- consider success to make sure focus goes away
		updateAfterRedefine(newValue0 == null);
	}

	/**
	 * @param success
	 *            whether redefinition was successful
	 */
	protected void doUpdateAfterRedefine(boolean success) {
		if (latexItem == null) {
			return;
		}
		if (!this.isInputTreeItem() && canvas != null
				&& content.getElement().isOrHasChild(latexItem.getElement())) {
			if (geo != null) {
				LayoutUtilW.replace(content, canvas, latexItem);
			}
		}

		if (!latex && !this.isInputTreeItem() && getPlainTextItem() != null
				&& content.getElement().isOrHasChild(latexItem.getElement())) {
			LayoutUtilW.replace(content, getPlainTextItem(), latexItem);

		}
		// maybe it's possible to enter something which is non-LaTeX
		if (success) {
			doUpdateEnsureNoEditor();
		}
	}

	protected boolean typeChanged() {
		return (isSliderItem() != getItemFactory().matchSlider(geo))
				|| (isCheckBoxItem() != ItemFactory.matchCheckbox(geo));
	}

	/**
	 * @return AV item factory
	 */
	protected ItemFactory getItemFactory() {
		return ((AlgebraViewW) av).getItemFactory();
	}

	private void cancelDV() {
		// LayoutUtilW.replace(ihtml, definitionPanel, latexItem);
		doUpdateEnsureNoEditor();
	}

	protected String stopCommon(String newValue) {
		return newValue;
	}

	protected void clearErrorLabel() {
		commandError = null;
		errorMessage = null;
	}

	protected final ErrorHandler getErrorHandler(final boolean valid,
			final boolean allowSliders) {
		return getErrorHandler(valid, allowSliders, false);
	}

	/**
	 * @param valid
	 *            whether this is for valid string (false = last valid substring
	 *            used)
	 * @param allowSliders
	 *            whether to allow sliders at all
	 * @param withSliders
	 *            whether to allow slider creation without asking
	 * @return error handler
	 */
	protected final ErrorHandler getErrorHandler(final boolean valid,
			final boolean allowSliders, final boolean withSliders) {
		clearErrorLabel();
		return new AVErrorHandler(this, valid, allowSliders, withSliders);
	}

	protected void saveError() {
		if (geo != null) {
			geo.setUndefined();
			geo.updateRepaint();
			updateAfterRedefine(true);
			if (marblePanel != null) {
				marblePanel.updateIcons(true);
			}
		}
	}

	boolean showCurrentError() {
		if (commandError != null) {
			ToolTipManagerW.sharedInstance().setBlockToolTip(false);
			ToolTipManagerW.sharedInstance().showBottomInfoToolTip(
					StringUtil.toHTMLString(errorMessage),
					app.getGuiManager().getHelpURL(Help.COMMAND, commandError),
					ToolTipLinkType.Help, app, true);
			ToolTipManagerW.sharedInstance().setBlockToolTip(true);
			return true;
		}

		if (errorMessage != null) {
			if (app.isUnbundled()) {
				return false;
			}
			ToolTipManagerW.sharedInstance().showBottomMessage(errorMessage,
					true, app);
			return true;

		}
		return false;
	}

	protected boolean setErrorText(String msg) {
		if (StringUtil.empty(msg)) {
			clearErrorLabel();
			return true;
		}
		ToolTipManagerW.sharedInstance().showBottomMessage(msg, true, app);
		return false;
	}

	/**
	 * @param show
	 *            whether to show input help
	 */
	public void setShowInputHelpPanel(boolean show) {
		if (show) {
			if (!av.isEditItem()) {
				ensureEditing();
			}

			removeDummy();

			if (isInputTreeItem()) {
				getController().setFocus(true);
			}
			InputBarHelpPanelW helpPanel = (InputBarHelpPanelW) app
					.getGuiManager().getInputHelpPanel();

			if (helpPopup == null) {
				helpPopup = new InputBarHelpPopup(this.app, this,
						"helpPopupAV");
				helpPopup.addAutoHidePartner(this.getElement());
				helpPopup.addCloseHandler(new CloseHandler<GPopupPanel>() {

					@Override
					public void onClose(CloseEvent<GPopupPanel> event) {
						focusAfterHelpClosed();
					}

				});

				if (marblePanel != null) {
					helpPopup.setBtnHelpToggle(marblePanel.getBtnHelpToggle());
				}
			} else if (helpPopup.getWidget() == null) {
				helpPanel = (InputBarHelpPanelW) app.getGuiManager()
						.getInputHelpPanel();
				helpPopup.add(helpPanel);
			}

			updateHelpPosition(helpPanel);

		} else if (helpPopup != null) {
			helpPopup.hide();
		}
	}

	boolean styleBarCanHide() {
		if (app.isUnbundled()
				|| !getAlgebraDockPanel().isStyleBarPanelShown()) {
			return false;
		}
		int itemTop = this.isInputTreeItem()
				? main.getElement().getAbsoluteTop()
				: getElement().getAbsoluteTop();
		return (itemTop - getAlgebraDockPanel().getAbsoluteTop() < 35);
	}

	/**
	 * @return width in case editor is open
	 */
	protected int getWidthForEdit() {
		if (isDefinitionAndValue()
				&& geo.needToShowBothRowsInAV() == DescriptionMode.DEFINITION_VALUE
				&& definitionPanel != null
				&& definitionPanel.getWidgetCount() > 0
				&& definitionPanel.getWidget(0) != null) {
			return marblePanel.getOffsetWidth()
					+ definitionPanel.getWidget(0).getOffsetWidth()
					+ controls.getOffsetWidth() + DEFINITION_ROW_EDIT_MARGIN
					+ MARGIN_RESIZE;
		}
		return marblePanel.getOffsetWidth() + content.getOffsetWidth()
				+ MARGIN_RESIZE;
	}

	/**
	 * Update position of buttons
	 */
	protected void updateButtonPanelPosition() {
		if (controls == null) {
			return;
		}

		controls.reposition();
	}

	protected AlgebraViewW getAV() {
		return (AlgebraViewW) av;
	}

	/**
	 * @return corresponding construction element
	 */
	public GeoElement getGeo() {
		return geo;
	}

	/**
	 * Scroll into view.
	 */
	public void scrollIntoView() {
		this.getElement().scrollIntoView();
	}

	/**
	 * Remove the controls.
	 */
	public void removeCloseButton() {
		if (controls != null) {
			controls.setVisible(false);
		}
	}

	protected void addAVEXWidget(Widget w) {
		main.add(w);
	}

	protected boolean hasGeoExtendedAV() {
		return (geo instanceof HasExtendedAV
				&& ((HasExtendedAV) geo).isShowingExtendedAV());
	}

	/**
	 * Make this draggable. Works with mouse only.
	 */
	public void setDraggable() {
		Widget draggableContent = main;
		getElement().setAttribute("position", "absolute");
		draggableContent.getElement().setDraggable(Element.DRAGGABLE_TRUE);
		draggableContent.addDomHandler(new DragStartHandler() {

			@Override
			public void onDragStart(DragStartEvent event) {
				event.setData("text", geo.getLabelSimple());
				event.getDataTransfer().setDragImage(getElement(), 10, 10);
				event.stopPropagation();
				getAV().dragStart(event, geo);
			}
		}, DragStartEvent.getType());
	}

	// @Override
	@Override
	public AppW getApplication() {
		return app;
	}

	@Override
	public void ensureEditing() {
		if (!controller.isEditing()) {
			enterEditMode(geo == null || isMoveablePoint(geo));

			if (av != null && ((AlgebraViewW) av).isNodeTableEmpty()) {
				updateGUIfocus(false);
			}
		}
	}

	@Override
	public ArrayList<String> getHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void requestFocus() {
		// TODO Auto-generated method stub
	}

	@Override
	public final Widget toWidget() {
		return main;
	}

	@Override
	public void onResize() {
		updateButtonPanelPosition();
	}

	public boolean isItemSelected() {
		return selectedItem;
	}

	/**
	 * @param selected
	 *            whether this ite is selected
	 */
	public void selectItem(boolean selected) {
		if (controls != null) {
			controls.show(!controller.hasMultiGeosSelected() && selected);
			controls.updateSuggestions(geo);
		}

		if (selectedItem == selected) {
			return;
		}

		setForceControls(false);

		selectedItem = selected;

		if (selected) {
			addStyleName("avSelectedRow");

		} else {
			removeStyleName("avSelectedRow");
		}
		if (marblePanel != null) {
			marblePanel.setHighlighted(selected);
		}
		if (!selected) {
			controls.reset();
		}
	}

	@Override
	public UIObject asWidget() {
		return main.asWidget();
	}

	/**
	 * cast method with no 'instanceof' check.
	 * 
	 * @param item
	 *            TreeItem to be casted
	 * @return Casted item to RadioTreeItem
	 */
	public static RadioTreeItem as(TreeItem item) {
		return (RadioTreeItem) item;
	}

	public Element getScrollElement() {
		return getWidget().getElement();
	}

	protected AlgebraPanelInterface getAlgebraDockPanel() {
		return (AlgebraPanelInterface) app.getGuiManager().getLayout()
				.getDockManager().getPanel(App.VIEW_ALGEBRA);

	}

	protected AlgebraPanelInterface getToolbarDockPanel() {
		return (AlgebraPanelInterface) app.getGuiManager().getLayout()
				.getDockManager().getPanel(App.VIEW_ALGEBRA);

	}

	/**
	 * @return whether this is a plaintext item (eg A=(1,1))
	 */
	public FlowPanel getPlainTextItem() {
		return plainTextItem;
	}

	private boolean isNeedsUpdate() {
		return needsUpdate;
	}

	/**
	 * Mark the item as (in)valid.
	 * 
	 * @param needsUpdate
	 *            whether update is needed
	 */
	public void setNeedsUpdate(boolean needsUpdate) {
		this.needsUpdate = needsUpdate;
	}

	protected boolean hasAnimPanel() {
		return controls.animPanel != null;
	}

	protected boolean hasMarblePanel() {
		return marblePanel != null;
	}

	/**
	 * Remove the main panel from parent
	 */
	public void removeFromParent() {
		main.removeFromParent();
	}

	public final boolean hasHelpPopup() {
		return this.helpPopup != null;
	}

	@Override
	public UIObject getHelpToggle() {
		return this.marblePanel;
	}

	private void updateIcons(boolean warning) {
		if (this.marblePanel != null) {
			marblePanel.updateIcons(warning);
		}
	}

	protected void updateHelpPosition(final InputBarHelpPanelW helpPanel) {
		helpPopup.setPopupPositionAndShow(new GPopupPanel.PositionCallback() {
			@Override
			public void setPosition(int offsetWidth, int offsetHeight) {
				Widget btn = marblePanel.getBtnHelpToggle() == null
						? marblePanel.getBtnPlus()
						: marblePanel.getBtnHelpToggle();
				double scale = app.getArticleElement().getScaleX();
				double renderScale = app.getArticleElement().getDataParamApp()
						? scale : 1;
				helpPopup.getElement().getStyle()
						.setProperty("left",
								(btn.getAbsoluteLeft() - app.getAbsLeft()
										+ btn.getOffsetWidth()) * renderScale
										+ "px");
				int maxOffsetHeight;
				int totalHeight = (int) app.getHeight();
				int toggleButtonTop = (int) ((btn.getParent().getAbsoluteTop()
						- (int) app.getAbsTop()) / scale);
				if (toggleButtonTop < totalHeight / 2) {
					int top = (toggleButtonTop
							+ btn.getParent().getOffsetHeight());
					maxOffsetHeight = totalHeight - top;
					helpPopup.getElement().getStyle().setProperty("top",
							top * renderScale + "px");
					helpPopup.getElement().getStyle().setProperty("bottom",
							"auto");
					helpPopup.removeStyleName("helpPopupAVBottom");
					helpPopup.addStyleName("helpPopupAV");
				} else {
					int minBottom = app.isApplet() ? 0 : 10;
					int bottom = (totalHeight - toggleButtonTop);
					maxOffsetHeight = bottom > 0 ? totalHeight - bottom
							: totalHeight - minBottom;
					helpPopup.getElement().getStyle().setProperty("bottom",
							(bottom > 0 ? bottom : minBottom) * renderScale
									+ "px");
					helpPopup.getElement().getStyle().setProperty("top",
							"auto");
					helpPopup.removeStyleName("helpPopupAV");
					helpPopup.addStyleName("helpPopupAVBottom");
				}
				helpPanel.updateGUI(maxOffsetHeight, 1);
				helpPopup.show();
			}
		});
	}

	protected final void insertHelpToggle() {
		updateIcons(false);
		if (marblePanel == null) {
			this.addMarble();
		}
		main.insert(marblePanel, 0);
	}

	/**
	 * @return whether input text is empty
	 */
	protected boolean isEmpty() {
		return "".equals(getText().trim());
	}

	protected void addDummyLabel() {
		if (dummyLabel == null) {
			dummyLabel = new Label(
					loc.getMenu("InputLabel") + Unicode.ELLIPSIS);
			dummyLabel.addStyleName("avDummyLabel");
			ariaLabel = new Label();
			ariaLabel.addStyleName("hidden");
			content.add(ariaLabel);
		}
		clearUndefinedVariables();
		updateFont(dummyLabel);
		if (canvas != null) {
			canvas.setVisible(false);
		}

		content.insert(dummyLabel, 0);
		removeOutput();

		if (btnClearInput != null) {
			btnClearInput.removeFromParent();
			btnClearInput = null;
		}
		if (controls != null) {
			controls.setVisible(true);
		}
		setLatexItemVisible(false);
	}

	private void setLatexItemVisible(boolean b) {
		if (this.latexItem != null) {
			this.latexItem.setVisible(b);
		}
	}

	protected void removeDummy() {
		if (this.dummyLabel != null) {
			dummyLabel.removeFromParent();
		}
		if (canvas != null) {
			canvas.setVisible(true);
		}

		if (isInputTreeItem()) {
			content.insert(getClearInputButton(), 0);

			if (controls != null) {
				controls.setVisible(true);
			}
			adjustStyleBar();
		}
		setLatexItemVisible(true);
	}

	protected void updateEditorFocus(boolean blurtrue) {
		// deselects current selection
		if (app.isUnbundled()) {
			if (controls != null) {
				updateButtonPanelPosition();
			}

			return;
		}

		((AlgebraViewW) av).setActiveTreeItem(null);

		boolean emptyCase = ((AlgebraViewW) av).isNodeTableEmpty()
				&& !this.getAlgebraDockPanel().hasLongStyleBar();

		// update style bar icon look
		if (!app.isUnbundled()) {
			if (emptyCase) {
				getAlgebraDockPanel().showStyleBarPanel(blurtrue);
			} else {
				getAlgebraDockPanel().showStyleBarPanel(true);
			}
		}

		// After changing the stylebar visibility, maybe the small stylebar's
		// position will be changed too
		if (controls != null) {
			updateButtonPanelPosition();
		}
	}

	@Override
	public void updatePosition(AbstractSuggestionDisplay sug) {
		sug.setPositionRelativeTo(content);
	}

	@Override
	public boolean isForCAS() {
		return false;
	}

	@Override
	public boolean needsAutofocus() {
		return true;
	}

	protected GPushButton getClearInputButton() {
		if (btnClearInput == null) {
			btnClearInput = new GPushButton(
					new Image(GuiResources.INSTANCE.algebra_delete()));
			ClickStartHandler.init(btnClearInput,
					new ClickStartHandler(false, true) {

						@Override
						public void onClickStart(int x, int y,
								PointerEventType type) {
							clearInput();
							getController().setFocus(true);

						}
					});
			btnClearInput.addStyleName("ggb-btnClearAVInput");
		}
		return btnClearInput;
	}

	public boolean isForceControls() {
		return forceControls;
	}

	public void setForceControls(boolean forceControls) {
		this.forceControls = forceControls;
	}

	/**
	 * @return whether this is a slider item
	 */
	public boolean isSliderItem() {
		return false;
	}

	/**
	 * @return whether this is a checkbox item
	 */
	public boolean isCheckBoxItem() {
		return false;
	}

	/**
	 * @return whether this is a text item
	 */
	public boolean isTextItem() {
		return false;
	}

	/**
	 * @return controller
	 */
	public RadioTreeItemController getController() {
		return controller;
	}

	/**
	 * @param controller
	 *            controller
	 */
	public void setController(RadioTreeItemController controller) {
		this.controller = controller;
	}

	/**
	 * Move the controls to fit new size.
	 */
	public void reposition() {
		if (controls != null) {
			controls.reposition();
		}
	}

	/**
	 * @return whether this belongs to an existing geo
	 */
	public boolean hasGeo() {
		return geo != null;
	}

	/**
	 * hide control buttons
	 */
	public void hideControls() {
		if (controls != null) {
			controls.setVisible(false);
		}
	}

	void adjustStyleBar() {
		if (app.isUnbundled()) {
			return;
		}

		if (styleBarCanHide() && (!getAlgebraDockPanel().isStyleBarVisible())) {
			stylebarShown = getAlgebraDockPanel().isStyleBarPanelShown();
			getAlgebraDockPanel().showStyleBarPanel(false);
			if (controls != null) {
				controls.reposition();
			}
		}
	}

	/**
	 * Show control buttons
	 */
	public void showControls() {
		if (controls != null) {
			controls.setVisible(true);
		}
	}

	@Override
	public void setError(String error) {
		this.errorMessage = error;

		this.commandError = null;

		if (marblePanel != null) {
			marblePanel.updateIcons(error != null);
		}
	}

	@Override
	public void setCommandError(String command) {
		this.commandError = command;
		if (marblePanel != null) {
			marblePanel.updateIcons(true);
		}
	}

	/**
	 * Remove output panel
	 */
	public void removeOutput() {
		if (outputPanel != null) {
			outputPanel.removeFromParent();
		}
	}

	protected RadioTreeItemController createController() {
		return new LatexTreeItemController(this);
	}

	/**
	 * 
	 * @return The controller as LatexTreeItemController.
	 */
	public LatexTreeItemController getLatexController() {
		return (LatexTreeItemController) getController();
	}

	/**
	 * Show keyboard
	 */
	public void showKeyboard() {
		// TODO default implementation is infinite recursion
		getLatexController().showKeyboard();
	}

	/**
	 * @param text0
	 *            new text
	 * @param showKeyboard
	 *            whether to show keyboard
	 */
	protected void renderLatex(String text0, boolean showKeyboard) {
		content.clear();

		if (!(latexItem == null || isInputTreeItem() || isSliderItem())) {
			latexItem.getElement().getStyle().setProperty("minHeight",
					getController().getEditHeigth() + "px");
		}

		ensureCanvas();
		appendCanvas();

		if (!content.isAttached()) {
			main.add(content);
		}

		setText(text0);
		getLatexController().initAndShowKeyboard(showKeyboard);
	}

	private void appendCanvas() {
		if (latexItem == null) {
			latexItem = new FlowPanel();
		}
		latexItem.clear();
		latexItem.add(canvas);
		content.add(latexItem);
	}

	/**
	 * @return whether canvas was created
	 */
	protected boolean ensureCanvas() {
		if (canvas == null) {
			canvas = Canvas.createIfSupported();
			initMathField();
			return true;
		}
		if (mf == null) {
			initMathField();
		}

		return false;
	}

	private void initMathField() {
		if (latexItem == null) {
			latexItem = new FlowPanel();
		}

		FactoryProvider.setInstance(new FactoryProviderGWT());

		mf = new MathFieldW(new FormatConverterImpl(kernel), latexItem, canvas,
				getLatexController(),
				app.has(Feature.MOW_DIRECT_FORMULA_CONVERSION),
				app.getGlobalKeyDispatcher().getFocusHandler());
		mf.setExpressionReader(ScreenReader.getExpressionReader(app));
		updateEditorAriaLabel("");
		mf.setFontSize(getFontSize());
		mf.setPixelRatio(app.getPixelRatio());
		mf.setScale(app.getArticleElement().getScaleX());
		mf.setOnBlur(getLatexController());
	}

	private void updateEditorAriaLabel(String text) {
		if (mf != null) {
			if (!StringUtil.emptyTrim(text)) {
				String label = ScreenReader.getAriaExpression(app, text,
						ariaPreview);
				if (StringUtil.empty(label)) {
					label = mf.getDescription(
							ScreenReader.getExpressionReader(app));
				}
				mf.setAriaLabel(label);
			} else {
				mf.setAriaLabel(loc.getMenu("EnterExpression"));
			}
		}
	}

	@Override
	public void setFocus(boolean focus, boolean sv) {
		if (focus) {
			if (app.isUnbundled() && app.isMenuShowing()) {
				app.toggleMenu();
			}
			removeDummy();

			content.addStyleName("scrollableTextBox");
			if (isInputTreeItem()) {
				MinMaxPanel.closeMinMaxPanel();

				getAV().restoreWidth(true);

			}
		} else {
			if (isInputTreeItem()) {
				setItemWidth(getAV().getFullWidth());
			} else {
				content.removeStyleName("scrollableTextBox");
			}
		}

		if (ensureCanvas()) {
			main.clear();
			main.add(this.marblePanel);

			if (isInputTreeItem()) {
				appendCanvas();
			}
			main.add(content);
			if (controls != null) {
				main.add(controls);
				updateButtonPanelPosition();
			}
		}

		if (focus) {
			preventBlur();
			canvas.setVisible(true);
		} else {
			if (geo == null && errorMessage == null) {
				addDummyLabel();
			}
		}
		mf.setFocus(focus);

		int kH = (int) (app.getAppletFrame().getKeyboardHeight());
		int h = getToolbarDockPanel().getOffsetHeight();
		if (h < kH) {
			app.adjustViews(true, false);
		}
	}

	@Override
	public String getText() {
		if (mf == null) {
			return "";
		}
		return mf.getText();
	}

	@Override
	public void onEnter(final boolean keepFocus) {
		getLatexController().onEnter(keepFocus, false);
	}

	private void onEnterWithSliders() {
		getLatexController().onEnter(true, true);
	}

	@Override
	public void setText(String text0) {
		if (!"".equals(text0)) {
			removeDummy();
		}
		if (mf != null) {
			mf.setText(text0, this.isTextItem());
		}
		updateEditorAriaLabel(text0);
		updatePreview();
	}

	@Override
	public void setLabels() {
		if (dummyLabel != null) {
			dummyLabel.setText(loc.getMenu("InputLabel") + Unicode.ELLIPSIS);
		}
		if (hasMarblePanel()) {
			marblePanel.setLabels();
			marblePanel.updateIcons(errorMessage != null);
		}
		if (controls != null) {
			controls.setLabels();
		}
		updateEditorAriaLabel(getText());
	}

	@Override
	public String getCommand() {
		return mf == null ? "" : mf.getCurrentWord();
	}

	@Override
	public void autocomplete(String text) {
		getLatexController().autocomplete(text);
	}

	protected void focusAfterHelpClosed() {
		getController().setFocus(true);
	}

	/**
	 * Update after key was typed
	 */
	public void onKeyTyped() {
		this.removeDummy();
		app.closePerspectivesPopup();
		updatePreview();
		popupSuggestions();
		onCursorMove();
		if (mf != null) {
			updateEditorAriaLabel(getText());
		}
	}

	/**
	 * Cursor listener
	 */
	public void onCursorMove() {
		MathFieldW.scrollParent(latexItem, 20);
	}

	/**
	 * Show suggestions.
	 * 
	 * @return whether suggestions are shown
	 */
	public boolean popupSuggestions() {
		if (controller.isInputAsText()) {
			return false;
		}
		return getInputSuggestions().popupSuggestions();
	}

	/**
	 * @return suggestions model
	 */
	InputSuggestions getInputSuggestions() {
		return getLatexController().getInputSuggestions();
	}

	private void updatePreview() {
		if (getController().isInputAsText()) {
			return;
		}
		String text = getText();
		app.getKernel().getInputPreviewHelper().updatePreviewFromInputBar(text,
				AlgebraInputW.getWarningHandler(this, app));
	}

	/**
	 * @return copy of this item
	 */
	public RadioTreeItem copy() {
		return new RadioTreeItem(geo);
	}

	@Override
	public void insertString(String text) {
		new MathFieldProcessing(mf).autocomplete(text);
	}

	/**
	 * Cancel editing
	 */
	public void cancelEditing() {
		stopEditing(null, null, true);
		updateIcons(this.errorMessage != null);
		app.getActiveEuclidianView().requestFocus();
	}

	protected void renderLatex(String text0, Widget w, boolean forceMQ) {
		if (!forceMQ) {
			replaceToCanvas(text0, w);
		}
	}

	protected void clearInput() {
		setText("");
	}

	protected void updateGUIfocus(boolean blurtrue) {
		if (geo == null) {
			updateEditorFocus(blurtrue);
		}
	}

	@Override
	public List<String> getCompletions() {
		return getInputSuggestions().getCompletions();
	}

	@Override
	public List<String> resetCompletions() {
		return getInputSuggestions().resetCompletions();
	}

	@Override
	public boolean getAutoComplete() {
		return true;
	}

	@Override
	public boolean isSuggesting() {
		return getLatexController().isSuggesting();
	}

	/**
	 * Update pixel ratio of the editor.
	 * 
	 * @param pixelRatio
	 *            pixel ratio
	 */
	public void setPixelRatio(double pixelRatio) {
		if (mf != null) {
			mf.setPixelRatio(pixelRatio);
			mf.setScale(app.getArticleElement().getScaleX());
			mf.repaint();
		}
	}

	protected void updateAfterRedefine(boolean success) {
		if (mf != null && success) {
			mf.setEnabled(false);
		}
		doUpdateAfterRedefine(success);
		if (ariaLabel != null) {
			ariaLabel.getElement().focus();

		}
	}

	/**
	 * 
	 * @return if the item is the input or not.
	 */
	public boolean isInputTreeItem() {
		return getAV().getInputTreeItem() == this;
	}

	/**
	 * @return math field
	 */
	public MathFieldW getMathField() {
		return mf;
	}

	/**
	 * Start editing.
	 * 
	 * @return whether editng is possible
	 */
	public boolean onEditStart() {
		String text;
		if (geo == null) {
			text = "";
		} else if (AlgebraItem.needsPacking(geo)
				|| !geo.isAlgebraLabelVisible()) {
			text = geo.getLaTeXDescriptionRHS(false,
					StringTemplate.editTemplate);
		} else {
			text = geo.getDefinitionForEditor();
		}

		if (geo != null && !geo.isDefined() && lastInput != null) {
			text = lastInput;
		}
		if (text == null) {
			return false;
		}
		clearErrorLabel();
		removeDummy();

		renderLatex(text, true);
		getMathField().requestViewFocus();
		app.getGlobalKeyDispatcher().setFocused(true);
		// canvas.addBlurHandler(getLatexController());
		CancelEventTimer.keyboardSetVisible();
		ClickStartHandler.init(main, new ClickStartHandler(false, false) {
			@Override
			public void onClickStart(int x, int y,
					final PointerEventType type) {
				getLatexController().setOnScreenKeyboardTextField();
			}
		});

		return true;
	}

	/**
	 * Move caret to click position.
	 * 
	 * @param x
	 *            caret x in canvas
	 * @param y
	 *            caret y in canvas
	 */
	public void adjustCaret(int x, int y) {
		if (mf != null) {
			mf.adjustCaret(x, y);
		}
	}

	/**
	 * Update font size of editor
	 */
	public final void updateFonts() {
		if (mf != null) {
			mf.setFontSize(getFontSize());
		}
	}

	/**
	 * @return LaTeX content of editor
	 */
	protected String getEditorLatex() {
		return mf == null ? null
				: TeXSerializer.serialize(mf.getFormula().getRootComponent());
	}

	protected void doUpdate() {
		if (mf != null) {
			mf.setEnabled(false);
		}
		setNeedsUpdate(false);
		if (typeChanged()) {
			av.remove(geo);
			((AlgebraViewW) av).add(geo, -1, false);
			return;
		}
		if (hasMarblePanel()) {
			marblePanel.update();
		}

		if (controls != null) {
			controls.updateAnimPanel();
		}

		if (!isInputTreeItem() && mayNeedOutput()) {
			buildItemContent();
		} else {
			updateTextItems();
		}

		if (plainTextItem != null) {
			updateFont(plainTextItem);
		}
	}

	public void preventBlur() {
		getController().preventBlur();
	}

	/**
	 * Start listening to blur events
	 */
	protected void listenToBlur() {
		mf.setOnBlur(getLatexController());
	}

	/**
	 * Switches editor to text mode
	 * 
	 * @param value
	 *            switches editor to text mode
	 */
	protected void setInputAsText(boolean value) {
		mf.setPlainTextMode(value);
	}

	/**
	 * Initialize LaTeX input
	 * 
	 * @return this
	 */
	public RadioTreeItem initInput() {
		this.insertHelpToggle();

		content.addStyleName("scrollableTextBox");
		if (isInputTreeItem()) {
			content.addStyleName("inputBorder");
		}

		getWidget().addStyleName("latexEditor");
		content.addStyleName("noPreview");
		renderLatex("", false);
		content.getElement().setTabIndex(GUITabs.AV_INPUT);
		return this;
	}

	/**
	 * @param withSuggestions
	 *            whether to show suggestions
	 */
	public void toggleSuggestionStyle(boolean withSuggestions) {
		if (withSuggestions) {
			content.addStyleName("withSuggestions");
			content.removeStyleName("noSuggestions");
		} else {
			if (content.getStyleName().contains("withSuggestions")) {
				content.addStyleName("noSuggestions");
			}
			content.removeStyleName("withSuggestions");
		}
	}

	/**
	 * Run suggestion for the new geo.
	 * 
	 * @param nGeo
	 *            new geo element
	 */
	public void runSuggestionCallbacks(GeoElementND nGeo) {
		if (controls != null) {
			controls.updateSuggestions(geo); // old geo
		}
		if (suggestionCallback != null && nGeo != null) {
			suggestionCallback.callback(nGeo);
			suggestionCallback = null;
		}
	}

	/**
	 * Set suggestion callback.
	 * 
	 * @param run
	 *            callback
	 * @param autoSliders
	 *            whether to create sliders
	 */
	public void runAfterGeoCreated(AsyncOperation<GeoElementND> run,
			boolean autoSliders) {
		if (geo != null) {
			run.callback(geo);
		} else {
			if (autoSliders) {
				onEnterWithSliders();
				return;
			}
			this.suggestionCallback = run;
		}
	}

	/**
	 * @return width of the whole item
	 */
	public int getItemWidth() {
		return geo == null ? main.getOffsetWidth() : getOffsetWidth();
	}

	public Element getContentElement() {
		return content.getElement();
	}

	@Override
	public void setUndefinedVariables(String vars) {
		WarningErrorHandler.setUndefinedValiables(vars);
	}

	public void clearUndefinedVariables() {
		WarningErrorHandler.setUndefinedValiables(null);
	}

	public void onStopEdit() {
		// for slider only
	}

	/**
	 * 
	 * @return the tab index of the item.
	 */
	public int getTabIndex() {
		return main.getElement().getTabIndex();
	}

	/**
	 * Move HTML focus to this element
	 */
	public void focus() {
		if (isInputTreeItem()) {
			marblePanel.focusPlusButton();
		}
	}

	/**
	 * Open settings context menu for this item.
	 */
	public void openMoreMenu() {
		controls.showMoreContexMenu();
	}
}
