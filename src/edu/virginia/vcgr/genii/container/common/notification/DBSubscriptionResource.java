package edu.virginia.vcgr.genii.container.common.notification;

import java.sql.Connection;
import java.sql.SQLException;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class DBSubscriptionResource extends BasicDBResource
{
	public DBSubscriptionResource(String key, Connection connection)
	{
		super(key, connection);
	}

	public DBSubscriptionResource(ResourceKey parentKey, DatabaseConnectionPool connectionPool) throws SQLException
	{
		super(parentKey, connectionPool);
	}

	void createSubscription(EndpointReferenceType subscriptionReference, SubscriptionConstructionParameters cons)
		throws ResourceException
	{
		try {
			SubscriptionsDatabase.createSubscription(_connection, _resourceKey, cons.publisherResourceKey(),
				subscriptionReference, cons.consumerReference(), cons.topicQuery(), cons.policies(), cons.additionalUserData());
		} catch (SQLException e) {
			throw new ResourceException("Unable to create subscription entry in table.", e);
		}
	}

	void toggleSubscriptionPause(boolean markPaused) throws ResourceException
	{
		try {
			SubscriptionsDatabase.toggleSubscriptionPause(_connection, _resourceKey, markPaused);
		} catch (SQLException e) {
			throw new ResourceException("Unable to toggle pause status on subscription.", e);
		}
	}

	@Override
	public void destroy() throws ResourceException
	{
		try {
			SubscriptionsDatabase.deleteSubscription(_connection, _resourceKey);
		} catch (SQLException e) {
			throw new ResourceException("Unable to clean up subscription entry in table.", e);
		}

		super.destroy();
	}
}