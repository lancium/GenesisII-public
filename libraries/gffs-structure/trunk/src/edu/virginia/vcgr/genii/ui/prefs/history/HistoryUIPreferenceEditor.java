package edu.virginia.vcgr.genii.ui.prefs.history;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;

class HistoryUIPreferenceEditor extends JPanel
{
	static final long serialVersionUID = 0L;

	PreferredLevelPanel _levelPanel;

	HistoryUIPreferenceEditor(HistoryUIPreferenceSet set)
	{
		super(new GridBagLayout());

		_levelPanel = new PreferredLevelPanel(set.preferredLevel());

		add(_levelPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
	}

	final HistoryEventLevel preferredLevel()
	{
		return _levelPanel.selectedItem();
	}
}
