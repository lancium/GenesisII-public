package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import java.util.Calendar;

import javax.swing.table.TableColumn;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

class NextUpdateColumn
	extends AbstractRowTableColumnDefinition<QueueResourceInformation, Calendar>
{
	NextUpdateColumn()
	{
		super("Next Update", Calendar.class, 128);
	}
	
	@Override
	final public Calendar extract(QueueResourceInformation row)
	{
		return row.resourceInformation().nextUpdate();
	}

	@Override
	final public void prepareTableColumn(TableColumn column)
	{
		super.prepareTableColumn(column);
		
		column.setCellRenderer(new CalendarCellRenderer());
	}
}