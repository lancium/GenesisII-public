package edu.virginia.vcgr.genii.gjt.gui.resource;

import javax.swing.JComboBox;

import edu.virginia.vcgr.genii.gjt.prefs.ToolPreferences;

@SuppressWarnings("rawtypes")
public class GPUComboBox extends JComboBox
{
	static final long serialVersionUID = 0L;

	@SuppressWarnings("unchecked")
	public GPUComboBox(ToolPreferences preferences)
	{
		super(new GPUComboModel(preferences));

		setEditable(false);
	}
}