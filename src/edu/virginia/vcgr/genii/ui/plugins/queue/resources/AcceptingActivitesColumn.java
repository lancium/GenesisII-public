package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import javax.swing.table.TableColumn;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

class AcceptingActivitiesColumn
	extends AbstractRowTableColumnDefinition<QueueResourceInformation, Boolean>
{
	AcceptingActivitiesColumn()
	{
		super("Accepting Jobs?", Boolean.class, 16);
	}
	
	@Override
	public Boolean extract(QueueResourceInformation row)
	{
		return row.resourceInformation().isAcceptingActivities();
	}
	
	@Override
	final public void prepareTableColumn(TableColumn column)
	{
		super.prepareTableColumn(column);
		
		column.setCellRenderer(new BooleanTextCellRenderer());
	}
}