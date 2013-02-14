/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.client.comm.axis;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.SimpleChain;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Call;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.cs.vcgr.genii._2006._12.resource_simple.TryAgainFaultType;
import edu.virginia.vcgr.appmgr.version.Version;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cache.LRUCache;
import edu.virginia.vcgr.genii.client.cache.ResourceAccessMonitor;
import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement.NotificationMessageIndexProcessor;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.CommConstants;
import edu.virginia.vcgr.genii.client.comm.GenesisIIEndpointInformation;
import edu.virginia.vcgr.genii.client.comm.GeniiSOAPHeaderConstants;
import edu.virginia.vcgr.genii.client.comm.MethodDescription;
import edu.virginia.vcgr.genii.client.comm.ResolutionContext;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.comm.axis.security.ISecurityRecvHandler;
import edu.virginia.vcgr.genii.client.comm.axis.security.ISecuritySendHandler;
import edu.virginia.vcgr.genii.client.comm.axis.security.MessageSecurity;
import edu.virginia.vcgr.genii.client.comm.axis.security.VcgrSslSocketFactory;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationUnloadedListener;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.SecurityConstants;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.invoke.IFinalInvoker;
import edu.virginia.vcgr.genii.client.invoke.InvocationInterceptorManager;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.NameResolutionFailedException;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.container.notification.NotificationBrokerConstants;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.MessageLevelSecurityRequirements;

public class AxisClientInvocationHandler implements InvocationHandler, IFinalInvoker
{
	
//----------------------------------------------------------------------------
// STATIC CONSTANT MEMBERS
//-----------------------------------------------------------------------------

	private static final String STUB_CONFIGURED = 
		"edu.virginia.vcgr.genii.client.security.stub-configured";

	/** We'll wait 16 seconds for a connection failure before it's considered
	 * TOO long for the exponential back-off retry.
	 */
	static private final long MAX_FAILURE_TIME_RETRY = 1000L * 16;
		
//----------------------------------------------------------------------------
// STATIC UTILITY METHODS
//-----------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	private static <HandlerClass> ArrayList<HandlerClass> getHandler(
			SimpleChain handlerChain, 
			Class<HandlerClass> handlerClass) {
		ArrayList<HandlerClass> retval = new ArrayList<HandlerClass>();		
		
		if (handlerChain != null) {
			for (org.apache.axis.Handler h : handlerChain.getHandlers()) {
				if (h instanceof SimpleChain) {
					retval.addAll(getHandler((SimpleChain) h, handlerClass));
				} else if (handlerClass.isAssignableFrom(h.getClass())) {
					retval.add((HandlerClass) h);
				}
			}
		}
		return retval;
	}	
	
//----------------------------------------------------------------------------
// STATIC CONFIGURATION MEMBERS
//-----------------------------------------------------------------------------
	
	static private MessageLevelSecurityRequirements __minClientMessageSec = null;

	/**
	 * Class to wipe our loaded config stuff in the event the config manager
	 * reloads. 
	 */
	static {
		// configure the JVM to use the SSL socket factory that obtains
		// trust material from our own trust store
		
		//Moved to main class (container.Container) (client.Driver)
		//java.security.Security.setProperty("ssl.SocketFactory.provider", 
		//		VcgrSslSocketFactory.class.getName());
		
		
		ConfigurationManager.addConfigurationUnloadListener(new ConfigUnloadListener());
	}
	public static class ConfigUnloadListener implements ConfigurationUnloadedListener {
		public void notifyUnloaded() {
			synchronized(AxisClientInvocationHandler.class) { 
				__minClientMessageSec = null;
			}
		}
	}
	
	
	static private Log _logger = LogFactory.getLog(AxisClientInvocationHandler.class);
	
	/**
	 * Retrieves the client's minimum allowable level of message security
	 */	
	static private synchronized MessageLevelSecurityRequirements getMinClientMessageSec() throws GeneralSecurityException {

		if (__minClientMessageSec != null) { 
			return __minClientMessageSec;
		}
		
		String minMessageSecurity = 
			Installation.getDeployment(new DeploymentName()).security().getProperty(
				SecurityConstants.Client.MESSAGE_MIN_CONFIG_PROP);
			
		__minClientMessageSec =  new MessageLevelSecurityRequirements(minMessageSecurity);
		return __minClientMessageSec;
	}
	
	
