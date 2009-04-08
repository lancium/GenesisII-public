package edu.virginia.vcgr.genii.client.cmd.tools.queue.gui;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.swing.table.AbstractTableModel;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.client.security.VerbosityLevel;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;

public class QueueTableModel extends AbstractTableModel
{
	static final long serialVersionUID = 0L;
	
	private EndpointReferenceType _queue;
	private List<JobInformation> _lastStat;
	
	private void resetList() throws RemoteException
	{
		QueueManipulator manip = new QueueManipulator(_queue);
		Iterator<JobInformation> jobInfo = manip.status(null);
		_lastStat.clear();
		while (jobInfo.hasNext())
		{
			_lastStat.add(jobInfo.next());
		}
		
		fireTableDataChanged();
	}
	
	public QueueTableModel(EndpointReferenceType queue) throws RemoteException
	{
		_queue = queue;
		_lastStat = new ArrayList<JobInformation>();
		
		resetList();
	}
	
	@Override
	public int getColumnCount()
	{
		return 5;
	}

	@Override
	public int getRowCount()
	{
		return _lastStat.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		JobInformation info = _lastStat.get(rowIndex);
		switch (columnIndex)
		{
			case 0 :
				return info.getTicket().toString();
				
			case 1 :
				TimeZone tz = TimeZone.getDefault();
				Calendar submitTime = info.getSubmitTime();
				submitTime.setTimeZone(tz);
				
				return String.format("%1$tH:%1$tM %1$tZ %1$td %1$tb %1$tY",
					submitTime);
				
			case 2 :
				StringBuilder builder = new StringBuilder();
				boolean first = true;
				for (Identity id : info.getOwners())
				{
					if (!first)
						builder.append("\n");
					builder.append(id.describe(VerbosityLevel.LOW));
				}
				
				return builder.toString();
				
			case 3 :
				return info.getFailedAttempts();
				
			default :
				String stateString = info.getScheduledOn();
				if (stateString != null)
					stateString = String.format("On %s", stateString);
				else
					stateString = String.format("%s", info.getJobState());
				 return stateString;
		}
	}	
}