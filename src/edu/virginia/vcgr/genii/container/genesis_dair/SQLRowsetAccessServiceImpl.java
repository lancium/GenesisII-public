package edu.virginia.vcgr.genii.container.genesis_dair;

import java.rmi.RemoteException;

import edu.virginia.vcgr.genii.container.rns.EnhancedRNSServiceImpl;
import edu.virginia.vcgr.genii.genesis_dai.DataResourceUnavailableFaultType;
import edu.virginia.vcgr.genii.genesis_dai.GetDataResourcePropertyDocumentRequest;
import edu.virginia.vcgr.genii.genesis_dai.InvalidDatasetFormatFaultType;
import edu.virginia.vcgr.genii.genesis_dai.InvalidResourceNameFaultType;
import edu.virginia.vcgr.genii.genesis_dai.NotAuthorizedFaultType;
import edu.virginia.vcgr.genii.genesis_dai.ServiceBusyFaultType;
import edu.virginia.vcgr.genii.genesis_dair.GetTuplesRequest;
import edu.virginia.vcgr.genii.genesis_dair.GetTuplesResponse;
import edu.virginia.vcgr.genii.genesis_dair.InvalidCountFaultType;
import edu.virginia.vcgr.genii.genesis_dair.InvalidGetTuplesRequestFaultType;
import edu.virginia.vcgr.genii.genesis_dair.InvalidPositionFaultType;
import edu.virginia.vcgr.genii.genesis_dair.SQLRowsetAccessPortType;
import edu.virginia.vcgr.genii.genesis_dair.SQLRowsetPropertyDocumentType;


public class SQLRowsetAccessServiceImpl extends EnhancedRNSServiceImpl 
	 implements SQLRowsetAccessPortType {
	
	public SQLRowsetAccessServiceImpl ()throws RemoteException
	{
		super("SQLRowsetAccessPortType");
	}

	protected SQLRowsetAccessServiceImpl(String serviceName)
			throws RemoteException {
		super(serviceName);
	}

	@Override
	public SQLRowsetPropertyDocumentType getSQLRowsetPropertyDocument(
			GetDataResourcePropertyDocumentRequest getSQLRowsetPropertyDocumentRequest)
			throws RemoteException, InvalidResourceNameFaultType,
			ServiceBusyFaultType, DataResourceUnavailableFaultType,
			NotAuthorizedFaultType {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GetTuplesResponse getTuples(GetTuplesRequest getTuplesRequest)
			throws RemoteException, InvalidResourceNameFaultType,
			ServiceBusyFaultType, InvalidCountFaultType,
			DataResourceUnavailableFaultType, InvalidPositionFaultType,
			InvalidGetTuplesRequestFaultType, NotAuthorizedFaultType,
			InvalidDatasetFormatFaultType {
		// TODO Auto-generated method stub
		return null;
	}

}
