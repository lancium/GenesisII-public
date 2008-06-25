/**
 * @author Krasi
 */

package edu.virginia.vcgr.genii.container.genesis_dair;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.AttributedURIType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.genesis_dai.ChildSensitiveToParent;
import edu.virginia.vcgr.genii.genesis_dai.ConfigurationDocumentType;
import edu.virginia.vcgr.genii.genesis_dai.ConfigurationMapType;
import edu.virginia.vcgr.genii.genesis_dai.DataResourceAddressType;
import edu.virginia.vcgr.genii.genesis_dai.DataResourceDescription;
import edu.virginia.vcgr.genii.genesis_dai.DataResourceManagement;
import edu.virginia.vcgr.genii.genesis_dai.DatasetMapType;
import edu.virginia.vcgr.genii.genesis_dai.LanguageMapType;
import edu.virginia.vcgr.genii.genesis_dai.ParentSensitiveToChild;
import edu.virginia.vcgr.genii.genesis_dai.TransactionInitiation;
import edu.virginia.vcgr.genii.genesis_dai.TransactionIsolation;
import edu.virginia.vcgr.genii.genesis_dair.SchemaDescription;

public class SQLPropertyAttributesHandler extends AbstractAttributeHandler {
	
	static public final String DAI_NS = "http://vcgr.cs.virginia.edu/genii/genesis_dai";

	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(SQLPropertyAttributesHandler.class);
	
	static public QName DATA_RESOURCE_ABSTRACT_NAME_ATTR= new QName(
			DAI_NS, "DataResourceAbstractName");
	
	static public QName DATA_RESOURCE_MANAGEMENT_ATTR = new QName(
			DAI_NS, "DataResourceManagement");
	
	static public QName PARENT_DATA_RESOURCE_ATTR = new QName(
			DAI_NS, "ParentDataResource");
	
	static public QName DATASET_MAP_ATTR = new QName(
			DAI_NS, "DatasetMap");
	
	static public QName CONFIGURATION_MAP_ATTR = new QName(
			DAI_NS, "ConfigurationMap");
	
	static public QName LANGUAGE_MAP_ATTR = new QName(
			DAI_NS, "LanguageMap");
	
	static public QName DATA_RESOURCE_DESCRIPTION_ATTR = new QName(
			DAI_NS, "DataResourceDescription");
	
	static public QName READABLE_ATTR = new QName(
			DAI_NS, "Readable");
	
	static public QName WRITEABLE_ATTR = new QName(
			DAI_NS, "Writeable");
	
	static public QName CONCURRENT_ACCESS_ATTR = new QName(
			DAI_NS, "ConcurrentAccess");
	
	static public QName TRANSACTION_INITIATION_ATTR = new QName(
			DAI_NS, "TransactionInitiation");
	
	static public QName TRANSACTION_ISOLATION_ATTR = new QName(
			DAI_NS, "TransactionIsolation");
	
	static public QName CHILD_SENSITIVE_TO_PARENT_ATTR = new QName(
			DAI_NS, "ChildSensitiveToParent");
	
	static public QName PARENT_SENSITIVE_TO_CHILD_ATTR = new QName(
			DAI_NS, "ParentSensitiveToChild");
	
	static public QName SCHEMA_DESCRIPTION_ATTR = new QName(
			DAI_NS, "SchemaDescription");
	
	private static Connection connection;
	private static URI abstractName;
	private static ResultSet rs;
	
