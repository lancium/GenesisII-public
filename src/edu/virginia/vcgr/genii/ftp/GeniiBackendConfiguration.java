package edu.virginia.vcgr.genii.ftp;

import java.io.IOException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class GeniiBackendConfiguration implements Cloneable
{
	private ICallingContext _callingContext;
	private RNSPath _root;
	
	private GeniiBackendConfiguration(ICallingContext callingContext, RNSPath root)
	{
		_callingContext = callingContext;
		_root = root;
	}
	
	public GeniiBackendConfiguration(ICallingContext callingContext)
	{
		_callingContext = callingContext.deriveNewContext();
		_root = _callingContext.getCurrentPath().getRoot();
	}
	
	public GeniiBackendConfiguration()
		throws IOException, ConfigurationException
	{
		this(ContextManager.getCurrentContext());
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
		return new GeniiBackendConfiguration(_callingContext.deriveNewContext(), _root);
	}
}
