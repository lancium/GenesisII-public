package edu.virginia.vcgr.genii.client.dialog.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.virginia.vcgr.genii.client.dialog.ComboBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.MenuItem;

@SuppressWarnings("rawtypes")
public class GuiComboBoxDialog extends AbstractGuiDialog implements ComboBoxDialog
{
	static final long serialVersionUID = 0L;

	private JLabel _label;
	private JComboBox _combo;

	public GuiComboBoxDialog(String title, String prompt, MenuItem defaultItem, MenuItem... items)
	{
		super(title, prompt, defaultItem, items);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JComponent createBody(Object[] parameters)
	{
		_label = new JLabel((String) parameters[0]);
		_combo = new JComboBox((MenuItem[]) parameters[2]);
		if (parameters[1] != null)
			_combo.setSelectedItem(parameters[1]);

		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(_label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		panel.add(_combo, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));

		return panel;
	}

	@Override
	public MenuItem getSelectedItem()
	{
		return (MenuItem) _combo.getSelectedItem();
	}
}