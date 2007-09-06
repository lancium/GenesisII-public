package edu.virginia.vcgr.genii.container.byteio.interop;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.common.rattrs.AttributeUnknownFaultType;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.container.attrs.IAttributeManipulator;
import edu.virginia.vcgr.genii.container.byteio.*;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.common.GeniiCommon;

import org.morgan.util.io.StreamUtils;

import org.apache.axis.message.MessageElement;
import org.ggf.byteio.*;
import org.ggf.rbyteio.*;
import org.ggf.schemas.byteio._2006._07.interop.*;
import org.oasis_open.docs.wsrf.rp_2.*;
import org.oasis_open.docs.wsrf.r_2.*;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import javax.xml.namespace.QName;

public class RandomByteIOInteropServiceImpl extends RandomByteIOServiceImpl
		implements RandomByteIOInteropPortType {

	public static final String SERVICE_NAME = "RandomByteIOInteropPortType";
	
	protected static final byte[] DEFAULT_CONTENTS = { 0x30, 0x31, 0x30, 0x32, 0x30,
			0x33, 0x30, 0x34, 0x30, 0x35, 0x30, 0x36, 0x30, 0x37, 0x30, 0x38,
			0x30, 0x39, 0x31, 0x30, 0x31, 0x31, 0x31, 0x32, 0x31, 0x33, 0x31,
			0x34, 0x31, 0x35, 0x31, 0x36, 0x31, 0x37, 0x31, 0x38, 0x31, 0x39,
			0x32, 0x30, 0x32, 0x31, 0x32, 0x32, 0x32, 0x33, 0x32, 0x34, 0x32,
			0x35, 0x32, 0x36, 0x32, 0x37, 0x32, 0x38, 0x32, 0x39, 0x33, 0x30 };

	public RandomByteIOInteropServiceImpl() throws RemoteException {
		super(SERVICE_NAME);
		addImplementedPortType(WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE);
	}

	protected RandomByteIOInteropServiceImpl(String serviceName)
			throws RemoteException {
		super(serviceName);
		addImplementedPortType(WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE);
	}

	/**
	 * Overloaded to create resources with particular interop contents
	 */
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
			HashMap<QName, Object> creationParameters)
			throws ResourceException, BaseFaultType, RemoteException {

		super.postCreate(rKey, newEPR, creationParameters);

		IRByteIOResource resource = (IRByteIOResource)rKey.dereference();

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

	public ReadResponse read(Read read) 
		throws RemoteException, CustomFaultType, 
		ReadNotPermittedFaultType, UnsupportedTransferFaultType, 
		ResourceUnknownFaultType {
		return super.read(read);
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public void deleteResource(DeleteResource deleteResource)
			throws java.rmi.RemoteException, ResourceUnavailableFaultType, 
			ResourceUnknownFaultType {

		try {
			EndpointReferenceType epr = deleteResource.getEndpointReference();
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, epr);
			common.immediateTerminate(null);
			Thread.sleep(3000);
		} catch (Exception e) {
			throw FaultManipulator.fillInFault(
					new CustomFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription(e.toString())
					}, 
					null));
		}
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public CreateResourceResponse createResource(CreateResource createResource)
			throws java.rmi.RemoteException {
		EndpointReferenceType newEpr = vcgrCreate(new VcgrCreate(null)).getEndpoint();
		return new CreateResourceResponse(newEpr);
	}

	@RWXMapping(RWXCategory.READ)
	public org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse queryResourceProperties(
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
				if (!(compareQuery.equals("/*/rbyteio:ModificationTime"))){
					throw new UnknownQueryExpressionDialectFaultType();
				}
			}
			catch(Exception e){
				throw new UnknownQueryExpressionDialectFaultType();
			}
		}
		
		QName []request = new QName[1];
		request[0] = new QName(
				"http://schemas.ggf.org/byteio/2005/10/random-access", 
				"ModificationTime");
				
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
					new AttributeUnknownFaultType());
			
			document.addAll(manipulator.getAttributeValues());
		}
		
		MessageElement []ret = new MessageElement[document.size()];
		document.toArray(ret);
		return ret;
	}

}
