package edu.virginia.vcgr.genii.ui.plugins.logs.panels.meta;

import java.awt.Component;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JComponent;

import org.morgan.util.gui.table.RowTableColumnDefinition;
import org.morgan.util.gui.table.RowTableModel;
import edu.virginia.vcgr.genii.client.logging.DLogDatabase;
import edu.virginia.vcgr.genii.client.logging.DLogUtils;

import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.common.LogEntryType;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

public class LogManagerMetaTableModel extends RowTableModel<LogEntryType> {
	static final long serialVersionUID = 0L;

	static private RowTableColumnDefinition<?, ?>[] COLUMNS = {};// new
																	// RPCIDColumn(),
																	// new
																	// LoggerColumn(),

	// new TimestampColumn(), new LevelColumn(), new MessageColumn() };

	private class LogEntryListFetcherTask extends
			AbstractTask<Collection<LogEntryType>> {
		@Override
		final public Collection<LogEntryType> execute(
				TaskProgressListener progressListener) throws Exception {
			Collection<LogEntryType> jobInfo = new LinkedList<LogEntryType>();
			for (LogEntryType i : _localLogger.selectLogs(null)) {
				jobInfo.add(i);
			}
			// if (_logger != null) {
			// for (LogEntryType i : _logger.getAllLogs(null).getLogEntries()) {
			// jobInfo.add(i);
			// }
			// }
			return jobInfo;
		}
	}

	private class LogEntryListCompletionListener implements
			TaskCompletionListener<Collection<LogEntryType>> {
		private Component _parentComponent;

		private LogEntryListCompletionListener(Component parentComponent) {
			_parentComponent = parentComponent;
		}

		@Override
		public void taskCancelled(Task<Collection<LogEntryType>> task) {
			// Don't need to do anything.
		}

		@Override
		public void taskCompleted(Task<Collection<LogEntryType>> task,
				Collection<LogEntryType> result) {
			_contents.clear();
			_contents.addAll(result);

			fireTableDataChanged();
		}

		@Override
		public void taskExcepted(Task<Collection<LogEntryType>> task,
				Throwable cause) {
			ErrorHandler.handleError(_uiContext.uiContext(),
					(JComponent) _parentComponent, cause);
		}
	}

	private UIPluginContext _uiContext;
	private DLogDatabase _localLogger = null;
	private ArrayList<LogEntryType> _contents = new ArrayList<LogEntryType>();

	void refresh(Component parentComponent) {
		_uiContext
				.uiContext()
				.progressMonitorFactory()
				.createMonitor(parentComponent, "Loading Log Entries",
						"Fetching log entry list from database", 1000L,
						new LogEntryListFetcherTask(),
						new LogEntryListCompletionListener(parentComponent))
				.start();
	}

	LogManagerMetaTableModel(UIPluginContext uiContext)
			throws RNSPathDoesNotExistException, RemoteException {
		_localLogger = DLogUtils.getDBConnector();

		_uiContext = uiContext;
	}

	@SuppressWarnings("unchecked")
	@Override
	final protected RowTableColumnDefinition<LogEntryType, ?>[] columnDefinitions() {
		return (RowTableColumnDefinition<LogEntryType, ?>[]) COLUMNS;
	}

	@Override
	final protected LogEntryType row(int rowNumber) {
		return _contents.get(rowNumber);
	}

	@Override
	final public int getRowCount() {
		return _contents.size();
	}
}