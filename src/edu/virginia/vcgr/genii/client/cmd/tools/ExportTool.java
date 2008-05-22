package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.exportdir.ExportedDirUtils;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.gui.exportdir.ExportDirDialog;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rcreate.ResourceCreator;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.exportdir.ExportedRootPortType;
import edu.virginia.vcgr.genii.exportdir.QuitExport;
import edu.virginia.vcgr.genii.exportdir.QuitExportResponse;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;

public class ExportTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Creates a new exported root directory or quits an existing one.";
	static final private String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/export-usage.txt";
	
	private boolean _create = false;
	private boolean _quit = false;
	private boolean _url = false;
	private boolean _replicate = false;
	
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
	
	public void setReplicate()
	{
		_replicate = true;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		int numArgs = numArguments();
		if (_create)
		{
			/* get rns path for exported root and ensure dne*/			
			String targetRNSName = null;
			if (numArgs == 3){
				targetRNSName = getArgument(2);
				
				//ensure location does not already exist
				ensureTargetDNE(targetRNSName);
			}
			
			EndpointReferenceType exportServiceEPR;
			String serviceLocation = getArgument(0);
			/* get EPR for target exported root service */
			if (_url)
				exportServiceEPR = EPRUtils.makeEPR(serviceLocation);
			else
			{
				exportServiceEPR = RNSUtilities.findService(
					"/containers/BootstrapContainer", "ExportedRootPortType", 
					new PortType[] {
							WellKnownPortTypes.EXPORTED_ROOT_SERVICE_PORT_TYPE
					}, serviceLocation).getEndpoint();
			}

			/* get local directory path to be exported */
			String localPath = getArgument(1);
			
			EndpointReferenceType epr = createExportedRoot(
				exportServiceEPR, localPath, targetRNSName, _replicate);

			if (targetRNSName == null)
			{
				stdout.println(ObjectSerializer.toString(epr,
					new QName(GenesisIIConstants.GENESISII_NS, "endpoint")));
			}
			
			return 0;
		} else if (_replicate)
		{
			/* get rns path for exported root and ensure dne*/			
			String targetRNSName = null;
			if (numArgs == 4){
				targetRNSName = getArgument(3);
				
				//ensure location does not already exist
				ensureTargetDNE(targetRNSName);
			}
			
			EndpointReferenceType exportServiceEPR = null;
			EndpointReferenceType replicationServiceEPR = null;
			
			String primaryLocation = getArgument(0);
			String replicationLocation = getArgument(1);
			
			/* get EPRs for needed services*/
			if (_url){
				exportServiceEPR = EPRUtils.makeEPR(primaryLocation);
				replicationServiceEPR = EPRUtils.makeEPR(replicationLocation);
			}
			else{
				exportServiceEPR = RNSUtilities.findService(
					"/containers/BootstrapContainer", "ExportedRootPortType", 
					new PortType[] {
							WellKnownPortTypes.EXPORTED_ROOT_SERVICE_PORT_TYPE
					}, primaryLocation).getEndpoint();
				
				replicationServiceEPR = RNSUtilities.findService(
					"/containers/BootstrapContainer", "RExportResolverPortType", 
					new PortType[] {
							WellKnownPortTypes.REXPORT_RESOLVER_PORT_TYPE
					}, replicationLocation).getEndpoint();
			}
			
			/* get local directory path to be exported */
			String localPath = getArgument(2);
			
			EndpointReferenceType epr = createReplicatedExportedRoot(
				exportServiceEPR, localPath, targetRNSName, 
				_replicate, replicationServiceEPR );

			if (targetRNSName == null)
			{
				stdout.println(ObjectSerializer.toString(epr,
					new QName(GenesisIIConstants.GENESISII_NS, "endpoint")));
			}
			
			return 0;
		}else if (_quit)
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
				throw new InvalidToolUsageException(
					"Invalid number of arguments.");
		} else if (_replicate){
			if (numArgs < 3 || numArgs > 4)
				throw new InvalidToolUsageException(
					"Invalid number of arguments.");
		}
		else if (_quit)
		{
			if (_create)
				throw new InvalidToolUsageException(
					"Only one of the options create or " +
					"quit can be specified.");
			
			if (numArgs != 1)
				throw new InvalidToolUsageException(
					"Invalid number of arguments.");
		}else{
			if (numArgs != 0)
				throw new InvalidToolUsageException(
					"Invalid arguments.");
		}
	}
	
	static public EndpointReferenceType createExportedRoot(
			EndpointReferenceType exportServiceEPR, String localPath, 
			String RNSPath, boolean isReplicated) 
		throws ResourceException,
			ResourceCreationFaultType, RemoteException, RNSException,
			CreationException, IOException
	{
		EndpointReferenceType newEPR = null;
		
		String replicationIndicator = "false";
		if (isReplicated)
			replicationIndicator = "true";
		
		MessageElement[] createProps = ExportedDirUtils.createCreationProperties(
			localPath, "", replicationIndicator);
		
		ICallingContext origContext = ContextManager.getCurrentContext();
		ICallingContext createContext = origContext.deriveNewContext();
		createContext.setSingleValueProperty(
				RNSConstants.RESOLVED_ENTRY_UNBOUND_PROPERTY, 
				RNSConstants.RESOLVED_ENTRY_UNBOUND_FALSE);
		try{
			ContextManager.storeCurrentContext(createContext);
			newEPR = createInstance(exportServiceEPR, RNSPath, createProps);
		}
		finally	{
			ContextManager.storeCurrentContext(origContext);
		}
		return newEPR;
	}
	
	/**
	 * local linking tool that on error uses quit export instead of resource delete
	 * 
	 * @param service
	 * @param optTargetName
	 * @param createProperties
	 * @return
	 * @throws ConfigurationExceptionMOOCH
	 * @throws ResourceException
	 * @throws ResourceCreationFaultType
	 * @throws RemoteException
	 * @throws RNSException
	 * @throws CreationException
	 * @throws IOException
	 */
	static public EndpointReferenceType createInstance(
			EndpointReferenceType service,
			String optTargetName,
			MessageElement [] createProperties) 
		throws ResourceException,
			ResourceCreationFaultType, RemoteException, RNSException, 
			CreationException, IOException
	{
		EndpointReferenceType epr = ResourceCreator.createNewResource(
			service, createProperties, null);
		
		if (optTargetName != null)
		{
			try{
				LnTool.link(epr, optTargetName);
			}
			catch (RNSException re){
				quitExportedRoot(epr, false);
				throw re;
			}
		}
		
		return epr;
	}
	
	static public EndpointReferenceType createReplicatedExportedRoot(
			EndpointReferenceType exportServiceEPR, String localPath, 
			String RNSPath, boolean isReplicated, 
			EndpointReferenceType replicationService) 
		throws ResourceException,
			ResourceCreationFaultType, RemoteException, RNSException,
			CreationException, IOException
	{
		EndpointReferenceType newEPR = null;
		String replicationIndicator = "false";
		if (isReplicated)
			replicationIndicator = "true";
		
		MessageElement[] createProps = ExportedDirUtils.createReplicationCreationProperties(
			localPath, "", replicationIndicator, replicationService);
		
		ICallingContext origContext = ContextManager.getCurrentContext();
		ICallingContext createContext = origContext.deriveNewContext();
		createContext.setSingleValueProperty(
				RNSConstants.RESOLVED_ENTRY_UNBOUND_PROPERTY, 
				RNSConstants.RESOLVED_ENTRY_UNBOUND_FALSE);
		try{
			ContextManager.storeCurrentContext(createContext);
			newEPR = createInstance(exportServiceEPR, RNSPath, createProps);
		}
		finally{
			ContextManager.storeCurrentContext(origContext);
		}
		return newEPR;
	}
	
	static public boolean quitExportedRootFromRNS(String exportedRootRNSPath) 
		throws IOException, RNSException
	{
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(exportedRootRNSPath, RNSPathQueryFlags.MUST_EXIST);
		boolean ret = quitExportedRoot(path.getEndpoint(), false);
		
		/* remove from RNS */
		path.unlink();
		
		return ret;
	}
	
	static public boolean quitExportedRootFromURL(String exportedRootURL) 
		throws IOException, RNSException
	{
		return quitExportedRoot(EPRUtils.makeEPR(exportedRootURL), false);
	}
	
	static public boolean quitExportedRoot(EndpointReferenceType epr, boolean deleteDirectory)
		throws IOException, RNSException
	{
		ExportedRootPortType exportedRoot = ClientUtils.createProxy(ExportedRootPortType.class, epr);
		QuitExport request = new QuitExport();
		request.setDelete_directory(deleteDirectory);
		QuitExportResponse response = exportedRoot.quitExport(request);
		return response.isSuccess();
	}
	
	static public void ensureTargetDNE(String targetPath)
		throws ResourceException
	{
		try{
			RNSPath currentPath = RNSPath.getCurrent();
			currentPath.lookup(targetPath, RNSPathQueryFlags.MUST_NOT_EXIST);
		}
		catch(RNSPathAlreadyExistsException e){
			throw new ResourceException ("Target RNS path already exists.");
		}
		catch(Exception e){
			throw new ResourceException ("Problem with ensuring target RNS path does not already exist.");
		}
	}
}