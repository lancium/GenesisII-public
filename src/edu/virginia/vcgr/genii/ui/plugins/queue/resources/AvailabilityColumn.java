package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import javax.swing.table.TableColumn;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

class AvailabilityColumn extends AbstractRowTableColumnDefinition<QueueResourceInformation, Boolean>
{
	AvailabilityColumn()
	{
		super("Status", Boolean.class, 32);
	}

	@Override
	final public Boolean extract(QueueResourceInformation row)
	{
		return row.resourceInformation().available();
	}

	@Override
	final public void prepareTableColumn(TableColumn column)
	{
		super.prepareTableColumn(column);

		column.setCellRenderer(new BooleanTextCellRenderer("Available", "Unavailable"));
	}
}