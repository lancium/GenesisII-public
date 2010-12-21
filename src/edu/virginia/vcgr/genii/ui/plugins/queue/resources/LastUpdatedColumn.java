package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import java.util.Calendar;

import javax.swing.table.TableColumn;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

class LastUpdatedColumn
	extends AbstractRowTableColumnDefinition<QueueResourceInformation, Calendar>
{
	LastUpdatedColumn()
	{
		super("Last Updated", Calendar.class, 128);
	}
	
	@Override
	final public Calendar extract(QueueResourceInformation row)
	{
		return row.resourceInformation().lastUpdated();
	}

	@Override
	final public void prepareTableColumn(TableColumn column)
	{
		super.prepareTableColumn(column);
		
		column.setCellRenderer(new CalendarCellRenderer());
	}
}