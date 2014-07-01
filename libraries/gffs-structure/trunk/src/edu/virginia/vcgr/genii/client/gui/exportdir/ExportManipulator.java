package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.tools.ExportTool;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;

public class ExportManipulator
{
	static private Log _logger = LogFactory.getLog(ExportManipulator.class);

	static public RNSPath createExport(String containerPath, String localPath, String rnsPath, boolean isLightweight)
		throws FileNotFoundException, ExportException, RNSException, CreationException, ResourceCreationFaultType,
		RemoteException, IOException, InvalidToolUsageException
	{

		RNSPath cpath, targetpath, servicepath;
		targetpath = RNSPath.getCurrent();
		cpath = RNSPath.getCurrent();
		servicepath = cpath;
		try {
			// First check that the target rns path is ok
			try {
				targetpath = RNSPath.getCurrent().lookup(rnsPath, RNSPathQueryFlags.MUST_NOT_EXIST);
			} catch (RNSPathAlreadyExistsException r) {
				throw new ResourceException("Path " + targetpath.toString() + " already exists.");
			} catch (Exception r) {
				throw new ResourceException("Problem with ensuring target RNS path does not already exist.");
			}
			// Ok it does not exist, now we need to make sure the
			// base part of the path exists

			if (!targetpath.getParent().exists()) {
				// Need to try again!
				throw new ResourceException("Path " + targetpath.toString() + " MUST exist.");
			}

			// Now check to make sure that the container info is OK
			cpath = cpath.lookup(containerPath);
			servicepath = cpath;
			String service = "";
			try {
				if (isLightweight) {
					service = "LightWeightExportPortType";
				} else {
					service = "ExportedDirPortType";
				}
				servicepath = cpath.lookup("Services/" + service, RNSPathQueryFlags.MUST_EXIST);
			} catch (RNSPathDoesNotExistException r) {
				throw new ResourceException("There is no Services/" + service + " on the container " + cpath.toString());
			} catch (Exception r) {
				throw new ResourceException("Problem with ensuring that there is a LightWeightExportPortType service.");
			}

		} catch (Exception r) {
			throw new ResourceException("exception during export checking.", r);
		}
		// Now we have both the container path and the target path.
		// hmmm: need to add in the owners of the export!
		ArrayList<String> owners = null;
		try {
			EndpointReferenceType exEPR =
				ExportTool.createExportedRoot(targetpath.toString(), servicepath.getEndpoint(), localPath, "", "", 0L,
					targetpath.toString(), false, owners);
			if (exEPR == null)
				_logger.debug("created null EPR with createExportedRoot");
		} catch (Exception r) {
			throw new ResourceException("exception during export root creation.", r);
		}
		return RNSPath.getCurrent().lookup(rnsPath, RNSPathQueryFlags.MUST_EXIST);
	}

	/*
	 * static public RNSPath createExport(URL containerURL, File localPath, String rnsPath, boolean
	 * isLightweight) throws FileNotFoundException, ExportException, RNSException,
	 * CreationException, ResourceCreationFaultType, RemoteException, IOException,
	 * InvalidToolUsageException { validate(localPath);
	 * 
	 * RNSPath target = RNSPath.getCurrent().lookup(rnsPath, RNSPathQueryFlags.MUST_NOT_EXIST);
	 * validate(target);
	 * 
	 * ExportTool.createExportedRoot( rnsPath, EPRUtils.makeEPR(containerURL.toString() +
	 * "/axis/services/" + (isLightweight ? "LightWeightExportPortType" : "ExportedRootPortType")),
	 * localPath.getAbsolutePath(), null, null, null, rnsPath, false); return
	 * RNSPath.getCurrent().lookup(rnsPath, RNSPathQueryFlags.MUST_EXIST); }
	 */

	static public void validate(File localPath) throws FileNotFoundException, ExportException
	{
		if (!localPath.exists())
			throw new FileNotFoundException("Couldn't find source directory \"" + localPath.getAbsolutePath() + "\".");
		if (!localPath.isDirectory())
			throw new ExportException("Cannot export \"" + localPath.getAbsolutePath() + "\" because it is not a directory.");
	}

	static public void validate(RNSPath rnsPath) throws ExportException, RNSPathDoesNotExistException
	{
		RNSPath parent = rnsPath.getParent();
		if (!parent.exists())
			throw new ExportException("Cannot create export because target RNS path \"" + parent.pwd() + "\" does not exist.");
		if (!(new TypeInformation(parent.getEndpoint())).isRNS())
			throw new ExportException("RNS path \"" + parent.pwd() + "\" is not an RNS capable endpoint.");
	}

	static public void quitExport(RNSPath exportRoot) throws RNSException, IOException
	{
		ExportTool.quitExportedRoot(exportRoot.getEndpoint(), false);
		exportRoot.unlink();
	}
}
