package edu.virginia.vcgr.genii.client.cmd.tools;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.utils.xml.SimpleNamespaceContext;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.LightweightNotificationServer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.LightweightSubscription;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.DefaultSubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscribeException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.Subscription;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;

public class WSNSubscribeTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Can either subscribe one service to another, " +
		"OR can subscribe itself to an endpoint and then listen.";
	static private final String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/subscribe-usage.txt";

	private class NotificationHandlerImpl
		extends AbstractNotificationHandler<NotificationMessageContents>
	{
		private NotificationHandlerImpl()
		{
			super(NotificationMessageContents.class);
		}

		@Override
		public void handleNotification(TopicPath topic,
			EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference,
			NotificationMessageContents contents) throws Exception
		{
			stdout.format(
				"Received notification [%s]:  %s\n",
				topic, contents);
		}
	}
	
	private GeniiPath _subscriptionPath = null;
	private SimpleNamespaceContext _context = new SimpleNamespaceContext();
	private TopicQueryExpression _filter = null;
	
	private void subscribe(GeniiPath publisher, GeniiPath subscriber)
		throws SubscribeException, RNSPathAlreadyExistsException, RNSException
	{
		RNSPath current = RNSPath.getCurrent();
		RNSPath publisherPath = current.lookup(publisher.path());
		RNSPath subscriberPath = current.lookup(subscriber.path());
		SubscriptionFactory factory = new DefaultSubscriptionFactory(
			subscriberPath.getEndpoint());
		Subscription result = factory.subscribe(publisherPath.getEndpoint(),
			_filter, null, null);
		
		if (_subscriptionPath != null)
		{
			RNSPath subscription = current.lookup(
				_subscriptionPath.path(), RNSPathQueryFlags.MUST_NOT_EXIST);
			subscription.link(result.subscriptionReference());
		}
	}
	
	private void subscribe(GeniiPath publisher) throws Exception
	{
		RNSPath current = RNSPath.getCurrent();
		RNSPath publisherPath = current.lookup(publisher.path());
		
		LightweightNotificationServer server =
			LightweightNotificationServer.createStandardServer();
		server.start();
		LightweightSubscription result = server.subscribe(
			publisherPath.getEndpoint(), _filter, null, null);
		result.registerNotificationHandler(new NotificationHandlerImpl());
		
		if (_subscriptionPath != null)
		{
			RNSPath subscription = current.lookup(
				_subscriptionPath.path(), RNSPathQueryFlags.MUST_NOT_EXIST);
			subscription.link(result.subscriptionReference());
		}
		
		while (true)
		{
			Thread.sleep(1000L * 1000);
		}
	}
	
	@Option("subscription-path")
	protected void subscriptionPath(String path) throws ToolException
	{
		_subscriptionPath = new GeniiPath(path);
		
		if (_subscriptionPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException(
				"Subscription path is not a grid path.");
	}
	
	protected void topicFilter(String topicFilter) throws ToolException
	{
		_filter = TopicPath.createTopicPath(
			_context, topicFilter).asConcreteQueryExpression();
	}
	
	protected void addPrefix(String prefix, String uri)
	{
		_context.associate(prefix, uri);
	}
	
	protected void addPrefix(String prefixString) throws ToolException
	{
		int index = prefixString.indexOf('=');
		if (index <= 0)
			throw new InvalidToolUsageException(String.format(
				"xmlns:%s is not a valid prefix instruction.", prefixString));
		
		addPrefix(prefixString.substring(0, index),
			prefixString.substring(index + 1));
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		GeniiPath publisher = new GeniiPath(getArgument(0));
		if (publisher.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException(
				"Publisher path must be a grid path.");
		
		if (numArguments() == 2)
		{
			GeniiPath subscriber = new GeniiPath(getArgument(1));
			if (subscriber.pathType() != GeniiPathType.Grid)
				throw new InvalidToolUsageException(
					"Subscriber path must be a grid path.");
			
			subscribe(publisher, subscriber);
		} else
			subscribe(publisher);
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1 || numArguments() > 2)
			throw new InvalidToolUsageException();
	}
	
	public WSNSubscribeTool()
	{
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), true);
	}
	
	@Override
	public void addArgument(String argument) throws ToolException
	{
		if (argument.startsWith("xmlns:"))
			addPrefix(argument.substring(6));
		
		super.addArgument(argument);
	}
}
