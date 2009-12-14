package edu.virginia.vcgr.genii.container.gridlog;

import java.rmi.RemoteException;

import org.oasis_open.wsrf.basefaults.BaseFaultType;

import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;

public class GridLogServiceUtils
{
	static public GridLogTargetBundle createLog()
		throws ResourceCreationFaultType, BaseFaultType, RemoteException
	{
		return null;
		/* Temporarily disabling grid logging (Mark Morgan, 1 Dec 09)
		ResourceKey key = null;
		
		try
		{
			String serviceURL = Container.getServiceURL(
				GridLogServiceImpl.SERVICE_NAME);
			
			EndpointReferenceType entryReference = 
				new GridLogServiceImpl().CreateEPR(null, serviceURL);
			key = ResourceManager.getTargetResource(entryReference);
			GridLogResource resource =
				(GridLogResource)key.dereference();
			
			return new GridLogTargetBundle(entryReference,
				new GridLogTarget(serviceURL,
					Container.getContainerID().toString(), resource.getKey()));
		}
		finally
		{
			StreamUtils.close(key);
		}
		*/
	}
}