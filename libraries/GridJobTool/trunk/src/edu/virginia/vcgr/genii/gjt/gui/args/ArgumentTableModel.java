package edu.virginia.vcgr.genii.gjt.gui.args;

import javax.swing.table.AbstractTableModel;

import edu.virginia.vcgr.genii.gjt.data.FilesystemAssociatedStringList;
import edu.virginia.vcgr.genii.gjt.data.StringFilesystemPair;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;

class ArgumentTableModel extends AbstractTableModel {
	static final long serialVersionUID = 0L;

	private FilesystemAssociatedStringList _items;

	public ArgumentTableModel(FilesystemAssociatedStringList items) {
		_items = items;
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return _items.size();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex > 0;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		StringFilesystemPair item = _items.get(rowIndex);

		switch (columnIndex) {
		case 0:
			return String.format("Argument %d", rowIndex + 1);

		case 1:
			return item.get();

		case 2:
			return item.getFilesystemType();
		}

		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		StringFilesystemPair item = _items.get(rowIndex);

		switch (columnIndex) {
		case 1:
			item.set((String) aValue, _items.getParameterizableBroker(),
					_items.getModificationBroker());
			break;

		case 2:
			FilesystemType filesystemType = (FilesystemType) aValue;
			item.setFilesystemType(filesystemType);
			break;
		}

		fireTableCellUpdated(rowIndex, columnIndex);
	}

	public void addRow(String value) {
		_items.add(new StringFilesystemPair(value));
		fireTableRowsInserted(_items.size() - 1, _items.size() - 1);
	}

	public void removeRow(int row) {
		_items.remove(row);
		fireTableRowsDeleted(row, row);
		fireTableRowsUpdated(row, _items.size());
	}

	public void moveUp(int row) {
		_items.moveUp(row);
		fireTableRowsUpdated(row - 1, _items.size());
	}

	public void moveDown(int row) {
		_items.moveDown(row);
		fireTableRowsUpdated(row, _items.size());
	}
}