package edu.virginia.vcgr.genii.ui.plugins.queue.jobs;

import java.awt.Color;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.morgan.util.Pair;
import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.queue.QueueStates;

class JobStateColumn extends AbstractRowTableColumnDefinition<JobInformation, Pair<QueueStates, String>>
{
	static private class StateRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 0L;

		@SuppressWarnings("unchecked")
		@Override
		protected void setValue(Object value)
		{
			Pair<QueueStates, String> pair = (Pair<QueueStates, String>) value;

			String host = pair.second();

			super.setValue(host == null ? pair.first() : host);

			if (pair.first() == QueueStates.ERROR)
				setForeground(Color.RED);
			else if (host != null)
				setForeground(Color.GREEN);
			else
				setForeground(Color.BLUE);
		}
	}

	@Override
	public void prepareTableColumn(TableColumn column)
	{
		super.prepareTableColumn(column);

		column.setCellRenderer(new StateRenderer());
	}

	@SuppressWarnings("unchecked")
	JobStateColumn()
	{
		super("Job State", (Class<Pair<QueueStates, String>>) new Pair<QueueStates, String>(null, null).getClass(), 45);
	}

	@Override
	final public Pair<QueueStates, String> extract(JobInformation row)
	{
		return new Pair<QueueStates, String>(row.getJobState(), row.getScheduledOn());
	}
}