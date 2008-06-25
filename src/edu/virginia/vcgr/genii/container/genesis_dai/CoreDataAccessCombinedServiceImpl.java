package edu.virginia.vcgr.genii.container.genesis_dai;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;


import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;


import edu.virginia.vcgr.genii.container.rns.EnhancedRNSServiceImpl;
import edu.virginia.vcgr.genii.genesis_dai.CoreDataAccessCombinedPortType;
import edu.virginia.vcgr.genii.genesis_dai.DataResourceAddressType;
import edu.virginia.vcgr.genii.genesis_dai.DataResourceUnavailableFaultType;
import edu.virginia.vcgr.genii.genesis_dai.DestroyDataResourceRequest;
import edu.virginia.vcgr.genii.genesis_dai.DestroyDataResourceResponse;
import edu.virginia.vcgr.genii.genesis_dai.GenericQueryRequest;
import edu.virginia.vcgr.genii.genesis_dai.GenericQueryResponse;
import edu.virginia.vcgr.genii.genesis_dai.GetDataResourcePropertyDocumentRequest;
import edu.virginia.vcgr.genii.genesis_dai.GetResourceListRequest;
import edu.virginia.vcgr.genii.genesis_dai.InvalidDatasetFormatFaultType;
import edu.virginia.vcgr.genii.genesis_dai.InvalidExpressionFaultType;
import edu.virginia.vcgr.genii.genesis_dai.InvalidLanguageFaultType;
import edu.virginia.vcgr.genii.genesis_dai.InvalidResourceNameFaultType;
import edu.virginia.vcgr.genii.genesis_dai.NotAuthorizedFaultType;
import edu.virginia.vcgr.genii.genesis_dai.PropertyDocumentType;
import edu.virginia.vcgr.genii.genesis_dai.ResolveRequest;
import edu.virginia.vcgr.genii.genesis_dai.ServiceBusyFaultType;

public class CoreDataAccessCombinedServiceImpl extends EnhancedRNSServiceImpl implements 
	CoreDataAccessCombinedPortType {
	
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(CoreDataAccessCombinedServiceImpl.class);

	public CoreDataAccessCombinedServiceImpl() throws RemoteException 
	{
		super("CoreDataAccessCombinedPortType");
		addImplementedPortType(WellKnownPortTypes.GENESIS_DAI_CORE_DATA_ACCESS_COMBINED_PORT_TYPE);
	}
	
	protected CoreDataAccessCombinedServiceImpl(String serviceName)
			throws RemoteException {
		super(serviceName);
		addImplementedPortType(WellKnownPortTypes.GENESIS_DAI_CORE_DATA_ACCESS_COMBINED_PORT_TYPE);
	}

	@Override
	public DataResourceAddressType[] getResosurceList(
			GetResourceListRequest getResourceListRequest)
			throws RemoteException, ServiceBusyFaultType,
			NotAuthorizedFaultType {
		return null;
	}

	@Override
	public DataResourceAddressType[] resolve(ResolveRequest resolveRequest)
			throws RemoteException, InvalidResourceNameFaultType,
			ServiceBusyFaultType, NotAuthorizedFaultType {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DestroyDataResourceResponse destroyDataResource(
			DestroyDataResourceRequest destroyDataResourceRequest)
			throws RemoteException, InvalidResourceNameFaultType,
			ServiceBusyFaultType, DataResourceUnavailableFaultType,
			NotAuthorizedFaultType {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenericQueryResponse genericQuery(GenericQueryRequest arg0)
			throws RemoteException, InvalidResourceNameFaultType,
			InvalidExpressionFaultType, ServiceBusyFaultType,
			DataResourceUnavailableFaultType, InvalidLanguageFaultType,
			NotAuthorizedFaultType, InvalidDatasetFormatFaultType {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PropertyDocumentType[] getDataResourcePropertyDocument(
			GetDataResourcePropertyDocumentRequest getDataResourcePropertyDocumentRequest)
			throws RemoteException, InvalidResourceNameFaultType,
			ServiceBusyFaultType, DataResourceUnavailableFaultType,
			NotAuthorizedFaultType {
		/*
		DatabaseConnectionPool connectionPool = null;
		Connection conn = null;
		try 
		{
			conn = connectionPool.acquire();
		} 
		catch (SQLException e) {_logger.warn(e.getLocalizedMessage(), e);}
		
		PropertyDocumentType propertyDocument = new PropertyDocumentType();
		
		DataResource myResource = new DataResource(getDataResourcePropertyDocumentRequest.toString(),
				null, conn);
		propertyDocument = myResource.getDataResourcePropertyDocument();

		PropertyDocumentType [] propertyDocimentToReturn = new PropertyDocumentType[1];
		propertyDocimentToReturn[0] = propertyDocument;
		return propertyDocimentToReturn; */
		return null;
	}

	@RWXMapping(RWXCategory.INHERITED)
	public CreateFileResponse createFile(CreateFile createFile) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("\"createFile\" not applicable.");
	}
	
}
