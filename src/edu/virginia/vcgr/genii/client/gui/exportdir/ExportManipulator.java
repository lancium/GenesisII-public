package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.ExportException;

import edu.virginia.vcgr.genii.client.cmd.tools.ExportTool;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;

public class ExportManipulator
{
	static public RNSPath createExport(
		URL containerURL, File localPath, String rnsPath,
		boolean isLightweight)
			throws FileNotFoundException, ExportException,
				RNSException, CreationException,
				ResourceCreationFaultType, RemoteException, IOException
	{
		validate(localPath);
			
		RNSPath target = RNSPath.getCurrent().lookup(rnsPath, RNSPathQueryFlags.MUST_NOT_EXIST);
		validate(target);
		
		ExportTool.createExportedRoot(rnsPath, EPRUtils.makeEPR(containerURL.toString() 
			+ "/axis/services/" + 
				(isLightweight ? "LightWeightExportPortType" : "ExportedRootPortType")),
			localPath.getAbsolutePath(), rnsPath, false);
		return RNSPath.getCurrent().lookup(rnsPath, RNSPathQueryFlags.MUST_EXIST);
	}
	
	static public void validate(File localPath)
		throws FileNotFoundException, ExportException
	{
		if (!localPath.exists())
			throw new FileNotFoundException("Couldn't find source directory \"" + 
				localPath.getAbsolutePath() + "\".");
		if (!localPath.isDirectory())
			throw new ExportException("Cannot export \"" + localPath.getAbsolutePath() 
				+ "\" because it is not a directory.");
	}
	
	static public void validate(RNSPath rnsPath)
		throws ExportException, RNSPathDoesNotExistException
	{
		RNSPath parent = rnsPath.getParent();
		if (!parent.exists())
			throw new ExportException("Cannot create export because target RNS path \"" +
				parent.pwd() + "\" does not exist.");
		if (!(new TypeInformation(parent.getEndpoint())).isRNS())
			throw new ExportException("RNS path \"" + parent.pwd() + "\" is not an RNS capable endpoint.");
	}
	
	static public void quitExport(
		RNSPath exportRoot) throws RNSException, IOException
	{
		ExportTool.quitExportedRoot(exportRoot.getEndpoint(), false);
		exportRoot.unlink();
	}
}