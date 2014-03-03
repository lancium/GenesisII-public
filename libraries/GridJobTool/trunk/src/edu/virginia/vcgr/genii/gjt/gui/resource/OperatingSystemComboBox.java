package edu.virginia.vcgr.genii.gjt.gui.resource;

import javax.swing.JComboBox;

import edu.virginia.vcgr.genii.gjt.prefs.ToolPreferences;

@SuppressWarnings("rawtypes")
public class OperatingSystemComboBox extends JComboBox
{
	static final long serialVersionUID = 0L;

	@SuppressWarnings("unchecked")
	public OperatingSystemComboBox(ToolPreferences preferences)
	{
		super(new OperatingSystemComboModel(preferences));

		setEditable(false);
	}
}