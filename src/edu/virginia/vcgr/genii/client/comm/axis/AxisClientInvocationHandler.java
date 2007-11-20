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

import org.morgan.util.StopWatch;
import org.morgan.util.configuration.XMLConfiguration;
import org.morgan.util.configuration.ConfigurationException;
import org.ogf.schemas.naming._2006._08.naming.ResolveFailedWithReferralFaultType;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.SimpleChain;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
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
import edu.virginia.vcgr.genii.client.security.x509.*;
import edu.virginia.vcgr.genii.client.utils.deployment.DeploymentRelativeFile;
import edu.virginia.vcgr.genii.client.invoke.IFinalInvoker;
import edu.virginia.vcgr.genii.client.invoke.InvocationInterceptorManager;
import edu.virginia.vcgr.genii.client.naming.EPIResolutionCache;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.NameResolutionFailedException;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.comm.axis.security.MessageSecurityData;
import edu.virginia.vcgr.genii.client.comm.axis.security.ISecurityHandler;
import edu.virginia.vcgr.genii.client.comm.axis.security.VcgrSslSocketFactory;

import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.naming.ReferenceResolver;

public class AxisClientInvocationHandler implements InvocationHandler, IFinalInvoker
{
	
//----------------------------------------------------------------------------
// STATIC CONSTANT MEMBERS
//-----------------------------------------------------------------------------

	private static final String TS_LOCATION = 
		"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-location";
	private static final String TS_TYPE = 
		"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-type";
	private static final String TS_PASSWORD = 
		"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-password";
	private static final String MIN_MESSAGE_SECURITY = 
		"edu.virginia.vcgr.genii.client.security.message.min-config";

	
	
		
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
	
	static private KeyStore __trustStore = null;
	static private MessageLevelSecurity __minClientMessageSec = null;

	/**
	 * Class to wipe our loaded config stuff in the event the config manager
	 * reloads. 
	 */
	public static class ConfigUnloadListener implements ConfigurationUnloadedListener {
		public void notifyUnloaded() {
			synchronized(AxisClientInvocationHandler.class) { 
				__trustStore = null;
				__minClientMessageSec = null;
			}
		}
	}
	
	
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(AxisClientInvocationHandler.class);

	// configure the JVM to use the SSL socket factory that obtains
	// trust material from our own trust store
	static {
		java.security.Security.setProperty("ssl.SocketFactory.provider", 
			VcgrSslSocketFactory.class.getName());
		ConfigurationManager.addConfigurationUnloadListener(new ConfigUnloadListener());
	}
	
	static public synchronized void configurationUnloaded() {
		__trustStore = null;
		__minClientMessageSec = null;
	}
	
