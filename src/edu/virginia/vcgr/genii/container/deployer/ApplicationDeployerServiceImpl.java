package edu.virginia.vcgr.genii.container.deployer;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType;
import edu.virginia.vcgr.genii.appdesc.PlatformDescriptionType;
import edu.virginia.vcgr.genii.appdesc.SupportDocumentType;
import edu.virginia.vcgr.genii.appdesc.bin.BinDeploymentType;
import edu.virginia.vcgr.genii.appdesc.zipjar.ZipJarDeploymentType;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.appdesc.ApplicationDescriptionUtils;
import edu.virginia.vcgr.genii.client.appdesc.DeploymentException;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.sysinfo.SystemUtils;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.deployer.bin.BinDeploymentProvider;
import edu.virginia.vcgr.genii.container.deployer.zipjar.ZipJarDeploymentProvider;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.deployer.ApplicationDeployerPortType;
import edu.virginia.vcgr.genii.deployer.CreateDeploymentRequestType;
import edu.virginia.vcgr.genii.deployer.CreateDeploymentResponseType;
import edu.virginia.vcgr.genii.deployer.ReifyJSDLRequestType;
import edu.virginia.vcgr.genii.deployer.ReifyJSDLResponseType;

public class ApplicationDeployerServiceImpl extends GenesisIIBase implements
		ApplicationDeployerPortType
{
	static private Log _logger = LogFactory.getLog(
		ApplicationDeployerServiceImpl.class);

	static private QName DEPLOYMENT_CONSTRUCTION_PARAM =
		new QName(WellKnownPortTypes.DEPLOYER_PORT_TYPE.getQName().getNamespaceURI(),
			"deployment");
	
	static private final String _DEPLOYMENT_PROPERTY =
		"edu.virginia.vcgr.genii.container.deployer.deployment-property";
	
	protected void setAttributeHandlers() throws NoSuchMethodException
	{
		super.setAttributeHandlers();
		
		new ApplicationDeployerAttributeHandler(getAttributePackage());
	}
	
	
	public ApplicationDeployerServiceImpl()
		throws RemoteException
	{
		super("ApplicationDeployerPortType");
		
		addImplementedPortType(WellKnownPortTypes.DEPLOYER_PORT_TYPE);
	}

	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.DEPLOYER_PORT_TYPE;
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateDeploymentResponseType createDeployment(
		CreateDeploymentRequestType createDeploymentRequest) 
			throws RemoteException
	{
		VcgrCreate create = new VcgrCreate(
			new MessageElement[] {
				new MessageElement(DEPLOYMENT_CONSTRUCTION_PARAM,
					createDeploymentRequest.getDeploymentDescription())
			});
		VcgrCreateResponse resp = vcgrCreate(create);
		return new CreateDeploymentResponseType(resp.getEndpoint());
	}
	
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
		HashMap<QName, Object> constructionParameters,
		Collection<MessageElement> resolverCreationParams) 
		throws ResourceException, BaseFaultType
	{
		IDeployment deployment = null;
		
		MessageElement eprElem = (MessageElement)constructionParameters.get(
			DEPLOYMENT_CONSTRUCTION_PARAM);
		if (eprElem == null)
			throw new ResourceException(
				"Unable to create deployment -- " +
				"application deployment description EPR not set.");
		EndpointReferenceType depDescEPR = 
			ObjectDeserializer.toObject(eprElem, EndpointReferenceType.class);
		DeploymentDocumentType deployDoc;
		
		InputStream bin = null;
		try
		{
			bin = ByteIOStreamFactory.createInputStream(depDescEPR);
			deployDoc = (DeploymentDocumentType)ObjectDeserializer.deserialize(
				new InputSource(bin), DeploymentDocumentType.class);

			MessageElement []any = deployDoc.get_any();
			if (any == null || any.length != 1)
				throw new RemoteException("Deployment document not recognized.");
			
			QName name = any[0].getQName();
			if (name == null)
				throw new RemoteException("Deployment document not recognized.");
			
			if (name.equals(
				ApplicationDescriptionUtils.ZIPJAR_DEPLOYMENT_ELEMENT_QNAME))
			{
				deployment = createZipJarDeployment(
					depDescEPR,
					ObjectDeserializer.toObject(
						any[0], ZipJarDeploymentType.class));
				
			} else if (name.equals(
				ApplicationDescriptionUtils.BINARY_DEPLOYMENT_ELEMENT_QNAME))
			{
				deployment = createBinaryDeployment(
					depDescEPR,
					ObjectDeserializer.toObject(
						any[0], BinDeploymentType.class));
			} else
			{
				throw new RemoteException("Deployment document not recognized.");
			}	
			
			rKey.dereference().setProperty(_DEPLOYMENT_PROPERTY, deployment);
			rKey.dereference().commit();
		}
		catch (ResourceException re)
		{
			throw re;
		}
		catch (BaseFaultType bft)
		{
			throw bft;
		}
		catch (Exception e)
		{
			_logger.error("Unknown exception occurred while deploying.", e);
			
			throw FaultManipulator.fillInFault(new BaseFaultType(
				null, null, null, null, new BaseFaultTypeDescription[] {
					new BaseFaultTypeDescription(e.getLocalizedMessage())
				}, null));
		}
		finally
		{
			StreamUtils.close(bin);
		}
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public ReifyJSDLResponseType reifyJSDL(
		ReifyJSDLRequestType reifyJSDLRequest) throws RemoteException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		IDeployment deployment = (IDeployment)resource.getProperty(
			_DEPLOYMENT_PROPERTY);
		if (deployment == null)
			throw new RemoteException(
				"Unable to reify JSDL -- no deployment information stored.");

		return new ReifyJSDLResponseType(
			deployment.reifyJSDL(reifyJSDLRequest.getUnreifiedDocument()));
	}
	
	@SuppressWarnings("unused")
	static private SupportDocumentType[] determineLocalSupport()
	{
		PlatformDescriptionType []platformDescriptions =
			determineSupportedPlatforms();
		
		return new SupportDocumentType[]
		   {
				new SupportDocumentType(platformDescriptions, null,
					ApplicationDescriptionUtils.DEPLOYMENT_TYPE_BINARY),
				new SupportDocumentType(platformDescriptions, null,
					ApplicationDescriptionUtils.DEPLOYMENT_TYPE_ZIPJAR)
		   };
	}
	
	static public PlatformDescriptionType[] determineSupportedPlatforms()
	{
		return new PlatformDescriptionType[] {
			new PlatformDescriptionType(
				SystemUtils.getSupportedArchitectures(),
				SystemUtils.getSupportedOperatingSystems(),
				null)
		};
	}
	
	static private IDeployment createZipJarDeployment(
		EndpointReferenceType deployDescEPR,
		ZipJarDeploymentType deploymentDescription)
		throws DeploymentException
	{
		String deploymentID = new WSName(
			deployDescEPR).getEndpointIdentifier().toString();
		ZipJarDeploymentProvider provider = new ZipJarDeploymentProvider(
			deployDescEPR, deploymentDescription);
		DeploySnapshot snapshot;
		
		snapshot = provider.getSnapshot();
		
		return DeploymentManager.createDeployment(
			deploymentID, provider, snapshot);
	}
	
	static private IDeployment createBinaryDeployment(
		EndpointReferenceType deployDescEPR,
		BinDeploymentType deploymentDescription)
		throws DeploymentException
	{
		String deploymentID = new WSName(
			deployDescEPR).getEndpointIdentifier().toString();
		BinDeploymentProvider provider = new BinDeploymentProvider(
			deployDescEPR, deploymentDescription);
		DeploySnapshot snapshot;
		
		snapshot = provider.getSnapshot();
		
		return DeploymentManager.createDeployment(
			deploymentID, provider, snapshot);
	}
}