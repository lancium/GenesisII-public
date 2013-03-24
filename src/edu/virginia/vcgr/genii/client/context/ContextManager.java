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
package edu.virginia.vcgr.genii.client.context;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.identity.Identity;

public class ContextManager
{
	static private final String _CONTEXT_RESOLVER_NAME = "context-resolver";
	static private Log _logger = LogFactory.getLog(ContextManager.class);

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
	static public ICallingContext getExistingContext() throws FileNotFoundException, IOException
	{
		ICallingContext ctxt = getCurrentContext();
		if (ctxt == null) {
			throw new ConfigurationException("Unable to locate calling context information.");
		}
		return ctxt;
	}

	// this function locates the current calling context, which could actually be null.
	static public ICallingContext getCurrentContext() throws FileNotFoundException, IOException
	{
		return getResolver().load();
	}

	static public void storeCurrentContext(ICallingContext context) throws FileNotFoundException, IOException
	{
		getResolver().store(context);
	}

	static public ICallingContext bootstrap(RNSPath root) throws IOException
	{
		ICallingContext bootContext = new CallingContextImpl(root);

		// we may have a dummy context that contains login information necesary to boot
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
				throw new ConfigurationException("Unable to locate a \"" + _CONTEXT_RESOLVER_NAME
					+ "\" resolver in the config file.");

			_resolver.set(resolver);
		}

		return resolver;
	}

	static public void setResolver(IContextResolver resolver)
	{
		_resolver.set(resolver);
	}

	static public Closeable temporarilyAssumeContext(ICallingContext context)
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

	static public boolean isGood(ICallingContext ctxt)
	{
		try {
			SecurityUpdateResults secResults = new SecurityUpdateResults();
			Date validUntil = new Date();
			validUntil.setTime(validUntil.getTime() + 1000L);
			ClientUtils.checkAndRenewCredentials(ctxt, validUntil, secResults);
			if (secResults.removedCredentials().size() > 0)
				return false;
			Collection<Identity> identities = SecurityUtils.getCallerIdentities(ctxt);
			if (identities == null || identities.size() == 0)
				return false;

			return true;
		} catch (Throwable cause) {
			_logger.warn("Got an exception trying to check validity of calling " + "context -- marking it bad.", cause);
		}

		return false;
	}
}
