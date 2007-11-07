package edu.virginia.vcgr.genii.container.informationService;

import java.io.File;
import java.io.FileNotFoundException;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.dbxml.XmlContainer;
import com.sleepycat.dbxml.XmlDocument;
import com.sleepycat.dbxml.XmlDocumentConfig;
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
	    config.setCacheSize(50 * 1024 * 1024);
	    config.setAllowCreate(true);			// if the environment does not exist, create it
	    config.setInitializeCache(true);		// required for multithreaded applications; 
												// this is turning on shared memory
		config.setInitializeLogging(true);
		config.setTransactional(true);
		
		//for setting up the XmlManager properties
		XmlManagerConfig managerConfig = new XmlManagerConfig();
		managerConfig.setAdoptEnvironment(true);		// can reference unopened containers
		managerConfig.setAllowExternalAccess(true);		// XQuery can access external sources 
														//(files, URLs, etc) 
		
		// for setting up the container properties
		// XmlContainerConfig containerConfig = new XmlContainerConfig();
	}
	
	private static void cleanup(XmlContainer openedContainer, XmlManager openedManager) 
    {
		try 
		{
		    if (openedContainer != null) openedContainer.close();
		    if (openedManager != null) openedManager.close();
		} 
		catch (Exception e) 
		{
		    // ignore exceptions on close
		}
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
		XmlDocument myDoc = null;
		XmlTransaction trn = null;
		String success = "the document is not added";
		
		try
		{    
			env = new Environment(new File(environmentPath), config);
			//Create XmlManager using that environment, no DBXML flags
			myManager = new XmlManager(env, null);
		    myManager.setDefaultContainerType(XmlContainer.NodeContainer);

			String theContainer = (serviceName.toString() + ".dbxml");
			
			if (document.toString() != null)
			{
				//starting the transaction
				trn = myManager.createTransaction();
				
				XmlUpdateContext theContext = myManager.createUpdateContext();
				
			    myDoc = myManager.createDocument();
			    myDoc.setName(name);			
				myDoc.setContent(document.toString());
				
			    // check if the container is already existing
				if (myManager.existsContainer(theContainer) == 0)
				{ container = myManager.createContainer(theContainer); }
				else
				{ container = myManager.openContainer(theContainer); }
				container.putDocument ( myDoc, theContext);
				trn.commit();
				success = "the document is added";
				trn.delete();
				theContext.delete();
			}			
		}
		catch (XmlException e) { System.err.println("The error in addBESContainer() is: " +e.getMessage());}
		catch (DatabaseException de){System.err.println("A database exception has occured in addBESContainer()") ;} 
		catch (FileNotFoundException e) 
		{
			System.err.println("The error is: " +e.getMessage());
			e.printStackTrace();
		}
		
		finally { cleanup(container, myManager);}
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
		String environmentPath = "C:/dbxml-2.3.10/MyStuff";
		EnvironmentConfig config = new EnvironmentConfig();
		setup(config);
    
	    //for setting the document properties
	    XmlDocumentConfig documentConfig = new XmlDocumentConfig();
	    documentConfig.setGenerateName(true);
	        
	    Environment env = null;
	    XmlManager myManager = null;
	    XmlContainer container = null;
	    XmlTransaction trn = null;
	    String result = "";
	    
	    String internalQuery = "" ;
	    
		try
		{    
		    env = new Environment(new File(environmentPath), config);
			//Create XmlManager using that environment, no DBXML flags
			myManager = new XmlManager(env, null);
		    myManager.setDefaultContainerType(XmlContainer.NodeContainer);

			String theContainer = (serviceName.toString() + ".dbxml");
			internalQuery = internalQuery.concat("collection('"+ theContainer+"')");
			internalQuery = internalQuery.concat(query);
			
			//starting the transaction
			trn = myManager.createTransaction();			
		    // check if the container is already existing
			if (myManager.existsContainer(theContainer) == 0)
			{ System.err.println("This container does not exist");}
			else
			{ 
				container = myManager.openContainer(theContainer); 
		
				XmlQueryContext qContext = myManager.createQueryContext();
				//String myQuery = "collection('newContainer.dbxml')/string()";
				XmlQueryExpression qe = myManager.prepare(internalQuery, qContext);
				XmlResults results = null;
				if (internalQuery.toString() != null)
					results = qe.execute(qContext);
			
				if (results.hasNext() ) 
					result += results.peek().asString() + "\n";
				while (results.hasNext())
				{
					result +=(results.next().asString() + "\n");
				}
				trn.commit();
				trn.delete();
				qContext.delete();
			}
		}
		
		catch (XmlException e) { System.err.println("The error is queryForProperties: " +e.getMessage());}
		catch (DatabaseException de){ System.err.println("A Database Exception has occured in queryForProperties()");}
		catch (FileNotFoundException fnfe){System.err.println("A Database Exception has occured in queryForProperties()");}
		
		finally { cleanup(container, myManager);}
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
		String environmentPath = "C:/dbxml-2.3.10/MyStuff";
		Environment env =  null; 
		XmlManager myManager = null;
		XmlContainer container = null;
		XmlDocument myDoc = null;
		XmlTransaction trn = null;
		try
		{    
			env = new Environment(new File(environmentPath), config);
			//Create XmlManager using that environment, no DBXML flags
			myManager = new XmlManager(env, null);
		    myManager.setDefaultContainerType(XmlContainer.NodeContainer);

			String theContainer = (serviceName.toString() + ".dbxml");
			
			if (elementToAdd != null)
			{
				
//				 check if the container is already existing
				if (myManager.existsContainer(theContainer) == 0)
				{ container = myManager.createContainer(theContainer); }
				else
				{ container = myManager.openContainer(theContainer); }
				
//				starting the transaction
				trn = myManager.createTransaction();
				
				XmlUpdateContext theContext = myManager.createUpdateContext();
				
			    myDoc = container.getDocument(elementToAdd);
			    if (myDoc != null)			   
			    {
			    	container.deleteDocument(elementToAdd, theContext);
			    }
				trn.commit();
				trn.delete();
				theContext.delete();
			}			
		}
		
		catch (XmlException e) {}
		catch (DatabaseException de){System.err.println("A database exception has occured in preexist()") ;} 
		catch (FileNotFoundException e) 
		{
			System.err.println("The error is: " +e.getMessage());
			e.printStackTrace();
		}
		
		finally { cleanup(container, myManager);}
	}

}
