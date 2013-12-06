package edu.virginia.vcgr.genii.ui.plugins.logs.panels.entry;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JComponent;
import org.morgan.util.gui.table.RowTableColumnDefinition;
import org.morgan.util.gui.table.RowTableModel;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.logging.DLogDatabase;
import edu.virginia.vcgr.genii.client.logging.DLogUtils;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.LogEntryType;

import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.logs.tree.LogPath;
import edu.virginia.vcgr.genii.ui.plugins.logs.tree.LogPathDoesNotExistException;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

public class LogManagerEntryTableModel extends RowTableModel<LogEntryType>
{
	static final long serialVersionUID = 0L;

	static private RowTableColumnDefinition<?, ?>[] COLUMNS = { new RPCIDColumn(), new LoggerColumn(),
		new TimestampColumn(), new LevelColumn(), new MessageColumn() };

	private class LocalLogEntryListFetcherTask extends AbstractTask<Collection<LogEntryType>>
	{
		private LogPath _selection;

		public LocalLogEntryListFetcherTask(LogPath selection) {
			_selection = selection;
		}

		@Override
		final public Collection<LogEntryType> execute(TaskProgressListener progressListener) throws Exception
		{
			String id = null;
			if (_selection != null) {
				id = _selection.getID();
			}
			
			Collection<LogEntryType> jobInfo = new LinkedList<LogEntryType>();
			for (LogEntryType i : _localLogger.selectLogs(id)) { 
				jobInfo.add(i);
			}
			return jobInfo; 
		}
	}

	private class RemoteLogEntryListFetcherTask extends AbstractTask<Collection<LogEntryType>>
	{
		private LogPath _selection;

		public RemoteLogEntryListFetcherTask(LogPath selection) {
			_selection = selection;
		}

		@Override
		final public Collection<LogEntryType> execute(TaskProgressListener progressListener) throws Exception
		{
			if (_selection == null) {
				throw new LogPathDoesNotExistException(
						"Can't retrieve remote logs without a path");
			}
			
			String id = _selection.getID();
			EndpointReferenceType epr = _selection.getEndpoint();
			GeniiCommon logger = DLogUtils.getLogger(epr);
			
			Collection<LogEntryType> jobInfo = new LinkedList<LogEntryType>();
		 	if (logger != null) {
		 		for (LogEntryType i : logger.getAllLogs(new String[]{id}).getLogEntries()) {
					jobInfo.add(i);
				}
		 	}
		 	return jobInfo; 
		}
	}

	private class LogEntryListCompletionListener implements TaskCompletionListener<Collection<LogEntryType>>
	{
		private Component _parentComponent;

		private LogEntryListCompletionListener(Component parentComponent)
		{
			_parentComponent = parentComponent;
		}

		@Override
		public void taskCancelled(Task<Collection<LogEntryType>> task)
		{
			// Don't need to do anything.
		}

		@Override
		public void taskCompleted(Task<Collection<LogEntryType>> task, Collection<LogEntryType> result)
		{
			_contents.clear();
			_contents.addAll(result);

			fireTableDataChanged();
		}

		@Override
		public void taskExcepted(Task<Collection<LogEntryType>> task, Throwable cause)
		{
			ErrorHandler.handleError(_uiContext.uiContext(), (JComponent) _parentComponent, cause);
		}
	}

	private UIPluginContext _uiContext;
	private DLogDatabase _localLogger = null;
	private ArrayList<LogEntryType> _contents = new ArrayList<LogEntryType>();

	void refresh(Component parentComponent, LogPath selection)
	{
		AbstractTask<Collection<LogEntryType>> task = null;
		try {
			if (selection != null && selection.getEndpoint() != null) {
				task = new RemoteLogEntryListFetcherTask(selection);
			}
		} catch (Exception e) {
			// Just means we can't get the epr for a logger
		}
		if (task == null) {
			task = new LocalLogEntryListFetcherTask(selection);
		}
			
		_uiContext
			.uiContext()
				.progressMonitorFactory()
				.createMonitor(parentComponent, "Loading Log Entries", "Fetching log entry list from database", 1000L,
						task, new LogEntryListCompletionListener(parentComponent)).start();
	}

	LogManagerEntryTableModel(UIPluginContext uiContext) 
	{
		_localLogger = DLogUtils.getDBConnector();
		
		_uiContext = uiContext;

	}

	@SuppressWarnings("unchecked")
	@Override
	final protected RowTableColumnDefinition<LogEntryType, ?>[] columnDefinitions()
	{
		return (RowTableColumnDefinition<LogEntryType, ?>[]) COLUMNS;
	}

	@Override
	final protected LogEntryType row(int rowNumber)
	{
		return _contents.get(rowNumber);
	}

	@Override
	final public int getRowCount()
	{
		return _contents.size();
	}
}