package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

class ResourceTypeColumn extends 
	AbstractRowTableColumnDefinition<QueueResourceInformation, String>
{
	ResourceTypeColumn()
	{
		super("Resource Type", String.class, 32);
	}
	
	@Override
	final public String extract(QueueResourceInformation row)
	{
		return row.resourceInformation().resourceManagerType().toString();
	}
}
