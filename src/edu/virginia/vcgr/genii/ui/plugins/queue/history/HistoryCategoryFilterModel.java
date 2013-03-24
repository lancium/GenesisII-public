package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.util.EnumSet;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;

class HistoryCategoryFilterModel extends AbstractTableModel
{
	static final long serialVersionUID = 0L;

	private Set<HistoryEventCategory> _selected;

	HistoryCategoryFilterModel(HistoryEventFilter filter)
	{
		_selected = EnumSet.copyOf(filter.categoryFilter());
	}

	final Set<HistoryEventCategory> getSelectionSet()
	{
		return _selected;
	}

	@Override
	final public int getRowCount()
	{
		return HistoryEventCategory.values().length;
	}

	@Override
	final public int getColumnCount()
	{
		return 2;
	}

	@Override
	final public Object getValueAt(int rowIndex, int columnIndex)
	{
		HistoryEventCategory category = HistoryEventCategory.values()[rowIndex];

		if (columnIndex == 0)
			return _selected.contains(category);
		else
			return category;
	}

	@Override
	final public String getColumnName(int column)
	{
		switch (column) {
			case 0:
				return "Selected?";
			default:
				return "Category";
		}
	}

	@Override
	final public Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex) {
			case 0:
				return Boolean.class;
			default:
				return HistoryEventCategory.class;
		}
	}

	@Override
	final public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex == 0;
	}

	@Override
	final public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		HistoryEventCategory category = HistoryEventCategory.values()[rowIndex];

		if (columnIndex == 0) {
			Boolean value = (Boolean) aValue;
			if (value == null || !value.booleanValue())
				_selected.remove(category);
			else
				_selected.add(category);

			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}
}