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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import java.security.cert.X509Certificate;

import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Call;
import org.apache.axis.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;
import org.apache.axis.configuration.FileProvider;

import org.ogf.schemas.naming._2006._08.naming.ResolveFailedWithReferralFaultType;

import java.io.IOException;
import org.apache.axis.types.URI;
import java.security.GeneralSecurityException;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.SimpleChain;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cache.LRUCache;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.CommConstants;
import edu.virginia.vcgr.genii.client.comm.MethodDescription;
import edu.virginia.vcgr.genii.client.comm.ResolutionContext;
import edu.virginia.vcgr.genii.client.configuration.*;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.MessageLevelSecurity;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.client.security.x509.*;
import edu.virginia.vcgr.genii.client.invoke.IFinalInvoker;
import edu.virginia.vcgr.genii.client.invoke.InvocationInterceptorManager;
import edu.virginia.vcgr.genii.client.naming.EPIResolutionCache;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.NameResolutionFailedException;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.comm.axis.security.*;

import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.naming.*;

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
	
	static private MessageLevelSecurity __minClientMessageSec = null;

	/**
	 * Class to wipe our loaded config stuff in the event the config manager
	 * reloads. 
	 */
	static {
		// configure the JVM to use the SSL socket factory that obtains
		// trust material from our own trust store
		java.security.Security.setProperty("ssl.SocketFactory.provider", 
				VcgrSslSocketFactory.class.getName());
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
	static private synchronized MessageLevelSecurity getMinClientMessageSec() throws GeneralSecurityException {

		if (__minClientMessageSec != null) { 
			return __minClientMessageSec;
		}
		
		String minMessageSecurity = 
			Installation.getDeployment(new DeploymentName()).security().getProperty(
				SecurityConstants.Client.MESSAGE_MIN_CONFIG_PROP);
			
		__minClientMessageSec =  new MessageLevelSecurity(minMessageSecurity);
		return __minClientMessageSec;
	}
	
	
//----------------------------------------------------------------------------
// AxisClientInvocationHandler
//-----------------------------------------------------------------------------
	
	private EndpointReferenceType _epr;
	
	static private final int _DEFAULT_TIMEOUT = 1000 * 120;
	private Integer _timeout = null;
	
	private ICallingContext _callContext;
	private X509Certificate _resourceCert;
	
	private AttachmentType _attachmentType =
		AttachmentType.DIME;
	private Collection<GeniiAttachment> _outAttachments = null;
	private Collection<GeniiAttachment> _inAttachments = null;
	
	private HashMap<MethodDescription, Object> _portMethods =
		new HashMap<MethodDescription, Object>();

	private Class<?> [] _locators = null;
	private AxisClientInvocationHandler _parentHandler = null;
	private FileProvider _providerConfig = null;
	
	// cache of signed, serialized delegation assertions
	static private int VALIDATED_CERT_CACHE_SIZE = 32;
	static private LRUCache<X509Certificate, Boolean> validatedCerts = 
		new LRUCache<X509Certificate, Boolean>(VALIDATED_CERT_CACHE_SIZE);
	
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
		MessageLevelSecurity minClientMessageSec = getMinClientMessageSec();
		MessageLevelSecurity minResourceSec = EPRUtils.extractMinMessageSecurity(_epr);
		MessageLevelSecurity neededMsgSec = minClientMessageSec.computeUnion(minResourceSec); 
		
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
					SecurityUtils.validateCertPath(chain);
			        
			        // insert into valid certs cache
			        validatedCerts.put(_resourceCert, Boolean.TRUE);
				}
			}
		} catch (Exception e) {
			if (minClientMessageSec.isWarn()) {
				Exception ex = new GenesisIISecurityException(
						"Cannot confirm trusted identity for " + _epr.getAddress().toString() + ": " + e.getMessage(), e);
				_logger.debug(ex.getMessage());
			} else {
				throw new GenesisIISecurityException("EPR for " + _epr.getAddress().toString() + " is untrusted: " + e.getMessage(), e);
			}
		}

		// prepare a message security datastructure for the message context
		// if needed
		MessageSecurityData msgSecData = null;
		if (!neededMsgSec.isNone()) {
			msgSecData = new MessageSecurityData(
					neededMsgSec,
					chain,
					epi);
		}
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
			cause = cause.getCause();
		}
		
		return false;
	}
	
	static private Random _expBackoffTwitter = new Random();
	// added resolution code - 1/07 - jfk3w
	// revamped resolution code 4/11/07 - jfk3w.
	public Object finalInvoke(Object obj, Method calledMethod, Object[] arguments)
		throws Throwable 
	{
		EndpointReferenceType origEPR = getTargetEPR();
		ResolutionContext context = null;
		int baseDelay = 100;
		int baseTwitter = 25;
		int attempt = 0;
		long startCommunicate = 0L;
		long deltaCommunicate = 0L;
		int timeout = (_timeout != null) ? _timeout.intValue() : _DEFAULT_TIMEOUT;
		
		while(true)
		{
			attempt++;
			context = new ResolutionContext(origEPR, (_parentHandler == null));
			_inAttachments = null;
	
			try
			{
				while (true)
				{
					AxisClientInvocationHandler handler = resolve(context);
					try
					{
						startCommunicate = System.currentTimeMillis();
						Object ret = handler.doInvoke(calledMethod, arguments, timeout);
						return ret;
					}
					catch(Throwable t)
					{
						// call failed
						//  ...As per discussion with mmm2a, it is difficult to 
						// determine which type of exceptions are "permanent" failures
						// and which are not - all depends on the semantics of the 
						// target and the resolver.  So, we punt and rebind on all 
						// exceptions.
						context.setErrorToReport(t);
					}
					finally
					{
						deltaCommunicate = System.currentTimeMillis() - startCommunicate;
					}
				}
			}
			catch (Throwable t)
			{
				// report last non-resolution error if possible
				if (context.getErrorToReport() == null)
					context.setErrorToReport(t);
				
				// strip exception down to base error
				while (!(context.getErrorToReport() instanceof RemoteException))
				{
					Throwable next = context.getErrorToReport().getCause();
					if (next == null)
						throw context.getErrorToReport();
					context.setErrorToReport(next);
				}
	
				Throwable cause = context.getErrorToReport();
				if (attempt <= 5 && isConnectionException(cause))
				{
					if (deltaCommunicate > MAX_FAILURE_TIME_RETRY)
					{
						_logger.warn(
							"Waited too long for a connection failure -- " +
							"it's not worth retrying so we'll give up.");
						_logger.debug("Unable to communicate with endpoint " +
								"(not a retryable-exception).", cause);
						throw cause;
					} else
					{
						try
						{
							int sleepTime = baseDelay + (_expBackoffTwitter.nextInt(
									baseTwitter) - (baseTwitter >> 1));
							_logger.debug("Exponential backoff delay of " +
								sleepTime + " for an exception.", cause);
							Thread.sleep(sleepTime);
						}
						catch (InterruptedException ie)
						{
							Thread.currentThread().isInterrupted();
							// Don't have to worry about it.
						}
						finally
						{
							baseDelay <<= 1;
							baseTwitter <<= 1;
						}
					}
				} else
				{
					_logger.debug("Unable to communicate with endpoint " +
						"(not a retryable-exception).", cause);
					throw cause;
				}
			}
			finally
			{
				_outAttachments = null;
			}
		}
	}

	protected Object doInvoke(Method calledMethod, Object[] arguments, 
		int timeout) throws Throwable
	{
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
		Object ret = calledMethod.invoke(stubInstance, arguments);

		Object [] inAttachments = stubInstance.getAttachments();
		if (inAttachments != null)
		{
			setInAttachments(inAttachments);
		}
		
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
		EndpointReferenceType resolvedEPR = null;
		AxisClientInvocationHandler newHandler = null;
		Throwable errorToReport = null;
		
		if (!context.triedOriginalEPR() && context.getOriginalAddress() != null
				&& !context.getOriginalAddress().get_value().toString()
						.equals(WSName.UNBOUND_ADDRESS)) {
			context.setTriedOriginalEPR();
			return this;
		}

		if (!context.rebindAllowed())
			throw new NameResolutionFailedException();

		/* check cache first */
		// took out cache because of issues with multiple resources with same EPI
		//		if (!context.triedCache())
		if(false)
		{
			URI epi = context.getEPI();
			if (epi != null)
			{
				resolvedEPR = EPIResolutionCache.get(epi);
				context.setTriedCache();
				newHandler = makeNewHandler(resolvedEPR);
				if (newHandler != null)
					return newHandler;
			}
		}
		
		/* cache failed - need to try resolvers */
		ListIterator<ResolverDescription> resolversIter = context.getResolversIter();
		if (resolversIter == null)
			throw new NameResolutionFailedException();

		while (resolversIter.hasNext())
		{
			ResolverDescription nextResolver = resolversIter.next();
			try
			{
				resolvedEPR = callResolve(nextResolver);
				if (resolvedEPR != null && resolvedEPR.getAddress() != null && !resolvedEPR.getAddress().toString().equals(WSName.UNBOUND_ADDRESS))
				{
					newHandler = makeNewHandler(resolvedEPR);
					if (newHandler != null)
					{
						// took out cache because of issues with multiple resources with same EPI
						//		EPIResolutionCache.put(epi, newHandler._epr);
						return newHandler;
					}
				}
			}
			catch(Throwable t)
			{
				_logger.debug("Call to resolve EPR failed.", t);
				errorToReport = t;
			}
		}

		throw new NameResolutionFailedException(errorToReport);
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
	
	protected EndpointReferenceType callResolve(ResolverDescription resolver)
		throws NameResolutionFailedException, ResourceException, 
			GenesisIISecurityException, RemoteException
	{
		try
		{
			if (resolver.getType() == ResolverDescription.ResolverType.EPI_RESOLVER)
			{
				EndpointIdentifierResolver resolverPT = ClientUtils.createProxy(EndpointIdentifierResolver.class, resolver.getEPR());
				return resolverPT.resolveEPI(
						new org.apache.axis.types.URI(resolver.getEPI().toString()));
			}
			else if (resolver.getType() == ResolverDescription.ResolverType.REFERENCE_RESOLVER)
			{
				ReferenceResolver resolverPT = ClientUtils.createProxy(ReferenceResolver.class, resolver.getEPR());
				return resolverPT.resolve(null);
			}
			throw new NameResolutionFailedException();
		}
		catch(org.apache.axis.types.URI.MalformedURIException mfe)
		{
			throw new NameResolutionFailedException(mfe);
		}
		catch(ResolveFailedWithReferralFaultType rfe)
		{
			_logger.debug("Resolver threw ResolveFailedWithReferralFaultType.  We do not handle these yet.");
			throw new NameResolutionFailedException();
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
}
