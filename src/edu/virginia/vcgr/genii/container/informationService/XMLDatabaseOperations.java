package edu.virginia.vcgr.genii.container.informationService;

import java.io.File;
import java.io.FileNotFoundException;

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

public class XMLDatabaseOperations {
	
	private void setup(EnvironmentConfig config)
	{
	    config.setAllowCreate(true);			// if the environment does not exist, create it
	    config.setInitializeCache(true);		// required for multithreaded applications; 
												// this is turning on shared memory
	    config.setCacheSize(4 * 1024 * 1024);	// 4 MB
	    config.setInitializeLogging(true);
	    config.setInitializeLocking(true);
		config.setTransactional(true);
		config.setVerboseRecovery(true);
		config.setThreaded(true);
		
		// for setting up the container properties
		// XmlContainerConfig containerConfig = new XmlContainerConfig();
	}
	

	public String addBESContainer(String document, String name, String serviceName)
	{
		EnvironmentConfig config = new EnvironmentConfig();
		setup(config);
		
		/*
		 *for creating the collection in the same place as the container
		 *
		 *String environmentPath = ENVIRONMENT_PATH_SYSTEM_PROPERTY;
		 *File collectionDir = new 
		 *GuaranteedDirectory(Container.getConfigurationManager().getUserDirectory(), "collection");
		 *String environmentPath = collectionDir.getPath();
		 */
		
		String environmentPath = "C:/dbxml-2.3.10/MyStuff";
		
		Environment env =  null;
		XmlManager myManager = null;
		XmlContainer container = null;
		XmlTransaction trn = null;
		XmlDocument myDoc = null;
		String success = "the document is not added";
		 if (document.toString()!=null)
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
				
				String containerName = serviceName.toString() + ".bdbxml";
				if (myManager.existsContainer(containerName)==0)
				{
					container = myManager.createContainer(containerName, cconfig);	
				}
				else container = myManager.openContainer(containerName, cconfig);
				
				myDoc = myManager.createDocument();
				myDoc.setName(name);
				myDoc.setContent(document.toString());
				
				trn = myManager.createTransaction();
				XmlUpdateContext theContext = myManager.createUpdateContext();
				container.putDocument(myDoc, theContext);
				trn.commit();
				success = "the document is added";
				trn.delete();
			}
			catch (XmlException e) { System.err.println("The error in addBESContainer() is: " +e.getMessage());}
			catch (DatabaseException de){System.err.println("A database exception has occured in addBESContainer()") ;} 
			catch (FileNotFoundException e) 
			{
				System.err.println("The error is: " +e.getMessage());
				e.printStackTrace();
			}
			