//----------------------------------------------------------------------------
// AxisClientInvocationHandler
//-----------------------------------------------------------------------------
	
	private EndpointReferenceType _epr;
	
	static final int _DEFAULT_TIMEOUT = 1000 * 360;  // default raised from 2 mins to 6.
	private Integer _timeout = null;
	
	private ICallingContext _callContext;
	private X509Certificate _resourceCert;
	
	private AttachmentType _attachmentType =
		AttachmentType.DIME;
	private Collection<GeniiAttachment> _outAttachments = null;
	private Collection<GeniiAttachment> _inAttachments = null;
	private GenesisIIEndpointInformation _lastEndpointInfo = null;
	
	private HashMap<MethodDescription, Object> _portMethods =
		new HashMap<MethodDescription, Object>();

	private Class<?> [] _locators = null;
	private AxisClientInvocationHandler _parentHandler = null;
	private FileProvider _providerConfig = null;
	
	// cache of signed, serialized delegation assertions
	static private int VALIDATED_CERT_CACHE_SIZE = 32;
	static private LRUCache<X509Certificate, Boolean> validatedCerts = 
		new LRUCache<X509Certificate, Boolean>(VALIDATED_CERT_CACHE_SIZE);
	
	static Object _lock = new Object();
	
	public AxisClientInvocationHandler(
			Class<?> locator, 
			EndpointReferenceType epr,
			ICallingContext callContext) 
			throws ResourceException, GenesisIISecurityException {
		this(new Class [] {locator}, epr, callContext);
	}
		
	public synchronized void configureSecurity(Stub stubInstance) 
		throws GenesisIISecurityException, GeneralSecurityException, ResourceException {

		if (stubInstance._getProperty(STUB_CONFIGURED) != null) {
			return;
		}
		stubInstance._setProperty(STUB_CONFIGURED, STUB_CONFIGURED);
		stubInstance._setProperty("attachments.implementation", 
			"org.apache.axis.attachments.AttachmentsImpl");
		
		X509Certificate[] chain = EPRUtils.extractCertChain(_epr);
		URI epi = EPRUtils.extractEndpointIdentifier(_epr);
		
		// determine the level of message security we need
		MessageLevelSecurityRequirements minClientMessageSec = getMinClientMessageSec();
		MessageLevelSecurityRequirements minResourceSec = null;
		
		synchronized(_lock)
		{
			minResourceSec = EPRUtils.extractMinMessageSecurity(_epr);
		}
		
		MessageLevelSecurityRequirements neededMsgSec = minClientMessageSec.computeUnion(minResourceSec); 
		
		// perform resource-AuthN as specified in the client config file
		try {
			if (chain == null) {
				throw new GenesisIISecurityException("EPR for " + _epr.getAddress().toString() + " does not contain a certificate chain.");
			}
			_resourceCert = chain[0];
			
			synchronized(validatedCerts) {
				if (!validatedCerts.containsKey(_resourceCert)) {

					// make sure the epi's match
					String certEpi = CertTool.getSN(chain[0]);
					if (!certEpi.equals(epi.toString())) {
						throw new GenesisIISecurityException("EPI for " + _epr.getAddress().toString() + " (" + epi.toString() + ") does not match that in the certificate (" + certEpi + ")");
					}
					
					// run it through the trust manager
					SecurityUtils.validateCertPath(chain, true);
			        
			        // insert into valid certs cache
			        validatedCerts.put(_resourceCert, Boolean.TRUE);
				}
			}
		} catch (Exception e) {
			if (minClientMessageSec.isWarn()) {
				// the security level is set to just warning, and this is as
				// loud of a warning as we want to emit.  otherwise we're just
				// constantly complaining that the level was set to warn only.
				_logger.trace("Cannot confirm trusted identity for " + _epr.getAddress().toString());
			} else {
				throw new GenesisIISecurityException("EPR for " + _epr.getAddress().toString() + " is untrusted: " + e.getMessage(), e);
			}
		}

		// prepare a message security datastructure for the message context
		// if needed
		MessageSecurity msgSecData = new MessageSecurity(
				neededMsgSec,
				chain,
				epi);
		if (msgSecData != null) {
			stubInstance._setProperty(CommConstants.MESSAGE_SEC_CALL_DATA, 
					msgSecData);
		}
		
		try {
			// configure the send handler(s), working backwards so as to set the 
			// last one that actually does work to serialize the message
			ArrayList<ISecuritySendHandler> sendHandlers = getHandler(
					(SimpleChain) _providerConfig.getGlobalRequest(), 
					ISecuritySendHandler.class);
			boolean serializerFound = false;
			for (int i = sendHandlers.size() - 1; i >= 0; i--) {
				ISecuritySendHandler h = sendHandlers.get(i);
				if (h.configure(_callContext, msgSecData) && !serializerFound) {
					serializerFound = true;
					h.setToSerialize();
				}
			}
	    	
			// configure the recv handler(s)
			ArrayList<ISecurityRecvHandler> recvHandlers = getHandler(
					(SimpleChain) _providerConfig.getGlobalResponse(), 
					ISecurityRecvHandler.class);
			for (ISecurityRecvHandler h : recvHandlers) {
				h.configure(_callContext);
			}
			
		} catch (Exception e) {
			throw new ResourceException("Unable to create locator instance: " + e.getMessage(),
				e);
		}
	}
		
	public AxisClientInvocationHandler(
			Class<?> []locators, 
			EndpointReferenceType epr,
			ICallingContext callContext) 
		throws ResourceException, GenesisIISecurityException 
	{
		try {

			_epr = epr;
			
			if (callContext == null) {
				callContext = new CallingContextImpl(new ContextType());
			}
			_callContext = callContext.deriveNewContext();
			_callContext.setSingleValueProperty(GenesisIIConstants.NAMING_CLIENT_CONFORMANCE_PROPERTY, "true");
			_locators = locators;
						
			// create the locator and add the methods
			for (Class<?> locator : locators) {
				Object locatorInstance = createLocatorInstance(locator);
				addMethods(locatorInstance, epr);
			}
		} catch (IOException ioe) {
			throw new ResourceException(
				"Error creating secure client stub: " + ioe.getMessage(), ioe);
		}

	}	

	private AxisClientInvocationHandler cloneHandlerForNewEPR (
			EndpointReferenceType epr) 
			throws ResourceException, GenesisIISecurityException {
		AxisClientInvocationHandler newHandler = 
			new AxisClientInvocationHandler(_locators, epr, _callContext);
		if (_outAttachments != null)
			newHandler._outAttachments = new LinkedList<GeniiAttachment> (_outAttachments);
		newHandler._attachmentType = _attachmentType;
		newHandler._parentHandler = this;
		return newHandler;
	}
	
	private Object createLocatorInstance(
			Class<?> loc)
		throws ResourceException
	{
		try
		{
			Constructor<?> cons = loc.getConstructor(org.apache.axis.EngineConfiguration.class);
			_providerConfig = new FileProvider("client-config.wsdd");
			Object retval = cons.newInstance(_providerConfig);
        	
        	return retval;
		}
		catch (NoSuchMethodException nsme)
		{
			throw new ResourceException("Class " + loc.getName() + 
				" does not refer to a known locator class type.", nsme);
		}
		catch (Exception e)
		{
			throw new ResourceException("Unable to create locator instance: " + e.getMessage(),
				e);
		}
	}
	
	
	private void addMethods(Object locatorInstance, EndpointReferenceType epr) 
			throws MalformedURLException, ResourceException
	{
		Method locatorPortTypeMethod;
		URL url = null;
		try
		{
			if (epr.getAddress().get_value().toString().equals(WSName.UNBOUND_ADDRESS))
				_logger.debug("Processing unbound address in AxisClientInvocationHandler");
			url = new URL(epr.getAddress().get_value().toString());
		}
		catch(java.net.MalformedURLException mue)
		{
			if (epr.getAddress().get_value().toString().equals(WSName.UNBOUND_ADDRESS))
				url = null;
			else
				throw mue;
		}
		try
		{
			locatorPortTypeMethod = ClientUtils.getLocatorPortTypeMethod(
				locatorInstance.getClass());
			Stub stubInstance = (Stub)locatorPortTypeMethod.invoke(
				locatorInstance, new Object[] {url});
			
			if (epr != null) {
				stubInstance._setProperty(CommConstants.TARGET_EPR_PROPERTY_NAME, epr);
			}
			if (_callContext != null) {
				stubInstance._setProperty(CommConstants.CALLING_CONTEXT_PROPERTY_NAME, 
						_callContext);
			}
			
			// Use the return type to get the methods that this stub supports
			Method []ms = locatorPortTypeMethod.getReturnType().getMethods();
			for (Method m : ms)
			{
				_portMethods.put(new MethodDescription(m), stubInstance);
			}
		}
		catch (InvocationTargetException ite)
		{
			Throwable t = ite.getCause();
			if (t != null)
			{
				if (t instanceof ResourceException)
					throw (ResourceException)t;
				else
					throw new ResourceException(t.toString(), t);
			}
			else
				throw new ResourceException(ite.toString(), ite);
		}
		catch (Exception e)
		{
			throw new ResourceException(
				"Unable to locate appropriate stub.", e);
		}
	}
	
	static private InvocationInterceptorManager _manager = null;
	synchronized static private InvocationInterceptorManager getManager()
	{
		if (_manager == null)
		{
			_manager = (InvocationInterceptorManager)ConfigurationManager.getCurrentConfiguration(
				).getClientConfiguration().retrieveSection(
						new QName("http://vcgr.cs.virginia.edu/Genesis-II", "client-pipeline"));
		}
		
		if (_manager == null)
		{
			_logger.error("Couldn't find client pipeline configuration.");
			return new InvocationInterceptorManager();
		}
		
		return _manager;			
	}
	
	@Override
	public Object invoke(Object target, Method m, Object []params) throws Throwable
	{
		InvocationInterceptorManager mgr = getManager();
		return mgr.invoke(getTargetEPR(), _callContext, this, m, params);
	}
	
	static private boolean isConnectionException(Throwable cause)
	{
		while (cause != null)
		{
			if (cause instanceof ConnectException)
				return true;
			if (cause instanceof SocketException)
			{
				if (cause.getMessage().contains("Connection reset"))
					return true;
			}
			cause = cause.getCause();
		}
		
		return false;
	}
	
	/**
	 * added resolution code - 1/07 - jfk3w
	 * revamped resolution code 4/11/07 - jfk3w.
	 */
	public Object finalInvoke(Object obj, Method calledMethod, Object[] arguments)
		throws Throwable 
	{
		EndpointReferenceType origEPR = getTargetEPR();
		ResolutionContext context = null;
		int baseDelay = 100;
		int baseTwitter = 25;
		int attempt = 0;
		long startAttempt = 0L;
		int timeout = (_timeout != null) ? _timeout.intValue() : _DEFAULT_TIMEOUT;
		TypeInformation type = null;
		
		ResourceAccessMonitor.reportResourceUsage(origEPR);
		
		while (true)
		{
			attempt++;
			startAttempt = System.currentTimeMillis();
			context = new ResolutionContext(origEPR, (_parentHandler == null));
			_inAttachments = null;
			try
			{
				return resolveAndInvoke(context, calledMethod, arguments, timeout);
			}
			catch (Throwable cause)
			{
				if (!isConnectionException(cause))
				{
					_logger.debug("Unable to communicate with endpoint " +
							"(not a retryable-exception).");
					throw cause;
				} else {
					// Presumably, here I need to invalidate the cache for all entries that 
					// belongs to that particular container.
				}
				if (type == null)
					type = new TypeInformation(_epr);
				int maxAttempts = (type.isEpiResolver() ? 1 : 5);
				if (attempt >= maxAttempts)
				{
					_logger.debug("Unable to communicate with endpoint " +
							"after " + attempt + " attempts.");
					throw cause;
				}
				// deltaCommunicate is the total amount of time spent on the attempt,
				// including time spent talking to the resolver and each instance.
				// If this single attempt took too long, then don't make another attempt.
				long deltaCommunicate = System.currentTimeMillis() - startAttempt;
				if (deltaCommunicate > MAX_FAILURE_TIME_RETRY)
				{
					_logger.debug("Unable to communicate with endpoint " +
							"after " + deltaCommunicate + " millis");
					throw cause;
				}
				try
				{
					// Sleep for a random period in the range of (-bt/2 ... +bt/2).
					int twitter = (int)(Math.random() * baseTwitter) - (baseTwitter >> 1);
					int sleepTime = baseDelay + twitter;
					_logger.debug("Exponential backoff delay of " +
							sleepTime + " for an exception.");
					Thread.sleep(sleepTime);
				}
				catch (InterruptedException ie)
				{
					Thread.currentThread().isInterrupted();
					// Don't have to worry about it.
				}
				baseDelay <<= 1;
				baseTwitter <<= 1;
			}
			finally
			{
				_outAttachments = null;
			}
		}
	}

	/**
	 * Send the message.  If it fails, then resolve a replica and try again.
	 *
	 * Possible sequence of events:
	 * 1. Send message to first instance.  Catch exception.
	 * 2. Ask resolver for second instance.
	 * 3. Send message to second instance.  Catch exception.
	 * 4. Ask resolver for next instance.
	 * 5. There are no more instances, so resolve() throws an exception.  Catch it.
	 * 6. Throw the failure that was reported by the first instance back in step 1.
	 *    Discard the exceptions from resolve() and from all other instances.
	 */
	private Object resolveAndInvoke(ResolutionContext context, Method calledMethod, Object[] arguments, int timeout)
		throws Throwable
	{
		AxisClientInvocationHandler handler = null;
		boolean tryAgain = false;
		Throwable firstException = null;
		while (true)
		{
			try
			{
				if (!tryAgain)
					handler = resolve(context);
			}
			catch (Throwable throwable)
			{
				if (firstException == null)
					firstException = throwable;
				throw firstException;
			}
			try
			{
				return handler.doInvoke(calledMethod, arguments, timeout);
			}
			catch (Throwable throwable)
			{
				if (throwable instanceof InvocationTargetException)
					throwable = throwable.getCause();
				_logger.debug("doInvoke failure: " + throwable);
				if ((throwable instanceof TryAgainFaultType) && (!tryAgain))
				{
					tryAgain = true;
				}
				else
				{
					if (firstException == null)
						firstException = throwable;
					tryAgain = false;
					
					_logger.debug("failed method: " + calledMethod.getName());
					
					// Resetting the client cache as the original EPR holding container might be 
					// down, which will invalidate existing subscriptions and the notification broker.
					String methodName = calledMethod.getName();
					if (!"destroy".equalsIgnoreCase(methodName) 
							&& !"createIndirectSubscriptions".equalsIgnoreCase(methodName) 
							&& !"createNotificationBrokerWithForwardingPort".equalsIgnoreCase(methodName)
							&& !"getMessages".equalsIgnoreCase(methodName)
							&& !"updateMode".equalsIgnoreCase(methodName)) {
						
						// If the method is not the destroy method or any notification management method 
						// only then cache has been refreshed. Destroy method is ignored because otherwise 
						// there is a chance of cycle formation as the cache management system itself use 
						// WS-resources that are destroyed with a cache refresh and invocation of destroy 
						// on those resources can fail too. Meanwhile, notification management methods are
						// ignored to avoid redundant cache refreshes.
						CacheManager.resetCachingSystem();
					}
				}
			}
		}
	}

	protected Object doInvoke(Method calledMethod, Object[] arguments, 
		int timeout) throws Throwable
	{
		_lastEndpointInfo = null;
		MethodDescription methodDesc = new MethodDescription(calledMethod);
		Stub stubInstance = (Stub) _portMethods.get(methodDesc);

		configureSecurity(stubInstance);
		
		if (_outAttachments != null)
		{
			if (_attachmentType == AttachmentType.DIME)
				stubInstance._setProperty(
					Call.ATTACHMENT_ENCAPSULATION_FORMAT,
					Call.ATTACHMENT_ENCAPSULATION_FORMAT_DIME);
			else
				stubInstance._setProperty(
					Call.ATTACHMENT_ENCAPSULATION_FORMAT,
					Call.ATTACHMENT_ENCAPSULATION_FORMAT_MTOM);
	
			for (GeniiAttachment outAttachment : _outAttachments)
			{
				ByteArrayDataSource ds = new ByteArrayDataSource(
						outAttachment.getData(), "application/octet-stream");
				String name = outAttachment.getName();
				if (name != null)
					ds.setName(name);
				stubInstance.addAttachment(new DataHandler(ds));
			}
		}
	
		stubInstance.setTimeout(timeout);
		
		/* Set calling context so that the socket factory has access to
		 * it.
		 */
		_logger.trace(String.format(
			"Starting an outcall for %s on thread [%x]%s.",
			calledMethod.getName(), Thread.currentThread().getId(), Thread.currentThread()));
		long start = System.currentTimeMillis();
		VcgrSslSocketFactory.threadCallingContext.set(_callContext);
		Object ret = calledMethod.invoke(stubInstance, arguments);
		VcgrSslSocketFactory.threadCallingContext.set(null);
		start = System.currentTimeMillis() - start;
		_logger.trace(String.format(
			"Finished an outcall for %s on thread [%x]%s (duration was %d ms).",
			calledMethod.getName(), Thread.currentThread().getId(), Thread.currentThread(), start));

		Object [] inAttachments = stubInstance.getAttachments();
		if (inAttachments != null)
			setInAttachments(inAttachments);
		
		boolean isGeniiEndpoint = false;
		Version endpointVersion = null;
		
		for (SOAPHeaderElement elem : stubInstance.getResponseHeaders())
		{
			QName name = elem.getQName();
			if (name.equals(GeniiSOAPHeaderConstants.GENII_ENDPOINT_QNAME))
			{
				org.w3c.dom.Node n = elem.getFirstChild();
				if (n != null)
				{
					String text = n.getNodeValue();
					if (text != null && text.equalsIgnoreCase("true"))
						isGeniiEndpoint = true;
				}
			} else if (name.equals(
				GeniiSOAPHeaderConstants.GENII_ENDPOINT_VERSION))
			{
				org.w3c.dom.Node n = elem.getFirstChild();
				if (n != null)
				{
					String text = n.getNodeValue();
					if (text != null)
					{
						try
						{
							endpointVersion = new Version(text);
						}
						catch (Throwable cause)
						{
							_logger.warn(
								"Unable to parse version from soap header.", 
								cause);
						}
					}
				}
			} else if (name.equals(NotificationBrokerConstants.MESSAGE_INDEX_QNAME)) {
				EndpointReferenceType target = getTargetEPR();
				int messageIndex = Integer.parseInt(elem.getValue());
				NotificationMessageIndexProcessor.processMessageIndexValue(target, messageIndex);
			}
		}
		
		_lastEndpointInfo = new GenesisIIEndpointInformation(
			isGeniiEndpoint, endpointVersion);
		return ret;
	}
	
	private void setInAttachments(Object [] inAttachments)
		throws IOException, SOAPException
	{
		Collection <GeniiAttachment> attachmentsList = new LinkedList<GeniiAttachment>();
		for (Object nextAttachment : inAttachments)
		{
			if (nextAttachment instanceof AttachmentPart)
			{
				AttachmentPart part = (AttachmentPart)nextAttachment;
				attachmentsList.add(new GeniiAttachment(
					GeniiAttachment.extractData(part)));
			} else
			{
				_logger.warn(
					"Received an attachment type that I don't know how to deal with.");
			}
		}
		
		// set inbound attachments all the way up the stack of invocation handlers
		AxisClientInvocationHandler nextHandler = this;
		while(nextHandler != null)
		{
			if (attachmentsList.size() > 0)
				nextHandler._inAttachments = attachmentsList;
			else
				nextHandler._inAttachments = null;
			nextHandler = nextHandler._parentHandler;
		}
	}
	
	/* 
	 * Try to resolve EPR (based on resolution context) and return a new invocation handler
	 * for next invocation attempt. 
	 * If resolution fails, throw exception.  
	 *	 
	 * WARNING: Contents of ResolutionContext may be changed during this call.
	 */
	protected AxisClientInvocationHandler resolve(ResolutionContext context)
		throws NameResolutionFailedException
	{
		EndpointReferenceType originalEPR = context.getOriginalEPR();
		if ((!context.triedOriginalEPR()) && (!EPRUtils.isUnboundEPR(originalEPR)))
		{
			context.setTriedOriginalEPR();
			return this;
		}
		if (!context.rebindAllowed())
		{
			throw new NameResolutionFailedException();
		}
		try
		{
			EndpointReferenceType resolvedEPR = context.resolve();
			if (resolvedEPR != null)
			{
				AxisClientInvocationHandler newHandler = makeNewHandler(resolvedEPR);
				if (newHandler != null)
				{
					return newHandler;
				}
			}
		}
		catch (RemoteException exception)
		{
			throw new NameResolutionFailedException(exception);
		}
		throw new NameResolutionFailedException();
	}
	
	protected AxisClientInvocationHandler makeNewHandler(EndpointReferenceType resolvedEPR)
	{
		try
		{
			return cloneHandlerForNewEPR(resolvedEPR); 
		}
		catch(Throwable t)
		{
			_logger.debug("Attempt to create new AxisClientInvocationHandle failed.", t);
			return null;
		}
	}

	public EndpointReferenceType getTargetEPR()
	{
		return _epr;
	}
	
	public void setOutAttachments(Collection<GeniiAttachment> attachments,
		AttachmentType attachmentType)
	{
		if (_parentHandler != null)
		{
			_logger.warn("Tried to set outbound attachments on cloned AxisClientInvocationHandler.");
		}
		_outAttachments = attachments;
		_attachmentType = attachmentType;
	}
	
	public void setTimeout(int timeoutMillis)
	{
		_timeout = new Integer(timeoutMillis);
	}
	
	public Collection<GeniiAttachment> getInAttachments()
	{
		if (_parentHandler != null)
		{
			_logger.warn("Tried to get inbound attachments on cloned AxisClientInvocationHandler.");
		}
		Collection<GeniiAttachment> res = _inAttachments;
		_inAttachments = null;
		
		return res;
	}
	
	public GenesisIIEndpointInformation getLastEndpointInformation()
	{
		return _lastEndpointInfo;
	}
}
