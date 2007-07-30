package edu.virginia.vcgr.genii.client.notification;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Token;
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpHandler;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SslListener;
import org.mortbay.http.handler.AbstractHttpHandler;
import org.mortbay.jetty.Server;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.AttributedURITypeSmart;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.notification.Notify;
import edu.virginia.vcgr.genii.common.notification.Subscribe;
import edu.virginia.vcgr.genii.common.notification.UserDataType;

/**
 * The notification server is a server (largely for clients that aren't also axis
 * services) which can receive asynchronous notifcations from notification producers.
 * 
 * @author mmm2a
 */
public class NotificationServer
{
	static private QName _USER_KEY_ELEMENT_NAME =
		new QName(GenesisIIConstants.GENESISII_NS, "notifcation-user-key");
	
	static private final String _HTTP_PROTOCOL = "http";
	static private final String _HTTPS_PROTOCOL = "https";
	
	static private final String _URL_PATTERN =
		"%1$s://%2$s:%3$d/";
	
	private String _protocol;
	protected Server _httpServer;
	private NotificationTable _listeners = new NotificationTable();
	private HashMap<GUID, ISubscription> _subscriptions =
		new HashMap<GUID, ISubscription>();
	
	private HttpContext createContext()
	{
		HttpContext context;
		
		context = new HttpContext();
		context.setContextPath("/");
		context.addHandler(createHandler());
		
		return context;
	}
	
	protected NotificationServer(Integer port)
	{
		SocketListener listener;
		
		_protocol = _HTTP_PROTOCOL;
		
		_httpServer = new Server();
		listener = new SocketListener();
		if (port != null)
			listener.setPort(port.intValue());
		_httpServer.addListener(listener);
		
		_httpServer.addContext(createContext());
	}
	
	protected NotificationServer(Integer port,
		String keystore, String keystoreType, 
		String password, String keyPassword)
	{
		SslListener listener;
		
		_protocol = _HTTPS_PROTOCOL;
		
		_httpServer = new Server();
		listener = new SslListener();
		if (port != null)
			listener.setPort(port.intValue());
		listener.setKeystore(keystore);
		listener.setKeystoreType(keystoreType);
		listener.setKeyPassword(keyPassword);
		listener.setPassword(password);
		_httpServer.addListener(listener);
		
		_httpServer.addContext(createContext());
	}
	
	protected void finalize() throws Throwable
	{
		try
		{
			stop();
		}
		catch (Throwable t)
		{
		}
		finally
		{
			super.finalize();
		}
	}
	
	/**
	 * Start this server running.
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception
	{
		_httpServer.start();
	}
	
	/**
	 * Stop the server so that it can't receive further notifications.
	 * @throws InterruptedException
	 */
	public void stop() throws InterruptedException
	{
		if (_httpServer.isStarted())
		{
			_httpServer.stop();
			synchronized(_subscriptions)
			{
				for (ISubscription sub : _subscriptions.values())
					sub.cancel();
			}
		}
	}
	
	protected HttpHandler createHandler()
	{
		return new NotificationHandler();
	}
	
	/**
	 * This method adds a new notification listener to the server (also subscribing this
	 * client to the topic at the given target) which will be called when a notification
	 * for that subscription arrives.
	 * 
	 * @param target The notification producer which we wish to subscribe to.
	 * @param topic The topic on which we wish to subscribe.
	 * @param listener A notification listener to call when a notification comes in.
	 * @return A subscription instance which can be used later to cancel the subscription.
	 * 
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public ISubscription addNotificationListener(EndpointReferenceType target,
		String topic, INotificationListener listener) 
		throws ConfigurationException, IOException
	{
		GUID userKey = _listeners.addEntry(listener);
		
		MessageElement userKeyElement = new MessageElement(
			_USER_KEY_ELEMENT_NAME, userKey.toString());
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, target);
		SubscriptionImpl subscription = new SubscriptionImpl(userKey,
			common.subscribe(new Subscribe(
				new Token(topic), null, createEndpoint(),
				new UserDataType(
					new MessageElement[] { userKeyElement }))).getSubscription());
		synchronized(_subscriptions)
		{
			_subscriptions.put(subscription.getSubscriptionKey(), subscription);
		}

		return subscription;
	}
	
	/**
	 * This method adds a new notification listener to the server (also subscribing this
	 * client to the topic at the given target) which will be called when a notification
	 * for that subscription arrives.
	 * 
	 * @param target The notification producer which we wish to subscribe to.
	 * @param topic The topic on which we wish to subscribe.
	 * @param listener A notification listener to call when a notification comes in.
	 * @return A subscription instance which can be used later to cancel the subscription.
	 * 
	 * @throws RNSPathDoesNotExistException
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public ISubscription addNotificationListener(RNSPath target,
		String topic, INotificationListener listener) 
			throws RNSPathDoesNotExistException, ConfigurationException, IOException
	{
		return addNotificationListener(target.getEndpoint(), topic, listener);
	}
	
	private EndpointReferenceType createEndpoint()
		throws IOException
	{
		if (!_httpServer.isStarted())
			throw new IOException("Server not started.");
		
		return new EndpointReferenceType(
			new AttributedURITypeSmart(String.format(_URL_PATTERN,
				_protocol, "127.0.0.1", _httpServer.getListeners()[0].getPort())),
			null, null, null);
	}
	
	/**
	 * Create a new, standard (non-SSL) notification server.
	 * 
	 * @return The newly created notification server.
	 */
	static public NotificationServer createStandardServer()
	{
		return new NotificationServer(null);
	}
	
