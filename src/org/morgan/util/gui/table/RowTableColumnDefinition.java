package org.morgan.util.gui.table;

import javax.swing.table.TableColumn;

public interface RowTableColumnDefinition<RowType, ColumnType>
{
	public Class<ColumnType> columnType();
	public ColumnType extract(RowType row);
	public boolean canModify();
	public void modify(RowType row, Object newValue);
	public void prepareTableColumn(TableColumn column);
}