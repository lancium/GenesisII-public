package edu.virginia.vcgr.genii.ui.plugins.queue.jobs;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

import edu.virginia.vcgr.genii.client.queue.JobInformation;

class AttemptNumberColumn 
	extends AbstractRowTableColumnDefinition<JobInformation, Integer>
{
	AttemptNumberColumn()
	{
		super("Attempts", Integer.class, 30);
	}

	@Override
	final public Integer extract(JobInformation row)
	{
		return row.getFailedAttempts();
	}
}