	/**
	 * Establishes the trust manager for use in verifying resource identities 
	 */
	static private synchronized KeyStore getTrustStore() throws GeneralSecurityException {

		if (__trustStore != null) {
			return __trustStore;
		}
		
		try {
			XMLConfiguration conf = 
				ConfigurationManager.getCurrentConfiguration().getClientConfiguration();
			Properties resourceIdSecProps = (Properties) conf.retrieveSection(
					GenesisIIConstants.RESOURCE_IDENTITY_PROPERTIES_SECTION_NAME);			

			String trustStoreLoc = resourceIdSecProps.getProperty(
				TS_LOCATION);
			String trustStoreType = resourceIdSecProps.getProperty(
				TS_TYPE, GenesisIIConstants.TRUST_STORE_TYPE_DEFAULT);
			String trustStorePass = resourceIdSecProps.getProperty(
				TS_PASSWORD);
			
			// open the trust store
			if (trustStoreLoc == null) {
				throw new GenesisIISecurityException("Could not load TrustManager: no identity trust store location specified");
			}
			char[] trustStorePassChars = null;
			if (trustStorePass != null) {
				trustStorePassChars = trustStorePass.toCharArray();
			}
			__trustStore = CertTool.openStoreDirectPath(
				new DeploymentRelativeFile(trustStoreLoc), trustStoreType, trustStorePassChars);
			return __trustStore;

		} catch (ConfigurationException e) { 
			throw new GeneralSecurityException("Could not load TrustManager: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new GeneralSecurityException("Could not load TrustManager: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Retrieves the client's minimum allowable level of message security
	 */	
	static private synchronized MessageLevelSecurity getMinClientMessageSec() throws GeneralSecurityException {

		if (__minClientMessageSec != null) { 
			return __minClientMessageSec;
		}
		
		try {
			XMLConfiguration conf = 
				ConfigurationManager.getCurrentConfiguration().getClientConfiguration();
	
			Properties resourceIdSecProps = (Properties) conf.retrieveSection(
					GenesisIIConstants.MESSAGE_SECURITY_PROPERTIES_SECTION_NAME);			
	
			String minMessageSecurity = resourceIdSecProps.getProperty(
				MIN_MESSAGE_SECURITY);
			
			__minClientMessageSec =  new MessageLevelSecurity(minMessageSecurity);
			return __minClientMessageSec;
			
		} catch (ConfigurationException e) {
			throw new GeneralSecurityException("Could not read minimum message level security configuration: " + e.getMessage(), e);
		}
	}
	
	
//----------------------------------------------------------------------------
// AxisClientInvocationHandler
//-----------------------------------------------------------------------------
	
	private EndpointReferenceType _epr;
	
	@SuppressWarnings("unused")
	private ICallingContext _callContext;
	@SuppressWarnings("unused")
	private X509Certificate _resourceCert;
	
	private AttachmentType _attachmentType =
		AttachmentType.DIME;
	private Collection<GeniiAttachment> _outAttachments = null;
	private Collection<GeniiAttachment> _inAttachments = null;
	
	private HashMap<MethodDescription, Object> _portMethods =
		new HashMap<MethodDescription, Object>();

	private Class<?> [] _locators = null;
	private AxisClientInvocationHandler _parentHandler = null;
	
	public AxisClientInvocationHandler(
			Class<?> locator, 
			EndpointReferenceType epr,
			ICallingContext callContext) 
			throws ResourceException, GenesisIISecurityException {
		this(new Class [] {locator}, epr, callContext);
	}
		
		
	public AxisClientInvocationHandler(
			Class<?> []locators, 
			EndpointReferenceType epr,
			ICallingContext callContext) 
		throws ResourceException, GenesisIISecurityException 
	{
		StopWatch watch = new StopWatch();
		
		try {

			_epr = epr;
			
			watch.start();
			if (callContext == null) {
				callContext = new CallingContextImpl(new ContextType());
			}
			_callContext = callContext.deriveNewContext();
			_callContext.setSingleValueProperty(GenesisIIConstants.NAMING_CLIENT_CONFORMANCE_PROPERTY, "true");
			_locators = locators;
			_logger.debug("A:  " + watch.lap());
			
			X509Certificate[] chain = EPRUtils.extractCertChain(epr);
			URI epi = EPRUtils.extractEndpointIdentifier(epr);

			// deterimine the level of message security we need
			MessageLevelSecurity minClientMessageSec = getMinClientMessageSec();
			MessageLevelSecurity minResourceSec = EPRUtils.extractMinMessageSecurity(epr);
			MessageLevelSecurity neededMsgSec = minClientMessageSec.computeUnion(minResourceSec); 
			_logger.debug("B:  " + watch.lap());
			
			// perform resource-AuthN as specified in the client config file
			try {
				if (chain == null) {
					throw new GenesisIISecurityException("EPR for " + epr.getAddress().toString() + " does not contain a certificate chain.");
				}
				_resourceCert = chain[0];
				
				// make sure the epi's match
				String certEpi = CertTool.getUID(chain[0]);
				if (!certEpi.equals(epi.toString())) {
					throw new GenesisIISecurityException("EPI for " + epr.getAddress().toString() + " (" + epi.toString() + ") does not match that in the certificate (" + certEpi + ")");
				}

				// run it through the trust manager
				ArrayList<X509Certificate> certList = new ArrayList<X509Certificate>();
				for (int i = 0; i < chain.length - 1; i++) {
					certList.add(chain[i]);
				}
		        CertPath cp = CertificateFactory.getInstance("X.509", "BC").
		        	generateCertPath(certList);
		        CertPathValidator cpv = CertPathValidator.getInstance(
		        		"PKIX", "BC");
		        PKIXParameters param = new PKIXParameters(getTrustStore());
		        param.setRevocationEnabled(false);
		        cpv.validate(cp, param);
		        _logger.debug("C:  " + watch.lap());
			} catch (Exception e) {
				if (minClientMessageSec.isWarn()) {
					Exception ex = new GenesisIISecurityException(
							"Cannot confirm trusted identity for " + epr.getAddress().toString() + ": " + e.getMessage(), e);
					_logger.warn(ex.getMessage());
				} else {
					throw new GenesisIISecurityException("EPR for " + epr.getAddress().toString() + " is untrusted: " + e.getMessage(), e);
				}
			}

			watch.start();
			// prepare a message security datastructure for the message context
			// if needed
			MessageSecurityData msgSecData = null;
			if (!neededMsgSec.isNone()) {
				msgSecData = new MessageSecurityData(
						neededMsgSec,
						chain,
						epi);
			}
			_logger.debug("D:  " + watch.lap());
			
			// create the locator and add the methods
			for (Class<?> locator : locators) {
				Object locatorInstance = createLocatorInstance(
						locator,
						_callContext);
				addMethods(locatorInstance, epr, msgSecData);
			}
			_logger.debug("E:  " + watch.lap());
		} catch (IOException ioe) {
			throw new ResourceException(
				"Error creating secure client stub.", ioe);
 		} catch (GeneralSecurityException gse) {
			throw new ResourceException(
				"Error creating secure client stub.", gse);
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
	
	static private Object createLocatorInstance(
			Class<?> loc,
			ICallingContext callingContext)
		throws ResourceException
	{
		try
		{
			Constructor<?> cons = loc.getConstructor(org.apache.axis.EngineConfiguration.class);
			FileProvider config = new FileProvider("client-config.wsdd");
			Object retval = cons.newInstance(config);

			// configure the send handler(s)
			ArrayList<ISecurityHandler> sendHandlers = getHandler(
					(SimpleChain) config.getGlobalRequest(), 
					ISecurityHandler.class);
			for (int i = 0; i < sendHandlers.size(); i++) {
				ISecurityHandler h = sendHandlers.get(i);
				// instruct the last handler to serialize the message
				if (i < sendHandlers.size() - 1) {
					h.configure(callingContext, false);
				} else {
					h.configure(callingContext, true);
				}
			}
        	
			// configure the recv handler(s)
			ArrayList<ISecurityHandler> recvHandlers = getHandler(
					(SimpleChain) config.getGlobalResponse(), 
					ISecurityHandler.class);
			for (ISecurityHandler h : recvHandlers) {
				h.configure(callingContext);
			}
        	
        	return retval;
		}
		catch (NoSuchMethodException nsme)
		{
			throw new ResourceException("Class " + loc.getName() + 
				" does not refer to a known locator class type.", nsme);
		}
		catch (Exception e)
		{
			throw new ResourceException("Unable to create locator instance.",
				e);
		}
	}
	
	
	private void addMethods(Object locatorInstance,
		EndpointReferenceType epr, MessageSecurityData msgSecData) 
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
			
			stubInstance.setTimeout(1000 * 120);
			if (epr != null) {
				stubInstance._setProperty(CommConstants.TARGET_EPR_PROPERTY_NAME, epr);
			}
			if (_callContext != null) {
				stubInstance._setProperty(CommConstants.CALLING_CONTEXT_PROPERTY_NAME, 
						_callContext);
			}
			if (msgSecData != null) {
				stubInstance._setProperty(CommConstants.MESSAGE_SEC_CALL_DATA, 
						msgSecData);
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
		try
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
		catch (ConfigurationException ce)
		{
			_logger.error("Couldn't find client pipeline configuration.", ce);
			return new InvocationInterceptorManager();
		}
			
	}
	public Object invoke(Object target, Method m, Object []params) throws Throwable
	{
		InvocationInterceptorManager mgr = getManager();
		return mgr.invoke(getTargetEPR(), _callContext, this, m, params);
	}
	
	// added resolution code - 1/07 - jfk3w
	// revamped resolution code 4/11/07 - jfk3w.
	public Object finalInvoke(Object arg0, Method arg1, Object[] arg2)
			throws Throwable 
	{
		EndpointReferenceType origEPR = getTargetEPR();
		ResolutionContext context = new ResolutionContext(origEPR, (_parentHandler == null));
		_inAttachments = null;

		try
		{
			while (true)
			{
				AxisClientInvocationHandler handler = resolve(context);
				try
				{
					Object ret = handler.doInvoke(arg0, arg1, arg2);
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
			}
		}
		catch (Throwable t) {
			// report last non-resolution error if possible
			if (context.getErrorToReport() == null)
				context.setErrorToReport(t);
			// strip exception down to base error
			while (!(context.getErrorToReport() instanceof RemoteException)) {
				Throwable next = context.getErrorToReport().getCause();
				if (next == null)
					throw context.getErrorToReport();
				context.setErrorToReport(next);
			}

			throw context.getErrorToReport();
		}
		finally
		{
			_outAttachments = null;
		}
	}

	protected Object doInvoke(Object arg0, Method arg1, Object[] arg2)
		throws Throwable
	{
		MethodDescription methodDesc = new MethodDescription(arg1);
		Stub stubInstance = (Stub) _portMethods.get(methodDesc);

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
	
		Object ret = arg1.invoke(stubInstance, arg2);

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
			GenesisIISecurityException, ConfigurationException, RemoteException
	{
		try
		{
			if (resolver.getType() == ResolverDescription.ResolverType.EPI_RESOLVER)
			{
				/* We don't handle EPI resolution yet */
				throw new NameResolutionFailedException();
			}
			else if (resolver.getType() == ResolverDescription.ResolverType.REFERENCE_RESOLVER)
			{
				ReferenceResolver resolverPT = ClientUtils.createProxy(ReferenceResolver.class, resolver.getEPR());
				return resolverPT.resolve(null);
			}
			throw new NameResolutionFailedException();
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
