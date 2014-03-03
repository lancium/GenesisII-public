package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

class ResourceNameColumn extends AbstractRowTableColumnDefinition<QueueResourceInformation, String>
{
	ResourceNameColumn()
	{
		super("Resource Name", String.class, 128);
	}

	@Override
	final public String extract(QueueResourceInformation row)
	{
		return row.name();
	}
}