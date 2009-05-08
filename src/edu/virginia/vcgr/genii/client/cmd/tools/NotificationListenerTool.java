package edu.virginia.vcgr.genii.client.cmd.tools;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.notification.INotificationListener;
import edu.virginia.vcgr.genii.client.notification.ISubscription;
import edu.virginia.vcgr.genii.client.notification.NotificationServer;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class NotificationListenerTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Lists for the given topic at the given target.";
	static private final String _USAGE =
		"notification-listener <target> <topic>";
	
	public NotificationListenerTool()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath path = RNSPath.getCurrent();
		RNSPath target = path.lookup(
			getArgument(0), RNSPathQueryFlags.MUST_EXIST);
		
		NotificationServer server = NotificationServer.createStandardServer();
		server.start();
		server.addNotificationListener(target, getArgument(1), 
			new NotificationListener());

		while (true)
		{
			Thread.sleep(1000 * 10);
		}
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}
	
	static private class NotificationListener implements INotificationListener
	{
		public void notify(ISubscription subscription, 
			EndpointReferenceType source, String topic, 
			MessageElement[] notificationData)
		{
			System.err.println("Received notification on topic \"" + topic + "\".");
			System.err.println("Data:");
			for (MessageElement elem : notificationData)
			{
				System.err.println(elem.toString());
			}
		}
	}
}