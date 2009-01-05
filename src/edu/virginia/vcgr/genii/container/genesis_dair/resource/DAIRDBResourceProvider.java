package edu.virginia.vcgr.genii.container.genesis_dair.resource;

import java.sql.SQLException;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.rns.RNSDBResourceProvider;

public class DAIRDBResourceProvider extends RNSDBResourceProvider {

	public DAIRDBResourceProvider(Properties props)
			throws ConfigurationException, SQLException {
		super(props);
	}

	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
	throws SQLException
	{
		return new DBDAIRResourceFactory(pool);
	}
}
