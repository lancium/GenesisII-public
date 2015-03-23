package edu.virginia.vcgr.genii.gui.swing;

import java.awt.Cursor;

import javax.swing.JComponent;
import javax.swing.RootPaneContainer;

public class GuiStatusTools
{
	/**
	 * displays the busy cursor, given any valid component of the UI.
	 */
	public static void showApplicationIsBusy(JComponent component)
	{
		RootPaneContainer root = (RootPaneContainer) component.getTopLevelAncestor();
		root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		root.getGlassPane().setVisible(true);
	}

	/**
	 * displays the normal cursor again, given any valid component of the UI.
	 */
	public static void showApplicationIsResponsive(JComponent component)
	{
		RootPaneContainer root = (RootPaneContainer) component.getTopLevelAncestor();
		root.getGlassPane().setCursor(Cursor.getDefaultCursor());
		root.getGlassPane().setVisible(false);
	}

}
