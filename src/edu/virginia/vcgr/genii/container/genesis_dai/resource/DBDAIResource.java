package edu.virginia.vcgr.genii.container.genesis_dai.resource;


import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.EntryType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.RNSDBResource;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class DBDAIResource extends RNSDBResource implements IDAIResource{

	
	public DBDAIResource(
			ResourceKey parentKey,
			DatabaseConnectionPool connectionPool,
			IResourceKeyTranslater translater)
		throws SQLException {
		super(parentKey, connectionPool, translater);
	}
	
	static private Log _logger = LogFactory.getLog(DBDAIResource.class);
	
	
	
	static private final String _ADD_DAI_ENTRY_STATEMENT =
		"INSERT INTO dair(servicekey, servicename, serviceEPR, resourcename, resourceEPR) VALUES(?, ?, ?, ?, ?)";
	
	//static private final String _REMOVE_DAI_STMT = "DELETE FROM dair WHERE servicename = ?";
	
	static private final String _REMOVE_DAI_RESOURCE_STMT =
		"DELETE FROM dair WHERE resourcename = ?";
	
	
	public void addEntry( String serviceName, EndpointReferenceType serviceEndpoint,
			String resourceName, EndpointReferenceType resourceEndpoint) 
	throws ResourceException {
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_ADD_DAI_ENTRY_STATEMENT);
			stmt.setString(1, this._resourceKey);
			stmt.setString(2, serviceName);
			stmt.setBlob(3, EPRUtils.toBlob(serviceEndpoint));
			stmt.setString(4, resourceName);
			stmt.setBlob(5, EPRUtils.toBlob(resourceEndpoint));
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update database.");
			stmt.close();
			stmt = null;
			
		}
		catch (SQLException sqe)
		{
			
			if (sqe.getErrorCode() == -104)
			{
				// Uniqueness problem
				RNSEntryExistsFaultType fault = new RNSEntryExistsFaultType();
				fault.setPath(serviceEndpoint.toString());
				try {
					throw FaultManipulator.fillInFault(fault);
				} 
				catch (RNSEntryExistsFaultType e) {
					_logger.warn(e.getLocalizedMessage(), e);}
			} 
			else
				_logger.warn(sqe.getLocalizedMessage(), sqe);
		
			throw new ResourceException("Unable to update database.", sqe);
		} 
		catch (IOException e) {
			_logger.warn(e.getLocalizedMessage(), e);
			throw new ResourceException ("Couldn't update database." , e);
		}
		finally
		{
			StreamUtils.close(stmt);
		}			
	}



	public Collection<String> removeEntries(String resourceName)
	throws ResourceException
	{
		ArrayList<String> ret = new ArrayList<String>();
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_REMOVE_DAI_RESOURCE_STMT);
			stmt.setString(1, resourceName);
			stmt.executeUpdate();
			ret.add(resourceName);
	
			return ret;
		}
		catch (SQLException sqe)
		{
			_logger.warn(sqe.getLocalizedMessage(), sqe);
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			close(stmt);
		}
	}
	
	
	static private final String _LIST_RESOURCES_STMT =
		"SELECT resourcename, resourceEPR FROM dair WHERE servicekey = ?";
	
	public Collection<EntryType> listResources(Pattern pattern) throws ResourceException {
		
		ResultSet rs = null;
		PreparedStatement stmt = null;
		
		
		Collection<EntryType> ret = new ArrayList<EntryType>();
		
		try
		{
				
			stmt = _connection.prepareStatement(_LIST_RESOURCES_STMT);
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				String entryName = rs.getString(1);
				EndpointReferenceType resourceEndpoint =
					EPRUtils.fromBlob(rs.getBlob(2));
					ret.add(new EntryType(entryName,
						new MessageElement[0], resourceEndpoint));
			}
			
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	public Collection<String> remove(Pattern pattern) throws ResourceException {
		_logger.warn("Method remove(Pattern pattern) in not implemented");
		return null;
	}
	
	public void configureResource(String resourceName, int numSlots) throws ResourceException {
		_logger.warn("Method configureResource() is not implemented");
		
	}
}
