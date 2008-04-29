package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rcreate.ResourceCreator;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;

public class CreateResourceTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Creates a new resource using generic creation mechanisms.";
	static private final String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/create-resource-usage.txt";

	private boolean _url = false;
	
	public CreateResourceTool()
	{
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), false);
	}
	
	public void setRns()
	{
	}
	
	public void setUrl()
	{
		_url = true;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		EndpointReferenceType epr;
		String serviceLocation = getArgument(0);
		String targetName = getArgument(1);
		
		if (_url)
			epr = createFromURLService(serviceLocation, targetName);
		else
			epr = createFromRNSService(serviceLocation, targetName);
		
		if (targetName == null)
			stdout.println(ObjectSerializer.toString(epr,
				new QName(GenesisIIConstants.GENESISII_NS, "endpoint")));
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if (numArgs < 1 || numArgs > 2)
			throw new InvalidToolUsageException();
	}
	
	static public EndpointReferenceType createFromRNSService(String rnsPath, 
		String optTargetName) throws IOException, ConfigurationException, 
			RNSException, CreationException
	{
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(rnsPath, RNSPathQueryFlags.MUST_EXIST);
		return createInstance(path.getEndpoint(), optTargetName);
	}
	
	static public EndpointReferenceType createFromURLService(
		String url, String optTargetName)
			throws ConfigurationException, ResourceException,
				ResourceCreationFaultType, RemoteException, RNSException,
				CreationException
	{
		return createInstance(EPRUtils.makeEPR(url), optTargetName);
	}
	
	static public EndpointReferenceType createInstance(
		EndpointReferenceType service,
		String optTargetName,
		MessageElement [] createProperties) 
		throws ConfigurationException, ResourceException,
			ResourceCreationFaultType, RemoteException, RNSException, 
			CreationException
	{
		EndpointReferenceType epr = ResourceCreator.createNewResource(
			service, createProperties, null);
		
		if (optTargetName != null)
		{
			try
			{
				LnTool.link(epr, optTargetName);
			}
			catch (RNSException re)
			{
				ResourceCreator.terminate(epr);
				throw re;
			}
		}
		
		return epr;
	}
	
	static public EndpointReferenceType createInstance(
		EndpointReferenceType service,
		String optTargetName) throws ConfigurationException, ResourceException,
			ResourceCreationFaultType, RemoteException, RNSException, 
			CreationException
	{
		return createInstance(service, optTargetName, null);
	}
}