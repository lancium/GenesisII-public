package org.morgan.util.gui.table;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public abstract class RowTableModel<RowType> extends AbstractTableModel
{
	static final long serialVersionUID = 0L;
	
	protected abstract RowTableColumnDefinition<RowType, ?>[] columnDefinitions();
	protected abstract RowType row(int rowNumber);
	
	final public void prepareTableColumns(TableColumnModel cModel)
	{
		RowTableColumnDefinition<RowType, ?> []columns = columnDefinitions();
		
		for (int lcv = 0; lcv < columns.length; lcv++)
		{
			TableColumn tColumn = cModel.getColumn(lcv);
			columns[lcv].prepareTableColumn(tColumn);
		}
	}
	
	@Override
	final public int getColumnCount()
	{
		return columnDefinitions().length;
	}

	@Override
	final public Object getValueAt(int rowIndex, int columnIndex)
	{
		RowType row = row(rowIndex);
		return columnDefinitions()[columnIndex].extract(row);
	}
	
	@Override
	final public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		RowType row = row(rowIndex);
		columnDefinitions()[columnIndex].modify(row, aValue);
	}
	
	
	@Override
	public int getRowCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	final public Class<?> getColumnClass(int columnIndex)
	{
		return columnDefinitions()[columnIndex].columnType();
	}
	
	@Override
	final public String getColumnName(int column)
	{
		return columnDefinitions()[column].toString();
	}
	
	@Override
	final public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnDefinitions()[columnIndex].canModify();
	}
}