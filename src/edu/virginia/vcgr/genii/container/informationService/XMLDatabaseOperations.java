/**
 * @author Krasi
 */
package edu.virginia.vcgr.genii.container.informationService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.GuaranteedDirectory;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlContainerConfig;
import com.sleepycat.dbxml.XmlDocument;
import com.sleepycat.dbxml.XmlException;
import com.sleepycat.dbxml.XmlManager;
import com.sleepycat.dbxml.XmlManagerConfig;
import com.sleepycat.dbxml.XmlQueryContext;
import com.sleepycat.dbxml.XmlQueryExpression;
import com.sleepycat.dbxml.XmlResults;
import com.sleepycat.dbxml.XmlTransaction;
import com.sleepycat.dbxml.XmlUpdateContext;

import edu.virginia.vcgr.genii.container.Container;

public class XMLDatabaseOperations {
	
	static private Log _logger = LogFactory.getLog(XMLDatabaseOperations.class);
	
	private void setup(EnvironmentConfig config)
	{
	    config.setAllowCreate(true);			// if the environment does not exist, create it
	    config.setInitializeCache(true);
												
	    config.setCacheSize(4 * 1024 * 1024);	// 4 MB
	    config.setInitializeLogging(true);
	    config.setInitializeLocking(true);
		config.setTransactional(true);
		config.setVerboseRecovery(true);
		config.setThreaded(true);				// required for multithreaded applications;
		
		// for setting up the container properties
		// XmlContainerConfig containerConfig = new XmlContainerConfig();
	}
	

	public String addBESContainer(String document, String name, String serviceName)
	{
		EnvironmentConfig config = new EnvironmentConfig();
		setup(config);
		
		/*
		 * for creating the collection in the user directory (.genesisII)
		 */
		 	
		File collectionDir = null;
		try {
			collectionDir = new 
			 GuaranteedDirectory(Container.getConfigurationManager().getUserDirectory(), "collection");
		} 
		catch (IOException ioe) {
			_logger.warn(ioe.getLocalizedMessage(), ioe);}
		String environmentPath = collectionDir.getPath();
		
		Environment env =  null;
		XmlManager myManager = null;
		XmlContainer container = null;
		XmlTransaction trn = null;
		XmlDocument myDoc = null;
		String success = "the document is not added";

		 if (document!=null)
		 {
			try
			{    
				env = new Environment(new File(environmentPath), config);	
				
				XmlManagerConfig managerConfig = new XmlManagerConfig();
				managerConfig.setAdoptEnvironment(true);
				managerConfig.setAllowExternalAccess(true);
				
				myManager = new XmlManager(env, managerConfig);
				myManager.setDefaultContainerType(XmlContainer.NodeContainer);
				
				XmlContainerConfig cconfig = new XmlContainerConfig();
				cconfig.setNodeContainer(true);
				cconfig.setIndexNodes(true);
				cconfig.setTransactional(true);
				
				//fix this
				String containerName = serviceName.toString() + ".bdbxml";
				
				/*
				 * start a transaction for adding the document to the XML database
				 */
				trn = myManager.createTransaction();
				
				/*
				 * check if a container already exists for this service. If there is one, open it.
				 * If not - create a new container.
				 */
				
				if (myManager.existsContainer(containerName)==0){
					container = myManager.createContainer(containerName, cconfig);	
				}
				else container = myManager.openContainer(containerName, cconfig);
				
				myDoc = myManager.createDocument();
				myDoc.setName(name);
				myDoc.setContent(document.toString());
				XmlUpdateContext theContext = myManager.createUpdateContext();
				container.putDocument(myDoc, theContext);
				container.close();
				trn.commit();
				success = "the document is added";
				trn.delete();
			}
			catch (XmlException xmle) { 
				_logger.warn(xmle.getLocalizedMessage(), xmle);}
			catch (DatabaseException dbe){
				_logger.warn(dbe.getLocalizedMessage(), dbe);} 
			catch (FileNotFoundException fnotfe) {
				_logger.warn(fnotfe.getLocalizedMessage(), fnotfe);
			}
			
			finally { 
				try
				{
					if (myDoc!=null) {myDoc.delete();}
					if (container !=null){container.close();}
					if (myManager !=null){myManager.close();}	
				}
				catch (DatabaseException dbe){
					_logger.warn(dbe.getLocalizedMessage(), dbe);}
				}
		 }
		return success;			
	}

