package edu.virginia.vcgr.genii.client.dialog.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.virginia.vcgr.genii.client.dialog.ComboBoxDialog;
import edu.virginia.vcgr.genii.client.dialog.MenuItem;

@SuppressWarnings("rawtypes")
public class GuiSingleSelectionListDialog extends AbstractGuiDialog
	implements ComboBoxDialog
{
	static final long serialVersionUID = 0L;
	
	private JList _list;
	
	public GuiSingleSelectionListDialog(String title, String prompt,
		MenuItem defaultItem, MenuItem...items)
	{
		super(title, prompt, defaultItem, items);
		
		if (_list.isSelectionEmpty())
			_okAction.setEnabled(false);
		else
			_okAction.setEnabled(true);
	}
	
	@SuppressWarnings("unchecked")
    @Override
	protected JComponent createBody(Object[] parameters)
	{
		String prompt = (String)parameters[0];
		MenuItem defaultItem = (MenuItem)parameters[1];
		MenuItem []items = (MenuItem[])parameters[2];
		
		_list = new JList(items);
		
		_list.addListSelectionListener(new SelectionListener());
		_list.addMouseListener(new MouseClickListener());
		
		JScrollPane scroller = new JScrollPane(_list);
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(scroller, new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
		panel.add(new JLabel(prompt), new GridBagConstraints(
			0, 1, 1, 1, 1.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		
		if (defaultItem != null)
			_list.setSelectedValue(defaultItem, true);
		
		return panel;
	}
	
	private class SelectionListener implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			if (_okAction != null)
				_okAction.setEnabled(!_list.isSelectionEmpty());
		}
	}
	
	private class MouseClickListener extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2) 
			{
				if (!_list.isSelectionEmpty())
				{
					_cancelled = false;
					okCalled();
					dispose();
				}
			}
		}
	}

	@Override
	public MenuItem getSelectedItem()
	{
		return (MenuItem)_list.getSelectedValue();
	}
}