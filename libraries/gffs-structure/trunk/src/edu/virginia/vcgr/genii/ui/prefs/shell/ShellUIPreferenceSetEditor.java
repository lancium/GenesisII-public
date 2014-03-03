package edu.virginia.vcgr.genii.ui.prefs.shell;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;

import org.morgan.util.gui.font.DefaultFontModel;
import org.morgan.util.gui.font.FontModel;
import org.morgan.util.gui.font.MFontChooserPanel;

import edu.virginia.vcgr.genii.gjt.gui.util.TitledPanel;
import edu.virginia.vcgr.genii.ui.shell.InputBindingsType;

class ShellUIPreferenceSetEditor extends JPanel
{
	static final long serialVersionUID = 0L;

	private FontModel _fontModel;
	private InputBindingsPanel _inputBindingsPanel;

	static private class ShellFontModel extends DefaultFontModel
	{
		private ShellFontModel(Font selectedFont)
		{
			super(selectedFont);
		}

		@Override
		final public int styleMask()
		{
			return Font.PLAIN;
		}
	}

	ShellUIPreferenceSetEditor(ShellUIPreferenceSet set)
	{
		super(new GridBagLayout());

		_inputBindingsPanel = new InputBindingsPanel(set.inputBindingsType());

		TitledPanel panel = new TitledPanel("Key Bindings");
		panel.add(_inputBindingsPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		add(panel, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL,
			new Insets(5, 5, 5, 5), 5, 5));

		MFontChooserPanel chooser = new MFontChooserPanel(_fontModel = new ShellFontModel(set.shellFont()));
		chooser.setSampleText("Genesis II");
		panel = new TitledPanel("Display Font");
		panel.add(chooser, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
		add(panel, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
			5, 5, 5, 5), 5, 5));
	}

	InputBindingsType selectedBindingsType()
	{
		return _inputBindingsPanel.selectedBindingsType();
	}

	Font selectedFont()
	{
		return _fontModel.selectedFont();
	}
}