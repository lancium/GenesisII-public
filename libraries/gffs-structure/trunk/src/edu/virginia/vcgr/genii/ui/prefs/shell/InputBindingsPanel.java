package edu.virginia.vcgr.genii.ui.prefs.shell;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import edu.virginia.vcgr.genii.ui.shell.InputBindingsType;

class InputBindingsPanel extends JPanel
{
	static final long serialVersionUID = 0L;

	private JRadioButton[] _radios;

	public InputBindingsPanel(InputBindingsType selectedType)
	{
		super(new GridBagLayout());

		ButtonGroup group = new ButtonGroup();

		_radios = new JRadioButton[InputBindingsType.values().length];
		for (InputBindingsType type : InputBindingsType.values()) {
			int index = type.ordinal();

			_radios[index] = new JRadioButton(type.toString());
			_radios[index].setSelected(type == selectedType);
			group.add(_radios[index]);

			add(_radios[index], new GridBagConstraints(0, index, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		}
	}

	public InputBindingsType selectedBindingsType()
	{
		for (int lcv = 0; lcv < _radios.length; lcv++) {
			if (_radios[lcv].isSelected())
				return InputBindingsType.values()[lcv];
		}

		return null;
	}
}