package org.morgan.util.gui.table;

import javax.swing.table.TableColumn;

public abstract class AbstractRowTableColumnDefinition<RowType, ColumnType>
	implements RowTableColumnDefinition<RowType, ColumnType>
{
	private String _columnName;
	private Class<ColumnType> _columnType;
	private int _preferredWidth;
	
	protected void modifyImpl(RowType row, ColumnType column)
	{
		// Do nothing
	}
	
	protected AbstractRowTableColumnDefinition(String columnName, 
		Class<ColumnType> columnType, int preferredWidth)
	{
		_columnName = columnName;
		_columnType = columnType;
		_preferredWidth = preferredWidth;
	}
	
	
	@Override
	public void prepareTableColumn(TableColumn column)
	{
		column.setHeaderValue(toString());
		column.setPreferredWidth(_preferredWidth);
	}


	@Override
	public boolean canModify()
	{
		return false;
	}

	@Override
	final public Class<ColumnType> columnType()
	{
		return _columnType;
	}

	@SuppressWarnings("unchecked")
	@Override
	final public void modify(RowType row, Object column)
	{
		modifyImpl(row, (ColumnType)column);
	}

	@Override
	final public String toString()
	{
		return _columnName;
	}
}