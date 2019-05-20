/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.genii.client.context;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.algorithm.application.ProgramTools;
import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.security.KeystoreManager;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.identity.Identity;

public class ContextManager
{
	static private final String _CONTEXT_RESOLVER_NAME = "context-resolver";
	static private Log _logger = LogFactory.getLog(ContextManager.class);
	
	
	static private final int MAX_IDENTITIES  = 200;
	static private final int LIFETIME = 1000*60*60*12; // 12 hours
	/* Added May 9, 2019 by ASG
	 * _idMap holds a set of credentials from login sessions
	 */
	static private TimedOutLRUCache<String,ICallingContext>	_idMap = new TimedOutLRUCache<String, ICallingContext>(MAX_IDENTITIES,LIFETIME, "IdentityCache" );

	/* Added May 9, 2019 by ASG
	 * stash and grab store and retrieve working contexts that contain security context information. They are here now so the client
	 * can rapidly change identities.
	 */
	synchronized static public void stash(String nonce, ICallingContext val){
		_idMap.put(nonce, val);
	}
	
	synchronized static public ICallingContext grab(String nonce) {
		return _idMap.get(nonce);
	}
	

	
	static private class ResolverThreadLocal extends InheritableThreadLocal<IContextResolver>
	{
		@Override
		protected IContextResolver childValue(IContextResolver parentValue)
		{
			if (parentValue == null)
				return null;

			return (IContextResolver) parentValue.clone();
		}
	}

	static private ResolverThreadLocal _resolver = new ResolverThreadLocal();

	// this function requires that the context has already been established.
	synchronized static public ICallingContext getExistingContext() throws FileNotFoundException, IOException
	{
		ICallingContext ctxt = getCurrentContext();
		if (ctxt == null) {
			throw new ConfigurationException("Unable to locate calling context information.");
		}
		return ctxt;
	}

	/*
	 * future: !!! context loading functions could be smarter! they could cache the context in memory, and if there is a change in the file,
	 * only then reload it.
	 */

	// this function locates the current calling context, which could actually be null.
	synchronized static public ICallingContext getCurrentContext() throws FileNotFoundException, IOException
	{
		IContextResolver res = getResolver();
		// _logger.debug("type of resolver here is: " + res.getClass().getCanonicalName());
		return res.resolveContext();
	}

	// this version guarantees that there will be a usable calling context if there is not already one.
	synchronized static public ICallingContext getCurrentOrMakeNewContext() throws FileNotFoundException, IOException
	{
		IContextResolver res = getResolver();
		// _logger.debug("type of resolver here is: " + res.getClass().getCanonicalName());
		ICallingContext toReturn = res.resolveContext();
		if (toReturn == null) {
			toReturn = new CallingContextImpl(new ContextType());
			ContextManager.storeCurrentContext(toReturn);
		}
		return toReturn;
	}

	synchronized static public void storeCurrentContext(ICallingContext context) throws FileNotFoundException, IOException
	{
		if (ConfigurationManager.getCurrentConfiguration().isServerRole()) {
			_logger
				.error("saving calling context on server side; do we really want to???  backtrace:\n" + ProgramTools.showLastFewOnStack(15));
		}

		getResolver().storeCurrentContext(context);
	}

	synchronized static public ICallingContext bootstrap(RNSPath root) throws IOException
	{
		ICallingContext bootContext = new CallingContextImpl(root);

		// we may have a dummy context that contains login information necessary to boot
		ICallingContext current = getCurrentContext();
		if (current != null) {
			ContextType t = bootContext.getSerialized();
			return current.deriveNewContext(t);
		}

		return bootContext;
	}

	synchronized static public IContextResolver getResolver()
	{
		IContextResolver resolver = _resolver.get();
		if (resolver == null) {
			resolver = (IContextResolver) NamedInstances.getRoleBasedInstances().lookup(_CONTEXT_RESOLVER_NAME);

			if (_resolver == null)
				throw new ConfigurationException("Unable to locate a \"" + _CONTEXT_RESOLVER_NAME + "\" resolver in the config file.");

			_resolver.set(resolver);
		}

		return resolver;
	}

	synchronized static public void setResolver(IContextResolver resolver)
	{
		_resolver.set(resolver);
	}

	synchronized static public Closeable temporarilyAssumeContext(ICallingContext context)
	{
		IContextResolver oldResolver = ContextManager.getResolver();
		ContextManager.setResolver(new MemoryBasedContextResolver(context));
		return new AssumedContextState(oldResolver);
	}

	static private class AssumedContextState implements Closeable
	{
		private IContextResolver _oldResolver;

		private AssumedContextState(IContextResolver oldResolver)
		{

			_oldResolver = oldResolver;
		}

		@Override
		public void close()
		{
			ContextManager.setResolver(_oldResolver);
		}
	}

	synchronized static public boolean isGood(ICallingContext ctxt)
	{
		try {
			SecurityUpdateResults secResults = new SecurityUpdateResults();
			ClientUtils.checkAndRenewCredentials(ctxt, BaseGridTool.credsValidUntil(), secResults);
			if (secResults.removedCredentials().size() > 0)
				return false;
			Collection<Identity> identities = KeystoreManager.getCallerIdentities(ctxt);
			if (identities == null || identities.size() == 0)
				return false;

			return true;
		} catch (Throwable cause) {
			_logger.warn("Got an exception trying to check validity of calling " + "context -- marking it bad.", cause);
		}

		return false;
	}
}
