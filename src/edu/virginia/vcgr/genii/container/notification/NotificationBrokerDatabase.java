package edu.virginia.vcgr.genii.container.notification;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;

public class NotificationBrokerDatabase
{

	private static Log _logger = LogFactory.getLog(NotificationBrokerDatabase.class);

	private static final String CREATE_BROKER_TABLE_STMT = "CREATE TABLE enhanced_notification_broker ("
		+ "resource_id 				VARCHAR(128) PRIMARY KEY, " + "mode 						SMALLINT NOT NULL WITH DEFAULT 0, "
		+ "message_index 				INT  NOT NULL WITH DEFAULT 0, " + "client_id					VARCHAR(128), "
		+ "forwarding_port_reference 	BLOB(2G))";

	private static final String CREATE_BROKER_SUBCRIPTION_TRACE_TABLE_STMT = "CREATE TABLE notification_broker_subscription ("
		+ "broker_id					VARCHAR(128), " + "subscription_identifier	VARCHAR(256), " + "publisher_key				VARCHAR(128), "
		+ "termination_time			BIGINT, " + "PRIMARY KEY (broker_id, subscription_identifier))";

	private static final String CREATE_BROKER_STMT = "INSERT INTO enhanced_notification_broker "
		+ "(resource_id, mode, message_index, client_id, forwarding_port_reference) " + "VALUES (?, ?, ?, ?, ?)";

	private static final String UPDATE_MODE_STMT = "UPDATE enhanced_notification_broker SET mode = ? WHERE resource_id = ?";

	private static final String UPDATE_MESSAGE_INDEX_STMT = "UPDATE enhanced_notification_broker SET message_index = ? WHERE resource_id = ?";

	private static final String LOAD_BROKER_STMT = "SELECT mode, message_index, forwarding_port_reference "
		+ "FROM enhanced_notification_broker WHERE resource_id = ?";

	private static final String GET_MESSAGE_INDEX_STMT = "SELECT message_index "
		+ "FROM enhanced_notification_broker WHERE resource_id = ?";

	private static final String GET_MESSAGE_INDEX_BY_CLIENT_ID_STMT = "SELECT message_index "
		+ "FROM enhanced_notification_broker WHERE client_id = ?";

	private static final String DELETE_BROKER_STMT = "DELETE FROM enhanced_notification_broker WHERE resource_id = ?";

	private static final String DELETE_ALL_SUBSCRIPTIONS_STMT = "DELETE FROM notification_broker_subscription WHERE broker_id = ?";

	private static final String DELETE_SUBSCRIPTION_STMT = "DELETE FROM notification_broker_subscription WHERE broker_id = ? AND subscription_identifier = ?";

	private static final String INSERT_SUBSCRIPTION_STMT = "INSERT INTO notification_broker_subscription "
		+ "(broker_id, subscription_identifier, publisher_key, termination_time) " + "VALUES (?, ?, ?, ?)";

	private static final String NOTIFICATION_BROKER_TABLE = "enhanced_notification_broker";
	private static final String FORWARDING_PORT_COLUMN = "forwarding_port_reference";

	public static void createTables(Connection connection) throws SQLException
	{
		DatabaseTableUtils
			.createTables(connection, false, CREATE_BROKER_TABLE_STMT, CREATE_BROKER_SUBCRIPTION_TRACE_TABLE_STMT);
	}

