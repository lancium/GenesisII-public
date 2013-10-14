package edu.virginia.vcgr.genii.ui.plugins.queue.jobs;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

import edu.virginia.vcgr.genii.client.queue.JobInformation;

public class JobNameColumn extends AbstractRowTableColumnDefinition<JobInformation, String>
{
	static final private String COLUMN_NAME = "Job Name";
	static final private Class<String> COLUMN_CLASS = String.class;
	static final private int PREFERRED_WIDTH = 128;

	public JobNameColumn()
	{
		super(COLUMN_NAME, COLUMN_CLASS, PREFERRED_WIDTH);
	}

	@Override
	public String extract(JobInformation row)
	{
		String jobName = row.jobName();
		return (jobName == null) ? "" : jobName;
	}
}