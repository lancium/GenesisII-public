package edu.virginia.vcgr.genii.container.exportdir.fsproxy;

import java.rmi.RemoteException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.common.ConstructionParametersType;
import edu.virginia.vcgr.genii.client.exportdir.FSProxyConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.ForkRoot;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkBaseService;
import edu.virginia.vcgr.genii.exportdir.QuitExport;
import edu.virginia.vcgr.genii.exportdir.QuitExportResponse;
import edu.virginia.vcgr.genii.exportdir.fsproxy.FSProxyPortType;
import edu.virginia.vcgr.genii.security.RWXCategory;

@ForkRoot(FSProxyDirFork.class)
@ConstructionParametersType(FSProxyConstructionParameters.class)
public class FSProxyServiceImpl extends ResourceForkBaseService
	implements FSProxyPortType
{
	static final public String SERVICE_NAME = "FSProxyPortType";
	
	public FSProxyServiceImpl() throws RemoteException
	{
		super(SERVICE_NAME);

		addImplementedPortType(WellKnownPortTypes.EXPORTED_FSPROXY_ROOT_SERVICE_PORT_TYPE);
	}

	@Override
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.EXPORTED_FSPROXY_ROOT_SERVICE_PORT_TYPE;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public QuitExportResponse quitExport(QuitExport arg0)
			throws RemoteException, ResourceUnknownFaultType
	{
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		resource.destroy();
		
		return new QuitExportResponse(true);
	}
}