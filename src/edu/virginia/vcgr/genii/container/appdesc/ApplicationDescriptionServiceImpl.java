package edu.virginia.vcgr.genii.container.appdesc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.appdesc.ApplicationDescriptionPortType;
import edu.virginia.vcgr.genii.appdesc.CreateDeploymentDocumentRequestType;
import edu.virginia.vcgr.genii.appdesc.CreateDeploymentDocumentResponseType;
import edu.virginia.vcgr.genii.appdesc.DeploymentDocumentType;
import edu.virginia.vcgr.genii.appdesc.DeploymentExistsFaultType;
import edu.virginia.vcgr.genii.appdesc.PlatformDescriptionType;
import edu.virginia.vcgr.genii.appdesc.SupportDocumentType;
import edu.virginia.vcgr.genii.byteio.streamable.factory.OpenStreamResponse;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.appdesc.ApplicationDescriptionConstants;
import edu.virginia.vcgr.genii.client.appdesc.ApplicationDescriptionCreator;
import edu.virginia.vcgr.genii.client.appdesc.ApplicationDescriptionUtils;
import edu.virginia.vcgr.genii.client.appdesc.ApplicationVersion;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;

import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.EnhancedRNSServiceImpl;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class ApplicationDescriptionServiceImpl 
	extends EnhancedRNSServiceImpl implements ApplicationDescriptionPortType
{
	static private Log _logger = LogFactory.getLog(
		ApplicationDescriptionServiceImpl.class);
	
	static final public String APPLICATION_DESCRIPTION_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.appdesc.app_desc_property";
	static final public String APPLICATION_VERSION_PROPERTY_NAME =
		"edu.virginia.vcgr.genii.container.appdesc.app_vers_property";
	
	protected void setAttributeHandlers() throws NoSuchMethodException
	{
		super.setAttributeHandlers();
		
		new ApplicationDescriptionAttributeHandler(getAttributePackage());
	}
	
	protected void postCreate(ResourceKey rKey,
		EndpointReferenceType myEPR, HashMap<QName, Object> creationParameters,
		Collection<MessageElement> resolverCreationParams)
		throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, myEPR, creationParameters, resolverCreationParams);
		
		IResource resource = rKey.dereference();
		
		MessageElement elem;
		
		elem = (MessageElement)creationParameters.get(
			ApplicationDescriptionCreator.APPLICATION_NAME_CREATION_PARAMETER);
		if (elem != null)
			resource.setProperty(APPLICATION_DESCRIPTION_PROPERTY_NAME,
				elem.getValue());
		
		elem = (MessageElement)creationParameters.get(
			ApplicationDescriptionCreator.APPLICATION_VERSION_CREATION_PARAMETER);
		if (elem != null)
		{
			String value = elem.getValue();
			if (value != null)
				resource.setProperty(APPLICATION_VERSION_PROPERTY_NAME,
						new ApplicationVersion(elem.getValue()));
		}
	}
	
	public ApplicationDescriptionServiceImpl()
		throws RemoteException
	{
		super("ApplicationDescriptionPortType");
		
		addImplementedPortType(WellKnownPortTypes.APPDESC_PORT_TYPE);
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateDeploymentDocumentResponseType createDeploymentDocument(
			CreateDeploymentDocumentRequestType createDeploymentDocumentRequest)
			throws RemoteException, DeploymentExistsFaultType
	{
		String name = createDeploymentDocumentRequest.getName();
		DeploymentDocumentType deployDoc = 
			createDeploymentDocumentRequest.getDeploymentDocument();
		
		URI deploymentType = 
			ApplicationDescriptionUtils.determineDeploymentType(deployDoc);
		PlatformDescriptionType []platformDesc = 
			deployDoc.getPlatformDescription();
		
		SupportDocumentType supportDoc = new SupportDocumentType(
			platformDesc, null, deploymentType);
		
		CreateFile createFile = new CreateFile(name);
		EndpointReferenceType newFile = null;
		OutputStream bos = null;
		OutputStreamWriter writer = null;
		
		try
		{
			CreateFileResponse response = null;
			response = super.createFile(createFile, 
				new MessageElement[] { new MessageElement(
					ApplicationDescriptionConstants.SUPPORT_DOCUMENT_ATTR_QNAME,
					supportDoc) });
			newFile = response.getEntry_reference();
			bos = ByteIOStreamFactory.createOutputStream(newFile);
			writer = new OutputStreamWriter(bos);
			ObjectSerializer.serialize(writer, deployDoc, new QName(
				"http://vcgr.cs.virginia.edu/genii/application-description",
				"deployment-description"));
			writer.flush();
			
			CreateDeploymentDocumentResponseType ret =
				new CreateDeploymentDocumentResponseType(newFile);
			newFile = null;
			return ret;
		}
		catch (IOException ioe)
		{
			throw new RemoteException(ioe.getLocalizedMessage(), ioe);
		}
		finally
		{
			StreamUtils.close(writer);
			
			if (newFile != null)
			{
				try
				{
					GeniiCommon common = ClientUtils.createProxy(
						GeniiCommon.class, newFile);
					common.destroy(new Destroy());
				}
				catch (Throwable t)
				{
					_logger.error(t);
				}
			}
		}
	}
	
	@RWXMapping(RWXCategory.INHERITED)
	public AddResponse add(Add addRequest) throws RemoteException,
			RNSEntryExistsFaultType, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		if (addRequest == null || addRequest.getEntry_name() == null ||
			addRequest.getEntry_reference() == null)
		{
			throw FaultManipulator.fillInFault(new RNSFaultType(
				null, null, null, null, new BaseFaultTypeDescription[] {
					new BaseFaultTypeDescription(
						"The \"add\" operation is only limitedly supported for this service.")
				}, null, null));
		}
		
		// TODO: For now, we'll allow this, but we should re-examine this
		// later
		
		return super.add(addRequest);
	}

	@RWXMapping(RWXCategory.INHERITED)
	public CreateFileResponse createFile(CreateFile createFileRequest)
			throws RemoteException, RNSEntryExistsFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType,
			RNSFaultType
	{
		throw FaultManipulator.fillInFault(new BaseFaultType(
			null, null, null, null,
			new BaseFaultTypeDescription[] {
				new BaseFaultTypeDescription(
					"Operation \"createFile\" is not supported on this service.")
		    }, null));
	}

	@RWXMapping(RWXCategory.READ)
	public OpenStreamResponse openStream(Object openStreamRequest)
			throws RemoteException, ResourceCreationFaultType,
			ResourceUnknownFaultType
	{
		// TODO Auto-generated method stub
		return null;
	}
}