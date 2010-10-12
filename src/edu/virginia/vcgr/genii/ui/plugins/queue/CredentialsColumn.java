package edu.virginia.vcgr.genii.ui.plugins.queue;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.security.VerbosityLevel;
import edu.virginia.vcgr.genii.client.security.credentials.identity.Identity;

class CredentialsColumn 
	extends AbstractRowTableColumnDefinition<JobInformation, String>
{
	CredentialsColumn()
	{
		super("Credentials", String.class, 250);
	}

	@Override
	final public String extract(JobInformation row)
	{
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Identity id : row.getOwners())
		{
			if (!first)
				builder.append("\n");
			builder.append(id.describe(VerbosityLevel.LOW));
		}
		
		return builder.toString();
	}
}