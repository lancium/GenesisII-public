/**
 * @author kkk5z
 */

package edu.virginia.vcgr.genii.container.informationService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;
import java.lang.Thread;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.ggf.bes.BESPortType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.RNSDirectoryNotEmptyFaultType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ggf.rns.Remove;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.informationService.resource.IISResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;

import edu.virginia.vcgr.genii.container.rns.RNSServiceImpl;
import edu.virginia.vcgr.genii.informationService.AddContainerRequestType;
import edu.virginia.vcgr.genii.informationService.BESAttributesDocumentRequestType;
import edu.virginia.vcgr.genii.informationService.BESAttributesDocumentResponseType;
import edu.virginia.vcgr.genii.informationService.InformationServicePortType;
import edu.virginia.vcgr.genii.informationService.QueryRequestType;
import edu.virginia.vcgr.genii.informationService.QueryResponseType;
import edu.virginia.vcgr.genii.informationService.RemoveContainerRequestType;
import edu.virginia.vcgr.genii.informationService.RemoveContainerResponseType;

public class InformationServiceImpl extends RNSServiceImpl implements
InformationServicePortType {
	
	private ICallingContext _serviceCallingContext = null;
	
	
	/**
	 * this is the thread that we have for updating the contents of the database
	 */
	private class Updates implements Runnable
	{

		private EndpointReferenceType resourceEPR;
		private String targetedServiceName;
		
		@SuppressWarnings("unused")
		private Thread executingThread;

		//	private RNSPath myPath;
		
		private InformationServiceImpl myParent;

	
		public Updates( EndpointReferenceType EPR, InformationServiceImpl myParent) 
		{
			resourceEPR = EPR;
			this.myParent = myParent;
            }
		public void run() 
		{
			executingThread = null;

			try
			{
				//setting up the working context
				
				targetedServiceName = EPRUtils.extractServiceName(resourceEPR);
				WorkingContext.setCurrentWorkingContext(new WorkingContext());
				WorkingContext.getCurrentWorkingContext().setProperty(
					WorkingContext.EPR_PROPERTY_NAME, resourceEPR);
				WorkingContext.getCurrentWorkingContext().setProperty(
					WorkingContext.TARGETED_SERVICE_NAME, targetedServiceName);
					
				
				synchronized(this)
				{
					executingThread = Thread.currentThread();
				}
				
				performOperation();
			}
			catch (AxisFault af){throw new RuntimeException(af.getLocalizedMessage(), af);}
			
		}
		
		//	polling the data from the BES containers
		protected void performOperation() 
			throws ResourceUnknownFaultType, ResourceException 
		{
			for (int i=0; i< 100000 ; i++)
			{
				System.out.println("iteration number: " + i);
				// the thread will be checking for updates every 5 min (30000)
				try 
				{
					Thread.sleep(500);
				} 
				catch (InterruptedException e) {e.printStackTrace();}
				Collection<EntryType> entries;
				Pattern pattern = null;
				
				IISResource resource = (IISResource)ResourceManager.getCurrentResource().dereference();
				entries = resource.listResources(pattern);
				
				
				int size = entries.size();
				
				if (size>0)
				{
					EntryType[] ServiceEntries = new EntryType[size];
					ServiceEntries = entries.toArray(new EntryType[0]);
					AddContainerRequestType request= new AddContainerRequestType();
					
					for (int j=0; j<size; j++)
					{
						EndpointReferenceType entryEPR = ServiceEntries[j].getEntry_reference();
						String entryName = ServiceEntries[j].getEntry_name().toString();
						try 
						{
							request.setEPRofContainerToAdd(entryEPR);
							request.setNameofContainerToAdd(entryName);
							myParent.addContainer(request);				
						} 
						catch (GenesisIISecurityException e1) {e1.printStackTrace();} 
						catch (RemoteException e) {e.printStackTrace();}
	
					}
				}		
			}			
		}	
	}
	
	
	
	public InformationServiceImpl() throws RemoteException 
	{
		super("InformationServicePortType");
		addImplementedPortType(WellKnownPortTypes.RNS_SERVICE_PORT_TYPE);
	}
	
	
	/**
	 * adding a container to the information service
	 */
	@RWXMapping(RWXCategory.EXECUTE)
	public Object addContainer (AddContainerRequestType path) throws RemoteException
	{
		XMLDatabaseOperations myDB = new XMLDatabaseOperations();
		EndpointReferenceType myEPR = new EndpointReferenceType();
		myEPR = path.getEPRofContainerToAdd();
		String name = path.getNameofContainerToAdd();
		
		BESAttributesDocumentResponseType resultset = new BESAttributesDocumentResponseType();
		BESAttributesDocumentRequestType attributesDocument =
			new BESAttributesDocumentRequestType();
		attributesDocument.setResourceEndpoint(myEPR);
		
		resultset = getBESAttributesDocument(attributesDocument);
		Date mydate = new Date();
		float date = mydate.getTime();
		
		/**
		 * before adding a document to the database we need to make sure that it's not
		 * already present. for that purpose first check if it's already in the DB. if it is - 
		 * delete it and replace it with the new one. If it's not in the database, simply 
		 * store it in it.
		 */

		String key = ResourceManager.getCurrentResource().getKey().toString();
		myDB.preexist(name, key);

		/**
		 * we add the epr at the beggining of the original BESAttributesDocument
		 * the name of the document will be the same as the name of the container we're trying to add
		 */
		String contentOfDocumentToAdd = "";
		String strToChange = resultset.getResult().toString();
		String strToAdd = strToChange.substring(strToChange.indexOf("<ns1:"));

		contentOfDocumentToAdd = contentOfDocumentToAdd.concat("<document>\n");
		 
		String TimetoString = "<time>" + date +"</time>\n";
		contentOfDocumentToAdd = contentOfDocumentToAdd.concat(TimetoString);
		
		String EPRtoString = "<epr>" + myEPR.toString() +"</epr>\n";
		contentOfDocumentToAdd = contentOfDocumentToAdd.concat(EPRtoString);

		contentOfDocumentToAdd = contentOfDocumentToAdd.concat("<attributes>\n");
		contentOfDocumentToAdd = contentOfDocumentToAdd.concat(strToAdd);
		contentOfDocumentToAdd = contentOfDocumentToAdd.concat("</attributes>\n");
		contentOfDocumentToAdd = contentOfDocumentToAdd.concat("</document>");
		
		
		//adding the container to the XML database
		myDB.addBESContainer(contentOfDocumentToAdd, name, key);

		return null;
	}
	
	/**
	 * this function is used to remove a container from the "watched" set -> it's
	 * deleting the container's BESAttributesDocument from the database
	 */
	@RWXMapping(RWXCategory.EXECUTE)
	public RemoveContainerResponseType deleteContainer(RemoveContainerRequestType removeContainerRequest) 
		throws RemoteException 
	{
		String containerToRemove = removeContainerRequest.getPathToContainer().toString();
		XMLDatabaseOperations myDB = new XMLDatabaseOperations();
		String key = ResourceManager.getCurrentResource().getKey().toString();
		
		myDB.preexist(containerToRemove, key);
		return null;
	}

	
	/**
	 * this is the function used to query the XML database for the documents stored in it
	 */
	
	@RWXMapping(RWXCategory.EXECUTE)
	public QueryResponseType queryForProperties(QueryRequestType queryRequest) throws RemoteException 
	{
		XMLDatabaseOperations myDB = new XMLDatabaseOperations();
		
		String key = ResourceManager.getCurrentResource().getKey().toString();
		String result = 
			myDB.queryForProperties(queryRequest.getQueryString().toString(), key);
		QueryResponseType res = new QueryResponseType();
		res.setResult(result);
		return res;
	}
	
	
	/**
	 * This is the function used to get the BESAttributesDocument of a BES container
	 */
	
	@RWXMapping(RWXCategory.EXECUTE)
	public BESAttributesDocumentResponseType getBESAttributesDocument
	(BESAttributesDocumentRequestType BESAttributesDocumentRequest) throws RemoteException 
	{
		ICallingContext callingContext = null;
		try {
			callingContext = ContextManager.getCurrentContext();
			} 
		catch (FileNotFoundException e1) {e1.printStackTrace();} 
		catch (ConfigurationException e1) {e1.printStackTrace();} 
		catch (IOException e1) {e1.printStackTrace();}
		
		String result = new String();
		
		if (BESAttributesDocumentRequest.toString() == null ) 
			return null;
	
		
		EndpointReferenceType myEPR = new EndpointReferenceType();
		myEPR=BESAttributesDocumentRequest.getResourceEndpoint();
		
		try 
		{
			BESPortType bes = null;
			
			if (_serviceCallingContext != null)
			{
				bes = ClientUtils.createProxy(BESPortType.class, myEPR, _serviceCallingContext);
			}
			else 
			{
				bes = ClientUtils.createProxy(BESPortType.class, myEPR, callingContext);
			}
			GetFactoryAttributesDocumentResponseType resp =
				bes.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());
			
			StringWriter writer = new StringWriter();
			ObjectSerializer.serialize(writer, resp, 
					new QName("http://tempuri.org", "bes-factory-attributes"));
			result = writer.toString();

		} 
		catch (ConfigurationException e) 
		{
			System.err.println(e.getMessage());
			e.printStackTrace();
		}			
	
		BESAttributesDocumentResponseType res = new BESAttributesDocumentResponseType();
		res.setResult(result);
		return res;
}	
	
	
	@RWXMapping(RWXCategory.INHERITED)
	public CreateFileResponse createFile(CreateFile createFile) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("\"createFile\" not applicable.");
	}
	
	
	@RWXMapping(RWXCategory.INHERITED)
    public ListResponse list(List listRequest) 
    	throws RemoteException, ResourceUnknownFaultType, 
    		RNSEntryNotDirectoryFaultType, RNSFaultType
    {
    	String regex = listRequest.getEntry_name_regexp();
    	Pattern pattern = Pattern.compile(regex);
    	Collection<EntryType> entries;
    	IISResource resource = (IISResource)ResourceManager.getCurrentResource().dereference();
    	
    	entries = resource.listResources(pattern);
    	
    	ArrayList<EntryType> aRet = new ArrayList<EntryType>(entries.size());
    	for (EntryType entry : entries)
    	{
    		String name = entry.getEntry_name().toString();
    		if (pattern.matcher(name).matches())
    		{
    		aRet.add(new EntryType(
    				entry.getEntry_name(), entry.get_any(), entry.getEntry_reference()));
    		}
    	}
    	
    	EntryType []ret = new EntryType[aRet.size()];
    	aRet.toArray(ret);
    	return new ListResponse(ret);
    }
	
	
	/**
	 * this add is overwritten so that when a container is linked its AttributesDocument
	 * is added to the XML Database
	 */
	@RWXMapping(RWXCategory.INHERITED)
	public AddResponse add(Add addRequest) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		return add(addRequest, null);
	}
	
	/**
	 * the overwritten add method
	 */
	
	protected AddResponse add(Add addRequest, MessageElement []attributes) 
		throws RemoteException, RNSEntryExistsFaultType, 
		ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
		{
			String resourceName =addRequest.getEntry_name();
			EndpointReferenceType resourceEndpoint = addRequest.getEntry_reference();
			
			EndpointReferenceType myEPR = 
				(EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
					WorkingContext.EPR_PROPERTY_NAME);
			IISResource resource = (IISResource)ResourceManager.getCurrentResource().dereference();
			
			ICallingContext callingContext = null;
			try 
			{
				callingContext = ContextManager.getCurrentContext();
			} 
			catch (FileNotFoundException e1) {e1.printStackTrace();} 
			catch (ConfigurationException e1) {e1.printStackTrace();} 
			catch (IOException e1) {e1.printStackTrace();}
			_serviceCallingContext = callingContext;
			
			resource.addResource(resourceName, resourceEndpoint, callingContext);
			AddContainerRequestType newEntry = new AddContainerRequestType();
			newEntry.setEPRofContainerToAdd(resourceEndpoint);
			newEntry.setNameofContainerToAdd(resourceName);
	
			addContainer(newEntry);
		
			//		starting the thread
			Pattern pattern = null;
			int resourceNumber = resource.listResources(pattern).size();
			if (resourceNumber==1)
			{	
				Thread t = new Thread(new Updates(myEPR, this));			
				t.setName(resource.getKey().toString());
				t.start();
			}
			
			return new AddResponse (resourceEndpoint);
		
		}
	
	/*
	 * (non-Javadoc)
	 * @see edu.virginia.vcgr.genii.container.rns.RNSServiceImpl#remove(org.ggf.rns.Remove)
	 * this method should be overwritten so that when a bes-container is unlinked,
	 * its AttributesDocument is removed from the XML Database
	 */
	
	@RWXMapping(RWXCategory.INHERITED)
    public String[] remove(Remove remove) 
    	throws RemoteException, ResourceUnknownFaultType, 
    		RNSDirectoryNotEmptyFaultType, RNSFaultType
    {
    
	    return remove(remove, null);
    }
	

	protected String[] remove(Remove remove, MessageElement []attributes) 
	throws RemoteException, ResourceUnknownFaultType, 
		RNSDirectoryNotEmptyFaultType, RNSFaultType
	{
		
		String entry_name = remove.getEntry_name();
		String []ret;
		IRNSResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IISResource)rKey.dereference();
	    Collection<String> removed = resource.removeEntries(entry_name);
	    
	    RemoveContainerRequestType documentToRemove= new RemoveContainerRequestType();
	    documentToRemove.setPathToContainer(entry_name);
	    deleteContainer(documentToRemove);
	    
	    ret = new String[removed.size()];
	    removed.toArray(ret);
	    resource.commit();
	
	    return ret;
	}

}