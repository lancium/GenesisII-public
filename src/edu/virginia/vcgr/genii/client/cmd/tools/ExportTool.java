package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.exportdir.ExportedDirUtils;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.gui.exportdir.ExportDirDialog;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.exportdir.ExportedRootPortType;
import edu.virginia.vcgr.genii.exportdir.QuitExport;
import edu.virginia.vcgr.genii.exportdir.QuitExportResponse;

public class ExportTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Creates a new exported root directory or quits an existing one.";
	static final private String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/export-usage.txt";
	
	private boolean _create = false;
	private boolean _quit = false;
	private boolean _url = false;
	
	public ExportTool()
	{
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), false);
	}
	
	public void setCreate()
	{
		_create = true;
	}
	
	public void setQuit()
	{
		_quit = true;
	}
	
	public void setUrl()
	{
		_url = true;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		int numArgs = numArguments();
		if (_create)
		{
			EndpointReferenceType exportServiceEPR;
			String serviceLocation = getArgument(0);
			/* get EPR for target exported root service */
			if (_url)
				exportServiceEPR = EPRUtils.makeEPR(serviceLocation);
			else
			{
				RNSPath path = RNSPath.getCurrent();
				path = path.lookup(serviceLocation, 
					RNSPathQueryFlags.MUST_EXIST);
				exportServiceEPR = path.getEndpoint();
			}

			/* get local directory path to be exported */
			String localPath = getArgument(1);
			String targetRNSName = null;
			if (numArgs == 3)
			{
				/* get rns path for exported root */
				targetRNSName = getArgument(2);
			}
			EndpointReferenceType epr = createExportedRoot(
				exportServiceEPR, localPath, targetRNSName);

			if (targetRNSName == null)
			{
				stdout.println(ObjectSerializer.toString(epr,
					new QName(GenesisIIConstants.GENESISII_NS, "endpoint")));
			}
			
			return 0;
		} else if (_quit)
		{
			String exportedRootLocation = getArgument(0);
			/* get EPR for target export service that will create exported root */
			if (_url)
				quitExportedRootFromURL(exportedRootLocation);
			else
				quitExportedRootFromRNS(exportedRootLocation);
			stdout.println("Exported root stopped successfully");
			return 0;
		} else
		{
			ExportDirDialog dialog = new ExportDirDialog();
			dialog.pack();
			GuiUtils.centerComponent(dialog);
			dialog.setVisible(true);
			return 0;
		}
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		
		if (_create)
		{
			if (_quit)
				throw new InvalidToolUsageException(
					"Only one of the options create or " +
					"quit can be specified.");
			
			if (numArgs < 2 || numArgs > 3)
				throw new InvalidToolUsageException();
		} else if (_quit)
		{
			if (numArgs != 1)
				throw new InvalidToolUsageException();
		} else
		{
			if (numArgs != 0)
				throw new InvalidToolUsageException();
		}
	}
	
	static public EndpointReferenceType createExportedRoot(
			EndpointReferenceType exportServiceEPR, String localPath, String RNSPath) 
		throws ConfigurationException, ResourceException,
			ResourceCreationFaultType, RemoteException, RNSException,
			CreationException
	{
		MessageElement[] createProps = ExportedDirUtils.createCreationProperties(
			localPath, "");
		return CreateResourceTool.createInstance(exportServiceEPR, RNSPath, createProps);
	}
	
	static public boolean quitExportedRootFromRNS(String exportedRootRNSPath) 
		throws IOException, ConfigurationException, RNSException
	{
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(exportedRootRNSPath, RNSPathQueryFlags.MUST_EXIST);
		boolean ret = quitExportedRoot(path.getEndpoint(), false);
		
		/* remove from RNS */
		path.unlink();
		
		return ret;
	}
	
	static public boolean quitExportedRootFromURL(String exportedRootURL) 
		throws IOException, ConfigurationException, RNSException
	{
		return quitExportedRoot(EPRUtils.makeEPR(exportedRootURL), false);
	}
	
	static public boolean quitExportedRoot(EndpointReferenceType epr, boolean deleteDirectory)
		throws IOException, ConfigurationException, RNSException
	{
		ExportedRootPortType exportedRoot = ClientUtils.createProxy(ExportedRootPortType.class, epr);
		QuitExport request = new QuitExport();
		request.setDelete_directory(deleteDirectory);
		QuitExportResponse response = exportedRoot.quitExport(request);
		return response.isSuccess();
	}
}