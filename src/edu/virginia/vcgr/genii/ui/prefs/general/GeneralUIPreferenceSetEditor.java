package edu.virginia.vcgr.genii.ui.prefs.general;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import org.morgan.utils.gui.GUIUtils;

class GeneralUIPreferenceSetEditor extends JPanel
{
	static final long serialVersionUID = 0L;
	
	private LocalContainerPanel _localContainerPanel;
	
	GeneralUIPreferenceSetEditor(GeneralUIPreferenceSet gps)
	{
		super(new GridBagLayout());
		
		_localContainerPanel = new LocalContainerPanel(gps.localContainerName());
		if (_localContainerPanel.hasChoices())
			add(GUIUtils.addTitle("Local Containers", _localContainerPanel),
				new GridBagConstraints(0 , 0, 1, 1, 1.0, 1.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 5, 5, 5), 5, 5));
	}
	
	final String selectedContainer()
	{
		return _localContainerPanel.getSelectedContainer();
	}
}