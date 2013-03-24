package edu.virginia.vcgr.genii.container.notification;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class NotificationBrokerDBResource extends BasicDBResource
{

	private static Log _logger = LogFactory.getLog(NotificationBrokerDBResource.class);

	private EndpointReferenceType forwardingPort;
	private boolean mode;
	private int messageIndex;

	public NotificationBrokerDBResource(ResourceKey parentKey, DatabaseConnectionPool connectionPool) throws SQLException
	{
		super(parentKey, connectionPool);
	}

	public NotificationBrokerDBResource(String parentKey, Connection connection)
	{
		super(parentKey, connection);
	}

	public void createNotificationBroker(String resourceId, boolean mode, int messageIndex, String clientId,
		EndpointReferenceType forwardingPort) throws ResourceException
	{

		NotificationBrokerDatabase.createNotificationBroker(resourceId, mode, messageIndex, clientId, forwardingPort,
			_connection);
	}

	@Override
	public void destroy() throws ResourceException
	{
		NotificationBrokerDatabase.deleteNotificationBroker(_resourceKey, _connection);
		super.destroy();
		_logger.info("destroy method is called on notification broker");
	}

	public EndpointReferenceType getForwardingPort()
	{
		return forwardingPort;
	}

	public void setForwardingPort(EndpointReferenceType forwardingPort)
	{
		this.forwardingPort = forwardingPort;
	}

	public boolean isActiveMode()
	{
		return mode;
	}

	public void setMode(boolean mode)
	{
		this.mode = mode;
	}

	public int getMessageIndex()
	{
		return messageIndex;
	}

	public void setMessageIndex(int messageIndex)
	{
		this.messageIndex = messageIndex;
	}

	public void storeSubscriptionTracesInDB(List<String> subscriptionEPIs, EndpointReferenceType publisher,
		long subscriptionTerminationTime) throws ResourceException
	{

		AddressingParameters addressingParameters = new AddressingParameters(publisher.getReferenceParameters());
		String publisherKey = addressingParameters.getResourceKey();

		NotificationBrokerDatabase.storeSubscriptionTraces(_resourceKey, subscriptionEPIs, publisherKey,
			subscriptionTerminationTime, _connection);
	}

	public void initializeResourceFromDB() throws ResourceException
	{
		NotificationBrokerDatabase.loadNotificationBroker(_resourceKey, this, _connection);
	}

	public void loadMessageIndexFromDB() throws ResourceException
	{
		this.messageIndex = NotificationBrokerDatabase.getMessageIndexOfBroker(_resourceKey, _connection);
	}

	public void updateModeInDB(boolean newMode) throws ResourceException
	{
		NotificationBrokerDatabase.updateMode(_resourceKey, newMode, _connection);
	}
}
