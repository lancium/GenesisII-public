package edu.virginia.vcgr.genii.gjt.gui.stage;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import edu.virginia.vcgr.genii.gjt.data.stage.DataStage;

public class DataStageCellEditor extends AbstractCellEditor implements TableCellEditor
{
	static final long serialVersionUID = 0l;

	private DataStage _currentValue = null;
	private JButton _button;

	public DataStageCellEditor()
	{
		_button = new JButton(new EditAction());
		_button.setBorderPainted(false);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		_currentValue = (DataStage) value;
		return _button;
	}

	@Override
	public Object getCellEditorValue()
	{
		return _currentValue;
	}

	private class EditAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		public void actionPerformed(ActionEvent e)
		{
			if (_currentValue.edit(SwingUtilities.getWindowAncestor((JComponent) e.getSource())) != null)
				fireEditingStopped();
			else
				fireEditingCanceled();
		}
	}
}