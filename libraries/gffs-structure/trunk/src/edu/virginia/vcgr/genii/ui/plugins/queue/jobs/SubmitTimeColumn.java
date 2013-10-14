package edu.virginia.vcgr.genii.ui.plugins.queue.jobs;

import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;

import edu.virginia.vcgr.genii.client.queue.JobInformation;

class SubmitTimeColumn extends AbstractRowTableColumnDefinition<JobInformation, Calendar>
{
	static private class StateRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 0L;

		@Override
		protected void setValue(Object value)
		{
			Calendar displayable = (Calendar) ((Calendar) value).clone();
			displayable.setTimeZone(TimeZone.getDefault());
			super.setValue(String.format("%tc", displayable));
		}
	}

	@Override
	public void prepareTableColumn(TableColumn column)
	{
		super.prepareTableColumn(column);

		column.setCellRenderer(new StateRenderer());
	}

	SubmitTimeColumn()
	{
		super("Submit Time", Calendar.class, 250);
	}

	@Override
	final public Calendar extract(JobInformation row)
	{
		return row.getSubmitTime();
	}
}