			finally { 
				try
				{
					if (myDoc!=null) {myDoc.delete();}
					if (container !=null){container.close();}
					if (myManager !=null){myManager.close();}	
				}
				catch (DatabaseException de){System.err.println("A database exception has occured in addBESContainer()") ;}
				}
		 }
		return success;			
	}

	public String queryForProperties(String query, String serviceName)
	{		
		/*
		 *for creating the collection in the same place as the container
		 *
		 *File collectionDir = new 
		 *GuaranteedDirectory(Container.getConfigurationManager().getUserDirectory(), "collection");
		 *String environmentPath = collectionDir.getPath();
		 */
		EnvironmentConfig config = new EnvironmentConfig();
		setup(config);
		
		/*
		 *for creating the collection in the same place as the container
		 *
		 *String environmentPath = ENVIRONMENT_PATH_SYSTEM_PROPERTY;
		 *File collectionDir = new 
		 *GuaranteedDirectory(Container.getConfigurationManager().getUserDirectory(), "collection");
		 *String environmentPath = collectionDir.getPath();
		 */
	
		String environmentPath = "C:/dbxml-2.3.10/MyStuff";
		
		Environment env =  null;
		XmlManager myManager = null;
		XmlContainer container = null;
		XmlTransaction trn = null;
	    XmlResults results = null;
	    XmlQueryExpression qe=  null;
	    String result = "";
	    
	    String internalQuery = "" ;
		
		
		try
		{    
			env = new Environment(new File(environmentPath), config);	
			
			XmlManagerConfig managerConfig = new XmlManagerConfig();
			managerConfig.setAdoptEnvironment(true);		// can reference unopened containers
			managerConfig.setAllowExternalAccess(true);		// XQuery can access external sources 
															//(files, URLs, etc)
	
			myManager = new XmlManager(env, managerConfig);
		    myManager.setDefaultContainerType(XmlContainer.NodeContainer);

		    /**
		     * setting up the container configuration
		     */
		    XmlContainerConfig cconfig = new XmlContainerConfig();
		    cconfig.setNodeContainer(true);
		    cconfig.setIndexNodes(true);
		    cconfig.setTransactional(true);

			String theContainer = (serviceName.toString() + ".bdbxml");
//			String myQuery = "collection('newContainer.dbxml')/string()";
			internalQuery = internalQuery.concat("collection('"+ theContainer+"')/string()");
			internalQuery = internalQuery.concat(query);
			
			if (internalQuery.toString()!= null)
			{
				if (myManager.existsContainer(theContainer) == 0)
				{ System.err.println("This container does not exist");}
				else
				{ 
					container = myManager.openContainer(theContainer, cconfig); 
				
					trn = myManager.createTransaction();
					XmlQueryContext qContext = myManager.createQueryContext();
					
					qe = myManager.prepare(internalQuery, qContext);
					results = qe.execute(qContext);
					String docNumber = "Number of documents found: " +String.valueOf(results.size());
					System.out.println(docNumber);
					
					 if (results.next()!= null) 
					 {
						result += results.peek().asString().toString() + "\n";
						
						results.next();
					 }
				
					trn.commit();
					trn.delete();
					qContext.delete();
				}
			}
		}
		catch (XmlException e) { System.err.println("The error in addBESContainer() is: " +e.getMessage());}
		catch (DatabaseException de){System.err.println("A database exception has occured in addBESContainer()") ;} 
		catch (FileNotFoundException e) 
		{
			System.err.println("The error is: " +e.getMessage());
			e.printStackTrace();
		}
		
		finally { 
			try
			{
				if (results != null) {results.delete();}
				if (qe != null) {qe.delete();}
				if (container !=null){container.close();}
				if (myManager !=null){myManager.close();}	
			}
			catch (DatabaseException de){System.err.println("A database exception has occured in addBESContainer()") ;}
			}
		
		return result;	
	}
	
	/*
	 * deletes an XML document if it already exists in the database
	 */
	
	public void preexist(String elementToAdd, String serviceName) {
		/*
		 * database setup
		 */ 
		 
		EnvironmentConfig config = new EnvironmentConfig();
		setup(config);
		
		/*
		 *for creating the collection in the same place as the container
		 *
		 *String environmentPath = ENVIRONMENT_PATH_SYSTEM_PROPERTY;
		 *File collectionDir = new 
		 *GuaranteedDirectory(Container.getConfigurationManager().getUserDirectory(), "collection");
		 *String environmentPath = collectionDir.getPath();
		 */
		String environmentPath = "C:/dbxml-2.3.10/MyStuff";
		
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
			
			myManager = new XmlManager(env, managerConfig);
			myManager.setDefaultContainerType(XmlContainer.NodeContainer);
			
			XmlContainerConfig cconfig = new XmlContainerConfig();
			cconfig.setNodeContainer(true);
			cconfig.setIndexNodes(true);
			cconfig.setTransactional(true);
			
			String containerName = serviceName.toString() + ".bdbxml";
			if (myManager.existsContainer(containerName)==0)
			{
				container = myManager.createContainer(containerName, cconfig);
				
			}
			else container = myManager.openContainer(containerName, cconfig);
			
			if (elementToAdd !=null)
			{
				trn = myManager.createTransaction();
				XmlUpdateContext theContext = myManager.createUpdateContext();
				try {
				myDoc = container.getDocument(elementToAdd);
				}
				catch (XmlException e)
				{
					//ignore
				}
				if (myDoc!= null)
					container.deleteDocument(elementToAdd, theContext);
				trn.commit();
				trn.delete();
			}

		}
		catch (XmlException e) { System.err.println("The error in addBESContainer() is: " +e.getMessage());}
		catch (DatabaseException de){System.err.println("A database exception has occured in addBESContainer()") ;} 
		catch (FileNotFoundException e) 
		{
			System.err.println("The error is: " +e.getMessage());
			e.printStackTrace();
		}
		
		finally { 
			try
			{
				if (myDoc !=null) {myDoc.delete();}
				if (container !=null){container.close();}
				if (myManager !=null){myManager.close();}	
			}
			catch (DatabaseException de){System.err.println("A database exception has occured in addBESContainer()") ;}
			}		
	}

}
