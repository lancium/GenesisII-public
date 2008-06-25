/**
 * @author Krasi
 */
package edu.virginia.vcgr.genii.container.genesis_dair;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;

import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.genesis_dair.resource.IDAIRResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.EnhancedRNSServiceImpl;
import edu.virginia.vcgr.genii.genesis_dai.DataResourceAddressType;
import edu.virginia.vcgr.genii.genesis_dai.DataResourceUnavailableFaultType;
import edu.virginia.vcgr.genii.genesis_dai.DatasetDataType;
import edu.virginia.vcgr.genii.genesis_dai.GetDataResourcePropertyDocumentRequest;
import edu.virginia.vcgr.genii.genesis_dai.InvalidConfigurationDocumentFaultType;
import edu.virginia.vcgr.genii.genesis_dai.InvalidDatasetFormatFaultType;
import edu.virginia.vcgr.genii.genesis_dai.InvalidExpressionFaultType;
import edu.virginia.vcgr.genii.genesis_dai.InvalidLanguageFaultType;
import edu.virginia.vcgr.genii.genesis_dai.InvalidPortTypeQNameFaultType;
import edu.virginia.vcgr.genii.genesis_dai.InvalidResourceNameFaultType;
import edu.virginia.vcgr.genii.genesis_dai.NotAuthorizedFaultType;
import edu.virginia.vcgr.genii.genesis_dai.ServiceBusyFaultType;
import edu.virginia.vcgr.genii.genesis_dair.CreateDataResourceRequest;
import edu.virginia.vcgr.genii.genesis_dair.GetSQLPropertyDocumentResponse;
import edu.virginia.vcgr.genii.genesis_dair.InvalidSQLExpressionParameterFaultType;
import edu.virginia.vcgr.genii.genesis_dair.SQLAccessCombinedPortType;
import edu.virginia.vcgr.genii.genesis_dair.SQLDatasetType;
import edu.virginia.vcgr.genii.genesis_dair.SQLExecuteFactoryRequest;
import edu.virginia.vcgr.genii.genesis_dair.SQLExecuteRequest;
import edu.virginia.vcgr.genii.genesis_dair.SQLExecuteResponse;
import edu.virginia.vcgr.genii.genesis_dair.SQLExpressionType;
import edu.virginia.vcgr.genii.genesis_dair.SQLPropertyDocumentType;


