package edu.virginia.vcgr.genii.ui.prefs.xml;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.morgan.utils.gui.GUIUtils;

class XMLUIPreferenceSetEditor extends JPanel
{
	static final long serialVersionUID = 0L;

	private JRadioButton _text;

	XMLUIPreferenceSetEditor(boolean preferText)
	{
		super(new GridBagLayout());

		JPanel preferTextPanel = new JPanel(new GridBagLayout());
		ButtonGroup group = new ButtonGroup();
		JRadioButton text = new JRadioButton("Display XML as Text", preferText);
		_text = text;
		JRadioButton tree = new JRadioButton("Display XML as a Tree", !preferText);
		group.add(text);
		group.add(tree);
		preferTextPanel.add(text, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		preferTextPanel.add(tree, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		add(GUIUtils.addTitle("XML Display", preferTextPanel), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
	}

	final public boolean preferText()
	{
		return _text.isSelected();
	}
}