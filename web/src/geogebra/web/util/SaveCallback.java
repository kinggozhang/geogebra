package geogebra.web.util;

import geogebra.common.move.ggtapi.models.Material;
import geogebra.html5.gui.tooltip.ToolTipManagerW;
import geogebra.html5.main.AppW;
import geogebra.web.gui.GuiManagerW;

/**
 * @author geogebra
 *
 */
public class SaveCallback {
	
	private final AppW app;
	
	/**
	 * @param app {@link AppW}
	 */
	public SaveCallback(final AppW app) {
	    this.app = app;
    }
	
	/**
	 * @param app {@link AppW}
	 */
	public static void onSaved(AppW app){
		app.setSaved();
		ToolTipManagerW.sharedInstance().showBottomMessage(app.getMenu("SavedSuccessfully"), true);
	}
	/**
	 * shows info to user and sets app saved
	 * @param mat Material
	 * @param isLocal boolean
	 */
	public void onSaved(final Material mat, final boolean isLocal) {
		onSaved(app);
		if (((GuiManagerW) app.getGuiManager()).browseGUIwasLoaded()) {
			((GuiManagerW) app.getGuiManager()).getBrowseGUI().refreshMaterial(mat, isLocal);
		}
	}

	/**
	 * shows tooltip "save file failed"
	 */
	public void onError() {
		ToolTipManagerW.sharedInstance().showBottomMessage(app.getLocalization().getError("SaveFileFailed"), true);
    }
}
