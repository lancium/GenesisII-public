package edu.virginia.vcgr.genii.container.gridlog;

import java.rmi.RemoteException;

import org.morgan.util.io.StreamUtils;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.gridlog.GridLogTarget;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class GridLogServiceUtils
{
	static public GridLogTargetBundle createLog()
		throws ResourceCreationFaultType, BaseFaultType, RemoteException
	{
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
	}
}