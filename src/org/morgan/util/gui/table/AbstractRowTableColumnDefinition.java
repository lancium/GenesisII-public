package org.morgan.util.gui.table;

import javax.swing.table.TableColumn;

public abstract class AbstractRowTableColumnDefinition<RowType, ColumnType>
	implements RowTableColumnDefinition<RowType, ColumnType>
{
	private String _columnName;
	private Class<ColumnType> _columnType;
	private int _preferredWidth;
	
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

	@Override
	public void modify(RowType row, ColumnType column)
	{
		// Do nothing
	}

	@Override
	final public String toString()
	{
		return _columnName;
	}
}