package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rcreate.ResourceCreator;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;

public class CreateResourceTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Creates a new resource using generic creation mechanisms.";
	static private final String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/create-resource-usage.txt";

	private boolean _url = false;
	private GeniiPath _constructionProperties = null;
	private String _shortDescription = null;
	
	public CreateResourceTool()
	{
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), false);
	}
	
	@Option({"url"})
	public void setUrl()
	{
		_url = true;
	}
	
	@Option({"construction-properties"})
	public void setConstruction_properties(String xmlFile)
	{
		_constructionProperties = new GeniiPath(xmlFile);
	}
	
	@Option({"description"})
	public void setDescription(String shortDescription)
	{
		_shortDescription = shortDescription;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		EndpointReferenceType epr;
		String serviceLocation = getArgument(0);
		String targetName = getArgument(1);
		ConstructionParameters cParams = getConstructionProperties();
		
		GeniiPath target = (targetName == null) ? null : new GeniiPath(targetName);
		
		if (_url)
			epr = createFromURLService(serviceLocation, target,
				cParams, _shortDescription);
		else
			epr = createFromRNSService(new GeniiPath(serviceLocation), target,
				cParams, _shortDescription);
		
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
	
	private ConstructionParameters getConstructionProperties()
		throws IOException, JAXBException
	{
		InputStream in = null;
		
		if (_constructionProperties != null)
		{
			try
			{
				in = _constructionProperties.openInputStream();
				return ConstructionParameters.deserializeConstructionParameters(in);
			}
			finally
			{
				StreamUtils.close(in);
			}
		}
		
		return new ConstructionParameters();
	}
	
	static public EndpointReferenceType createFromRNSService(
		GeniiPath rnsPath, 
		GeniiPath optTarget, ConstructionParameters cParams,
		String shortDescription)
		throws IOException, RNSException, CreationException,
			InvalidToolUsageException
	{
		RNSPath path = lookup(rnsPath);
		return createInstance(path.getEndpoint(), optTarget, 
			cParams, shortDescription);
	}
	
	static public EndpointReferenceType createFromURLService(
		String url, GeniiPath optTargetName, ConstructionParameters cParams,
		String shortDescription)
			throws ResourceException,
				ResourceCreationFaultType, RemoteException, RNSException,
				CreationException, InvalidToolUsageException
	{
		return createInstance(EPRUtils.makeEPR(url), optTargetName,
			cParams, shortDescription);
	}
	
	static public EndpointReferenceType createInstance(
		EndpointReferenceType service,
		GeniiPath optTargetName,
		MessageElement [] createProperties) 
		throws ResourceException,
			ResourceCreationFaultType, RemoteException, RNSException, 
			CreationException, InvalidToolUsageException
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
		EndpointReferenceType service, GeniiPath optTargetName,
		ConstructionParameters cParams, String shortDescription)
		throws ResourceException,
			ResourceCreationFaultType, RemoteException, RNSException, 
			CreationException, InvalidToolUsageException
	{
		if (shortDescription != null)
			cParams.humanName(shortDescription);
	
		MessageElement me = cParams.serializeToMessageElement();
		
		return createInstance(service, optTargetName,
			new MessageElement[] { me });
	}
}