	protected SQLPropertyAttributesHandler(SQLAccessCombinedServiceImpl accessCombinedServiceImpl, AttributePackage attributePackage)
			throws NoSuchMethodException {
		super(attributePackage);
		connection=accessCombinedServiceImpl.connection;
		abstractName = accessCombinedServiceImpl.dataAbstractName;
		rs = accessCombinedServiceImpl.rs;
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException {
		addHandler(DATA_RESOURCE_ABSTRACT_NAME_ATTR, "getAbstractNameAttr");
		addHandler(DATA_RESOURCE_MANAGEMENT_ATTR, "getManagementAttr");
		addHandler(PARENT_DATA_RESOURCE_ATTR, "getPatentDataResourceAttr");
		addHandler(DATASET_MAP_ATTR, "getDatasetMapAttr");
		addHandler(CONFIGURATION_MAP_ATTR, "getConfigurationMapAttr");
		addHandler(LANGUAGE_MAP_ATTR, "getLanguageMapAttr");
		addHandler(DATA_RESOURCE_DESCRIPTION_ATTR, "getDataResourceDescriptionAttr");
		addHandler(READABLE_ATTR, "getReadableAttr");
		addHandler(WRITEABLE_ATTR, "getWriteableAttr");
		addHandler(CONCURRENT_ACCESS_ATTR, "getConcurrentAccessAttr");
		addHandler(TRANSACTION_INITIATION_ATTR, "getTransactionInitiationAttr");
		addHandler(TRANSACTION_ISOLATION_ATTR, "getTransactionIsolationAttr");
		addHandler(CHILD_SENSITIVE_TO_PARENT_ATTR, "getChildSensitiveToParentAttr");
		addHandler(PARENT_SENSITIVE_TO_CHILD_ATTR, "getParentSensitiveToChildAttr");
		addHandler(SCHEMA_DESCRIPTION_ATTR, "getSchemaDescriptionAttr");

	}

	static public URI getAbstractName()
	{
		return abstractName;	
	}
	
	static public DataResourceManagement getDataResourceManagement()
	{		
		 DataResourceManagement resourceManagement = DataResourceManagement.ExternallyManaged;
		return resourceManagement;
	}
	
	/**
	 * initially it will always return a generic address since for now i'm only looking at databases that
	 * already exist, i.e. not resulting DataSets.
	 * @return
	 * @throws MalformedURIException 
	 */
	static public DataResourceAddressType getParentDataResource() throws MalformedURIException
	{
		DataResourceAddressType dataResourceAddress = new DataResourceAddressType();
		AttributedURIType address = new AttributedURIType();
		URI _value =new URI ("http://vcgr.cs.virginia.edu/genii/genesis-dair/");
		address.set_value(_value);
		dataResourceAddress.setAddress(address);
		return dataResourceAddress;
	}
	
	/**
	 * 
	 * @return DatasetMapType[]
	 * @throws MalformedURIException
	 * @throws SQLException
	 * @throws MalformedURLException 
	 */
	static public DatasetMapType []getDatasetMap() throws MalformedURIException, SQLException, MalformedURLException{
		DatasetMapType [] myDatasetMap= new DatasetMapType[1];
		myDatasetMap[0] = new DatasetMapType();
		myDatasetMap[0].setMessageQName(new QName("MessageQName"));
		myDatasetMap[0].setDatasetFormatURI(new URI( "http://vcgr.cs.virginia.edu/genii/genesis-dair"));
		return myDatasetMap;
	}
	
	/**
	 * @return ConfigurationMapType[]
	 * @throws SQLException
	 */
	
	static public ConfigurationMapType[] getConfigurationMap() throws SQLException
	{
		ConfigurationDocumentType defaultConfigurationDocument = new ConfigurationDocumentType();
			/* one */
		defaultConfigurationDocument.setChildSensitiveToParent(ChildSensitiveToParent.Sensitive);
			/* two */
		defaultConfigurationDocument.setParentSensitiveToChild(ParentSensitiveToChild.Insensitive);
			/* three */
		MessageElement[] myMessage = new MessageElement[1];
		myMessage[0] = new MessageElement(new QName(GenesisIIConstants.GENESIS_DAIR_RESULTS, "dataResults ConfigurationMap property"));
		myMessage[0].setValue(connection.getMetaData().getDatabaseProductName().toString());
		defaultConfigurationDocument.setDataResourceDescription(new DataResourceDescription(myMessage));
			/* four */
		defaultConfigurationDocument.setReadable(true);
			/* five */
		defaultConfigurationDocument.setWriteable(!connection.getMetaData().isReadOnly());
			/* six */
		if (connection.getTransactionIsolation()== Connection.TRANSACTION_NONE) {
			defaultConfigurationDocument.setTransactionIsolation(TransactionIsolation.NotSupported);} 
		else if (connection.getTransactionIsolation()== Connection.TRANSACTION_READ_COMMITTED)
			defaultConfigurationDocument.setTransactionIsolation(TransactionIsolation.ReadCommitted);
		else if (connection.getTransactionIsolation()== Connection.TRANSACTION_READ_UNCOMMITTED)
			defaultConfigurationDocument.setTransactionIsolation(TransactionIsolation.ReadUncommitted);
		else if (connection.getTransactionIsolation()== Connection.TRANSACTION_REPEATABLE_READ)
			defaultConfigurationDocument.setTransactionIsolation(TransactionIsolation.RepeatableRead);
		else defaultConfigurationDocument.setTransactionIsolation(TransactionIsolation.Serialisable);
		
			/* seven */
		if (connection.getMetaData().supportsTransactions())
			defaultConfigurationDocument.setTransactionInitiation(TransactionInitiation.Automatic);
		else defaultConfigurationDocument.setTransactionInitiation(TransactionInitiation.NotSupported);
		
		/*
		 * building a configuration map first. the configuration map has five elements:
		 */
		ConfigurationMapType[] newConfigurationMap = new ConfigurationMapType[1];
		newConfigurationMap[0] = new ConfigurationMapType(new QName("MessageQName"), 
				new QName(WellKnownPortTypes.GENESIS_DAIR_SQL_ACCESS_COMBINED_PORT_TYPE.toString()),
				new QName(abstractName.toString()), defaultConfigurationDocument );
		return newConfigurationMap;
	}
	
	/**
	 * 
	 * @return LanguageMapType[]
	 * @throws SQLException
	 * @throws MalformedURIException
	 */
	
	static public LanguageMapType[] getLanguageMap() throws SQLException, MalformedURIException
	{
		int supportedSQLCount=0;
		if (connection.getMetaData().supportsANSI92EntryLevelSQL())
		{ supportedSQLCount++;}
		if (connection.getMetaData().supportsANSI92FullSQL())
		{ supportedSQLCount++;}
		if (connection.getMetaData().supportsANSI92IntermediateSQL())
		{ supportedSQLCount++;}
		
		LanguageMapType[] languageMap = new LanguageMapType[supportedSQLCount];
		int position=0;
		
		
		if (connection.getMetaData().supportsANSI92EntryLevelSQL())
		{
			languageMap[position] = new LanguageMapType();
			languageMap[position].setMessageQName(new QName("MessageQName"));
			languageMap[position].setLanguageURI(new URI("ANSI92EntryLevelSQL"));
			position++;
		}
		
		if (connection.getMetaData().supportsANSI92FullSQL())
		{
			languageMap[position].setMessageQName(new QName("MessageQName"));
			languageMap[position].setLanguageURI(new URI("ANSI92FullSQL"));
			position++;
		}
		
		if (connection.getMetaData().supportsANSI92IntermediateSQL())
		{
			languageMap[position].setMessageQName(new QName("MessageQName"));
			languageMap[position].setLanguageURI(new URI("ANSI92IntermediateSQL"));
			position++;
		}
		
		if (position >0)
		return languageMap;
		else return null;
	}
	
	/**
	 * 
	 * @return DataResourceDescription
	 * @throws SQLException
	 */
	
	static public DataResourceDescription getDataResourceDesctiption() throws SQLException
	{
		MessageElement[] myMessage = new MessageElement[1];
		String description = null;
		String tableName = null;
		tableName = rs.getMetaData().getTableName(1);
		if (tableName!= null)
			description = "This resource has been created from table " +tableName +".";
		else
			description = "This resource is an original table from a database.";
		myMessage[0]= new MessageElement(new QName("DataResourceDescription", description));
		
		DataResourceDescription myDataResourceDescr = new DataResourceDescription(myMessage);
		return myDataResourceDescr;
	}
	
	static public boolean getReadable()
	{
		return true;
	}
	
	static public boolean getWriteable() throws SQLException
	{
		return !(connection.getMetaData().isReadOnly());
	}
	
	static public boolean getConcurrentAccess()
	{
		return false;
	}
	
	/**
	 * 
	 * @return TransactionInitiation
	 * @throws SQLException
	 */
	
	static public TransactionInitiation getTransactionInitiation() throws SQLException
	{
		 TransactionInitiation transactionInitiation; 
		if (connection.getMetaData().supportsTransactions())
			transactionInitiation = TransactionInitiation.Automatic;
		else transactionInitiation = TransactionInitiation.NotSupported;
		return transactionInitiation;
	}
	
	/**
	 * 
	 * @return TransactionIsolation
	 * @throws SQLException
	 */
	
	static public TransactionIsolation getTransactionIsolation() throws SQLException
	{
		TransactionIsolation transactionIsolation;
		if (connection.getTransactionIsolation()== Connection.TRANSACTION_NONE)
			transactionIsolation = TransactionIsolation.NotSupported;
		 else if (connection.getTransactionIsolation()== Connection.TRANSACTION_READ_COMMITTED)
			transactionIsolation = TransactionIsolation.ReadCommitted;
		else if (connection.getTransactionIsolation()== Connection.TRANSACTION_READ_UNCOMMITTED)
			transactionIsolation = TransactionIsolation.ReadUncommitted;
		else if (connection.getTransactionIsolation()== Connection.TRANSACTION_REPEATABLE_READ)
			transactionIsolation = TransactionIsolation.RepeatableRead;
		else transactionIsolation = TransactionIsolation.Serialisable;
		
		return transactionIsolation;
	}
	
	static public ChildSensitiveToParent getChildSensitiveToParent()
	{
		return ChildSensitiveToParent.Insensitive;
	}
	
	static public ParentSensitiveToChild getParentSensitiveToChild()
	{
		return ParentSensitiveToChild.Insensitive;
	}
	
	/**
	 * We can add only one schema description for now (due to the specification of SQLPropertyDocument)
	 * @return SchemaDescription
	 * @throws SQLException
	 */
	static public SchemaDescription getSchemaDescription() throws SQLException
	{
		
		SchemaDescription result = new SchemaDescription();
		
			MessageElement [] myMessage = new MessageElement[1];
			myMessage[0]= new MessageElement(new QName("Schema description"));
			ResultSetMetaData metadata =rs.getMetaData();
			if (metadata != null){
				int numberOfColumns = metadata.getColumnCount();
				String schemaDescription = "";
				for (int j=0; j< numberOfColumns; j++)
				{
					String tableName = metadata.getTableName(j+1);
					String columnName = metadata.getColumnName(j+1);
					int columnSize = metadata.getColumnDisplaySize(j+1);
					String columnType = metadata.getColumnTypeName(j+1);
					boolean caseSensitive = metadata.isCaseSensitive(j+1);
					
					schemaDescription = schemaDescription.concat("Column name: " + columnName +"\tThe column is from table: " + tableName +
							"\tcolumn display size: " +columnSize + "\tcolumn type: " +columnType);
					if (caseSensitive)
						schemaDescription = schemaDescription.concat("\t the column is case sensitive.\n");
					else
						schemaDescription = schemaDescription.concat("\t the column is not case sensitive.\n");	
				}
				myMessage[0].setValue(schemaDescription);	
			}
			result.set_any(myMessage);
		
		return result;	
	}
	
	/**
	 * @return MessageElement
	 */
	
	public MessageElement getAbstractNameAttr ()
	{
		return new MessageElement (DATA_RESOURCE_ABSTRACT_NAME_ATTR, getAbstractName());
	}
	
	/**
	 * @return MessageElement
	 */
	
	public MessageElement getManagementAttr()
	{
		return new MessageElement(DATA_RESOURCE_MANAGEMENT_ATTR, getDataResourceManagement());
	}
	
	/**
	 * @return MessageElement
	 * @throws MalformedURIException
	 */
	public MessageElement getPatentDataResourceAttr() throws MalformedURIException
	{
		return new MessageElement(PARENT_DATA_RESOURCE_ATTR, getParentDataResource());
	}
	
	/**
	 * @return ArrayList<MessageElement>
	 * @throws MalformedURIException
	 * @throws SQLException
	 * @throws MalformedURLException 
	 */
	
	public ArrayList<MessageElement> getDatasetMapAttr() throws 
		MalformedURIException, SQLException, MalformedURLException
	{
		DatasetMapType [] maps = getDatasetMap();
		ArrayList<MessageElement> result = new ArrayList<MessageElement>(maps.length);
		for (int i=0; i< maps.length; i++)
		{
			result.add(new MessageElement(DATASET_MAP_ATTR, maps[i]));
		}
		return result;
	}
	
	/**
	 * @return ArrayList<MessageElement>
	 * @throws MalformedURIException
	 * @throws SQLException
	 */
	public ArrayList<MessageElement> getConfigurationMapAttr() throws 
		MalformedURIException, SQLException
	{
		ConfigurationMapType [] maps = getConfigurationMap();
		ArrayList<MessageElement> result = new ArrayList<MessageElement>(maps.length);
		for (int i=0; i< maps.length; i++)
		{
			result.add(new MessageElement(CONFIGURATION_MAP_ATTR, maps[i]));
		}
		return result;
	}
	
	/**
	 * @return ArrayList<MessageElement>
	 * @throws MalformedURIException
	 * @throws SQLException
	 */
	
	public ArrayList<MessageElement> getLanguageMapAttr() throws 
		MalformedURIException, SQLException
	{
		LanguageMapType [] maps = getLanguageMap();
		ArrayList<MessageElement> result = new ArrayList<MessageElement>(maps.length);
		for (int i=0; i< maps.length; i++)
		{
			result.add(new MessageElement(LANGUAGE_MAP_ATTR, maps[i]));
		}
		return result;
	}
	
	/**
	 * @return MessageElement
	 * @throws SQLException
	 */
	public MessageElement getDataResourceDescriptionAttr() throws SQLException
	{
		return new MessageElement(DATA_RESOURCE_DESCRIPTION_ATTR, getDataResourceDesctiption());
	}
	/**
	 * @return MessageElement
	 */
	public MessageElement getReadableAttr()
	{
		return new MessageElement (READABLE_ATTR, getReadable());
	}
	
	/**
	 * @return MessageElement
	 * @throws SQLException
	 */
	public MessageElement getWriteableAttr() throws SQLException
	{
		return new MessageElement(WRITEABLE_ATTR, getWriteable());
	}
	
	public MessageElement getConcurrentAccessAttr()
	{
		return new MessageElement(CONCURRENT_ACCESS_ATTR, getConcurrentAccess());
	}
	
	/**
	 * @return MessageElement
	 * @throws SQLException
	 */

	public MessageElement getTransactionInitiationAttr() throws SQLException{
		return new MessageElement(TRANSACTION_INITIATION_ATTR, getTransactionInitiation());
	}
	
	/**
	 * @return MessageElement
	 * @throws SQLException
	 */
	public MessageElement getTransactionIsolationAttr() throws SQLException{
		return new MessageElement(TRANSACTION_ISOLATION_ATTR, getTransactionIsolation());
	}
	
	public MessageElement getChildSensitiveToParentAttr (){
		return new MessageElement(CHILD_SENSITIVE_TO_PARENT_ATTR, getChildSensitiveToParent());
	}
	
	public MessageElement getParentSensitiveToChildAttr(){
		return new MessageElement(PARENT_SENSITIVE_TO_CHILD_ATTR, getParentSensitiveToChild());
	}
	
	/**
	 * @return ArrayList<MessageElement>
	 * @throws SQLException
	 */
	public MessageElement getSchemaDescriptionAttr() throws SQLException
	{
		return new MessageElement (SCHEMA_DESCRIPTION_ATTR, getSchemaDescription());
	}
}
