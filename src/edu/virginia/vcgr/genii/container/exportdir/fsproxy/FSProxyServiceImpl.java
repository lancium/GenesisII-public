package edu.virginia.vcgr.genii.container.exportdir.fsproxy;

import java.rmi.RemoteException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.common.ConstructionParametersType;
import edu.virginia.vcgr.genii.client.exportdir.FSProxyConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.ForkRoot;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkBaseService;
import edu.virginia.vcgr.genii.exportdir.QuitExport;
import edu.virginia.vcgr.genii.exportdir.QuitExportResponse;
import edu.virginia.vcgr.genii.exportdir.fsproxy.FSProxyPortType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@ForkRoot(FSProxyDirFork.class)
@ConstructionParametersType(FSProxyConstructionParameters.class)
public class FSProxyServiceImpl extends ResourceForkBaseService implements FSProxyPortType
{
	static final public String SERVICE_NAME = "FSProxyPortType";
	
	// 2020-12-1 by ASG
	// keyInEPR is intended as a replacement for instanceof(GeniiNoOutcalls) which was a bit hacky.
	// If it is "true", we will not put key material in the X.509. This will in turn prevent delegation to instances
	// of a type that returns true, and will make transporting and storing EPR's consume MUCH less space.
	public boolean keyInEPR() {
		return false;
	}

	public FSProxyServiceImpl() throws RemoteException
	{
		super(SERVICE_NAME);

		addImplementedPortType(WellKnownPortTypes.EXPORTED_FSPROXY_ROOT_SERVICE_PORT_TYPE());
	}

	@Override
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.EXPORTED_FSPROXY_ROOT_SERVICE_PORT_TYPE();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public QuitExportResponse quitExport(QuitExport arg0) throws RemoteException, ResourceUnknownFaultType
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		resource.destroy();

		return new QuitExportResponse(true);
	}
}