public class SQLAccessCombinedServiceImpl extends EnhancedRNSServiceImpl
	implements SQLAccessCombinedPortType {

	static private Log _logger = LogFactory.getLog(SQLAccessCombinedServiceImpl.class);
	protected Connection connection;
	protected URI dataAbstractName;
	protected ResultSet rs;
	private Properties props; 	//connection properties
	
	private Hashtable<String, SQLDataResource> SQLDataResources = new Hashtable<String, SQLDataResource>(40);
	
	/*
	 * (non-Javadoc)
	 * @see edu.virginia.vcgr.genii.container.common.GenesisIIBase#setAttributeHandlers()
	 * 
	 * setting up the Attributes for an SQLAccess DataResource
	 */
	
	protected void setAttributeHandlers() throws NoSuchMethodException
	{
		super.setAttributeHandlers();
		new SQLPropertyAttributesHandler(this, getAttributePackage());
	}
	
	public SQLAccessCombinedServiceImpl() throws RemoteException 
	{
		super("SQLAccessCombinedPortType");
		addImplementedPortType(WellKnownPortTypes.GENESIS_DAIR_SQL_ACCESS_COMBINED_PORT_TYPE);
	}
	
	protected SQLAccessCombinedServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);
		addImplementedPortType(WellKnownPortTypes.GENESIS_DAIR_SQL_ACCESS_COMBINED_PORT_TYPE);
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public SQLExecuteResponse SQLExecute(SQLExecuteRequest SQLRequest)
			throws RemoteException, InvalidResourceNameFaultType,
			InvalidExpressionFaultType, ServiceBusyFaultType,
			DataResourceUnavailableFaultType,
			InvalidSQLExpressionParameterFaultType, InvalidLanguageFaultType,
			NotAuthorizedFaultType, InvalidDatasetFormatFaultType {
		
		dataAbstractName =SQLRequest.getDataResourceAbstractName();
		String dataResourceAbstractName = dataAbstractName.toString();
		
		int z = dataResourceAbstractName.indexOf("dair");
		z = z +"dair".length();
		String abstractName = dataResourceAbstractName.substring(z);
		SQLRequest.getDatasetFormatURI();
		String SQLExpression = SQLRequest.getSQLExpression().getExpression();
		

		@SuppressWarnings("unused")
		String result = "";
	    Statement stmt = null;
	    String toPrintOut = "";
	    EndpointReferenceType dataResourceEPR = new EndpointReferenceType();
		
		try {
	    	connection = DriverManager.getConnection(
    			  "jdbc:derby:"+Container.getConfigurationManager().getUserDirectory()+"\\testDB;create=true", props);
			
			result = "connection successful";
		//	System.out.println(result);
			
			if (SQLExpression.substring(0, 6).equals("SELECT")){
				stmt = connection.createStatement(ResultSet.CONCUR_UPDATABLE, 
						ResultSet.TYPE_SCROLL_SENSITIVE);
				rs = stmt.executeQuery(SQLExpression);
				
				
				if (rs!= null && !SQLExpression.substring(0, 8).equals("SELECT *"))
				{
				
					/*
					 * setting up the properties
					 */
					
					try {
						setAttributeHandlers();
					} 
					catch (NoSuchMethodException e1) {_logger.warn(e1.getLocalizedMessage(), e1);}
					
					GetSQLPropertyDocumentResponse SQLPropertiesDocument = 
						getSQLPropertyDocument(new GetDataResourcePropertyDocumentRequest());
					
					
					SQLPropertyDocumentType props = SQLPropertiesDocument.getSQLPropertyDocument();
					SQLDataResource newResource = new SQLDataResource(dataResourceEPR, abstractName, 
							rs, connection, props);
					
					
					Add addRequest = new Add();
					addRequest.setEntry_name(abstractName);
					
					/*
					 * create a EPR for the new DataResource
					 */
					EndpointReferenceType myEPR = (EndpointReferenceType)WorkingContext.getCurrentWorkingContext(
						).getProperty(WorkingContext.EPR_PROPERTY_NAME);
					
					PortType[] implementedPortTypes =
					{ WellKnownPortTypes.GENESIS_DAI_CORE_DATA_ACCESS_COMBINED_PORT_TYPE,
							RNSConstants.RNS_PORT_TYPE,
							RNSConstants.ENHANCED_RNS_PORT_TYPE};
					
					dataResourceEPR = ResourceManager.createEPR(
							ResourceManager.getCurrentResource(), 
							myEPR.getAddress().get_value().toString(), 
							implementedPortTypes);
					
					addRequest.setEntry_reference(dataResourceEPR);
					add(addRequest);
					


					SQLDataResources.put(abstractName, newResource);
				}
					
				while (rs.next())
				{
					String familyName = rs.getString(1);
					String age = rs.getString(2);
					
					toPrintOut = toPrintOut.concat(familyName + " " + age +"\n");
				}
				stmt.close();
				stmt = null;
				
		    }
		}
		catch (SQLException e) { _logger.warn(e.getLocalizedMessage(), e);}
		
		//System.out.println(toPrintOut);
		
		MessageElement [] me = new MessageElement[1];

		me[0] = new MessageElement(new QName(GenesisIIConstants.GENESIS_DAIR_RESULTS, "DataResource EPR"));
		me[0].setValue(dataResourceEPR.toString());
		

		DatasetDataType datasetData = new DatasetDataType();
		datasetData.set_any(me);
		SQLDatasetType mySQLDataset = new SQLDatasetType();
		mySQLDataset.setDatasetData(datasetData);
		SQLExecuteResponse returnedResponse = new SQLExecuteResponse();
		returnedResponse.setSQLDataset(mySQLDataset);

		return returnedResponse;
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public GetSQLPropertyDocumentResponse getSQLPropertyDocument(
			GetDataResourcePropertyDocumentRequest getSQLPropertyDocumentRequest)
			throws RemoteException, InvalidResourceNameFaultType,
			ServiceBusyFaultType, DataResourceUnavailableFaultType,
			NotAuthorizedFaultType {
		
		
		SQLPropertyDocumentType props = null;
		try {
			props = new SQLPropertyDocumentType(
					SQLPropertyAttributesHandler.getAbstractName(),
					SQLPropertyAttributesHandler.getDataResourceManagement(),
					SQLPropertyAttributesHandler.getParentDataResource(),
					SQLPropertyAttributesHandler.getDatasetMap(),
					SQLPropertyAttributesHandler.getConfigurationMap(),
					SQLPropertyAttributesHandler.getLanguageMap(),
					SQLPropertyAttributesHandler.getDataResourceDesctiption(),
					SQLPropertyAttributesHandler.getReadable(),
					SQLPropertyAttributesHandler.getWriteable(),
					SQLPropertyAttributesHandler.getConcurrentAccess(),
					SQLPropertyAttributesHandler.getTransactionInitiation(),
					SQLPropertyAttributesHandler.getTransactionIsolation(),
					SQLPropertyAttributesHandler.getChildSensitiveToParent(),
					SQLPropertyAttributesHandler.getParentSensitiveToChild(),
					SQLPropertyAttributesHandler.getSchemaDescription());
		} 
		catch (MalformedURIException e) {_logger.warn(e.getLocalizedMessage(), e);} 
		catch (SQLException e) {_logger.warn(e.getLocalizedMessage(), e);} 
		catch (MalformedURLException e) {_logger.warn(e.getLocalizedMessage(), e);}
		
		/*
		StringWriter writer = new StringWriter();
		ObjectSerializer.serialize(writer, props, 
				new QName("http://tempuri.org", "SLQDataResource-attributes"));
		String result = writer.toString();
		System.out.println(result);
		*/
		GetSQLPropertyDocumentResponse propertyResponse = new GetSQLPropertyDocumentResponse();
		propertyResponse.setSQLPropertyDocument(props);
	

		return propertyResponse;
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public DataResourceAddressType[] SQLExecuteFactory(
			SQLExecuteFactoryRequest arg0) throws RemoteException,
			InvalidResourceNameFaultType, InvalidExpressionFaultType,
			ServiceBusyFaultType, DataResourceUnavailableFaultType,
			InvalidSQLExpressionParameterFaultType,
			InvalidConfigurationDocumentFaultType,
			InvalidPortTypeQNameFaultType, InvalidLanguageFaultType,
			NotAuthorizedFaultType {
		// TODO Auto-generated method stub
		return null;
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
		return addExternal(addRequest, null);
	}
	
	/**
	 * The overwritten add() method. It's called when we link an data resource which is
	 * externally managed
	 */
	
	
	protected AddResponse addExternal(Add addRequest, MessageElement []attributes) 
		throws RemoteException, RNSEntryExistsFaultType, 
		ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType, RNSFaultType
		{

			String resourceName =addRequest.getEntry_name();
			EndpointReferenceType resourceEndpoint = addRequest.getEntry_reference();
			
			EndpointReferenceType serviceEPR = 
				(EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
					WorkingContext.EPR_PROPERTY_NAME);
			IDAIRResource resource = (IDAIRResource)ResourceManager.getCurrentResource().dereference();
			
			
			/*
			 * Adding the dataResource to the DAIR's entries table.
			 */
			resource.addEntry(serviceEPR, resourceName, resourceEndpoint);
		
			return new AddResponse (resourceEndpoint);
		}

	@RWXMapping(RWXCategory.EXECUTE)
	public Object createDataResource(
			CreateDataResourceRequest createDataResourceRequest) {	
					
		/*
        * loading the JDBC driver
        */
		loadDriver(createDataResourceRequest.getDriverPath());
	    
	    SQLExecuteRequest queryRequest = new SQLExecuteRequest();
	    try {
			queryRequest.setDataResourceAbstractName(new 
					URI("http://vcgr.cs.virginia.edu/genii/genesis-dair/"
							+createDataResourceRequest.getResourceName().toString()));
		} 
	    catch (MalformedURIException e4) { _logger.warn(e4.getLocalizedMessage(), e4);}
	    
	    
	    SQLExpressionType sql = new SQLExpressionType();
	    sql.setExpression(createDataResourceRequest.getQuery());
	    queryRequest.setSQLExpression(sql);
	    queryRequest.setDatasetFormatURI(null);
	    SQLExecuteResponse queryResult = null;
	    
	    try {
	    	queryResult =SQLExecute(queryRequest);
		} 
	    catch (InvalidResourceNameFaultType e3) {_logger.warn(e3.getLocalizedMessage(), e3);}
		catch (InvalidExpressionFaultType e3) {_logger.warn(e3.getLocalizedMessage(), e3);} 
		catch (ServiceBusyFaultType e3) {_logger.warn(e3.getLocalizedMessage(), e3);} 
		catch (DataResourceUnavailableFaultType e3) {_logger.warn(e3.getLocalizedMessage(), e3);} 
		catch (InvalidLanguageFaultType e3) {_logger.warn(e3.getLocalizedMessage(), e3);} 
		catch (NotAuthorizedFaultType e3) {_logger.warn(e3.getLocalizedMessage(), e3);} 
		catch (InvalidDatasetFormatFaultType e3) {_logger.warn(e3.getLocalizedMessage(), e3);} 
		catch (InvalidSQLExpressionParameterFaultType e3) {_logger.warn(e3.getLocalizedMessage(), e3);} 
		catch (RemoteException e3) {_logger.warn(e3.getLocalizedMessage(), e3);}
	    
		MessageElement[] res = queryResult.getSQLDataset().getDatasetData().get_any();
		String EPR = "";
		MessageElement current = new MessageElement();
		
		for (int i = 0; i< res.length; i++)
		{
			current = res[i];
			
			EPR = EPR.concat(current.getObjectValue().toString() + "\n");	
		}
		try {
			connection.close();
		} 
		catch (SQLException e1) { _logger.warn(e1.getLocalizedMessage(), e1);}
				
		return EPR;
	}
	
	static void setConnectionProps(Properties props) throws Throwable
	{
		props.setProperty("edu.virginia.vcgr.genii.container.db.db-class-name",
			"org.apache.derby.jdbc.EmbeddedDriver");
		props.setProperty("edu.virginia.vcgr.genii.container.db.db-connect-string",
			"jdbc:derby:C:\\marks-database\\database;create=true");
		/*
		 * providing a user name and a password is optional in the embedded
		 * and derby client frameworks.
		 */
		props.setProperty("edu.virginia.vcgr.genii.container.db.db-user", "sa");
		props.setProperty("edu.virginia.vcgr.genii.container.db.db-password", "");
	}

	protected void loadDriver(String driver) {
        /*
         *  The JDBC driver is loaded by loading its class.
         *  If you are using JDBC 4.0 (Java SE 6) or newer, JDBC drivers may
         *  be automatically loaded, making this code optional.
         *
         *  In an embedded environment, this will also start up the Derby
         *  engine (though not any databases), since it is not already
         *  running. In a client environment, the Derby engine is being run
         *  by the network server framework.
         *
         *  In an embedded environment, any static Derby system properties
         *  must be set before loading the driver to take effect.
         */
        try {
            Class.forName(driver).newInstance();
            System.out.println("Loaded the appropriate driver");
        } 
        catch (ClassNotFoundException cnfe) {
            System.err.println("\nUnable to load the JDBC driver " + driver);
            cnfe.printStackTrace(System.err);
        } 
        catch (InstantiationException ie) {
            System.err.println("\nUnable to instantiate the JDBC driver " + driver);
            ie.printStackTrace(System.err);
        } 
        catch (IllegalAccessException iae) {
            System.err.println("\nNot allowed to access the JDBC driver " + driver);
            iae.printStackTrace(System.err);
        }
   	}
}
