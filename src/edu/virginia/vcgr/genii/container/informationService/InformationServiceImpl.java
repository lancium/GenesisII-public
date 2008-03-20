/**
 * @author Krasi
 */

package edu.virginia.vcgr.genii.container.informationService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.lang.Thread;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
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

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.informationService.resource.DBISResourceFactory;
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
	
	static private Log _logger = LogFactory.getLog(InformationServiceImpl.class);
	private ICallingContext _serviceCallingContext = null;
	static final ReentrantLock lock = new ReentrantLock();
	
	
	/**
	 * this is the thread that we have for updating the contents of the database. It is alive
	 * for as long as there are containers in the IS that are being watched.
	 */
	
	private static class Updates implements Runnable
	{
		private EndpointReferenceType resourceEPR;
		private String targetedServiceName;
		private InformationServiceImpl myParent;
		
		@SuppressWarnings("unused")
		private Thread executingThread;

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
				/*
				 * setting up the working context
				 */
				
			//	WorkingContext.temporarilyAssumeNewIdentity(resourceEPR);
				
				targetedServiceName = EPRUtils.extractServiceName(resourceEPR);
				WorkingContext.setCurrentWorkingContext(new WorkingContext());
				WorkingContext.getCurrentWorkingContext().setProperty(
					WorkingContext.EPR_PROPERTY_NAME, resourceEPR);
				WorkingContext.getCurrentWorkingContext().setProperty(
					WorkingContext.TARGETED_SERVICE_NAME, targetedServiceName);
					
		
				performOperation();
			}
			catch (AxisFault af){
				_logger.warn(af.getLocalizedMessage(), af);}
			
		}
		
		/*
		 * polling the data from the BES containers
		 */	
		protected void performOperation() 
			throws ResourceUnknownFaultType, ResourceException 
		{
			Pattern pattern = null;
			/*
			 * the thread will run untill there are containers in the Information Service Resource
			 */
			IISResource resource = (IISResource)ResourceManager.getCurrentResource().dereference();
			/*
			 * get the number of containers that are being watched at the moment
			 */
			
			
			int numberOfContainersWatched = resource.listResources(pattern).size();
			while (numberOfContainersWatched >0)
			{
				
				// the thread will be checking for updates every 30 seconds (3000)
				try 
				{
					Thread.sleep(3000);
				} 
				catch (InterruptedException e) {
					_logger.warn(e.getLocalizedMessage(), e);}
				
				if (numberOfContainersWatched>0)
				{
					EntryType[] ServiceEntries = resource.listResources(pattern).toArray(new EntryType[0]);
					AddContainerRequestType request= new AddContainerRequestType();
					
					/*
					 * replace the old entries in the XML database with the new ones
					 */
					for (int j=0; j<numberOfContainersWatched; j++)
					{
						EndpointReferenceType entryEPR = ServiceEntries[j].getEntry_reference();
						String entryName = ServiceEntries[j].getEntry_name().toString();
						try 
						{
							request.setEPRofContainerToAdd(entryEPR);
							request.setNameofContainerToAdd(entryName);
							/*
							 * this is where the database gets updated
							 */
							myParent.addContainer(request);				
						} 
						catch (GenesisIISecurityException e1) {
							_logger.warn(e1.getLocalizedMessage(), e1);} 
						catch (RemoteException e) {
							_logger.warn(e.getLocalizedMessage(), e);}
	
					}
				}
				/*
				 * for testing
				 * System.out.println("*    " + resource.getKey().toString());
				 */ 
				
				 
				
				/*
				 * check the number of containers that are still being watched
				 */
				
				numberOfContainersWatched = resource.listResources(pattern).size();
			}			
		}	
	}
	
	
	
	public InformationServiceImpl() throws RemoteException 
	{
		super("InformationServicePortType");
		addImplementedPortType(WellKnownPortTypes.INFORMATION_SERVICE_PORT_TYPE);
	}
	
	protected InformationServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);
		addImplementedPortType(WellKnownPortTypes.INFORMATION_SERVICE_PORT_TYPE);
	}
	
	/*
	 * adding a container to the information service
	 */
	@RWXMapping(RWXCategory.EXECUTE)
	public Object addContainer (AddContainerRequestType request) throws RemoteException
	{
		
		XMLDatabaseOperations myDB = new XMLDatabaseOperations();
		
		EndpointReferenceType myEPR = request.getEPRofContainerToAdd();
		String name = request.getNameofContainerToAdd();

		BESAttributesDocumentRequestType attributesDocument =
			new BESAttributesDocumentRequestType();
		attributesDocument.setResourceEndpoint(myEPR);
		
		/*
		 * get the BES attributes document
		 */
		BESAttributesDocumentResponseType resultset = getBESAttributesDocument(attributesDocument);
		Date mydate = new Date();
		 String dateStringValue = mydate.toString();
		
		/*
		 * before adding a document to the database we need to make sure that it's not
		 * already present. for that purpose first check if it's already in the DB. if it is - 
		 * delete it and replace it with the new one. If it's not in the database, simply 
		 * store it in it.
		 */

		IISResource resource = (IISResource)ResourceManager.getCurrentResource().dereference();
		String isResourceName = resource.getLockKey().toString();
		
		/*
		 * we add the epr at the beggining of the original BESAttributesDocument
		 * the name of the document will be the same as the name of the container we're trying to add
		 */
		
		
		String xmlString = resultset.getResult();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		StringWriter output = new StringWriter();
		try {
			System.setProperty("javax.xml.transform.TransformerFactory",
			"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xmlString)));
			
			Node root = document.getDocumentElement();
			
			
			/*
			 * appending additional data to the XML Document Properties Document.
			 * A new element is created and it holds the time the document was created and
			 * the EPR of the BES container this document describes.
			 */
			Element additionalData = document.createElement("additionalData");
			root.appendChild(additionalData);
						
			Element timeElement = document.createElement("time");
			timeElement.setTextContent(dateStringValue+ " ");
			additionalData.appendChild(timeElement);

			Element eprElement = document.createElement("epr");
			eprElement.setTextContent(myEPR.toString());
			additionalData.appendChild(eprElement);
			
			TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document), new StreamResult(output));
		} 
		catch (ParserConfigurationException e1) {_logger.warn(e1.getLocalizedMessage(), e1);} 
		catch (SAXException e) {_logger.warn(e.getLocalizedMessage(), e);} 
		catch (IOException e) {_logger.warn(e.getLocalizedMessage(), e);} 
		catch (TransformerConfigurationException e) {_logger.warn(e.getLocalizedMessage(), e);} 
		catch (TransformerException e) {_logger.warn(e.getLocalizedMessage(), e);} 
		catch (TransformerFactoryConfigurationError e) {_logger.warn(e.getLocalizedMessage(), e);}

		//adding the container to the XML database
		lock.lock();
		try {
			myDB.deleteIfExisting(name, isResourceName);
			myDB.addBESContainer(output.toString(), name, isResourceName);
		 }
		finally {
			lock.unlock();
		}

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
		IISResource resource = (IISResource)ResourceManager.getCurrentResource().dereference();
		String isResourceName = resource.getLockKey().toString();
		
		myDB.deleteIfExisting(containerToRemove, isResourceName);
		return null;
	}

	
	/**
	 * this is the function used to query the XML database for the documents stored in it
	 */
	
	@RWXMapping(RWXCategory.EXECUTE)
	public QueryResponseType queryForProperties(QueryRequestType queryRequest) throws RemoteException 
	{
		XMLDatabaseOperations myDB = new XMLDatabaseOperations();
		
		IISResource resource = (IISResource)ResourceManager.getCurrentResource().dereference();
		String isResourceName = resource.getLockKey().toString();
	
		String result = null;
		lock.lock();
		try {
		result = 
			myDB.queryForProperties(queryRequest.getQueryString().toString(), isResourceName);
		}
		finally {
			lock.unlock();
		}
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
		catch (FileNotFoundException fnotf) {
			_logger.warn(fnotf.getLocalizedMessage(), fnotf);} 
		catch (ConfigurationException confe) {
			_logger.warn(confe.getLocalizedMessage(), confe);} 
		catch (IOException ioe) {
			_logger.warn(ioe.getLocalizedMessage(), ioe);}
		
		String result = new String();
		
		if (BESAttributesDocumentRequest == null)
			return null;
	
		EndpointReferenceType myEPR = BESAttributesDocumentRequest.getResourceEndpoint();
		
		try 
		{
			GeniiBESPortType bes = null;
			/*
			 * it's done in this way so that the update thread can also perform those functions
			 * while using the calling context of the parent thread.
			 */
			if (_serviceCallingContext != null) {
				bes = ClientUtils.createProxy(GeniiBESPortType.class, myEPR, _serviceCallingContext);
			}
			else {
				bes = ClientUtils.createProxy(GeniiBESPortType.class, myEPR, callingContext);
			}
			GetFactoryAttributesDocumentResponseType resp =
				bes.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());
			
			StringWriter writer = new StringWriter();
			ObjectSerializer.serialize(writer, resp, 
					new QName("http://tempuri.org", "bes-factory-attributes"));
			result = writer.toString();

		} 
		catch (ConfigurationException confe) {
			_logger.warn(confe.getLocalizedMessage(), confe);
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
	 * the overwritten add method. when a container is added to 
	 */
	
	protected AddResponse add(Add addRequest, MessageElement []attributes) 
		throws RemoteException, RNSEntryExistsFaultType, 
		ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
		{
			String resourceName =addRequest.getEntry_name();
			EndpointReferenceType resourceEndpoint = addRequest.getEntry_reference();
			
			EndpointReferenceType serviceEPR = 
				(EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
					WorkingContext.EPR_PROPERTY_NAME);
			IISResource resource = (IISResource)ResourceManager.getCurrentResource().dereference();
			
			ICallingContext callingContext = null;
			try 
			{
				callingContext = ContextManager.getCurrentContext();
			} 
			catch (FileNotFoundException fnotf) {
				_logger.warn(fnotf.getLocalizedMessage(), fnotf);} 
			catch (ConfigurationException confe) {
				_logger.warn(confe.getLocalizedMessage(), confe);} 
			catch (IOException ioe) {
				_logger.warn(ioe.getLocalizedMessage(), ioe);}
			_serviceCallingContext = callingContext;
			
			/*
			 * Adding the container to the IS's entries table.
			 */
			resource.addResource(resourceName, resourceEndpoint, serviceEPR, callingContext);
			AddContainerRequestType newEntry = new AddContainerRequestType();
			newEntry.setEPRofContainerToAdd(resourceEndpoint);
			newEntry.setNameofContainerToAdd(resourceName);
	
			addContainer(newEntry);
		
			/*
			 * starting the update thread. a thread is started only if that's the first entry to the
			 * IS's table
			 */
			Pattern pattern = null;
			int resourceNumber = resource.listResources(pattern).size();
			if (resourceNumber==1)
			{	
				Thread t = new Thread(new Updates(serviceEPR, this));			
				t.setName("ISUpdateThread");
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
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IRNSResource resource = (IISResource)rKey.dereference();
	    Collection<String> removed = resource.removeEntries(entry_name);
	    
	    RemoveContainerRequestType documentToRemove= new RemoveContainerRequestType();
	    documentToRemove.setPathToContainer(entry_name);
	    /*
	     * deleting the container's attributes document from the XML database
	     */
	    deleteContainer(documentToRemove);
	    
	    ret = new String[removed.size()];
	    removed.toArray(ret);
	   	
	    return ret;
	}
	/*
	 * This method is called automatically when the Web server first comes up.
	 * We use it to restart the Information Service from where it left off.
	 */

	@Override
	public boolean startup()
	{
		boolean serviceCreated = super.startup();
		
		try
		{
			/* In order to make out calls, we have to have a working context
			 * so we go ahead and create an empty one.
			 */

			WorkingContext.setCurrentWorkingContext(new WorkingContext());
			
			/* Now we get the database connection pool configured 
			 * with this service */
			DatabaseConnectionPool connectionPool =(
				(DBISResourceFactory)ResourceManager.getServiceResource(_serviceName
					).getProvider().getFactory()).getConnectionPool();
			
			
			_logger.debug("Restarting the Information Service");
			ArrayList<ISInternalType> results =
				ISManager.StartIS(connectionPool);
			int resourcesToRestart = results.size();
			if (resourcesToRestart == 0) {
				return serviceCreated;
			}
			
			else{
				if (lock.isLocked())
					lock.unlock();
				/*
				 * for every Information service that was running before we start up an Update thread.
				 */
				for (int i =0; i<results.size(); i++)
				{
					ICallingContext myContext = results.get(i).getCallingContext();
					EndpointReferenceType serviceEPR = results.get(i).getResourceEndpoint();
					_serviceCallingContext = myContext;
					
					Thread t = new Thread(new Updates(serviceEPR, this));			
					t.setName("ISUpdateThread");
					t.start();

				}
			}

		}
		catch (Exception e)
		{
			_logger.error("Unable to start information service resources.", e);
		}
		finally
		{
			WorkingContext.setCurrentWorkingContext(null);
		}
		
		return serviceCreated;
	}

}