package edu.virginia.vcgr.genii.container.exportdir.lightweight;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.ExportControl.ModeAllowance;
import edu.virginia.vcgr.genii.client.ExportControlsList;
import edu.virginia.vcgr.genii.client.ExportProperties;
import edu.virginia.vcgr.genii.client.ExportProperties.ExportMechanisms;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.exportdir.ExportedDirUtils;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.PreferredIdentity;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.container.axis.ServerWSDoAllReceiver;
import edu.virginia.vcgr.genii.container.exportdir.GffsExportConfiguration;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.SudoExportUtils;
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
public class LightWeightExportServiceImpl extends ResourceForkBaseService implements LightWeightExportPortType
{
	static private Log _logger = LogFactory.getLog(LightWeightExportServiceImpl.class);

	/**
	 * checks whether the user's current credentials from the calling context is sufficient to make them an admin on the current resource port
	 * type. this can be checked during export creation on the parent resource, *before* we create a new resource, so we can decide whether to
	 * let the user force a particular credential as the export owner.
	 */
	private boolean testForAdministrativeRights()
	{
		try {
			IResource resource = ResourceManager.getCurrentResource().dereference();
			if (_logger.isDebugEnabled())
				_logger.debug("pre-checking for admin access on: " + resource.getClass().getCanonicalName());
			return ServerWSDoAllReceiver.checkAccess(resource, RWXCategory.WRITE);
		} catch (Exception e) {
			if (_logger.isDebugEnabled()) {
				String msg = "exception caught while testing for admin rights on export port type.";
				_logger.debug(msg, e);
			}
			return false;
		}
	}

