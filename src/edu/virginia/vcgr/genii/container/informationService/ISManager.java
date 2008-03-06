/**
 * @author Krasi
 */

package edu.virginia.vcgr.genii.container.informationService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;


import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

/**
 * This class is used to extract the information from the derby database about the
 * information services that were running before a container fails.
 */
public class ISManager{
	
	
	static private Log _logger = LogFactory.getLog(ISManager.class);
	
	/**
	 * The database connection pool from whence to acquire
	 * temporary connections to the database.
	 */
	static private DatabaseConnectionPool _connectionPool = null;
	
	static public ArrayList<ISInternalType> StartIS (DatabaseConnectionPool connectionPool)
	{
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs= null;
		ArrayList<ISInternalType> ret = new ArrayList<ISInternalType>();
		
		
		synchronized (ISManager.class)
		{
			if (_connectionPool !=null)
				throw new IllegalArgumentException ("The Information Service is already running");
			_connectionPool = connectionPool;
		}
		
		try
		{
			/* Acquire a new connection to access the database with. */
			connection = _connectionPool.acquire();
			
			/*
			 * we look through the resource table to find all the information service
			 * resources that have been created and also the containers that those resources
			 * were pooling
			 */
			
			stmt = connection.createStatement();
			/*
			 * get the data from the table holding the entries for the information services
			 */
			rs = stmt.executeQuery("SELECT servicekey, serviceEPR, callingcontext FROM isbescontainers");
			
			
			while (rs.next())
			{
				String serviceKey = rs.getString(1);
				EndpointReferenceType resourceEndpoint =
					EPRUtils.fromBlob(rs.getBlob(2));
				ICallingContext callingContext =
					(ICallingContext) DBSerializer.fromBlob(rs.getBlob(3));
				if (ret.size()==0)
					ret.add(new ISInternalType(serviceKey, resourceEndpoint, callingContext));
				else
				{
					boolean unique=true;
					for (int i=0; i<ret.size(); i++)
					{
						if (serviceKey.equals(ret.get(i).getResourceKey()))
							unique=false;
					}
					if (unique==true)
						ret.add(new ISInternalType(serviceKey, resourceEndpoint, callingContext));
				}
			}
			return ret;

		} 
		catch (SQLException e) { _logger.warn(e.getLocalizedMessage(), e);} 
		catch (ResourceException e) { _logger.warn(e.getLocalizedMessage(), e);} 
		catch (IOException e) {_logger.warn(e.getLocalizedMessage(), e);} 
		catch (ClassNotFoundException e) {_logger.warn(e.getLocalizedMessage(), e);}
			
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			_connectionPool.release(connection);
		}
		
		return ret;	
	}
}
