package edu.virginia.vcgr.genii.ui.prefs.general;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import org.morgan.utils.gui.GUIUtils;

import edu.virginia.vcgr.genii.ui.shell.InputBindingsType;

class GeneralUIPreferenceSetEditor extends JPanel
{
	static final long serialVersionUID = 0L;
	
	private InputBindingsPanel _inputBindingsPanel;
	private LocalContainerPanel _localContainerPanel;
	
	GeneralUIPreferenceSetEditor(GeneralUIPreferenceSet gps)
	{
		super(new GridBagLayout());
		
		_inputBindingsPanel = new InputBindingsPanel(gps.bindingsType());
		add(GUIUtils.addTitle(gps.preferenceSetName(), _inputBindingsPanel),
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		
		_localContainerPanel = new LocalContainerPanel(gps.localContainerName());
		if (_localContainerPanel.hasChoices())
			add(GUIUtils.addTitle("Local Containers", _localContainerPanel),
				new GridBagConstraints(0 ,1, 1, 1, 1.0, 1.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(5, 5, 5, 5), 5, 5));
	}
	
	final InputBindingsType selectedBindingsType()
	{
		return _inputBindingsPanel.selectedBindingsType();
	}
	
	final String selectedContainer()
	{
		return _localContainerPanel.getSelectedContainer();
	}
}