package edu.virginia.vcgr.genii.ui.prefs.security;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.morgan.utils.gui.GUIUtils;

import edu.virginia.vcgr.genii.client.security.VerbosityLevel;

public class SecurityUIPreferenceSetEditor extends JPanel
{
	static final long serialVersionUID = 0L;
	
	private VerbosityLevel _level;
	
	private JComponent createVerbosityLevelPanel(VerbosityLevel level)
	{
		JPanel panel = new JPanel(new GridBagLayout());
		
		ButtonGroup group = new ButtonGroup();
		VerbosityLevel []levels = VerbosityLevel.values();
		for (int lcv = 0; lcv < levels.length; lcv++)
		{
			JRadioButton button = new JRadioButton(
				new LevelAction(levels[lcv]));
			group.add(button);
			panel.add(button, new GridBagConstraints(0, lcv,
				1, 1, 1.0, 1.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
			
			if (levels[lcv] == level)
				button.setSelected(true);
		}
		
		return GUIUtils.addTitle("ACL Verbosity Level", panel);
	}
	
	public SecurityUIPreferenceSetEditor(VerbosityLevel level)
	{
		super(new GridBagLayout());
		
		_level = level;
		
		add(createVerbosityLevelPanel(level), new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
	}
	
	VerbosityLevel aclVerbosityLevel()
	{
		return _level;
	}
	
	private class LevelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private VerbosityLevel _level;
		
		private LevelAction(VerbosityLevel level)
		{
			super(level.toString());
			
			_level = level;
		}
		
		@Override
		public void actionPerformed(ActionEvent event)
		{
			JRadioButton button = (JRadioButton)event.getSource();
			if (button.isSelected())
			{
				System.err.format("Setting level to \"%s\".\n", _level);
				SecurityUIPreferenceSetEditor.this._level = _level;
			}
		}
	}
}