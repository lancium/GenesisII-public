package edu.virginia.vcgr.genii.ui.plugins.queue.jobs;

import java.awt.Component;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.morgan.util.io.StreamUtils;
import org.morgan.utils.gui.GUIUtils;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.iterator.WSIterable;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.common.HistoryEventBundleType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsRequestType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType;
import edu.virginia.vcgr.genii.queue.QueuePortType;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.queue.history.JobHistoryFrame;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;

class QueueManipulation
{
	static private abstract class TypicalTask<Type> extends AbstractTask<Type>
	{
		private UIPluginContext _context;
		
		protected Collection<String> _jobTickets;
		
		protected TypicalTask(UIPluginContext context,
			Collection<String> jobTickets)
		{
			_context = context;
			_jobTickets = jobTickets;
		}
		
		final protected QueuePortType queue()
			throws ResourceException, GenesisIISecurityException, 
				RNSPathDoesNotExistException
		{
			return ClientUtils.createProxy(QueuePortType.class,
				_context.endpointRetriever().getTargetEndpoints().iterator().next().getEndpoint(),
				_context.uiContext().callingContext());
		}
	}
	
	static private class JobKillerTask extends TypicalTask<Integer>
	{
		private JobKillerTask(UIPluginContext context,
			Collection<String> jobTickets)
		{
			super(context, jobTickets);
		}

		@Override
		final public Integer execute(TaskProgressListener progressListener)
			throws Exception
		{
			QueuePortType queue = queue();
			queue.killJobs(_jobTickets.toArray(new String[_jobTickets.size()]));
			return 0;
		}
	}
	
	static private class JobCompleterTask extends TypicalTask<Integer>
	{
		private JobCompleterTask(UIPluginContext context,
			Collection<String> jobTickets)
		{
			super(context, jobTickets);
		}

		@Override
		final public Integer execute(TaskProgressListener progressListener)
			throws Exception
		{
			QueuePortType queue = queue();
			queue.completeJobs(_jobTickets.toArray(new String[_jobTickets.size()]));
			return 0;
		}
	}
	
	static private class JobHistoryTask extends TypicalTask<Collection<HistoryEvent>>
	{
		private JobHistoryTask(UIPluginContext context,
				Collection<String> jobTickets)
		{
			super(context, jobTickets);
		}

		@Override
		final public Collection<HistoryEvent> execute(
			TaskProgressListener progressListener) throws Exception
		{
			Collection<HistoryEvent> ret = null;
			WSIterable<HistoryEventBundleType> iterable = null;
			
			try
			{
				QueuePortType queue = queue();
				IterateHistoryEventsResponseType resp = queue.iterateHistoryEvents(
					new IterateHistoryEventsRequestType(
						_jobTickets.iterator().next()));
				iterable = new WSIterable<HistoryEventBundleType>(
					HistoryEventBundleType.class, resp.getResult(), 25, true);
				if (wasCancelled())
					return null;
				progressListener.updateSubTitle("Iterating through events.");
				ret = new LinkedList<HistoryEvent>();
				for (HistoryEventBundleType bundle : iterable)
				{
					ret.add((HistoryEvent)DBSerializer.deserialize(
						bundle.getData()));
					if (wasCancelled())
						return null;
				}
				
				return ret;
			}
			finally
			{
				StreamUtils.close(iterable);
			}
		}
	}
	
	static private abstract class TypicalTaskCompletionListener<Type>
		implements TaskCompletionListener<Type>
	{
		protected UIPluginContext _context;
		protected Component _ownerComponent;
		protected QueueManagerTableModel _model;
		
		protected TypicalTaskCompletionListener(
			Component ownerComponent, UIPluginContext context,
			QueueManagerTableModel model)
		{
			_ownerComponent = ownerComponent;
			_context = context;
			_model = model;
		}
		
		final public void taskExcepted(Task<Type> task, Throwable cause)
		{
			ErrorHandler.handleError(_context.uiContext(),
				(JComponent)_ownerComponent, cause);
		}
	}
	
