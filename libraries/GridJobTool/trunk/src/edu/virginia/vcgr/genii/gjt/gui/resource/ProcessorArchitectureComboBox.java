package edu.virginia.vcgr.genii.gjt.gui.resource;

import javax.swing.JComboBox;

import edu.virginia.vcgr.genii.gjt.prefs.ToolPreferences;

@SuppressWarnings("rawtypes")
public class ProcessorArchitectureComboBox extends JComboBox
{
	static final long serialVersionUID = 0L;

	@SuppressWarnings("unchecked")
	public ProcessorArchitectureComboBox(ToolPreferences preferences)
	{
		super(new ProcessorArchitectureComboModel(preferences));

		setEditable(false);
	}
}