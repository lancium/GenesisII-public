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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.context.ContextType;

public class ContextManager
{
	static private final String _CONTEXT_RESOLVER_NAME = "context-resolver";
	
	static private class ResolverThreadLocal 
		extends InheritableThreadLocal<IContextResolver>
	{
		@Override
		protected IContextResolver childValue(IContextResolver parentValue)
		{
			if (parentValue == null)
				return null;
			
			return (IContextResolver)parentValue.clone();
		}	
	}
	
	static private ResolverThreadLocal _resolver =
		new ResolverThreadLocal();
	
	static public ICallingContext getCurrentContext()
		throws FileNotFoundException, IOException
	{
		return getCurrentContext(true);
	}
	
	static public ICallingContext getCurrentContext(boolean mustExist)
		throws FileNotFoundException, IOException
	{
		ICallingContext ctxt = getResolver().load();
		if (ctxt == null && mustExist)
		{
			throw new ConfigurationException("Unable to locate calling context information.");
		}
		
		return ctxt;
	}
	
	static public void storeCurrentContext(ICallingContext context)
		throws FileNotFoundException, IOException
	{
		getResolver().store(context);
	}
	
	static public ICallingContext bootstrap(RNSPath root) throws IOException
	{
		ICallingContext bootContext = new CallingContextImpl(root);
		
		// we may have a dummy context that contains login information necesary to boot
		ICallingContext current = getCurrentContext(false);
		if (current != null) {
			ContextType t = bootContext.getSerialized();
			return current.deriveNewContext(t);
		}
		
		return bootContext;
	}
	
	synchronized static public IContextResolver getResolver()
	{
		IContextResolver resolver = _resolver.get();
		if (resolver == null)
		{
			resolver = 
				(IContextResolver)NamedInstances.getRoleBasedInstances().lookup(
					_CONTEXT_RESOLVER_NAME);
			
			if (_resolver == null)
				throw new ConfigurationException(
					"Unable to locate a \"" + _CONTEXT_RESOLVER_NAME + 
					"\" resolver in the config file.");
			
			_resolver.set(resolver);
		}
		
		return resolver;
	}
	
	static public void setResolver(IContextResolver resolver)
	{
		_resolver.set(resolver);
	}
}
