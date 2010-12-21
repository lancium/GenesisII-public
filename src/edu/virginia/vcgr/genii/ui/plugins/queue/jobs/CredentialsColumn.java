package edu.virginia.vcgr.genii.ui.plugins.queue.jobs;

import javax.swing.table.TableColumn;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

import edu.virginia.vcgr.genii.client.queue.JobInformation;

class CredentialsColumn 
	extends AbstractRowTableColumnDefinition<JobInformation, CredentialBundle>
{
	
	CredentialsColumn()
	{
		super("Credentials", CredentialBundle.class, 250);
	}

	@Override
	final public void prepareTableColumn(TableColumn column)
	{
		super.prepareTableColumn(column);
		
		column.setCellRenderer(new CredentialsCellRenderer());
	}

	@Override
	final public CredentialBundle extract(JobInformation row)
	{
		return new CredentialBundle(row.getOwners());
	}
}