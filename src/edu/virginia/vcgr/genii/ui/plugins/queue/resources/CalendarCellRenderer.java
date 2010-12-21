package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import java.util.Calendar;
import java.util.TimeZone;

import javax.swing.table.DefaultTableCellRenderer;

class CalendarCellRenderer extends DefaultTableCellRenderer
{
	static final long serialVersionUID = 0L;

	@Override
	final protected void setValue(Object value)
	{
		if (value == null)
			super.setValue("");
		else
		{
			Calendar displayable = (Calendar)((Calendar)value).clone();
			displayable.setTimeZone(TimeZone.getDefault());
			super.setValue(String.format("%tc", displayable));
		}
	}
}