	static private class JobKillerCompletionListener
		extends TypicalTaskCompletionListener<Integer>
	{
		private JobKillerCompletionListener(Component ownerComponent,
			UIPluginContext context, QueueManagerTableModel model)
		{
			super(ownerComponent, context, model);
		}

		@Override
		public void taskCancelled(Task<Integer> task)
		{
			JOptionPane.showMessageDialog(_ownerComponent,
				"This task runs asynchronously and results from executing it may not appear immediately.",
				"Delayed Results Warning", JOptionPane.WARNING_MESSAGE);
			_model.refresh(_ownerComponent);
		}

		@Override
		public void taskCompleted(Task<Integer> task, Integer result)
		{
			JOptionPane.showMessageDialog(_ownerComponent,
				"This task runs asynchronously and results from executing it may not appear immediately.",
				"Delayed Results Warning", JOptionPane.WARNING_MESSAGE);
			_model.refresh(_ownerComponent);
		}
	}
	
	static private class JobCompleterCompletionListener
		extends TypicalTaskCompletionListener<Integer>
	{
		private JobCompleterCompletionListener(Component ownerComponent,
			UIPluginContext context, QueueManagerTableModel model)
		{
			super(ownerComponent, context, model);
		}
	
		@Override
		public void taskCancelled(Task<Integer> task)
		{
			_model.refresh(_ownerComponent);
		}
	
		@Override
		public void taskCompleted(Task<Integer> task, Integer result)
		{
			_model.refresh(_ownerComponent);
		}
	}
	
	static private class JobHistoryCompletionListener
		extends TypicalTaskCompletionListener<Collection<HistoryEvent>>
	{
		private String _ticket;
		
		private JobHistoryCompletionListener(Component ownerComponent,
			UIPluginContext context, String ticket, 
			QueueManagerTableModel model)
		{
			super(ownerComponent, context, model);
			
			_ticket = ticket;
		}

		@Override
		public void taskCancelled(Task<Collection<HistoryEvent>> task)
		{
			// Don't really need to do anything.
		}

		@Override
		public void taskCompleted(Task<Collection<HistoryEvent>> task,
			Collection<HistoryEvent> result)
		{
			JobHistoryFrame frame = new JobHistoryFrame(
				_context.uiContext(),
				_context.endpointRetriever().getTargetEndpoints().iterator().next(),
				_ticket, result);
			frame.pack();
			GUIUtils.centerWindow(frame);
			frame.setVisible(true);
		}
	}
	
	static void killJobs(UIPluginContext context, Component ownerComponent,
		QueueManagerTableModel model, Collection<String> jobTickets)
	{
		context.uiContext().progressMonitorFactory().createMonitor(
			ownerComponent, "Killing Jobs", "Asking queue to terminate jobs",
			1000L, new JobKillerTask(context, jobTickets),
			new JobKillerCompletionListener(ownerComponent, context, model)).start();
	}
	
	static void completeJobs(UIPluginContext context, Component ownerComponent,
		QueueManagerTableModel model, Collection<String> jobTickets)
	{
		context.uiContext().progressMonitorFactory().createMonitor(
			ownerComponent, "Removing Jobs", "Asking queue to remove jobs",
			1000L, new JobCompleterTask(context, jobTickets),
			new JobCompleterCompletionListener(ownerComponent, context, model)).start();
	}
	
	static void jobHistory(UIPluginContext context, Component ownerComponent,
		QueueManagerTableModel model, Collection<String> jobTickets)
	{
		context.uiContext().progressMonitorFactory().createMonitor(
			ownerComponent, "Getting Job History", "Getting job history events",
			1000L, new JobHistoryTask(context, jobTickets),
			new JobHistoryCompletionListener(ownerComponent, context, 
				jobTickets.iterator().next(), model)).start();
	}
}