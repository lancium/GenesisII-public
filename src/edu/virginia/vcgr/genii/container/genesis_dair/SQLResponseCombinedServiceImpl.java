package edu.virginia.vcgr.genii.container.genesis_dair;

import java.rmi.RemoteException;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.container.rns.EnhancedRNSServiceImpl;
import edu.virginia.vcgr.genii.genesis_dai.DataResourceAddressType;
import edu.virginia.vcgr.genii.genesis_dai.DataResourceUnavailableFaultType;
import edu.virginia.vcgr.genii.genesis_dai.GetDataResourcePropertyDocumentRequest;
import edu.virginia.vcgr.genii.genesis_dai.InvalidDatasetFormatFaultType;
import edu.virginia.vcgr.genii.genesis_dai.InvalidResourceNameFaultType;
import edu.virginia.vcgr.genii.genesis_dai.NotAuthorizedFaultType;
import edu.virginia.vcgr.genii.genesis_dai.ServiceBusyFaultType;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLCommunicationAreaRequest;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLOutputParameterRequest;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLResponseItemRequest;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLResponseItemResponse;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLResponsePropertyDocumentResponse;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLReturnValueRequest;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLReturnValueResponse;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLRowsetFactoryRequest;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLRowsetRequest;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLRowsetResponse;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLUpdateCountRequest;
import edu.virginia.vcgr.genii.genesis_dair.InvalidCountFaultType;
import edu.virginia.vcgr.genii.genesis_dair.InvalidPositionFaultType;
import edu.virginia.vcgr.genii.genesis_dair.SQLCommunicationsAreaType;
import edu.virginia.vcgr.genii.genesis_dair.SQLOutputParameterType;
import edu.virginia.vcgr.genii.genesis_dair.SQLResponseCombinedPortType;


public class SQLResponseCombinedServiceImpl extends EnhancedRNSServiceImpl
	implements SQLResponseCombinedPortType {

	
	public SQLResponseCombinedServiceImpl() throws RemoteException 
	{
		super("SQLResponseCombinedPortType");
		addImplementedPortType(WellKnownPortTypes.GENESIS_DAIR_SQL_RESPONSE_COMBINED_PORT_TYPE);
	}
	
	protected SQLResponseCombinedServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);
		addImplementedPortType(WellKnownPortTypes.GENESIS_DAIR_SQL_RESPONSE_COMBINED_PORT_TYPE);
	}

	@Override
	public SQLCommunicationsAreaType[] getSQLCommunicationArea(
			GetSQLCommunicationAreaRequest arg0) throws RemoteException,
			InvalidResourceNameFaultType, ServiceBusyFaultType,
			InvalidCountFaultType, DataResourceUnavailableFaultType,
			InvalidPositionFaultType, NotAuthorizedFaultType {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SQLOutputParameterType[] getSQLOutputParameter(
			GetSQLOutputParameterRequest arg0) throws RemoteException,
			InvalidResourceNameFaultType, ServiceBusyFaultType,
			InvalidCountFaultType, DataResourceUnavailableFaultType,
			InvalidPositionFaultType, NotAuthorizedFaultType {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * SQLResponseItem: A structure holding information about a response item resulting 
	 * from the execution of a SQL request. There will be an instance of this structure
	 * for each response item returned.
	 * 
	 * so it's like metadata for every response
	 */
	public GetSQLResponseItemResponse getSQLResponseItem(
			GetSQLResponseItemRequest arg0) throws RemoteException,
			InvalidResourceNameFaultType, ServiceBusyFaultType,
			InvalidCountFaultType, DataResourceUnavailableFaultType,
			InvalidPositionFaultType, NotAuthorizedFaultType,
			InvalidDatasetFormatFaultType {

		return null;
	}

	/**
	 * SQLResponsePropertyDocument: A structure that describes a SQLResponse data resource,
	 * combining the properties defined in the DAI specification with those defined in DAIR
	 */
	@Override
	public GetSQLResponsePropertyDocumentResponse getSQLResponsePropertyDocument(
			GetDataResourcePropertyDocumentRequest getSQLResponsePropertyDocumentRequest)
			throws RemoteException, InvalidResourceNameFaultType,
			ServiceBusyFaultType, DataResourceUnavailableFaultType,
			NotAuthorizedFaultType {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetSQLReturnValueResponse getSQLReturnValue(
			GetSQLReturnValueRequest arg0) throws RemoteException,
			InvalidResourceNameFaultType, ServiceBusyFaultType,
			InvalidCountFaultType, DataResourceUnavailableFaultType,
			InvalidPositionFaultType, NotAuthorizedFaultType {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetSQLRowsetResponse getSQLRowset(GetSQLRowsetRequest arg0)
			throws RemoteException, InvalidResourceNameFaultType,
			ServiceBusyFaultType, InvalidCountFaultType,
			DataResourceUnavailableFaultType, InvalidPositionFaultType,
			NotAuthorizedFaultType, InvalidDatasetFormatFaultType {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getSQLUpdateCount(GetSQLUpdateCountRequest arg0)
			throws RemoteException, InvalidResourceNameFaultType,
			ServiceBusyFaultType, InvalidCountFaultType,
			DataResourceUnavailableFaultType, InvalidPositionFaultType,
			NotAuthorizedFaultType {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataResourceAddressType[] getSQLRowsetFactory(
			GetSQLRowsetFactoryRequest arg0) throws RemoteException,
			InvalidResourceNameFaultType, ServiceBusyFaultType,
			InvalidCountFaultType, DataResourceUnavailableFaultType,
			InvalidPositionFaultType, NotAuthorizedFaultType {
		// TODO Auto-generated method stub
		return null;
	}

	
}