	@Override
	protected ResourceKey createResource(GenesisHashMap creationParameters) throws ResourceException, BaseFaultType
	{
		if (_logger.isDebugEnabled())
			_logger.debug("Creating new LightWeightExport Resource.");

		ExportedDirUtils.ExportedDirInitInfo initInfo = ExportedDirUtils.extractCreationProperties(creationParameters);

		boolean hasAdminAccess = false;

		// we set the owner info for the resource before we need it.
		String ownerDN = initInfo.getPrimaryOwnerDN();
		boolean forcing = false;

		if (ownerDN == null) {
			/*
			 * they didn't give us any hints about which identity to use as the owner/creator of the export, so we'll go with the current
			 * preferred identity.
			 */
			PreferredIdentity prefId = PreferredIdentity.getCurrent();
			if (prefId != null) {
				_logger.debug("found a preferred id for the export owner as: " + prefId);
				ownerDN = prefId.getIdentityString();
			} else {
				// well, we got nothing. the owner dn stays null.
				_logger.debug("export did not find any preferred id in the context!");
			}
		} else {
			/*
			 * check if the user creating the export wants the owner / creator of the export to be set forcibly. this only works if they are
			 * admin of the port (i.e. they have write permission on it, not just execute).
			 */
			if (ownerDN.contains("force:")) {
				ownerDN = ownerDN.substring(6);
				_logger.debug("found force flag for owner DN: " + ownerDN);
				forcing = true;
			}
		}

		ICallingContext context;
		try {
			context = ContextManager.getCurrentContext();
		} catch (Exception e) {
			String msg = "could not load calling context to inspect credentials during export.";
			_logger.error(msg);
			throw new ResourceException(msg);
		}

		if (forcing) {
			hasAdminAccess = testForAdministrativeRights();
			if (hasAdminAccess) {
				/*
				 * they passed the check as an admin. ownerDN will stay at what it is already set to...
				 */
				_logger.debug("ownerDN set by 'force' to: '" + ownerDN + "'");
			} else {
				// no go, hoser. you don't have the rights to force the owner.
				throw new ResourceException("permission was denied for WRITE access as admin on export port type; cannot force ownership.");
			}
		} else {
			// need to look the supposed creator up.
			X509Certificate owner = GffsExportConfiguration.findPreferredIdentityServerSide(context, ownerDN);
			if (owner != null) {
				ownerDN = PreferredIdentity.getDnString(owner);
			}
			if (_logger.isDebugEnabled())
				_logger.debug("export ownerDN resolved to: '" + ownerDN + "'");
		}

		// retrieve the owner as a unix user if possible.
		String owningUnixUser = SudoExportUtils.doGridMapping(ownerDN);

		// make sure the user is set properly if we need to know it.
		if (ExportProperties.getExportProperties().getExportMechanism().equals(ExportMechanisms.EXPORT_MECH_PROXYIO)
			|| ExportProperties.getExportProperties().getExportMechanism().equals(ExportMechanisms.EXPORT_MECH_ACLANDCHOWN)) {
			if (owningUnixUser == null) {
				// not having a unix user is fatal for these types of export creations.
				String msg = "Failed to discover Unix user in the grid-mapfile for this export, owner DN was: " + ownerDN;
				_logger.error(msg);
				throw new ResourceException(msg);
			}

			// additionally we need to check the path for proxyio type exports.
			if (ExportProperties.getExportProperties().getExportMechanism().equals(ExportMechanisms.EXPORT_MECH_PROXYIO)) {
				// check that the path is allowed by our current set of restrictions.
				ExportControlsList ecl = ExportControlsList.getExportControlsList();
				Set<ModeAllowance> modes;
				/*
				 * future: how to pick the right modes for the export based on the export's read-only or read-write nature? currently
				 * impossible since exports are just exports without a sense of read-only. need to add this feature in future.
				 */
				modes = ModeAllowance.getReadWriteMode();
				if (!ecl.checkCreationOkay(initInfo.getPath(), owningUnixUser, modes)) {
					String msg =
						"cannot create export; this path is blocked for user '" + owningUnixUser + "' by export controls: "
							+ initInfo.getPath();
					_logger.error(msg);
					throw new ResourceException(msg);
				}
			}
		}

		// finally, after all the checks above, create the resource.
		ResourceKey key = super.createResource(creationParameters);
		// save the starting path for the export.
		key.dereference().setProperty(LightWeightExportConstants.ROOT_DIRECTORY_PROPERTY_NAME, initInfo.getPath());

		// record the type of export that this is based on, to avoid hosing ourselves if container
		// mode changes later.
		key.dereference().setProperty(LightWeightExportConstants.EXPORT_MECHANISM,
			ExportProperties.getExportProperties().getExportMechanism().toString());
		if (_logger.isDebugEnabled())
			_logger.debug("setting export's creation mode to: " + key.dereference().getProperty(LightWeightExportConstants.EXPORT_MECHANISM));

		// now use the fruits of our efforts on calculating the user name, if there were any.
		if (owningUnixUser != null) {
			key.dereference().setProperty(LightWeightExportConstants.EXPORT_OWNER_UNIX_NAME, owningUnixUser);
			_logger.info("export owner unix user resolved as: '" + owningUnixUser + "'");
		}

		/*
		 * ensure that local dir to be exported is readable. if so, proceed with export creation.
		 */
		try {
			// check if directory exists.
			ExportMechanisms exportType = ExportProperties.getExportProperties().getExportMechanism();
			if (exportType == ExportMechanisms.EXPORT_MECH_PROXYIO) {
				// test whether this is an OS where we support proxyio exports.
				boolean isCompatibleOS = false;
				if (!OperatingSystemType.isWindows()) {
					isCompatibleOS = true;
				}
				if (!isCompatibleOS) {
					throw FaultManipulator.fillInFault(new ResourceCreationFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Sudo export " + "unsupported on this OS ("
							+ OperatingSystemType.getOpsysName() + ")") }, null));
				}

				if (!SudoExportUtils.dirReadable(initInfo.getPath(), key)) {
					throw FaultManipulator.fillInFault(new ResourceCreationFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Target directory " + initInfo.getPath()
							+ " does not exist or is not readable.  " + "Cannot create export from this path.") }, null));
				}
			} else if ((exportType == null) || exportType.equals(ExportMechanisms.EXPORT_MECH_ACL)
				|| exportType.equals(ExportMechanisms.EXPORT_MECH_ACLANDCHOWN)) {
				// handle ACL and ACLANDCHOWN type exports.
				if (!ExportedDirUtils.dirReadable(initInfo.getPath())) {
					throw FaultManipulator.fillInFault(new ResourceCreationFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Target directory " + initInfo.getPath()
							+ " does not exist or is not readable.  " + "Cannot create export from this path.") }, null));
				}
			} else {
				throw new ResourceException("unknown or unimplemented type of export mechanism: " + exportType);
			}

		} catch (IOException ioe) {
			throw new ResourceException("Could not determine if export localpath is readable.", ioe);
		}

		String svnUser = initInfo.svnUser();
		String svnPass = initInfo.svnPass();
		Long svnRevision = initInfo.svnRevision();

		if (svnUser != null)
			key.dereference().setProperty(LightWeightExportConstants.SVN_USER_PROPERTY_NAME, svnUser);

		if (svnPass != null)
			key.dereference().setProperty(LightWeightExportConstants.SVN_PASS_PROPERTY_NAME, svnPass);

		if (svnRevision != null)
			key.dereference().setProperty(LightWeightExportConstants.SVN_REVISION_PROPERTY_NAME, svnRevision);

		return key;
	}

	public LightWeightExportServiceImpl() throws RemoteException
	{
		super("LightWeightExportPortType");
		addImplementedPortType(WellKnownPortTypes.EXPORTED_ROOT_SERVICE_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.EXPORTED_LIGHTWEIGHT_ROOT_SERVICE_PORT_TYPE());
	}

	@Override
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.EXPORTED_ROOT_SERVICE_PORT_TYPE();
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