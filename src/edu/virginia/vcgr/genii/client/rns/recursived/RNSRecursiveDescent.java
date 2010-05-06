package edu.virginia.vcgr.genii.client.rns.recursived;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;

public class RNSRecursiveDescent
{
	static private Log _logger = LogFactory.getLog(
		RNSRecursiveDescentCallback.class);
	
	static public int DEFAULT_ALLOWED_RETRIES = 0;
	static public int DEFAULT_MAXIMUM_DEPTH = -1;
	
	private int _allowedRetries;
	private int _maximumDepth;
	private ICallingContext _callingContext;
	private boolean _reversed;
	private boolean _avoidCycles = true;
	private RNSFilter _filter = null;
	
	private RNSRecursiveDescent(int allowedRetries, int maximumDepth, 
		ICallingContext callingContext, boolean reversed)
	{
		_allowedRetries = allowedRetries;
		_maximumDepth = maximumDepth;
		_callingContext = callingContext;
		_reversed = reversed;
	}
	
	public int allowedRetries()
	{
		return _allowedRetries;
	}
	
	public void setAllowedRetries(int allowedRetries)
	{
		_allowedRetries = allowedRetries;
	}
	
	public boolean avoidCycles()
	{
		return _avoidCycles;
	}
	
	public void setAvoidCycles(boolean avoidCycles)
	{
		_avoidCycles = avoidCycles;
	}
	
	public int maximumDepth()
	{
		return _maximumDepth;
	}
	
	public void setMaximumDepth(int maxDepth)
	{
		_maximumDepth = maxDepth;
	}
	
	public void setRNSFilter(RNSFilter filter)
	{
		_filter = filter;
	}
	
	private RNSRecursiveDescentCallbackResult callCallback(RNSPath path, 
		RNSRecursiveDescentCallback callback)
	{
		if (_filter != null)
		{
			if (!_filter.matches(path))
				return RNSRecursiveDescentCallbackResult.Continue;
		}
		
		try
		{
			return callback.handleRNSPath(path);
		}
		catch (Throwable cause)
		{
			_logger.warn("RNSRecursiveDescentHandler threw exception.", cause);
			return RNSRecursiveDescentCallbackResult.Continue;
		}
	}
	
	private RNSRecursiveDescentCallbackResult descend(Set<URI> visited,
		RNSPath root, RNSRecursiveDescentCallback callback, int depth)
	{
		int attempt = 0;
		EndpointReferenceType epr = null;
		WSName name = null;
		Throwable cause = null;
		
		do
		{
			if (attempt > 0)
			{
				try { Thread.sleep(5000L * (1 << (attempt - 1))); } 
				catch (Throwable c) {}
			}
			
			attempt++;
			
			try
			{
				epr = root.getEndpoint();
				name = new WSName(epr);
				break;
			}
			catch (Throwable c)
			{
				cause = c;
			}
		} while ( (attempt - 1) < _allowedRetries);
		
		if (cause != null)
			return RNSRecursiveDescentCallbackResult.Continue;
		
		if ( (_maximumDepth >= 0) && (depth > _maximumDepth) )
			return RNSRecursiveDescentCallbackResult.ContinueLeaf;
		
		URI uri = name.getEndpointIdentifier();
		if (_avoidCycles && visited.contains(uri))
			return RNSRecursiveDescentCallbackResult.ContinueLeaf;
		
		RNSRecursiveDescentCallbackResult result = 
			RNSRecursiveDescentCallbackResult.Continue;
		
		if (!_reversed)
		{
			result = callCallback(root, callback);
			
			if (result == RNSRecursiveDescentCallbackResult.Halt)
				return result;
		}
			
		if (!(name.isValidWSName() && visited.contains(uri)))
		{
			if (name.isValidWSName() && _avoidCycles)
				visited.add(uri);
			
			if (result == RNSRecursiveDescentCallbackResult.Continue)
			{
				TypeInformation typeInfo = new TypeInformation(epr);
				if (typeInfo.isRNS())
				{			
					Collection<RNSPath> contents = null;
					attempt = 0;
					do
					{
						if (attempt > 0)
						{
							try { Thread.sleep(5000L * (1 << (attempt - 1))); } 
							catch (Throwable c) {}
						}
						
						attempt++;
						try
						{
							contents = root.listContents();
							break;
						}
						catch (Throwable c)
						{
							cause = c;
						}
					} while ( (attempt - 1) < _allowedRetries);
					
					if (cause == null)
					{
						for (RNSPath child : contents)
						{
							result = descend(visited, child, callback, depth + 1);
							if (result == RNSRecursiveDescentCallbackResult.Halt)
								return result;
						}
					}
				}
			}
		}
		
		if (_reversed)
			return callCallback(root, callback);
		
		return RNSRecursiveDescentCallbackResult.Continue;
	}
	
	public void descend(RNSPath root, RNSRecursiveDescentCallback callback)
		throws Throwable
	{
		IContextResolver oldResolver = null;
		
		try
		{
			oldResolver = ContextManager.getResolver();
			ContextManager.setResolver(new MemoryBasedContextResolver(
				_callingContext));
			
			Set<URI> visited = new HashSet<URI>();
			descend(visited, root, callback, 0);
			callback.finish();
		}
		finally
		{
			if (oldResolver != null)
				ContextManager.setResolver(oldResolver);
		}
	}
	
	public void asyncDescend(RNSPath root, 
		RNSRecursiveDescentCallback callback)
	{
		Thread thread = new Thread(
			new AsyncDescender(root, callback));
		thread.setName("Asynchronous RNS Recursive Descender");
		thread.setDaemon(true);
		thread.start();
	}
	
	static public RNSRecursiveDescent createDescent(
		ICallingContext callingContext)
	{
		return new RNSRecursiveDescent(
			DEFAULT_ALLOWED_RETRIES, DEFAULT_MAXIMUM_DEPTH,
			callingContext.deriveNewContext(),
			false);
	}
	
	static public RNSRecursiveDescent createDescent()
		throws FileNotFoundException, IOException
	{
		return createDescent(
			ContextManager.getCurrentContext());
	}
	
	static public RNSRecursiveDescent createReverseDescent(
		ICallingContext callingContext)
	{
		return new RNSRecursiveDescent(
			DEFAULT_ALLOWED_RETRIES, DEFAULT_MAXIMUM_DEPTH,
			callingContext.deriveNewContext(),
			true);
	}
	
	static public RNSRecursiveDescent createReverseDescent()
		throws FileNotFoundException, IOException
	{
		return createReverseDescent(
			ContextManager.getCurrentContext());
	}
		
	private class AsyncDescender implements Runnable
	{
		private RNSPath _root;
		private RNSRecursiveDescentCallback _callback;
		
		private AsyncDescender(RNSPath root, 
			RNSRecursiveDescentCallback callback)
		{
			_root = root;
			_callback = callback;
		}
		
		@Override
		public void run()
		{
			try
			{
				descend(_root, _callback);
			}
			catch (Throwable cause)
			{
				_logger.warn("RNS Recursive Descent Callback threw exception.",
					cause);
			}
		}
	}
}