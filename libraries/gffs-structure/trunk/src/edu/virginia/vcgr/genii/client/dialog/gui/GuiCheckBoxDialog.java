package edu.virginia.vcgr.genii.client.dialog.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.virginia.vcgr.genii.client.dialog.CheckBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.CheckBoxItem;
import edu.virginia.vcgr.genii.client.dialog.MenuItem;

public class GuiCheckBoxDialog extends AbstractGuiDialog implements CheckBoxDialog
{
	static final long serialVersionUID = 0L;

	private CheckBoxItem[] _items;
	private JCheckBox[] _boxes;

	public GuiCheckBoxDialog(String title, String prompt, CheckBoxItem... items)
	{
		super(title, prompt, items);
	}

	@Override
	protected JComponent createBody(Object[] parameters)
	{
		String prompt = (String) parameters[0];
		_items = (CheckBoxItem[]) parameters[1];

		_boxes = new JCheckBox[_items.length];
		for (int lcv = 0; lcv < _items.length; lcv++) {
			CheckBoxItem item = _items[lcv];
			_boxes[lcv] = new JCheckBox(item.toString(), item.isChecked());
			_boxes[lcv].setEnabled(item.isEditable());
		}

		JPanel panel = new JPanel(new GridBagLayout());

		for (JCheckBox box : _boxes) {
			panel.add(box, new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		}

		panel.add(new JLabel(prompt), new GridBagConstraints(0, GridBagConstraints.RELATIVE, 1, 1, 1.0, 1.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		return panel;
	}

	@Override
	public Collection<MenuItem> getCheckedItems()
	{
		Collection<MenuItem> items = new LinkedList<MenuItem>();

		for (int lcv = 0; lcv < _boxes.length; lcv++) {
			if (_boxes[lcv].isSelected())
				items.add(_items[lcv]);
		}

		return items;
	}
}