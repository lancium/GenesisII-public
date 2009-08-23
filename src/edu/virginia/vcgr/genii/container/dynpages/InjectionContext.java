package edu.virginia.vcgr.genii.container.dynpages;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class InjectionContext implements Closeable
{
	static private Log _logger = LogFactory.getLog(InjectionContext.class);
	
	static private DatabaseConnectionPool _pool;
	
	static
	{
		_pool = 
			(DatabaseConnectionPool)NamedInstances.getServerInstances(
				).lookup("connection-pool");
		if (_pool == null)
			throw new ConfigurationException(
				"Unable to find named instance \"connection-pool\".");
	}
	
	private HttpServletRequest _request;
	private Connection _connection = null;
	
	@Override
	protected void finalize() throws Throwable
	{
		close();
	}
	
	InjectionContext(HttpServletRequest request)
	{
		_request = request;
	}
	
	@Override
	synchronized public void close() throws IOException
	{
		if (_connection != null)
		{
			try 
			{
				_connection.commit(); 
			} 
			catch (Throwable cause) 
			{
				_logger.error("Unable to commit connection.", cause);
			}
			
			_pool.release(_connection);
			_connection = null;
		}
	}
	
	final DatabaseConnectionPool connectionPool()
	{
		return _pool;
	}
	
	final Connection connection() throws SQLException
	{
		if (_connection == null)
			_connection = _pool.acquire(false);
		
		return _connection;
	}
	
	final Cookie[] cookies()
	{
		return _request.getCookies();
	}
	
	final String[] parameter(String parameterName)
	{
		return _request.getParameterValues(parameterName);
	}
}