package edu.virginia.vcgr.genii.gjt.gui.resource;

import javax.swing.table.AbstractTableModel;

import edu.virginia.vcgr.genii.gjt.data.MatchingParameterList;
import edu.virginia.vcgr.genii.gjt.data.StringStringPair;

public class MatchingParameterTableModel extends AbstractTableModel {
	static final long serialVersionUID = 0L;

	private MatchingParameterList _list;

	MatchingParameterTableModel(MatchingParameterList list) {
		_list = list;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return _list.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		StringStringPair pair = _list.get(rowIndex);
		String matchType, name;
		name = pair.name();
		int index = name.indexOf(':');
		if (index > 0) {
			matchType = name.substring(0, index);
			name = name.substring(index + 1);
		} else
			matchType = "requires";

		switch (columnIndex) {
		case 0:
			return name;
		case 1:
			return pair.value();
		case 2:
			return matchType;
		}

		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		StringStringPair pair = _list.get(rowIndex);

		switch (columnIndex) {
		case 0:
			pair.name(this.getValueAt(rowIndex, 2) + ":" + (String) aValue,
					_list.getParameterizableBroker(),
					_list.getModificationBroker());
			break;
		case 1:
			pair.value((String) aValue, _list.getParameterizableBroker(),
					_list.getModificationBroker());
			break;
		case 2:
			pair.name((String) aValue + ":" + this.getValueAt(rowIndex, 0),
					_list.getParameterizableBroker(),
					_list.getModificationBroker());
		}

		fireTableCellUpdated(rowIndex, columnIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	public void addRow(String key, String value) {
		_list.add(new StringStringPair(key, value));
		fireTableRowsInserted(_list.size() - 1, _list.size() - 1);
	}

	public void removeRow(int row) {
		_list.remove(row);
		fireTableRowsDeleted(row, row);
	}
}