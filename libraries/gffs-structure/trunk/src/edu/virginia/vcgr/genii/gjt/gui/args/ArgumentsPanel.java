package edu.virginia.vcgr.genii.gjt.gui.args;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.virginia.vcgr.genii.gjt.data.FilesystemAssociatedStringList;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.gui.icons.ShapeIcons;
import edu.virginia.vcgr.genii.gjt.gui.util.ButtonPanel;
import edu.virginia.vcgr.genii.gjt.gui.util.SimpleIconButton;

public class ArgumentsPanel extends JPanel
{
	static final long serialVersionUID = 0L;

	static private final Dimension SIZE = new Dimension(200, 200);

	private AbstractAction _minusAction = new MinusAction();
	private AbstractAction _upAction = new UpAction();
	private AbstractAction _downAction = new DownAction();

	private ArgumentTable _table;
	private ArgumentTableModel _tableModel;

	public ArgumentsPanel(FilesystemMap filesystemMap, FilesystemAssociatedStringList arguments)
	{
		super(new GridBagLayout());

		setPreferredSize(SIZE);

		ArgumentTable table = new ArgumentTable(filesystemMap, arguments);
		ArgumentTableModel model = (ArgumentTableModel) table.getModel();
		_table = table;
		_tableModel = model;

		add(new JLabel("Arguments"), new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		JScrollPane scroller = new JScrollPane(table);
		add(scroller, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		add(ButtonPanel.createHorizontalPanel(new SimpleIconButton(ShapeIcons.Plus, new PlusAction()), new SimpleIconButton(
			ShapeIcons.Minus, _minusAction), null, new SimpleIconButton(ShapeIcons.UpArrow, _upAction), new SimpleIconButton(
			ShapeIcons.DownArrow, _downAction)), new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		table.getSelectionModel().addListSelectionListener(new ListSelectionListenerImpl());
	}

	private class PlusAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_tableModel.addRow("");
		}
	}

	private class MinusAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private MinusAction()
		{
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int[] indices = _table.getSelectedRows();
			for (int lcv = indices.length - 1; lcv >= 0; lcv--)
				_tableModel.removeRow(indices[lcv]);
		}
	}

	private class UpAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private UpAction()
		{
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int row = _table.getSelectedRow();
			_tableModel.moveUp(row);
			_table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
		}
	}

	private class DownAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private DownAction()
		{
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int row = _table.getSelectedRow();
			_tableModel.moveDown(row);
			_table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
		}
	}

	private class ListSelectionListenerImpl implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			int num = _table.getSelectedRowCount();
			_minusAction.setEnabled(num > 0);
			_upAction.setEnabled(num == 1 && _table.getSelectedRow() > 0);
			_downAction.setEnabled(num == 1 && _table.getSelectedRow() < (_tableModel.getRowCount() - 1));
		}
	}
}