	/**
	 * Create a new, standard (non-SSL) notification server on a given port.
	 * 
	 * @param port The port to create the notification server on.
	 * @return The newly created notification server.
	 */
	static public NotificationServer createStandardServer(int port)
	{
		return new NotificationServer(new Integer(port));
	}
	
	/**
	 * Create a new SSL based notification server.
	 * 
	 * @param keystoreLocation The file path of the keystore to use for this SSL
	 * connection.
	 * @param keystoreType The type of keystore represented.
	 * @param keystorePassword The keystore password
	 * @param keyPassword The private key password.
	 * 
	 * @return A new NotificationServer.
	 */
	static public NotificationServer createSslServer(
		String keystoreLocation, String keystoreType, String keystorePassword,
		String keyPassword)
	{
		return new NotificationServer(null, keystoreLocation, keystoreType,
			keystorePassword, keyPassword);
	}
	
	/**
	 * Create a new SSL based notification server.
	 * 
	 * @param port The port on which to create the SSL conection.
	 * @param keystoreLocation The file path of the keystore to use for this SSL
	 * connection.
	 * @param keystoreType The type of keystore represented.
	 * @param keystorePassword The keystore password
	 * @param keyPassword The private key password.
	 * 
	 * @return A new NotificationServer.
	 */
	static public NotificationServer createSslServer(int port,
		String keystoreLocation, String keystoreType, String keystorePassword,
		String keyPassword)
	{
		return new NotificationServer(new Integer(port),
			keystoreLocation, keystoreType,
			keystorePassword, keyPassword);
	}
	
	private class NotificationHandler extends AbstractHttpHandler
	{
		static final long serialVersionUID = 0L;
		
		public void handle(String pathInContext, String pathParams,
			HttpRequest request, HttpResponse response) 
				throws HttpException, IOException
		{
			InputStream in = null;
			
			try
			{
				in = request.getInputStream();
				SOAPMessage msg = MessageFactory.newInstance().createMessage(null, in);
				SOAPBody body = msg.getSOAPBody();
				MessageElement notifyElement = (MessageElement)body.getFirstChild();
				Notify notify = (Notify)ObjectDeserializer.toObject(
					notifyElement, Notify.class);
				UserDataType userData = notify.getUserData();
				if (userData != null)
				{
					MessageElement[]any = userData.get_any();
					if (any != null && any.length == 1)
					{
						QName name = any[0].getQName();
						if (name.equals(_USER_KEY_ELEMENT_NAME))
						{
							String key = any[0].getValue();
							GUID gKey = GUID.fromString(key);
							ISubscription subscription;
							synchronized(_subscriptions)
							{
								subscription = _subscriptions.get(gKey);
							}
							if (subscription != null)
								_listeners.notify(GUID.fromString(key), subscription,
									notify);
						}
					}
				}
			}
			catch (SOAPException se)
			{
				throw new IOException(se.getLocalizedMessage());
			}
			finally
			{
				StreamUtils.close(in);
			}
		}
	}
	
	private class SubscriptionImpl implements ISubscription
	{
		private EndpointReferenceType _subscription;
		private GUID _subscriptionKey;
		
		public SubscriptionImpl(GUID subscriptionKey,
			EndpointReferenceType subscription)
		{
			_subscription = subscription;
			_subscriptionKey = subscriptionKey;
		}
		
		public GUID getSubscriptionKey()
		{
			return _subscriptionKey;
		}
		
		public void cancel()
		{
			new Thread(new Runnable() {
				public void run()
				{
					try
					{
						GeniiCommon common = ClientUtils.createProxy(
							GeniiCommon.class, _subscription);
						common.immediateTerminate(null);
					}
					catch (Throwable t)
					{
						// It's best effort anyways.
					}
				}
			}).start();
			_listeners.remove(_subscriptionKey);
			synchronized(_subscriptions)
			{
				_subscriptions.remove(_subscriptionKey);
			}
		}
	}
}