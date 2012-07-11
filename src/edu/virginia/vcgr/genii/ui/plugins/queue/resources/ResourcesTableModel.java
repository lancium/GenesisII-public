package edu.virginia.vcgr.genii.ui.plugins.queue.resources;

import java.awt.Component;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Future;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.axis.types.UnsignedInt;
import org.ggf.rns.RNSEntryResponseType;
import org.morgan.util.gui.table.AbstractRowTableColumnDefinition;
import org.morgan.util.gui.table.RowTableColumnDefinition;
import org.morgan.util.gui.table.RowTableModel;
import org.morgan.util.io.StreamUtils;

import edu.virginia.cs.vcgr.genii.job_management.ConfigureRequestType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSIterable;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.queue.QueuePortType;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

class ResourcesTableModel extends RowTableModel<QueueResourceInformation>
{
	static final long serialVersionUID = 0L;

	private class RefreshCompletionListener 
		implements TaskCompletionListener<Boolean>
	{
		private Component _parentComponent;
		
		private RefreshCompletionListener(Component parentComponent)
		{
			_parentComponent = parentComponent;
		}
		
		@Override
		final public void taskCompleted(Task<Boolean> task, Boolean result)
		{
			refresh(_parentComponent);
		}

		@Override
		final public void taskCancelled(Task<Boolean> task)
		{
			// Nothing to do
		}

		@Override
		final public void taskExcepted(Task<Boolean> task, Throwable cause)
		{
			ErrorHandler.handleError(_uiContext.uiContext(),
				(JComponent)_parentComponent, cause);
		}
	}
	
	private class SlotChangerTask extends AbstractTask<Boolean>
	{
		private QueueResourceInformation _row;
		private int _newSlots;
		
		private SlotChangerTask(QueueResourceInformation row, int newSlots)
		{
			_row = row;
			_newSlots = newSlots;
		}
		
		@Override
		final public Boolean execute(TaskProgressListener progressListener)
				throws Exception
		{
			_queue.configureResource(new ConfigureRequestType(_row.name(),
				new UnsignedInt(_newSlots)));
			return Boolean.TRUE;
		}
	}
	
	private class ResourceSlotsColumn 
		extends AbstractRowTableColumnDefinition<QueueResourceInformation, Integer>
	{
		@Override
		protected void modifyImpl(QueueResourceInformation row, Integer column)
		{
			int answer = JOptionPane.showConfirmDialog(row.parent(), 
				String.format(
				"Change Max Slots for %s to %d?", row.name(), column),
				"Confirm Slot Change", JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.YES_OPTION)
			{
				_uiContext.uiContext().progressMonitorFactory().createMonitor(
					row.parent(), "Modifying Slot Count", String.format(
						"Changing %s's slots to %d", row.name(), column),
						100L, new SlotChangerTask(row, column),
						new RefreshCompletionListener(row.parent())).start();
			}
		}

		ResourceSlotsColumn()
		{
			super("Max Slots", Integer.class, 32);
		}
	
		@Override
		final public Integer extract(QueueResourceInformation row)
		{
			return row.resourceInformation().maxSlots();
		}
	
		@Override
		final public boolean canModify()
		{
			return true;
		}
	}
	
	private RowTableColumnDefinition<?, ?>[] COLUMNS = {
		new ResourceNameColumn(),
		new ResourceTypeColumn(),
		new OperatingSystemTypeColumn(),
		new ProcessorArchitectureTypeColumn(),
		new AcceptingActivitiesColumn(),
		new AvailabilityColumn(),
		new LastUpdatedColumn(),
		new NextUpdateColumn(),
		new ResourceSlotsColumn()
	};
	
	private class QueueResourcesFetcherTask 
		extends AbstractTask<Collection<QueueResourceInformation>>
	{
		@Override
		final public Collection<QueueResourceInformation> execute(
			TaskProgressListener progressListener)
				throws Exception
		{
			Collection<QueueResourceInformation> resourceInfo = 
				new LinkedList<QueueResourceInformation>();
			RNSIterable iterable = new RNSIterable(
				_rnsFuture.get().lookup(null), 
				_uiContext.uiContext().callingContext(), 100);
			for (RNSEntryResponseType entry : iterable)
				resourceInfo.add(new QueueResourceInformation(_uiContext, entry));
				
			StreamUtils.close(iterable.getIterable());
			return resourceInfo;
		}	
	}
	
	private class QueueResourcesCompletionListener
		implements TaskCompletionListener<Collection<QueueResourceInformation>>
	{
		private Component _parentComponent;
		
		private QueueResourcesCompletionListener(Component parentComponent)
		{
			_parentComponent = parentComponent;
		}
		
		@Override
		public void taskCancelled(Task<Collection<QueueResourceInformation>> task)
		{
			// Don't need to do anything.
		}
	
		@Override
		public void taskCompleted(Task<Collection<QueueResourceInformation>> task,
			Collection<QueueResourceInformation> result)
		{
			_contents.clear();
			_contents.addAll(result);
			
			for (QueueResourceInformation info : _contents)
				info.parent((JComponent)_parentComponent);
			
			fireTableDataChanged();
		}
	
		@Override
		public void taskExcepted(Task<Collection<QueueResourceInformation>> task,
				Throwable cause)
		{
			ErrorHandler.handleError(_uiContext.uiContext(),
				(JComponent)_parentComponent, cause);
		}
	}

	private UIPluginContext _uiContext;
	private QueuePortType _queue;
	private Future<EnhancedRNSPortType> _rnsFuture;
	private ArrayList<QueueResourceInformation> _contents = 
		new ArrayList<QueueResourceInformation>();
	
	void forceUpdate(Collection<String> besNames) throws RemoteException
	{
		_queue.forceUpdate(besNames.toArray(new String[besNames.size()]));
	}
	
	void refresh(Component parentComponent)
	{	
		_uiContext.uiContext().progressMonitorFactory().createMonitor(
			parentComponent,
			"Loading Queue Resources", "Fetching resources from queue", 1000L,
			new QueueResourcesFetcherTask(), 
			new QueueResourcesCompletionListener(parentComponent)).start();
	}
	
	ResourcesTableModel(UIPluginContext uiContext)
		throws ResourceException, GenesisIISecurityException, 
			RNSPathDoesNotExistException
	{
		_uiContext = uiContext;
		_rnsFuture = uiContext.uiContext().executor().submit(
			new AsynchronousQueueResourcesExtractor(uiContext));
		_queue = ClientUtils.createProxy(QueuePortType.class,
			_uiContext.endpointRetriever().getTargetEndpoints().iterator().next().getEndpoint(),
			_uiContext.uiContext().callingContext());
	}
	
	@Override
	final public int getRowCount()
	{
		return _contents.size();
	}

	@SuppressWarnings("unchecked")
	@Override
	final protected RowTableColumnDefinition<QueueResourceInformation, ?>[] columnDefinitions()
	{
		return (RowTableColumnDefinition<QueueResourceInformation, ?>[])COLUMNS;
	}

	@Override
	final protected QueueResourceInformation row(int rowNumber)
	{
		return _contents.get(rowNumber);
	}
}
