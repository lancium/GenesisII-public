package edu.virginia.vcgr.genii.container.common.notification;

import java.rmi.RemoteException;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextException;
import edu.virginia.vcgr.genii.common.notification.GeniiNotificationConsumerPortType;
import edu.virginia.vcgr.genii.common.notification.Notify;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.context.WorkingContext;

public class NotificationPool implements Runnable
{
	static private Log _logger = LogFactory.getLog(NotificationPool.class);
	
	private LinkedList<NotificationWorker> _workers =
		new LinkedList<NotificationWorker>();
	
	public NotificationPool()
	{
		for (int lcv = 0; 
			lcv < Container.getContainerConfiguration().getNotificationPoolSize();
			lcv++)
		{
			Thread th = new Thread(this);
			th.setDaemon(true);
			th.start();
		}
	}
	
	static private class NotificationWorker
	{
		private WorkingContext _workingContext;
		private EndpointReferenceType _target;
		private Notify _notifyMessage;
		
		public NotificationWorker(
			EndpointReferenceType target, Notify notifyMessage)
			throws ContextException
		{
			_target = target;
			_notifyMessage = notifyMessage;
			
			_workingContext = 
				(WorkingContext)WorkingContext.getCurrentWorkingContext().clone();
		}
		
		public void doNotify()
			throws RemoteException
		{
			try
			{
				WorkingContext.setCurrentWorkingContext(_workingContext);
				
				GeniiNotificationConsumerPortType consumer =
					ClientUtils.createProxy(
						GeniiNotificationConsumerPortType.class, _target);
				consumer.notify(_notifyMessage);
			}
			finally
			{
				WorkingContext.setCurrentWorkingContext(null);
			}
		}
	}

	public void run()
	{
		NotificationWorker worker;
		
		while (true)
		{
			try
			{
				synchronized(_workers)
				{
					while (_workers.isEmpty())
						_workers.wait();
					worker = _workers.removeFirst();
				}
				
				worker.doNotify();
			}
			catch (Throwable t)
			{
				_logger.warn(
					"Notification Pool saw an exception during a notification.", t);
			}
		}
	}
	
	public void submitNotificationRequest(
		EndpointReferenceType target, Notify notifyMessage)
			throws ContextException
	{
		NotificationWorker worker = new NotificationWorker(
			target, notifyMessage);
		
		synchronized(_workers)
		{
			_workers.addLast(worker);
			_workers.notify();
		}
	}
}