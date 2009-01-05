/**
 * @author Krasi
 */
package edu.virginia.vcgr.genii.container.genesis_dai.resource;

import java.sql.SQLException;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.rns.RNSDBResourceProvider;

public class DAIDBResourceProvider extends RNSDBResourceProvider {

	public DAIDBResourceProvider(Properties props)
			throws ConfigurationException, SQLException 
	{
		super(props);
	}
	
	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
	throws SQLException
	{
		return new DBDAIResourceFactory(pool);
	}

}