	public static void createNotificationBroker(String resourceId, boolean mode, int messageIndex, String clientId,
		EndpointReferenceType forwardingPort, Connection connection) throws ResourceException
	{

		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(CREATE_BROKER_STMT);
			stmt.setString(1, resourceId);
			stmt.setBoolean(2, mode);
			stmt.setInt(3, messageIndex);
			stmt.setString(4, clientId);
			if (forwardingPort == null) {
				stmt.setNull(5, Types.BLOB);
			} else {
				stmt.setBlob(5, EPRUtils.toBlob(forwardingPort, NOTIFICATION_BROKER_TABLE, FORWARDING_PORT_COLUMN));
			}
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new ResourceException("couldn't create the notification broker", e);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	public static void deleteNotificationBroker(String resourceId, Connection connection) throws ResourceException
	{
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		try {
			stmt1 = connection.prepareStatement(DELETE_BROKER_STMT);
			stmt1.setString(1, resourceId);
			stmt1.executeUpdate();
			stmt2 = connection.prepareStatement(DELETE_ALL_SUBSCRIPTIONS_STMT);
			stmt2.setString(1, resourceId);
			stmt2.executeUpdate();
		} catch (SQLException e) {
			throw new ResourceException("couldn't delete the notification broker", e);
		} finally {
			StreamUtils.close(stmt1);
			StreamUtils.close(stmt2);
		}
	}

	public static void deleteSubscriptions(String resourceId, Set<String> subscriptionEPIs, Connection connection)
		throws ResourceException
	{

		if (subscriptionEPIs == null || subscriptionEPIs.isEmpty())
			return;
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(DELETE_SUBSCRIPTION_STMT);
			for (String subsriptionEPI : subscriptionEPIs) {
				stmt.setString(1, resourceId);
				stmt.setString(2, subsriptionEPI);
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			throw new ResourceException("couldn't delete subscriptions of the notification broker", e);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	public static void loadNotificationBroker(String resourceId, NotificationBrokerDBResource uninitializedInstance,
		Connection connection) throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(LOAD_BROKER_STMT);
			stmt.setString(1, resourceId);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				uninitializedInstance.setMode(resultSet.getBoolean("mode"));
				uninitializedInstance.setMessageIndex(resultSet.getInt("message_index"));
				Blob blob = resultSet.getBlob("forwarding_port_reference");
				uninitializedInstance.setForwardingPort(EPRUtils.fromBlob(blob));
			}
		} catch (SQLException e) {
			throw new ResourceException("couldn't load the notification broker", e);
		} finally {
			StreamUtils.close(resultSet);
			StreamUtils.close(stmt);
		}
	}

	public static int getMessageIndexOfBroker(String resourceId, Connection connection) throws ResourceException
	{
		return getMessageIndexOfBroker(GET_MESSAGE_INDEX_STMT, resourceId, connection);
	}

	public static Integer getMessageIndexOfBrokerByClientId(String clientId, Connection connection)
	{
		try {
			return getMessageIndexOfBroker(GET_MESSAGE_INDEX_BY_CLIENT_ID_STMT, clientId, connection);
		} catch (ResourceException e) {
			return null;
		}
	}

