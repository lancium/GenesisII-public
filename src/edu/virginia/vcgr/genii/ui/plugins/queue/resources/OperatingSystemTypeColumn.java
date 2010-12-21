package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

class OperatingSystemTypeColumn
	extends AbstractRowTableColumnDefinition<QueueResourceInformation, String>
{
	OperatingSystemTypeColumn()
	{
		super("OS", String.class, 64);
	}
	
	@Override
	final public String extract(QueueResourceInformation row)
	{
		return row.resourceInformation().operatingSystem().toString();
	}
}