package edu.virginia.vcgr.genii.ui.plugins.queue.jobs;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.queue.JobTicket;

class JobTicketColumn 
	extends AbstractRowTableColumnDefinition<JobInformation, JobTicket>
{
	JobTicketColumn()
	{
		super("Job Ticket", JobTicket.class, 300);
	}

	@Override
	final public JobTicket extract(JobInformation row)
	{
		return row.getTicket();
	}
}