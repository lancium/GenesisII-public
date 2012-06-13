package edu.virginia.vcgr.genii.client.wsrf.wsn.notification;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.morgan.util.io.StreamUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.security.SslSocketConnector;
import org.oasis_open.wsn.base.Notify;
import org.oasis_open.wsn.base.SubscribeResponse;
import org.w3c.dom.Element;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;
import edu.virginia.vcgr.genii.client.wsrf.wsn.DefaultNotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationRegistration;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.AbstractSubscription;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.AbstractSubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscribeException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscribeRequest;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.Subscription;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.TerminationTimeType;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicy;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class LightweightNotificationServer
{
	static private enum Protocols
	{
		http,
		https;
	}
	
	static final private String URL_PATTERN = "%1$s://%2$s:%3$d/";
	
	static private SocketConnector createSocketConnector(Integer port)
	{
		SocketConnector listener = new SocketConnector();
		
		if (port != null)
			listener.setPort(port);
		
		return listener;
	}
	
	static private SocketConnector createSslSocketConnector(Integer port,
		String keystore, String keystoreType,
		String password, String keyPassword)
	{
		SslSocketConnector listener = new SslSocketConnector();
		
		if (port != null)
			listener.setPort(port);
		
		listener.setKeystore(keystore);
		listener.setKeystoreType(keystoreType);
		listener.setKeyPassword(keyPassword);
		listener.setPassword(password);
		
		return listener;
	}
	
	private class NotificationJettyHandler extends AbstractHandler
	{
		@Override
		public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) 
				throws IOException, ServletException
		{
			InputStream in = null;
			
			try
			{
				in = request.getInputStream();
				SOAPMessage msg =
					MessageFactory.newInstance().createMessage(null, in);
				SOAPBody body = msg.getSOAPBody();
				Element notifyElement = (Element)body.getFirstChild();
				Notify notify = (Notify)ObjectDeserializer.toObject(
					notifyElement, Notify.class);
				NotificationHelper.notify(notify, _multiplexer);
			}
			catch (SOAPException e)
			{
				throw new IOException("Unable to parse notification message.", 
					e);
			}
			finally
			{
				StreamUtils.close(in);
			}
		}
	}
	
	private class LightweightSubscriptionImpl extends AbstractSubscription
		implements LightweightSubscription
	{
		private TopicQueryExpression _queryExpression;
		
		LightweightSubscriptionImpl(TopicQueryExpression queryExpression,
			SubscribeResponse response)
		{
			super(response);
		}
		
		@Override
		public void cancel()
		{
			synchronized(_subscriptions)
			{
				_subscriptions.remove(this);
			}
			
			super.cancel();
		}

		@Override
		final public <ContentsType extends NotificationMessageContents> NotificationRegistration registerNotificationHandler(
				NotificationHandler<ContentsType> handler)
		{
			return _multiplexer.registerNotificationHandler(
				_queryExpression, handler);
		}
	}
	
	private NotificationMultiplexer _multiplexer =
		new DefaultNotificationMultiplexer();
	
	private Protocols _protocol;
	private Server _httpServer;
	
	private Set<LightweightSubscription> _subscriptions =
		new HashSet<LightweightSubscription>();
	
	public void setMultiplexer(NotificationMultiplexer multiplexer) {
		this._multiplexer = multiplexer;
	}
	
	public EndpointReferenceType getEPR() throws IOException
	{
		if (!_httpServer.isStarted())
			throw new IOException("Server not started!");
		
		return new EndpointReferenceType(
			new AttributedURIType(String.format(URL_PATTERN,
				_protocol, Hostname.getMostGlobal().getCanonicalHostName(),
				_httpServer.getConnectors()[0].getLocalPort())),
				null, null, null);
	}
	
	private ContextHandler createContext()
	{
		ContextHandler handler = new ContextHandler();
		handler.setContextPath("/");
		handler.addHandler(new NotificationJettyHandler());
		return handler;
	}
	
	private LightweightNotificationServer(SocketConnector listener)
	{
		_protocol = (listener instanceof SslSocketConnector) ?
			Protocols.https : Protocols.http;
		
		_httpServer = new Server();
		
		_httpServer.addConnector(listener);
		_httpServer.addHandler(createContext());
	}
	
	private LightweightNotificationServer(Integer port)
	{
		this(createSocketConnector(port));
	}
	
	private LightweightNotificationServer(Integer port,
		String keystore, String keystoreType,
		String password, String keyPassword)
	{
		this(createSslSocketConnector(
			port, keystore, keystoreType, password, keyPassword));
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		stop();
	}
	
	final public void start() throws Exception
	{
		_httpServer.start();
	}
	
	final public void stop() throws Exception
	{
		if (_httpServer.isStarted())
		{
			_httpServer.stop();
			synchronized (_subscriptions)
			{
				for (Subscription subscription : _subscriptions)
					subscription.cancel();
			}
		}
	}
	
	final public SubscribeRequest createSubscribeRequest(
		TopicQueryExpression topicFilter,
		TerminationTimeType terminationTime,
		AdditionalUserData additionalUserData,
		SubscriptionPolicy...policies) throws IOException
	{
		return AbstractSubscriptionFactory.createRequest(
			getEPR(), topicFilter, terminationTime,
			additionalUserData, policies);
	}
	
	final public LightweightSubscription subscribe(
		EndpointReferenceType publisher,
		TopicQueryExpression topicFilter,
		TerminationTimeType terminationTime,
		AdditionalUserData additionalUserData,
		SubscriptionPolicy...policies) throws SubscribeException
	{
		try
		{
			GeniiCommon common = ClientUtils.createProxy(
				GeniiCommon.class, publisher);
			return new LightweightSubscriptionImpl(topicFilter,
				common.subscribe(createSubscribeRequest(
					topicFilter, terminationTime, additionalUserData, 
					policies).asRequestType()));
		}
		catch (IOException e)
		{
			throw new SubscribeException(
				"Unable to subscribe consumer to publisher!", e);
		}
	}
	
	final public <ContentsType extends NotificationMessageContents> NotificationRegistration registerNotificationHandler(
		TopicQueryExpression topicFilter, NotificationHandler<ContentsType> handler)
	{
		return _multiplexer.registerNotificationHandler(topicFilter, handler);
	}
	
	static public LightweightNotificationServer createStandardServer()
	{
		return new LightweightNotificationServer((Integer)null);
	}
	
	static public LightweightNotificationServer createStandardServer(int port)
	{
		return new LightweightNotificationServer(port);
	}
	
	static public LightweightNotificationServer createSslServer(
		String keystoreLocation, String keystoreType, String keystorePassword,
		String keyPassword)
	{
		return new LightweightNotificationServer(null, keystoreLocation,
			keystoreType, keystorePassword, keyPassword);
	}
	
	static public LightweightNotificationServer createSslServer(int port,
		String keystoreLocation, String keystoreType, String keystorePassword,
		String keyPassword)
	{
		return new LightweightNotificationServer(port, keystoreLocation,
			keystoreType, keystorePassword, keyPassword);
	}
}