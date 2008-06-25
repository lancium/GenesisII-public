package edu.virginia.vcgr.genii.container.genesis_dair;

import java.sql.Connection;
import java.sql.ResultSet;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.genesis_dai.PropertyDocumentType;
import edu.virginia.vcgr.genii.genesis_dair.SQLPropertyDocumentType;

public class SQLDataResource {
	
	private EndpointReferenceType _dataEPR;
	private String _abstractName;
	private ResultSet _value;
	private Connection _connection;
	private SQLPropertyDocumentType _propertyDocument;
	
	public SQLDataResource(EndpointReferenceType dataEPR, 
			String abstractName, ResultSet rs, Connection conn, SQLPropertyDocumentType propertyDoc){
		this._dataEPR = dataEPR;
		this._abstractName = abstractName;
		this._value = rs;
		this._connection = conn;
		this._propertyDocument = propertyDoc;
	}
	
	public String getAbstractName(){
		return _abstractName;
	}
	
	public Connection getConnection(){
		return _connection;
	}
	
	public EndpointReferenceType getEndpoint (){
		return _dataEPR;
	}
	
	public ResultSet getValue(){
		return _value;
	}
	
	public PropertyDocumentType getDataResourcePropertyDocument() {
		return _propertyDocument;		
	}

}
