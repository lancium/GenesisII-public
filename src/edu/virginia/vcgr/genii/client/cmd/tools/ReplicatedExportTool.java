package edu.virginia.vcgr.genii.client.cmd.tools;

import org.apache.axis.types.URI;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Token;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.notification.Subscribe;
import edu.virginia.vcgr.genii.common.notification.SubscribeResponse;
import edu.virginia.vcgr.genii.common.notification.UserDataType;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;

import edu.virginia.vcgr.genii.client.replicatedExport.RExportUtils;;

public class ReplicatedExportTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Creates a replicated exportDir.";
	static final private String _USAGE =
		"rexport --create " +
		//"{ --url1 <export-service1-url> | <container1> }" +
		"<local-path> <new-rns-path> " +
		"{ --url <export-service-url>+ | <container-name>+ }" +
		"OR" +
		"rexport --quit { --url <export-root-url> | <export-root-rns-path> }";
	
	public ReplicatedExportTool() {
		super(_DESCRIPTION, _USAGE, false);
	}
	
	static private Log _logger = LogFactory.getLog(ReplicatedExportTool.class);
	
	static final private String _CONTAINERS_DIR_RNS_PATH = "/containers";
	static final private String _CONTAINER_SERVICES_DIR_PATH = "/Services";
	static final private String _REXPORT_FACTORY_SERVICE_NAME = "RExportFilePortType";
	
	private boolean _create = false;
	//old private boolean _url1 = false;
	//old private boolean _url2 = false;
	
	private boolean _quit = false;
	private boolean _url = false;
	
	public void setCreate(){
		_create = true;
	}
	
	/* old
	public void setUrl1(){
		_url = true;
	}
	
	public void setUrl2(){
		_url2 = true;
	}
	*/
	
	public void setQuit(){
		_quit = true;
	}
	
	public void setUrl(){
		_url = true;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		if (_create){
			RNSPath currentPath = RNSPath.getCurrent();
			
			/* process arg 0 to get local directory path to be exported */
			String localPath = getArgument(0);
			
			/* process arg 1 to get RNS path for exported root (ensure it DNE)*/
			String targetRNSName = getArgument(1);
			RNSPath targetRNS = currentPath.lookup(targetRNSName, 
					RNSPathQueryFlags.DONT_CARE);
			if (targetRNS.exists())
				throw new RNSPathAlreadyExistsException(targetRNSName);
			String targetFileRNSPath = targetRNS.pwd();
			
			/*process argt 2 to get EPR for export service1 */
			EndpointReferenceType exportService1EPR;
			
			/*url specified*/
			if (_url)
				exportService1EPR = EPRUtils.makeEPR(getArgument(2));
			
			/*container name specified*/
			else{
				String service1Location = _CONTAINERS_DIR_RNS_PATH + "/" 
					+ getArgument(2) + _CONTAINER_SERVICES_DIR_PATH + "/" 
					+ _REXPORT_FACTORY_SERVICE_NAME;
				RNSPath service1RNS = currentPath.lookup(service1Location, 
						RNSPathQueryFlags.MUST_EXIST);
				exportService1EPR = service1RNS.getEndpoint();
			}
			
		/* must process more args to get replica container names */
			//currently assuming primary and replica on same container
			
		/*put in separate class that sets appropriate creation params*/
			/*create common EPI*/
			URI epi = WSName.generateNewEPI();
			MessageElement []creationProperties = RExportUtils.createCreationProperties(localPath, null, epi);
			
			/*set creation parameters for chosen EPI
			MessageElement []creationProperties = new MessageElement[2];
			creationProperties[0] = ClientConstructionParameters.createEndpointIdentifierProperty(epi);
			creationProperties[1] = new MessageElement(new QName(
					GenesisIIConstants.GENESISII_NS, "path"), localPath);
			*/
			
			
			ICallingContext origContext = ContextManager.getCurrentContext();
			ICallingContext createContext = origContext.deriveNewContext();
			//MEEP: is this still valid?
			createContext.setSingleValueProperty(
					RNSConstants.RESOLVED_ENTRY_UNBOUND_PROPERTY, 
					RNSConstants.RESOLVED_ENTRY_UNBOUND_FALSE);
			
			/*create EPRs for exported root and replica with chosen EPI*/
			EndpointReferenceType primaryRootEPR = null;
			EndpointReferenceType secondaryRootEPR = null;
			
			try{
				ContextManager.storeCurrentContext(createContext);
				primaryRootEPR = CreateResourceTool.createInstance(exportService1EPR, 
						targetFileRNSPath, creationProperties);
				secondaryRootEPR = CreateResourceTool.createInstance(exportService1EPR, 
						targetFileRNSPath + "-R", creationProperties);
			}
			finally{
				ContextManager.storeCurrentContext(origContext);
			}		

			/*following should all happen backend*/
			
			/*copy data from local file to primary and replica
			InputStream inLocal = null, inPrimary = null;
			OutputStream outPrimary = null;
			OutputStream outSecondary = null;
			
			//copy local file to primary
			try{
				inLocal = new FileInputStream(localPath);
				outPrimary = new ByteIOOutputStream(primaryRootEPR);
				copy(inLocal, outPrimary);
			}
			catch (Exception e){
				throw new Exception(e.getMessage());
			}
			finally{
				StreamUtils.close(inLocal);
				StreamUtils.close(outPrimary);
			}
			
			/*copy primary to secondary
			try{
				inPrimary = new ByteIOInputStream(primaryRootEPR);
				outSecondary = new ByteIOOutputStream(secondaryRootEPR);
				copy(inPrimary, outSecondary);	
			}
			catch (Exception e){
				throw new Exception(e.getMessage());
			}
			finally{
				StreamUtils.close(inPrimary);
				StreamUtils.close(outSecondary);	
			}
			*/
			//subsribe replica to be notified of primary rybtio ops and termination
			createUpdateSubscription(primaryRootEPR, 
					secondaryRootEPR, targetFileRNSPath);			
			
			/*get eprs associated with paths
			EndpointReferenceType eprPrimary = pathP.getEndpoint();
			EndpointReferenceType eprSecondary = pathS.getEndpoint();
			
			/*gin up EPR for replicated file.
			WSName rexportWSName = new WSName(eprPrimary);
			rexportWSName.getEndpointIdentifier();
			
			/*EndpointReferenceType exportEPR = rexportWSName.getEndpoint();
			
			// link in EPR for replicated file into target RNS entry - make sure RNS
			// does not make address unbound.
			ICallingContext origContext = ContextManager.getCurrentContext();
			ICallingContext linkContext = origContext.deriveNewContext();
			linkContext.setSingleValueProperty(
					RNSConstants.RESOLVED_ENTRY_UNBOUND_PROPERTY, 
					RNSConstants.RESOLVED_ENTRY_UNBOUND_FALSE);
			try{
				ContextManager.storeCurrentContext(linkContext);
				targetRNS.link(exportEPR);
			}
			finally{
				ContextManager.storeCurrentContext(origContext);
			}
			*/

		}
		else if(_quit){
		}
		
		return 0;
	}

	static public EndpointReferenceType createPrimaryExportedRoot(
			EndpointReferenceType exportServiceEPR, String localPath, String RNSPath) 
		throws ConfigurationException, ResourceException,
			ResourceCreationFaultType, RemoteException, RNSException,
			CreationException
	{
		MessageElement[] createProps = new MessageElement[2];
		createProps[0] = new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, "path"), localPath);
		createProps[1] = new MessageElement(new QName(
			GenesisIIConstants.GENESISII_NS, "parent-ids"), "");

		return CreateResourceTool.createInstance(exportServiceEPR, RNSPath, createProps);
	}
	
	static public EndpointReferenceType createUpdateSubscription(
			EndpointReferenceType primaryEPR, 
			EndpointReferenceType replicaEPR,
			String filename)
	{
		EndpointReferenceType newSubscriptionEPR = null;

		try{
			/* create subscription for updates */
			UserDataType userData = new UserDataType(new MessageElement[] { 
					new MessageElement(new QName(
							GenesisIIConstants.GENESISII_NS, "export-file-path"),
							filename)
				});
						
			GeniiCommon producer = ClientUtils.createProxy(GeniiCommon.class, primaryEPR);
			SubscribeResponse subscription = producer.subscribe(new Subscribe(
					new Token(WellknownTopics.RANDOM_BYTEIO_OP), 
					null, replicaEPR, userData));
			/*SubscribeResponse subscription2 = producer.subscribe(new Subscribe(
					new Token(WellknownTopics.TERMINATED), 
					null, replicaEPR, userData));
			*/
			newSubscriptionEPR = subscription.getSubscription();
		}
		catch (Exception e){ 
			_logger.debug("Could not create subscription to export update.", e);
		}
		
		System.err.println("Successful subscription creation.");
		
		return newSubscriptionEPR;
	}
	
	@Override
	protected void verify() throws ToolException {
		int numArgs = numArguments();
		
		if (_create){
			if (_quit)
				throw new InvalidToolUsageException(
					"Only one of the options create or " +
					"quit can be specified.");
			
			if (numArgs < 3)
				throw new InvalidToolUsageException();
		} 
		else if (_quit){
			if (_create)
				throw new InvalidToolUsageException(
					"Only one of the options create or " +
					"quit can be specified.");
			if (numArgs != 1)
				throw new InvalidToolUsageException();
		} else
			throw new InvalidToolUsageException();
	}
	
}