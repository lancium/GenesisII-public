package edu.virginia.vcgr.genii.client.cmd.tools;

import org.ggf.rns.RNSPortType;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class MkdirTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Creates the directory(s) indicated.";
	static private final String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/mkdir-usage.txt";
	
	private boolean _parents = false;
	private String _rnsService = null;
	
	public MkdirTool()
	{
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), false);
	}
	
	public void setParents()
	{
		_parents = true;
	}
	
	public void setP()
	{
		setParents();
	}
	
	public void setRns_service(String service)
	{
		_rnsService = service;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		return runECatcher();
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException();
	}
	
	private EndpointReferenceType lookupPath(String path)
		throws ConfigurationException, RNSPathDoesNotExistException, RNSException
	{
		RNSPath current = RNSPath.getCurrent();
		return current.lookup(path, RNSPathQueryFlags.MUST_EXIST).getEndpoint();
	}
	
	private int runECatcher()
		throws Exception
	{
		boolean createParents = false;
		EndpointReferenceType service = null;
	
		if (_rnsService != null)
			service = lookupPath(_rnsService);
		
		ICallingContext ctxt = ContextManager.getCurrentContext();
		
		if (_parents)
			createParents = true;
		
		RNSPath path = ctxt.getCurrentPath();
		for (String sPath : getArguments())
		{
			RNSPath newDir = path.lookup(sPath, 
				RNSPathQueryFlags.MUST_NOT_EXIST);
			
			path = newDir;
			
			if (service == null)
			{
				if (createParents)
					path.mkdirs();
				else
					path.mkdir();
			} else
			{
				RNSPath parent = path.getParent();
				
				if (!parent.exists())
				{
					stderr.println("Can't create directory \"" + path.pwd() 
						+ "\".");
					return 1;
				}
				
				TypeInformation typeInfo = new TypeInformation(parent.getEndpoint());
				if (!typeInfo.isRNS())
				{
					stderr.println("\"" + parent.pwd() + 
						"\" is not a directory.");
					return 1;
				}
				
				RNSPortType rpt = ClientUtils.createProxy(
					RNSPortType.class, service);
				path.link(rpt.add(null).getEntry_reference());
			}
		}
		
		return 0;
	}
}