	public String queryForProperties(String query, String serviceName)
	{		
		/*
		 * for creating the collection in the user directory (.genesisII)
		 */
		
		File collectionDir = null;
		try {
			collectionDir = new 
			 GuaranteedDirectory(Container.getConfigurationManager().getUserDirectory(), "collection");
		} 
		catch (IOException ioe) {
			_logger.warn(ioe.getLocalizedMessage(), ioe);}
		
		String environmentPath = collectionDir.getPath();
		 
		EnvironmentConfig config = new EnvironmentConfig();
		setup(config);
		
		Environment env =  null;
		XmlManager myManager = null;
		XmlContainer container = null;
		XmlTransaction trn = null;
	    XmlResults results = null;
	    XmlQueryExpression qe=  null;
	    String result = "";
	    
	    String internalQuery = "";
		
		try
		{    
			env = new Environment(new File(environmentPath), config);	
			
			XmlManagerConfig managerConfig = new XmlManagerConfig();
			managerConfig.setAdoptEnvironment(true);		// can reference unopened containers
			managerConfig.setAllowExternalAccess(true);		// XQuery can access external sources 
															//(files, URLs, etc)
	
			myManager = new XmlManager(env, managerConfig);
		    myManager.setDefaultContainerType(XmlContainer.NodeContainer);

		    /*
		     * setting up the container configuration
		     */
		    XmlContainerConfig cconfig = new XmlContainerConfig();
		    cconfig.setNodeContainer(true);
		    cconfig.setIndexNodes(true);
		    cconfig.setTransactional(true);

		    //fix up the query
		    
			String theContainer = (serviceName.toString() + ".bdbxml");
//			String myQuery = "collection('newContainer.dbxml')/string()";
			//internalQuery = internalQuery.concat("collection('"+ theContainer+"')");
			//internalQuery = internalQuery.concat(query+ "/ns1:bes-factory-attributes/ns2:FactoryResourceAttributesDocument/ns2:OperatingSystem/string()");
			if (query.equals(""))
				internalQuery = "for $i in collection('"+theContainer+"') return $i/string()";
			else
				internalQuery = "for $i in collection('"+theContainer+"') " +
					"where "+query+" return $i/string()";
			if (internalQuery.toString()!= null)
			{
				if (myManager.existsContainer(theContainer) == 0)
				{ System.err.println("This container does not exist");}
				else
				{ 
									
				
					trn = myManager.createTransaction();
					container = myManager.openContainer(theContainer, cconfig);	
					XmlQueryContext qContext = myManager.createQueryContext();
					
					qContext.setNamespace("ns1", "http://tempuri.org");
					qContext.setNamespace("ns2", "http://schemas.ggf.org/bes/2006/08/bes-factory");
					qContext.setNamespace("ns3", "http://schemas.ggf.org/jsdl/2005/11/jsdl");
					qContext.setNamespace("ns4", "http://schemas.ggf.org/jsdl/2005/11/jsdl");
					qContext.setNamespace("ns5", "http://vcgr.cs.virginia.edu/Genesis-II");
					qContext.setNamespace("ns6", "http://schemas.ggf.org/bes/2006/08/bes/naming");
					qContext.setNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
					qContext.setNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
					
					
					qe = myManager.prepare(internalQuery, qContext);
					results = qe.execute(qContext);
					
					/* for test
					* String docNumber = "Number of documents found: " +String.valueOf(results.size());
					* System.out.println(docNumber);
					*/
					 while (results.next()!= null) 
					 {
						String intermediate =results.peek().asString();
						result = result.concat(intermediate).concat("\n");
					 }
					container.close();
					trn.commit();
					trn.delete();
					qContext.delete();
				}
			}
		}
		catch (XmlException xmle) { 
			_logger.warn(xmle.getLocalizedMessage(), xmle);}
		catch (DatabaseException dbe){
			_logger.warn(dbe.getLocalizedMessage(), dbe) ;} 
		catch (FileNotFoundException fnotfe) {
			_logger.warn(fnotfe.getLocalizedMessage(), fnotfe);
		}
		
		finally { 
			try
			{
				if (results != null) {results.delete();}
				if (qe != null) {qe.delete();}
				if (container !=null){container.close();}
				if (myManager !=null){myManager.close();}	
			}
			catch (DatabaseException dbe){_logger.warn(dbe.getLocalizedMessage(), dbe) ;}
			}
		
		return result.toString();	
	}
	
