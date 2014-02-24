package edu.virginia.vcgr.genii.container.dynpages;

import javax.servlet.http.HttpServletRequest;

import org.morgan.dpage.InjectionException;
import org.morgan.dpage.ObjectInjectionHandler;
import org.morgan.dpage.ObjectInjectionHandlerFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;

public class GenesisIIInjectionHandlerFactory implements ObjectInjectionHandlerFactory
{
	private ServerDatabaseConnectionPool _connectionPool;

	public GenesisIIInjectionHandlerFactory()
	{
		_connectionPool = (ServerDatabaseConnectionPool) NamedInstances.getServerInstances().lookup("connection-pool");
		if (_connectionPool == null)
			throw new ConfigurationException("Unable to find named instance \"connection-pool\".");
	}

	@Override
	public ObjectInjectionHandler createHandler(String target, HttpServletRequest request) throws InjectionException
	{
		return new GenesisIIInjectionHandler(_connectionPool);
	}
}