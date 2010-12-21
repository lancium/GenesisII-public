package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

class ProcessorArchitectureTypeColumn	
	extends AbstractRowTableColumnDefinition<QueueResourceInformation, String>
{
	ProcessorArchitectureTypeColumn()
	{
		super("Arch", String.class, 64);
	}
	
	@Override
	final public String extract(QueueResourceInformation row)
	{
		return row.resourceInformation().processorArchitecture().toString();
	}
}