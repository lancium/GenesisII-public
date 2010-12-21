package edu.virginia.vcgr.genii.container.cleanup.wsnsubscription;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Triple;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.container.cleanup.CleanupContext;
import edu.virginia.vcgr.genii.container.cleanup.basicresource.BasicResourceCleanupHandler;

public class WSNSubscriptionCleanupHandler
	extends BasicResourceCleanupHandler
{
	static private Log _logger = LogFactory.getLog(
		WSNSubscriptionCleanupHandler.class);
	
	@Override
	protected void detectResourcesToCleanup(Connection connection,
		CleanupContext context)
	{
		_logger.info("Finding all WSNSubscriptions without any publishers.");
		
		// Find all the resources for which the publisher resource is gone.
		ResultSet rs = null;
		Statement stmt = null;
		
		try
		{
			stmt = connection.createStatement();
			rs = stmt.executeQuery(
				"SELECT subscriptionresourcekey, publisherresourcekey " +
					"FROM wsnsubscriptions " +
				"WHERE publisherresourcekey NOT IN " +
					"(SELECT resourceid FROM resources)");
			
			while (rs.next())
			{
				String subkey = rs.getString(1);
				String pubkey = rs.getString(2);
				
				context.addResource(subkey,
					"WSNSubscription %s whose publisher (%s) does not exist.",
					subkey, pubkey);
			}
		}
		catch (SQLException sqe)
		{
			_logger.warn("Unable to detect bad subscriptions.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void enactCleanup(Connection connection, String resourceID)
		throws Throwable
	{
		_logger.info(String.format("Cleaning up subscription %s.", resourceID));
		
		super.enactCleanup(connection, resourceID);
		
		removeRowsFromTable(connection,
			new Triple<String, String, String>(
				"wsnsubscriptions", "subscriptionresourcekey", resourceID));
	}
}