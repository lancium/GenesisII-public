package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileNotFoundException;

import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;

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
		throws RNSPathDoesNotExistException, RNSException,
			FileNotFoundException
	{

		return RNSUtilities.findService("/containers/BootstrapContainer",
			"EnhancedRNSPortType", 
			new PortType[] { RNSConstants.RNS_PORT_TYPE }, new GeniiPath(path).path()).getEndpoint();
	}
	
	private int runECatcher()
		throws Exception
	{
		boolean createParents = false;
		EndpointReferenceType service = null;
	
		if (_rnsService != null)
		{
			GeniiPath gPath = new GeniiPath(_rnsService);
			if(gPath.pathType() != GeniiPathType.Grid)
				throw new InvalidToolUsageException("RNSService must be a grid path. ");
			service = lookupPath(_rnsService);
		}
		
		ICallingContext ctxt = ContextManager.getCurrentContext();
		
		if (_parents)
			createParents = true;
		
		RNSPath path = ctxt.getCurrentPath();
		for (String sPath : getArguments())
		{
			GeniiPath gPath = new GeniiPath(sPath);
			if(gPath.exists())
				throw new RNSPathAlreadyExistsException(gPath.path());
			if(gPath.pathType() == GeniiPathType.Grid)
			{
				RNSPath newDir = lookup(gPath, 
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
				
					GeniiCommon common = ClientUtils.createProxy(
							GeniiCommon.class, service);
					EndpointReferenceType newEPR = common.vcgrCreate(
							new VcgrCreate(null)).getEndpoint();
					try
					{
						path.link(newEPR);
						newEPR = null;
					}
					finally
					{
						if (newEPR != null)
						{
							common = ClientUtils.createProxy(
									GeniiCommon.class, newEPR);
							common.destroy(new Destroy());
						}
					}
				}
			}
			else
			{
				File newFile = new File(gPath.path());
				if(createParents)
				{
					if(!newFile.mkdirs())
					{
						stderr.println("Could not create directory " + gPath.path());
						return 1;
					}
				}
				else
					if(!newFile.mkdir())
					{
						stderr.println("Could not create directory " + gPath.path());
						return 1;
					}
			}
		}
	return 0;
	}
}