package edu.virginia.vcgr.genii.container.dynpages;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import org.morgan.dpage.InjectObject;
import org.morgan.dpage.InjectionException;
import org.morgan.dpage.ObjectInjectionHandler;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

class GenesisIIInjectionHandler implements ObjectInjectionHandler, Closeable
{
	private DatabaseConnectionPool _connectionPool;
	private Connection _connection = null;
	
	private Object getInjectionValue(Class<?> type, InjectObject annotation)
		throws InjectionException
	{
		if (type.equals(DatabaseConnectionPool.class))
			return _connectionPool;
		else if (type.equals(Connection.class))
		{
			if (_connection == null)
			{
				try
				{
					_connection = _connectionPool.acquire(false);
				}
				catch (SQLException sqe)
				{
					throw new InjectionException(
						"Error while trying to create conneciton.", sqe);
				}
			}
			
			return _connection;
		} else
			throw new InjectionException(String.format(
				"Don't know how to inject into type \"%s\".",
				type.getName()));
	}
	
	GenesisIIInjectionHandler(DatabaseConnectionPool connectionPool)
	{
		_connectionPool = connectionPool;
	}
	
	@Override
	public Object getFieldInjectionValue(Field field, InjectObject annotation)
		throws InjectionException
	{
		return getInjectionValue(field.getType(), annotation);
	}

	@Override
	public Object getMethodInjectionValue(Method method,
		InjectObject annotation)
			throws InjectionException
	{
		return getInjectionValue(method.getParameterTypes()[0], annotation);
	}

	@Override
	public void close() throws IOException
	{
		if (_connection != null)
			_connectionPool.release(_connection);
		_connection = null;
	}
}