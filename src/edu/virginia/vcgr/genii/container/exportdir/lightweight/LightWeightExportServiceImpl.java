package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.exportdir.ExportedDirUtils;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.ForkRoot;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkBaseService;
import edu.virginia.vcgr.genii.exportdir.QuitExport;
import edu.virginia.vcgr.genii.exportdir.QuitExportResponse;
import edu.virginia.vcgr.genii.exportdir.lightweight.LightWeightExportPortType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@ForkRoot(LightWeightExportDirFork.class)
public class LightWeightExportServiceImpl extends ResourceForkBaseService
		implements LightWeightExportPortType {
	static private Log _logger = LogFactory
			.getLog(LightWeightExportServiceImpl.class);

	@Override
	protected ResourceKey createResource(GenesisHashMap creationParameters)
			throws ResourceException, BaseFaultType {
		if (_logger.isDebugEnabled())
			_logger.debug("Creating new LightWeightExport Resource.");

		ExportedDirUtils.ExportedDirInitInfo initInfo = ExportedDirUtils
				.extractCreationProperties(creationParameters);

		ResourceKey key = super.createResource(creationParameters);
		key.dereference().setProperty(
				LightWeightExportConstants.ROOT_DIRECTORY_PROPERTY_NAME,
				initInfo.getPath());
		// ensure that local dir to be exported is readable
		// if so, proceed with export creation
		try {
			// check if directory exists
			if (!ExportedDirUtils.dirReadable(initInfo.getPath())) {
				throw FaultManipulator
						.fillInFault(new ResourceCreationFaultType(
								null,
								null,
								null,
								null,
								new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(
										"Target directory "
												+ initInfo.getPath()
												+ " does not exist or is not readable.  "
												+ "Cannot create export from this path.") },
								null));
			}
		} catch (IOException ioe) {
			throw new ResourceException(
					"Could not determine if export localpath is readable.", ioe);
		}

		String svnUser = initInfo.svnUser();
		String svnPass = initInfo.svnPass();
		Long svnRevision = initInfo.svnRevision();

		if (svnUser != null)
			key.dereference().setProperty(
					LightWeightExportConstants.SVN_USER_PROPERTY_NAME, svnUser);

		if (svnPass != null)
			key.dereference().setProperty(
					LightWeightExportConstants.SVN_PASS_PROPERTY_NAME, svnPass);

		if (svnRevision != null)
			key.dereference().setProperty(
					LightWeightExportConstants.SVN_REVISION_PROPERTY_NAME,
					svnRevision);

		return key;
	}

	public LightWeightExportServiceImpl() throws RemoteException {
		super("LightWeightExportPortType");

		addImplementedPortType(WellKnownPortTypes
				.EXPORTED_ROOT_SERVICE_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes
				.EXPORTED_LIGHTWEIGHT_ROOT_SERVICE_PORT_TYPE());
	}

	@Override
	public PortType getFinalWSResourceInterface() {
		return WellKnownPortTypes.EXPORTED_ROOT_SERVICE_PORT_TYPE();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public QuitExportResponse quitExport(QuitExport arg0)
			throws RemoteException, ResourceUnknownFaultType {
		IResource resource = ResourceManager.getCurrentResource().dereference();
		resource.destroy();

		return new QuitExportResponse(true);
	}
}