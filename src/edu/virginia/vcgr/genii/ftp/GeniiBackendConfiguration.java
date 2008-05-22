package edu.virginia.vcgr.genii.ftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

import edu.virginia.vcgr.genii.client.cmd.tools.GamlLoginTool;
import edu.virginia.vcgr.genii.client.cmd.tools.LogoutTool;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.InMemoryContextResolver;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class GeniiBackendConfiguration implements Cloneable
{
	private ICallingContext _callingContext;
	private RNSPath _root;
	
	private GeniiBackendConfiguration(ICallingContext callingContext, RNSPath root)
		throws IOException
	{
		_callingContext = callingContext;
		_root = root;
		
		// This always gets called in a new thread, so...
		ContextManager.setResolver(new InMemoryContextResolver());
		ContextManager.storeCurrentContext(_callingContext);
	}
	
	public GeniiBackendConfiguration(BufferedReader stdin,
		PrintStream stdout, PrintStream stderr, ICallingContext callingContext)
		throws Throwable
	{
		IContextResolver oldResolver = ContextManager.getResolver();
		try
		{
			IContextResolver newResolver = new InMemoryContextResolver();
			ContextManager.setResolver(newResolver);
	
			newResolver.store(callingContext);
			_callingContext = newResolver.load();
			
			_root = _callingContext.getCurrentPath().getRoot();
			_callingContext.setCurrentPath(_root);
			
			LogoutTool logout = new LogoutTool();
			logout.setAll();
			logout.run(stdout, stderr, stdin);
			
			GamlLoginTool login = new GamlLoginTool();
			login.run(stdout, stderr, stdin);
			_callingContext = newResolver.load();
		}
		finally
		{
			ContextManager.setResolver(oldResolver);
		}
	}
	
	public GeniiBackendConfiguration(BufferedReader stdin,
		PrintStream stdout, PrintStream stderr)
		throws Throwable
	{
		this(stdin, stdout, stderr, ContextManager.getCurrentContext());
	}
	
	public void setSandboxPath(String sandboxPath)
		throws RNSException
	{
		_root = _root.lookup(sandboxPath, RNSPathQueryFlags.MUST_EXIST);
		_callingContext.setCurrentPath(_root);
	}
	
	public RNSPath getRoot()
	{
		return _root;
	}
	
	public ICallingContext getCallingContext()
	{
		return _callingContext;
	}
	
	public Object clone()
	{
		try
		{
			return new GeniiBackendConfiguration(_callingContext.deriveNewContext(), _root);
		}
		catch (IOException ioe)
		{
			throw new RuntimeException("Unexpected internal exception.", ioe);
		}
	}
}
