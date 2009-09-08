package edu.virginia.vcgr.genii.ui.prefs.general;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.install.ContainerInformation;
import edu.virginia.vcgr.genii.client.install.InstallationState;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;

class LocalContainerPanel extends JPanel
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(LocalContainerPanel.class);
	
	private JRadioButton []_buttons;
	
	LocalContainerPanel(String def)
	{
		super(new GridBagLayout());
		
		boolean selectedOne = false;
		List<String> containerNames = null;
		
		HashMap<String, ContainerInformation> containers;
		
		try
		{
			containers = InstallationState.getRunningContainers();
		}
		catch (FileLockException cause)
		{
			_logger.warn("Unable to get local container information.");
			containers = null;
		}
		
		if (containers == null)
			containerNames = new Vector<String>();
		else
			containerNames = new Vector<String>(containers.keySet());
		Collections.sort(containerNames);
		
		_buttons = new JRadioButton[containerNames.size()];
		ButtonGroup group = new ButtonGroup();
		for (int lcv = 0; lcv < _buttons.length; lcv++)
		{
			String containerName = containerNames.get(lcv);
			boolean selected = (def != null && def.equals(containerName));
			if (selected)
				selectedOne = true;
			
			_buttons[lcv] = new JRadioButton(
				containerName, selected);
			group.add(_buttons[lcv]);
			
			add(_buttons[lcv], new GridBagConstraints(0, lcv, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		}
		
		if (!selectedOne && _buttons.length > 0)
			_buttons[0].setSelected(true);
	}
	
	public boolean hasChoices()
	{
		return _buttons.length > 1;
	}
	
	public String getSelectedContainer()
	{
		if (_buttons.length == 0)
			return null;
		
		for (JRadioButton button : _buttons)
		{
			if (button.isSelected())
				return button.getText();
		}
		
		return null;
	}
}