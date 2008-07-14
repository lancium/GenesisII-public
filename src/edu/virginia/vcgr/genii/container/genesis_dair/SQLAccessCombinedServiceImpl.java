/**
 * @author Krasi
 */
package edu.virginia.vcgr.genii.container.genesis_dair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Token;
import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedLong;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.morgan.util.io.GuaranteedDirectory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.cmd.tools.dair.TestQuery;
import edu.virginia.vcgr.genii.client.comm.ClientConstructionParameters;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.dair.DAIRUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.utils.creation.CreationProperties;
import edu.virginia.vcgr.genii.common.notification.Notify;
import edu.virginia.vcgr.genii.common.notification.Subscribe;
import edu.virginia.vcgr.genii.common.notification.UserDataType;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.byteio.RByteIOResource;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.genesis_dair.resource.IDAIRResource;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.EnhancedRNSServiceImpl;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
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
import edu.virginia.vcgr.genii.genesis_dair.CreateDataResourceResponse;
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

	// database connection parameters
	
	static private Log _logger = LogFactory.getLog(SQLAccessCombinedServiceImpl.class);
	protected Connection connection;
	protected URI dataAbstractName;
	protected ResultSet rs;
		
	static private final long _DEFAULT_TIME_TO_LIVE = 1000L * 60 * 60;
	static private QName _FILENAME_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "create-file-filename");
	static private QName _FILEPATH_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "data-filepath");
	
	private HashMap<String, SQLDataResource> SQLDataResources = new HashMap<String, SQLDataResource>(40);
	
	private EndpointReferenceType EPRofNewDataResource;
	private String resultsetToString = null;
	
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

	protected ResourceKey createResource (HashMap<QName, Object> constructionParameters) 
		throws ResourceException, BaseFaultType
	{
		_logger.debug("Creating new SQLAccessCombined Resource");
		
		if (constructionParameters == null)
		{
			ResourceCreationFaultType rcft =
				new ResourceCreationFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription(
							"Could not create SQLAccessCombined resource without cerationProperties")
						}, null);
			throw FaultManipulator.fillInFault(rcft);
		}
		
		DAIRUtils.SQLAccessCombinedInitInfo initInfo = null;
		initInfo = DAIRUtils.extractCreationProperties(constructionParameters);
		
		String DBDriverString = initInfo.getDriver();
		constructionParameters.put(IDAIRResource._DB_DRIVER_NAME_PARAM, DBDriverString);
		
		String DBConnectString = initInfo.getConnectString();
		constructionParameters.put(IDAIRResource._CONNECT_STRING_PARAM, DBConnectString);
		
		String username = initInfo.getUsername();
		constructionParameters.put(IDAIRResource._USERNAME_PARAM, username);
		
		String password = initInfo.getPassword();
		constructionParameters.put(IDAIRResource._PASSWORD_PARAM, password);
		
		return super.createResource(constructionParameters);
	}
	
	@Override
	protected void postCreate(ResourceKey key, EndpointReferenceType newEPR,
			HashMap<QName, Object> constructionParameters,
			Collection<MessageElement> resolverCreationParameters)
			throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(key, newEPR, constructionParameters,
				resolverCreationParameters);
		
		Properties props = (Properties)constructionParameters.get(
				CreationProperties.CREATION_PROPERTIES_QNAME);
		if (props!=null);
		String driver = props.getProperty("_DB_DRIVER_NAME");
		String connection = props.getProperty("_CONNECT_STRING");
		String username = props.getProperty("_USERNAME");
		String password = props.getProperty("_PASSWORD");
		
		IResource resource = key.dereference();
		resource.setProperty("_DB_DRIVER_NAME", driver);
		resource.setProperty("_CONNECT_STRING", connection);
		resource.setProperty("_USERNAME", username);
		resource.setProperty("_PASSWORD", password);
		/*
		RNSPath servicePath = RNSPath.getCurrent();
		//RNSPath movePath = servicePath

		String SQLQueriesPath = servicePath.pwd() + "SQLQueries";
		RNSPath queriesDirRNS = null;
		try {
			queriesDirRNS = RNSPath.getCurrent().lookup(SQLQueriesPath, RNSPathQueryFlags.DONT_CARE);
			if (!queriesDirRNS.exists())
			{
				queriesDirRNS.mkdir();
			}
		} 
		catch (RNSPathDoesNotExistException e) { _logger.warn(e.getLocalizedMessage(), e);} 
		catch (RNSPathAlreadyExistsException e) { _logger.warn(e.getLocalizedMessage(), e);} 
		catch (RNSException e) { _logger.warn(e.getLocalizedMessage(), e);}
		*/
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
		String abstractName = dataResourceAbstractName.substring(z+1);
		SQLRequest.getDatasetFormatURI();
		String SQLExpression = SQLRequest.getSQLExpression().getExpression();
		System.out.println(SQLExpression);
		
		String resultSetSchema = abstractName + "\n\n";

		@SuppressWarnings("unused")
		String result = "";
	    Statement stmt = null;
	    String toPrintOut = "";
	    EndpointReferenceType dataResourceEPR = new EndpointReferenceType();
	    String[] tableSchemas = null;
	    SQLPropertyDocumentType SQLprops = null;
		
		try {
			
	    	// if we're executing a SELECT clause	
			if (SQLExpression.substring(0, 6).equals("SELECT")){
				stmt = connection.createStatement(ResultSet.CONCUR_UPDATABLE, 
						ResultSet.TYPE_SCROLL_SENSITIVE);
				rs = stmt.executeQuery(SQLExpression);
				
				tableSchemas =getTablesSchemas(rs);
				
				// if the SELECT had a result to Return
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
					
					SQLprops = SQLPropertiesDocument.getSQLPropertyDocument();
					
						
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
							RNSConstants.ENHANCED_RNS_PORT_TYPE,
							WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE,
							WellKnownPortTypes.SBYTEIO_SERVICE_PORT_TYPE};
					
					dataResourceEPR = ResourceManager.createEPR(
							ResourceManager.getCurrentResource(), 
							myEPR.getAddress().get_value().toString(), 
							implementedPortTypes);
					
					addRequest.setEntry_reference(dataResourceEPR);
					MessageElement[] query = new MessageElement[1];
					query[0] = new MessageElement(new QName("queryString"));
					query[0].setValue(SQLExpression);
					addRequest.set_any(query);
					add(addRequest);
					
					
					SQLDataResource newResource = new SQLDataResource(dataResourceEPR, abstractName, 
						rs, connection, SQLprops);
					SQLDataResources.put(abstractName, newResource);
					
					EPRofNewDataResource = dataResourceEPR;
					
					
					// get the schema of the resultset
					
					int columnNumber = rs.getMetaData().getColumnCount();
					
					for (int i = 0; i<columnNumber; i++)
					{
						String columnName = rs.getMetaData().getColumnName(i+1);
						String columnType = rs.getMetaData().getColumnTypeName(i+1);
						
						resultSetSchema = resultSetSchema.concat(columnName + "\t" + columnType + "\n");
					}
					String row = "";
					while(rs.next()){
						for(int i=0; i<columnNumber; i++){
							
							row = row.concat(rs.getString(i+1) + "\t");
						}
						toPrintOut = toPrintOut.concat(row + "\n") ;
						row = "";
						
					}
					stmt.close();
					stmt = null;
					System.out.println(toPrintOut);
					resultsetToString = toPrintOut;
		
					MessageElement [] me = new MessageElement[tableSchemas.length +1];
			
					for (int i=0; i<tableSchemas.length;i++)
					{
						me[i] = new MessageElement(new QName ("Table Schema"));
						me[i].setValue(tableSchemas[i]);
					}
			
					me[tableSchemas.length] = new MessageElement(new QName ("ResultSet Schema"));
					me[tableSchemas.length].setValue(resultSetSchema);
					
					DatasetDataType datasetData = new DatasetDataType();
					datasetData.set_any(me);
					SQLDatasetType mySQLDataset = new SQLDatasetType();
					mySQLDataset.setDatasetData(datasetData);
					SQLExecuteResponse returnedResponse = new SQLExecuteResponse();
					returnedResponse.setSQLDataset(mySQLDataset);
			
					return returnedResponse;
				}

		    }
		}
		catch (SQLException e) { _logger.warn(e.getLocalizedMessage(), e);}
		
		return null;
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
			
			String query = "";
			try {
				MessageElement[] me = addRequest.get_any();
				query = me[0].getObjectValue().toString();
			} 
			catch (Exception e) { _logger.warn(e.getLocalizedMessage(), e);}
			
			/*
			 * Adding the dataResource to the DAIR's entries table.
			 */
			resource.addEntry(serviceEPR, resourceName, resourceEndpoint, query);
		
			return new AddResponse (resourceEndpoint);
		}

	@RWXMapping(RWXCategory.EXECUTE)
	public CreateDataResourceResponse createDataResource(
			CreateDataResourceRequest createDataResourceRequest) throws ResourceException, ResourceUnknownFaultType {	
					
		/*
        * loading the JDBC driver
        */
		
		IDAIRResource resource = (IDAIRResource) ResourceManager.getCurrentResource().dereference();
		String driver = (String) resource.getProperty("_DB_DRIVER_NAME");
		
		loadDriver(driver);
	    
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
		
		String []tableNames = new String[res.length];
		for (int i=0; i<res.length; i++)
		{
			tableNames[i] = res[i].getObjectValue().toString();
		}
		try {
			connection.close();
		} 
		catch (SQLException e1) { _logger.warn(e1.getLocalizedMessage(), e1);}
		CreateDataResourceResponse results = new CreateDataResourceResponse();
		results.setEPR(EPRofNewDataResource);
		results.setResultset(resultsetToString);
		results.setTableName(tableNames);

		return results;

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

	protected void loadDriver(String driver) throws ResourceUnknownFaultType, ResourceException {
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
        
        IDAIRResource resource = (IDAIRResource) ResourceManager.getCurrentResource().dereference();
        String DBConnectString =(String) resource.getProperty("_CONNECT_STRING");
		String username = (String) resource.getProperty("_USERNAME");
		String password = (String) resource.getProperty("_PASSWORD");
		
        
        try {
        	if (driver.equals("org.apache.derby.jdbc.EmbeddedDriver"))
        	{
        		if (!username.equals("") && !password.equals(""))
        		{ 
        			connection = DriverManager.getConnection(DBConnectString, username, password);
        		}
        		else connection = DriverManager.getConnection(DBConnectString);
        	/*	connection = DriverManager.getConnection(
				  "jdbc:derby:"+Container.getConfigurationManager().getUserDirectory()+"\\testDB;create=true", props);
				  */
        	}
        	else if (driver.equals("com.mysql.jdbc.Driver"))
        		connection = DriverManager.getConnection(
    					DBConnectString + "user="+username+"&password="+password);
        	
		} 

        catch (SQLException e) { _logger.warn(e.getLocalizedMessage(), e);}
   	}
	
	private String[] getTablesSchemas (ResultSet rs) throws SQLException
	{
		int columnCount = rs.getMetaData().getColumnCount();
		String [] tables = new String[columnCount];
		int newTablesCount = 0;
		for (int i = 0; i<columnCount; i++)
		{
			String name = rs.getMetaData().getTableName(i+1);
			if (i==0){
				tables[newTablesCount] = name;
				newTablesCount++;
			}
			else{
				boolean unique = false;
			
			for (int j = 0; j<i; j++){
				if (!name.equals(tables[j]) && tables[j]!= null)
					unique = true;
			}
				if (unique)
				{
					tables[newTablesCount] = name;
					newTablesCount++;
				}
			}
			
		}
		
		String[] uniqueTableNames = new String[newTablesCount];
		
		for (int i=0; i<tables.length; i++)
		{
			if (tables[i]!=null)
				uniqueTableNames[i] = tables[i];
		}
		
		// get the schema for the tables
		
		for (int i =0; i<newTablesCount; i++)
		{
				String name = uniqueTableNames[i];
				ResultSet tableRS = connection.getMetaData().getColumns(null, null, name, "%");
				String tableSchema = null;
				tableSchema = name;
				tableSchema = tableSchema.concat("\n\n");
				while (tableRS.next())
					tableSchema = tableSchema.concat(tableRS.getString(4)+ "\t" + tableRS.getString(6)+"\n");
				uniqueTableNames[i] = tableSchema;
			
		}
		for (int i =0; i<newTablesCount; i++)
		{
			System.out.println(uniqueTableNames[i]);
				
		}
		return uniqueTableNames;
	}
	
	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponse createFile(CreateFile createFileRequest)
			throws RemoteException, RNSEntryExistsFaultType, RNSFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType
	{
		MessageElement []parameters = null;
		
		File filePath;
		
		try
		{
			File userDir = ConfigurationManager.getCurrentConfiguration().getUserDirectory();
			GuaranteedDirectory sbyteiodir = new GuaranteedDirectory(userDir, "SQLQueries");
			filePath = File.createTempFile("query", ".dat", sbyteiodir);
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.getLocalizedMessage(), ioe);
		}
		
		Subscribe subscribeRequest = new Subscribe(new Token(
			WellknownTopics.SBYTEIO_INSTANCE_DYING),
			new UnsignedLong(_DEFAULT_TIME_TO_LIVE),
			(EndpointReferenceType)WorkingContext.getCurrentWorkingContext(
				).getProperty(WorkingContext.EPR_PROPERTY_NAME),
			createUserData(createFileRequest.getFilename(), 
				filePath.getAbsolutePath()));
			
		
		parameters = new MessageElement [] {
			new MessageElement(RByteIOResource.FILE_PATH_PROPERTY,
				filePath.getAbsolutePath()),
			new MessageElement(
				ByteIOConstants.SBYTEIO_SUBSCRIBE_CONSTRUCTION_PARAMETER,
				subscribeRequest),
			new MessageElement(
				ByteIOConstants.MUST_DESTROY_PROPERTY,
				Boolean.FALSE),
            new MessageElement(
            	ByteIOConstants.SBYTEIO_DESTROY_ON_CLOSE_FLAG,
            	Boolean.TRUE),
			ClientConstructionParameters.createTimeToLiveProperty(
				_DEFAULT_TIME_TO_LIVE)
		};
		
		StreamableByteIOPortType sbyteio = ClientUtils.createProxy(
			StreamableByteIOPortType.class, EPRUtils.makeEPR(
				Container.getServiceURL("StreamableByteIOPortType")));
		VcgrCreateResponse resp = sbyteio.vcgrCreate(new VcgrCreate(parameters));
		
		return new CreateFileResponse(resp.getEndpoint());
	}
	
	
	@RWXMapping(RWXCategory.OPEN)
	public void notify (Notify notify) 
		throws RemoteException, ResourceUnknownFaultType{
		
		try
		{
			String SQLQueriesPath = null;
			String topic = notify.getTopic().toString();
			String name = null;
			
			if (topic.equals(WellknownTopics.SBYTEIO_INSTANCE_DYING))
			{
				UserDataType userData = notify.getUserData();
				if (userData == null || (userData.get_any() == null) )
					throw new RemoteException(
						"Missing required user data for notification");
				MessageElement []data = userData.get_any();
				if (data.length != 2)
					throw new RemoteException(
						"Missing required user data for notification");
				String filepath = null;
				
				for (MessageElement elem : data)
				{
					QName elemName = elem.getQName();
					if (elemName.equals(_FILENAME_QNAME))
					{
						//gets the name of the file i want to put it into
						name = elem.getValue();
					} else if (elemName.equals(_FILEPATH_QNAME))
					{
						filepath = elem.getValue();
					} else
					{
						throw new RemoteException(
							"Unknown user data found in notification.");
					}
				}
				
				if (name == null)
					throw new ResourceException(
						"Couldn't locate name parameter in UserData for notification.");
				if (filepath == null)
					throw new ResourceException(
						"Couldn't locate filepath parameter in UserData " +
						"for notification.");
				
	
				// read the query from the file
				File queryString = new File(filepath);
				StringBuilder contents = new StringBuilder();
				BufferedReader input =  new BufferedReader(new FileReader(queryString));
				String line = null;
				while (( line = input.readLine()) != null){
			         contents.append(line);
			         contents.append(System.getProperty("line.separator"));
			    }
				input.close();

								
				/**
				 * must find a way to get the name of the current service!!
				*/
				RNSPath servicePath = RNSPath.getCurrent();
				
				SQLQueriesPath =servicePath.pwd() + "SQLQueries";
				RNSPath queriesDirRNS = null;
				try {
					queriesDirRNS = RNSPath.getCurrent().lookup(SQLQueriesPath, RNSPathQueryFlags.DONT_CARE);
					/*
					if (!queriesDirRNS.exists())
					{
						queriesDirRNS.mkdir();
					} */
				} 
				catch (RNSPathDoesNotExistException e) { _logger.warn(e.getLocalizedMessage(), e);} 
				catch (RNSPathAlreadyExistsException e) { _logger.warn(e.getLocalizedMessage(), e);}

				
				CreateDataResourceRequest createDataResourceRequest = new CreateDataResourceRequest();
				createDataResourceRequest.setQuery(contents.toString());
				createDataResourceRequest.setResourceName(name);
				
				CreateDataResourceResponse resp = createDataResource(createDataResourceRequest);
				TestQuery build = new TestQuery();
				build.createFileStructure(queriesDirRNS, resp, name);
				
			}
		}
		catch (Throwable t)
		{
			_logger.warn(t.getLocalizedMessage(), t);
		}
		
	}
	
	static private UserDataType createUserData(String filename, String filepath)
	{
		return new UserDataType(new MessageElement[] { 
			new MessageElement(
				_FILENAME_QNAME, filename),
			new MessageElement(
				_FILEPATH_QNAME, filepath)
		});
	}
	
	
	
}