	public static void updateMode(String resourceId, boolean mode, Connection connection) throws ResourceException
	{
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(UPDATE_MODE_STMT);
			stmt.setBoolean(1, mode);
			stmt.setString(2, resourceId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new ResourceException("couldn't update the mode of the notification broker", e);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	public static void increaseMessageIndicesOfBrokers(List<NotificationBrokerDBResource> brokerList, Connection connection)
		throws ResourceException
	{
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(UPDATE_MESSAGE_INDEX_STMT);
			for (NotificationBrokerDBResource resource : brokerList) {
				int newMessageIndex = resource.getMessageIndex() + 1;
				stmt.setInt(1, newMessageIndex);
				stmt.setString(2, resource.getKey());
				stmt.addBatch();
				resource.setMessageIndex(newMessageIndex);
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			throw new ResourceException("couldn't update the message indices of the notification brokers", e);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	public static void storeSubscriptionTraces(String brokerId, List<String> subscriptionEPIs, String publisherKey,
		long subscriptionTerminationTime, Connection connection) throws ResourceException
	{

		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(INSERT_SUBSCRIPTION_STMT);
			for (String subscriptionEPI : subscriptionEPIs) {
				stmt.setString(1, brokerId);
				stmt.setString(2, subscriptionEPI);
				stmt.setString(3, publisherKey);
				stmt.setLong(4, subscriptionTerminationTime);
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			throw new ResourceException("couldn't store subscription traces for broker", e);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	// We have to provide a safer implementation of this method to avoid to many IDs in a SQL IN
	// clause. Chances
	// are that the database will fail to handle a query that has more than several hundred of
	// elements in a IN
	// clause.
	public static List<NotificationBrokerDBResource> getBrokers(Collection<String> publisherKeys, List<String> resourceIds,
		String clientId, Connection connection) throws ResourceException
	{

		if (publisherKeys == null || publisherKeys.isEmpty())
			return null;
		if (resourceIds == null || resourceIds.isEmpty())
			return null;

		long currentTime = System.currentTimeMillis();
		String publisherKeysStr = joinStringsForInClause(publisherKeys);
		String brokerIdsStr = joinStringsForInClause(resourceIds);

		String activeBrokerQuery = "SELECT resource_id, message_index, forwarding_port_reference "
			+ "FROM enhanced_notification_broker broker WHERE broker.mode = 1 "
			+ "AND EXISTS (SELECT * FROM notification_broker_subscription subscription "
			+ "	WHERE subscription.broker_id = broker.resource_id " + "	AND subscription.publisher_key in (" + publisherKeysStr
			+ ")" + "	AND subscription.termination_time > ? ) " + "AND broker.resource_id IN (" + brokerIdsStr + ") "
			+ "AND broker.client_id != ?";

		String passiveBrokerQuery = "SELECT resource_id, message_index "
			+ "FROM enhanced_notification_broker broker WHERE broker.mode = 0 "
			+ "AND EXISTS (SELECT * FROM notification_broker_subscription subscription "
			+ "	WHERE subscription.broker_id = broker.resource_id " + "	AND subscription.publisher_key in (" + publisherKeysStr
			+ ")" + "	AND subscription.termination_time > ? ) " + "AND broker.resource_id IN (" + brokerIdsStr + ") "
			+ "AND broker.client_id != ?";

		List<NotificationBrokerDBResource> listOfBrokers = new ArrayList<NotificationBrokerDBResource>();
		PreparedStatement stmt1 = null;
		ResultSet resultSet1 = null;
		PreparedStatement stmt2 = null;
		ResultSet resultSet2 = null;
		try {
			stmt1 = connection.prepareStatement(activeBrokerQuery);
			stmt1.setLong(1, currentTime);
			stmt1.setString(2, clientId);
			resultSet1 = stmt1.executeQuery();
			while (resultSet1.next()) {
				NotificationBrokerDBResource resource = new NotificationBrokerDBResource(resultSet1.getString("resource_id"),
					null);
				resource.setMessageIndex(resultSet1.getInt("message_index"));
				Blob blob = resultSet1.getBlob("forwarding_port_reference");
				resource.setForwardingPort(EPRUtils.fromBlob(blob));
				resource.setMode(true);
				listOfBrokers.add(resource);
			}
			stmt2 = connection.prepareStatement(passiveBrokerQuery);
			stmt2.setLong(1, currentTime);
			stmt2.setString(2, clientId);
			resultSet2 = stmt2.executeQuery();
			while (resultSet2.next()) {
				NotificationBrokerDBResource resource = new NotificationBrokerDBResource(resultSet2.getString("resource_id"),
					null);
				resource.setMessageIndex(resultSet2.getInt("message_index"));
				resource.setMode(false);
				listOfBrokers.add(resource);
			}
			return listOfBrokers;

		} catch (SQLException e) {
			if (_logger.isDebugEnabled())
				_logger.debug("\n\n One of the following two queries failed in execution: \n\n" + activeBrokerQuery + "\n\n"
					+ passiveBrokerQuery);
			throw new ResourceException("Failed to load notification brokers", e);
		} finally {
			StreamUtils.close(resultSet1);
			StreamUtils.close(stmt1);
			StreamUtils.close(resultSet2);
			StreamUtils.close(stmt2);
		}
	}

	public static int getMessageIndexOfBroker(String sql, String resourceOrClientId, Connection connection)
		throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = connection.prepareStatement(sql);
			stmt.setString(1, resourceOrClientId);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				return resultSet.getInt("message_index");
			}
			throw new ResourceException("incorrect result-set size while retrieving message index");
		} catch (SQLException e) {
			throw new ResourceException("couldn't get the message index of the notification broker", e);
		} finally {
			StreamUtils.close(resultSet);
			StreamUtils.close(stmt);
		}
	}

	private static String joinStringsForInClause(Collection<String> stringCollection)
	{
		Set<String> setOfStrings = new HashSet<String>(stringCollection);
		StringBuilder buffer = new StringBuilder();
		for (String resourceId : setOfStrings) {
			buffer.append("'");
			buffer.append(resourceId);
			buffer.append("'").append(',');
		}
		int positionOfLastComma = buffer.length() - 1;
		buffer.deleteCharAt(positionOfLastComma);
		return buffer.toString();
	}
}
