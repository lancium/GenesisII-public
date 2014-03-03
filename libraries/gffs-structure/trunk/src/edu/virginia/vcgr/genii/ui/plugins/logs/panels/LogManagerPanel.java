package edu.virginia.vcgr.genii.ui.plugins.logs.panels;

import java.awt.GridBagLayout;
import java.util.Collection;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.ui.plugins.logs.tree.LogPath;

public abstract class LogManagerPanel extends JPanel
{
	public LogManagerPanel(GridBagLayout layout)
	{
		super(layout);
	}

	private static final long serialVersionUID = 1L;

	abstract public void updateStatus(Collection<LogPath> descriptions);

}
