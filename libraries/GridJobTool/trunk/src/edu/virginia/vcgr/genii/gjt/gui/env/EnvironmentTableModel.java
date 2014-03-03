package edu.virginia.vcgr.genii.gjt.gui.env;

import javax.swing.table.AbstractTableModel;

import edu.virginia.vcgr.genii.gjt.data.EnvironmentList;
import edu.virginia.vcgr.genii.gjt.data.StringStringFilesystemTriple;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;

class EnvironmentTableModel extends AbstractTableModel
{
	static final long serialVersionUID = 0L;

	private EnvironmentList _list;

	EnvironmentTableModel(EnvironmentList list)
	{
		_list = list;
	}

	@Override
	public int getColumnCount()
	{
		return 3;
	}

	@Override
	public int getRowCount()
	{
		return _list.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		StringStringFilesystemTriple triple = _list.get(rowIndex);

		switch (columnIndex) {
			case 0:
				return triple.getKey();
			case 1:
				return triple.getValue();
			case 2:
				return triple.getFilesystemType();
		}

		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		StringStringFilesystemTriple triple = _list.get(rowIndex);

		switch (columnIndex) {
			case 0:
				triple.setKey((String) aValue, _list.getParameterizableBroker(), _list.getModificationBroker());
				break;

			case 1:
				triple.setValue((String) aValue, _list.getParameterizableBroker(), _list.getModificationBroker());
				break;

			case 2:
				FilesystemType filesystemType = (FilesystemType) aValue;
				triple.setFilesystemType(filesystemType);
				break;
		}

		fireTableCellUpdated(rowIndex, columnIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return true;
	}

	public void addRow(String key, String value)
	{
		_list.add(new StringStringFilesystemTriple(key, value));
		fireTableRowsInserted(_list.size() - 1, _list.size() - 1);
	}

	public void removeRow(int row)
	{
		_list.remove(row);
		fireTableRowsDeleted(row, row);
	}
}