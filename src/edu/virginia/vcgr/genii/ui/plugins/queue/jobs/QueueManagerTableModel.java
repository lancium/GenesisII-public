package edu.virginia.vcgr.genii.ui.plugins.queue.jobs;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JComponent;

import org.morgan.util.gui.table.RowTableColumnDefinition;
import org.morgan.util.gui.table.RowTableModel;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.iterator.WSIterable;
import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator.JobInformationIterator;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.queue.JobInformationType;
import edu.virginia.vcgr.genii.queue.QueuePortType;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

public class QueueManagerTableModel extends RowTableModel<JobInformation>
{
	static final long serialVersionUID = 0L;
	
	static private RowTableColumnDefinition<?, ?>[] COLUMNS = {
		new JobTicketColumn(),
		new JobNameColumn(),
		new SubmitTimeColumn(),
		new CredentialsColumn(),
		new AttemptNumberColumn(),
		new JobStateColumn()
	};
	
	private class QueueJobListFetcherTask 
		extends AbstractTask<Collection<JobInformation>>
	{
		@Override
		final public Collection<JobInformation> execute(
			TaskProgressListener progressListener)
				throws Exception
		{
			Collection<JobInformation> jobInfo = new LinkedList<JobInformation>();
			
			WSIterable<JobInformationType> iterable = null;
			try
			{
				iterable = new WSIterable<JobInformationType>(
					JobInformationType.class, 
					_queue.iterateStatus(null).getResult(), 200, true);
				JobInformationIterator iter = new JobInformationIterator(
					iterable.iterator());
				while (iter.hasNext())
					jobInfo.add(iter.next());
				
				return jobInfo;
			}
			finally
			{
				StreamUtils.close(iterable);
			}
		}	
	}
	
	private class QueueJobListCompletionListener
		implements TaskCompletionListener<Collection<JobInformation>>
	{
		private Component _parentComponent;
		
		private QueueJobListCompletionListener(Component parentComponent)
		{
			_parentComponent = parentComponent;
		}
		
		@Override
		public void taskCancelled(Task<Collection<JobInformation>> task)
		{
			// Don't need to do anything.
		}

		@Override
		public void taskCompleted(Task<Collection<JobInformation>> task,
			Collection<JobInformation> result)
		{
			_contents.clear();
			_contents.addAll(result);
			
			fireTableDataChanged();
		}

		@Override
		public void taskExcepted(Task<Collection<JobInformation>> task,
				Throwable cause)
		{
			ErrorHandler.handleError(_uiContext.uiContext(),
				(JComponent)_parentComponent, cause);
		}
	}
	
	private UIPluginContext _uiContext;
	private QueuePortType _queue;
	private ArrayList<JobInformation> _contents = 
		new ArrayList<JobInformation>();
	
	void refresh(Component parentComponent)
	{
		_uiContext.uiContext().progressMonitorFactory().createMonitor(
			parentComponent,
			"Loading Queue Jobs", "Fetching job list from queue", 1000L,
			new QueueJobListFetcherTask(), 
			new QueueJobListCompletionListener(parentComponent)).start();
	}
	
	QueueManagerTableModel(UIPluginContext uiContext)
		throws ResourceException, GenesisIISecurityException, 
			RNSPathDoesNotExistException
	{
		_uiContext = uiContext;
		_queue = ClientUtils.createProxy(QueuePortType.class,
			uiContext.endpointRetriever().getTargetEndpoints().iterator().next().getEndpoint(),
			uiContext.uiContext().callingContext());
	}

	@SuppressWarnings("unchecked")
	@Override
	final protected RowTableColumnDefinition<JobInformation, ?>[] columnDefinitions()
	{
		return (RowTableColumnDefinition<JobInformation, ?>[])COLUMNS;
	}

	@Override
	final protected JobInformation row(int rowNumber)
	{
		return _contents.get(rowNumber);
	}

	@Override
	final public int getRowCount()
	{
		return _contents.size();
	}
}