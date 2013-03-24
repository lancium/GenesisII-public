package edu.virginia.vcgr.genii.client.cmd.tools.queue.gui;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.identity.Identity;

public class QueueTableModel extends AbstractTableModel
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(QueueTableModel.class);

	private class ResetListRunnable implements Runnable
	{
		private List<JobInformation> _jobInfo;

		private ResetListRunnable(List<JobInformation> jobInfo)
		{
			_jobInfo = jobInfo;
		}

		@Override
		public void run()
		{
			resetList(_jobInfo);
		}
	}

	private class UpdateWorker implements Runnable
	{
		@Override
		public void run()
		{
			List<JobInformation> info;

			try {
				QueueManipulator manip = new QueueManipulator(_queue);
				Iterator<JobInformation> jobInfo = manip.status(null);
				info = new ArrayList<JobInformation>();
				while (jobInfo.hasNext()) {
					info.add(jobInfo.next());
				}

				resetList(info);
			} catch (Throwable cause) {
				_logger.warn("Unable to update queue contents.", cause);
				JOptionPane.showMessageDialog(null, "Unable to update queue contents.", "Queue Update Failed",
					JOptionPane.ERROR_MESSAGE);
			} finally {
				_amRefreshing = false;
				fireRefreshState(false);
			}
		}
	}

	private Collection<RefreshListener> _listeners = new ArrayList<RefreshListener>();
	private boolean _amRefreshing = false;
	private EndpointReferenceType _queue;
	private List<JobInformation> _lastStat;

	protected void fireRefreshState(boolean isStarted)
	{
		Collection<RefreshListener> listeners;

		synchronized (_listeners) {
			listeners = new ArrayList<RefreshListener>(_listeners);
		}

		for (RefreshListener listener : listeners) {
			if (isStarted)
				listener.refreshStarted();
			else
				listener.refreshEnded();
		}
	}

	public void addRefreshListener(RefreshListener listener)
	{
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	public void refresh()
	{
		synchronized (_queue) {
			if (_amRefreshing)
				return;

			_amRefreshing = true;
			fireRefreshState(true);
		}

		Thread th = new Thread(new UpdateWorker());
		th.setName("Queue Manager Updater");
		th.setDaemon(true);
		th.start();
	}

	private void resetList(List<JobInformation> newList)
	{
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(new ResetListRunnable(newList));
		else {
			_lastStat = newList;
			fireTableDataChanged();
		}
	}

	public QueueTableModel(EndpointReferenceType queue) throws RemoteException
	{
		_queue = queue;
		_lastStat = new ArrayList<JobInformation>();

		refresh();
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
		switch (columnIndex) {
			case 0:
				return info.getTicket().toString();

			case 1:
				TimeZone tz = TimeZone.getDefault();
				Calendar submitTime = info.getSubmitTime();
				submitTime.setTimeZone(tz);
				return submitTime;

			case 2:
				StringBuilder builder = new StringBuilder();
				boolean first = true;
				for (Identity id : info.getOwners()) {
					if (!first)
						builder.append("\n");
					builder.append(id.describe(VerbosityLevel.LOW));
				}

				return builder.toString();

			case 3:
				return info.getFailedAttempts();

			default:
				String stateString = info.getScheduledOn();
				if (stateString != null)
					stateString = String.format("On %s", stateString);
				else
					stateString = String.format("%s", info.getJobState());
				return stateString;
		}
	}
}