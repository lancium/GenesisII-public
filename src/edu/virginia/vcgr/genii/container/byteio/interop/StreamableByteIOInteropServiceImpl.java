package edu.virginia.vcgr.genii.container.byteio.interop;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.common.GeniiCommon;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.attrs.IAttributeManipulator;
import edu.virginia.vcgr.genii.container.byteio.*;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;

import org.apache.axis.message.MessageElement;
import org.ggf.byteio.CustomFaultType;
import org.ggf.sbyteio.*;
import org.ggf.schemas.byteio._2006._07.interop.*;
import org.morgan.util.io.GuaranteedDirectory;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.*;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rp_2.*;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;


public class StreamableByteIOInteropServiceImpl 
	extends StreamableByteIOServiceImpl
	implements StreamableByteIOPortType {

	public static final String SERVICE_NAME = "StreamableByteIOInteropPortType";
	
	protected static final byte[] DEFAULT_CONTENTS = { 0x30, 0x31, 0x30, 0x32, 0x30,
			0x33, 0x30, 0x34, 0x30, 0x35, 0x30, 0x36, 0x30, 0x37, 0x30, 0x38,
			0x30, 0x39, 0x31, 0x30, 0x31, 0x31, 0x31, 0x32, 0x31, 0x33, 0x31,
			0x34, 0x31, 0x35, 0x31, 0x36, 0x31, 0x37, 0x31, 0x38, 0x31, 0x39,
			0x32, 0x30, 0x32, 0x31, 0x32, 0x32, 0x32, 0x33, 0x32, 0x34, 0x32,
			0x35, 0x32, 0x36, 0x32, 0x37, 0x32, 0x38, 0x32, 0x39, 0x33, 0x30 };

	public StreamableByteIOInteropServiceImpl() throws RemoteException {
		super(SERVICE_NAME);
		addImplementedPortType(WellKnownPortTypes.SBYTEIO_SERVICE_PORT_TYPE);
	}

	protected StreamableByteIOInteropServiceImpl(String serviceName)
			throws RemoteException {
		super(serviceName);
		addImplementedPortType(WellKnownPortTypes.SBYTEIO_SERVICE_PORT_TYPE);
	}

	/**
	 * Overloaded to create resources with the particular interop 
	 * contents
	 */
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
			HashMap<QName, Object> creationParameters,
			Collection<MessageElement> resolverCreationParams)
			throws ResourceException, BaseFaultType, RemoteException {

		//choose file if not chosen
		File file = null;
		File superDir = Container.getConfigurationManager().getUserDirectory();
		String fileAny;
		String destroyAny;
		Boolean mustDelete = null;
		
		fileAny = (String)creationParameters.get(
				new QName(GenesisIIConstants.GENESISII_NS, "file-path"));
		
		//Set File-Path property if null
		if (fileAny == null){
			try{
				superDir = new GuaranteedDirectory(superDir, "sbyteio-data");
				file = File.createTempFile("sbyteio", ".dat", superDir);
				creationParameters.put(
						new QName(GenesisIIConstants.GENESISII_NS, "file-path"),
						file.getAbsolutePath());
			}
			catch (IOException ioe){
				throw new ResourceException(ioe.getLocalizedMessage(), ioe);
			}
		}
		
		destroyAny = (String)creationParameters.get(
				new QName(GenesisIIConstants.GENESISII_NS, "must-destroy"));
		
		//Set destroy file property if null
		if (destroyAny == null){
			mustDelete = true;
			creationParameters.put(
					new QName(GenesisIIConstants.GENESISII_NS, "must-destroy"),
					mustDelete);
		}
		
		super.postCreate(rKey, newEPR, creationParameters, resolverCreationParams);
		
		ISByteIOResource resource = (ISByteIOResource)rKey.dereference();

		synchronized(rKey.getLockObject()) {
			RandomAccessFile raf = null;
			try {
				File myFile = resource.getCurrentFile();
				raf = new RandomAccessFile(myFile, "rw");

				raf.write(DEFAULT_CONTENTS); 
			} catch (IOException ioe) {
				throw FaultManipulator.fillInFault(
					new CustomFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription(ioe.toString())
					}, null));
			} finally {
				StreamUtils.close(raf);
			}
		}
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public void deleteResource(DeleteResource deleteResource)
			throws java.rmi.RemoteException, ResourceUnavailableFaultType, 
			ResourceUnknownFaultType {

		try {
			EndpointReferenceType epr = deleteResource.getEndpointReference();
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, epr);
			common.destroy(new Destroy());
			Thread.sleep(3000);
		
		} catch (Exception e) {
			throw FaultManipulator.fillInFault(
					new CustomFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription(e.toString())
					}, null));
		}
	}


	@RWXMapping(RWXCategory.EXECUTE)
	public CreateResourceResponse createResource(Object createResource)
			throws java.rmi.RemoteException {
		EndpointReferenceType newEpr = vcgrCreate(new VcgrCreate(null)).getEndpoint();
		return new CreateResourceResponse(newEpr);
	}
    	
	@RWXMapping(RWXCategory.READ)
    public QueryResourcePropertiesResponse queryResourceProperties(
    		QueryResourceProperties queryResourcePropertiesRequest) 
    		throws RemoteException, InvalidResourcePropertyQNameFaultType, 
    		InvalidQueryExpressionFaultType, QueryEvaluationErrorFaultType, 
    		ResourceUnavailableFaultType, ResourceUnknownFaultType, 
    		UnknownQueryExpressionDialectFaultType {
    	
		QueryExpressionType fullQuery = queryResourcePropertiesRequest.getQueryExpression();
		MessageElement textQuery[] = fullQuery.get_any();
		
		if (textQuery != null) {
			try{
				String compareQuery = textQuery[0].getAsString();
				if (!(compareQuery.equals("/*/sbyteio:Writeable")))
				{
					throw new UnknownQueryExpressionDialectFaultType();
				}
			}
			catch(Exception e){
				throw new UnknownQueryExpressionDialectFaultType();
			}
			
		}
		
		QName []request = new QName[1];
		request[0] = new QName(
				"http://schemas.ggf.org/byteio/2005/10/streamable-access", 
				"Writeable");
				
		return new QueryResourcePropertiesResponse(getResourceProperties(request));
    }
    
	@RWXMapping(RWXCategory.READ)
	public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(
			QName[] getMultipleResourcePropertiesRequest)
			throws RemoteException, InvalidResourcePropertyQNameFaultType,
			ResourceUnavailableFaultType, ResourceUnknownFaultType {

		return new GetMultipleResourcePropertiesResponse(
				getResourceProperties(getMultipleResourcePropertiesRequest));
	}

	@RWXMapping(RWXCategory.READ)
	public GetResourcePropertyResponse getResourceProperty(
			QName getResourcePropertyRequest) throws RemoteException,
			InvalidResourcePropertyQNameFaultType,
			ResourceUnavailableFaultType, ResourceUnknownFaultType {

		QName []request = new QName[1];
		request[0] = getResourcePropertyRequest;
				
		return new GetResourcePropertyResponse(getResourceProperties(request));	
	}
	
	private MessageElement[] getResourceProperties(
			QName[] getMultipleResourcePropertiesRequest)
			throws RemoteException, InvalidResourcePropertyQNameFaultType,
			ResourceUnavailableFaultType, ResourceUnknownFaultType {
		
		ArrayList<MessageElement> document = new ArrayList<MessageElement>();
		for (QName name : getMultipleResourcePropertiesRequest){
			IAttributeManipulator manipulator =
				getAttributePackage().getManipulator(name);
			
			if (manipulator == null)
				throw FaultManipulator.fillInFault(
					new InvalidResourcePropertyQNameFaultType());
			
			document.addAll(manipulator.getAttributeValues());
		}
		
		MessageElement []ret = new MessageElement[document.size()];
		document.toArray(ret);
		return ret;
	}
	
}