	/**
	 * deletes an XML document if it already exists in the database
	 */
	
	public void deleteIfExisting(String elementToAdd, String serviceName) {
		/* database setup
		 */ 
		 
		EnvironmentConfig config = new EnvironmentConfig();
		setup(config);
		
		/*
		 * for creating the collection in the user directory (.genesisII)
		 */
		
		File collectionDir = null;
		try {
			collectionDir = new 
			 GuaranteedDirectory(Container.getConfigurationManager().getUserDirectory(), "collection");
		} 
		catch (IOException ioe) {
			_logger.warn(ioe.getLocalizedMessage(), ioe);}
		
		 String environmentPath = collectionDir.getPath();
		
		Environment env =  null;
		XmlManager myManager = null;
		XmlContainer container = null;
		XmlTransaction trn = null;
		XmlDocument myDoc = null;
		
		try
		{    
			env = new Environment(new File(environmentPath), config);	
			
			XmlManagerConfig managerConfig = new XmlManagerConfig();
			managerConfig.setAdoptEnvironment(true);
			managerConfig.setAllowExternalAccess(true);
			managerConfig.setAllowAutoOpen(true);
			
			myManager = new XmlManager(env, managerConfig);
			myManager.setDefaultContainerType(XmlContainer.NodeContainer);
			
			XmlContainerConfig cconfig = new XmlContainerConfig();
			cconfig.setNodeContainer(true);
			cconfig.setIndexNodes(true);
			cconfig.setTransactional(true);
			
			String containerName = serviceName.toString() + ".bdbxml";

			
			if (elementToAdd !=null)
			{
				if (myManager.existsContainer(containerName)==0){
					container = myManager.createContainer(containerName, cconfig);
				}
			
				//freaking out here
				else container = myManager.openContainer(containerName, cconfig);
				trn = myManager.createTransaction();
				XmlUpdateContext theContext = myManager.createUpdateContext();
				try {
				myDoc = container.getDocument(elementToAdd);
				}
				catch (XmlException xmle){
					//ignore as otherwise it will cause an exception if the document is not in the database
					}
				if (myDoc!= null)
					container.deleteDocument(elementToAdd, theContext);
				container.close();
				trn.commit();
				trn.delete();
			}

		}
		catch (XmlException xmle) {
			_logger.warn(xmle.getLocalizedMessage(), xmle);}
		catch (DatabaseException dbe){
			_logger.warn(dbe.getLocalizedMessage(), dbe);} 
		catch (FileNotFoundException fnotfe){
			_logger.warn(fnotfe.getLocalizedMessage(), fnotfe);
		}
		
		finally { 
			try
			{
				if (myDoc !=null) {myDoc.delete();}
				if (container !=null){container.close();}
				if (myManager !=null){myManager.close();}	
			}
			catch (DatabaseException dbe){
				_logger.warn(dbe.getLocalizedMessage(), dbe) ;}
			}		
	}

}
