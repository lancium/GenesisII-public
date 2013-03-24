package edu.virginia.vcgr.genii.container.common.notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicy;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicyTypes;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;

public class SubscriptionsDatabase
{
	static private Log _logger = LogFactory.getLog(SubscriptionsDatabase.class);

	static final private String[] TABLE_STMTS = new String[] {
		"CREATE TABLE wsnsubscriptions(" + "subscriptionresourcekey VARCHAR(128) PRIMARY KEY,"
			+ "publisherresourcekey VARCHAR(128) NOT NULL," + "subscriptionreference BLOB(2G) NOT NULL,"
			+ "consumerreference BLOB(2G)," + "topicquery BLOB(2G)," + "policies BLOB(2G)," + "additionaluserdata BLOB(2G),"
			+ "paused SMALLINT NOT NULL WITH DEFAULT 0)",
		"CREATE INDEX wsnsubscriptionspubreskeyidx ON wsnsubscriptions(publisherresourcekey)" };

	static private void destroySubscription(Connection connection, String subscriptionkey) throws ResourceException
	{
		DBSubscriptionResource resource = new DBSubscriptionResource(subscriptionkey, connection);
		resource.destroy();
		StreamUtils.close(resource);
	}

	static void createTables(Connection connection) throws SQLException
	{
		DatabaseTableUtils.createTables(connection, false, TABLE_STMTS);
	}

	static void createSubscription(Connection connection, String subscriptionResourceKey, String publisherResourceKey,
		EndpointReferenceType subscriptionReference, EndpointReferenceType consumerReference, TopicQueryExpression topicFilter,
		Map<SubscriptionPolicyTypes, SubscriptionPolicy> policies, AdditionalUserData additionalUserData) throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("INSERT INTO wsnsubscriptions(subscriptionresourcekey,"
				+ "publisherresourcekey, subscriptionreference," + "consumerreference, topicquery,"
				+ "policies, additionaluserdata) " + "VALUES (?, ?, ?, ?, ?, ?, ?)");

			stmt.setString(1, subscriptionResourceKey);
			stmt.setString(2, publisherResourceKey);

			if (subscriptionReference != null)
				stmt.setBlob(3, EPRUtils.toBlob(subscriptionReference, "wsnsubscriptions", "subscriptionreference"));
			else
				stmt.setNull(3, Types.BLOB);

			if (consumerReference != null)
				stmt.setBlob(4, EPRUtils.toBlob(consumerReference, "wsnsubscriptions", "consumerreference"));
			else
				stmt.setNull(4, Types.BLOB);

			if (topicFilter != null)
				stmt.setBlob(5, DBSerializer.toBlob(topicFilter, "wsnsubscriptions", "topicquery"));
			else
				stmt.setNull(5, Types.BLOB);

			if (policies != null)
				stmt.setBlob(6, DBSerializer.toBlob(policies, "wsnsubscriptions", "policies"));
			else
				stmt.setNull(6, Types.BLOB);

			if (additionalUserData != null)
				stmt.setBlob(7, DBSerializer.toBlob(additionalUserData, "wsnsubscriptions", "additionaluserdata"));
			else
				stmt.setNull(7, Types.BLOB);

			stmt.executeUpdate();
		} catch (ResourceException e) {
			throw new SQLException("Unable to serialize EPR to blob.", e);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	static void deleteSubscription(Connection connection, String resourceKey) throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("DELETE FROM wsnsubscriptions WHERE subscriptionresourcekey = ?");
			stmt.setString(1, resourceKey);
			stmt.executeUpdate();
		} finally {
			StreamUtils.close(stmt);
		}
	}

	static public void destroyMySubscriptions(Connection connection, String publisherKey) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT subscriptionresourcekey FROM wsnsubscriptions "
				+ "WHERE publisherresourcekey = ?");
			stmt.setString(1, publisherKey);
			rs = stmt.executeQuery();

			while (rs.next()) {
				String subKey = null;

				try {
					subKey = rs.getString(1);
					destroySubscription(connection, subKey);
				} catch (Throwable e) {
					_logger.warn(String.format("Unable to destroy subscription %s for resource %s.", subKey, publisherKey), e);
				}
			}
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	static public Collection<WSNSubscriptionInformation> subscriptionsForPublisher(Connection connection, String publisherKey,
		TopicPath topic) throws SQLException
	{
		Collection<WSNSubscriptionInformation> subscriptions = new LinkedList<WSNSubscriptionInformation>();

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT subscriptionreference, consumerreference, topicquery, "
				+ "policies, additionaluserdata FROM wsnsubscriptions " + "WHERE publisherresourcekey = ? AND paused = 0");
			stmt.setString(1, publisherKey);
			rs = stmt.executeQuery();

			while (rs.next()) {
				addSubscriptionFromResults(topic, subscriptions, rs);
			}
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}

		return subscriptions;
	}

	@SuppressWarnings("unchecked")
	static public Collection<WSNSubscriptionInformation> getSubscriptionsForIndirectPublishers(Connection connection,
		Collection<String> publishers, TopicPath topic) throws SQLException
	{

		if (publishers == null || publishers.isEmpty())
			return Collections.EMPTY_LIST;

		Collection<WSNSubscriptionInformation> subscriptions = new ArrayList<WSNSubscriptionInformation>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql = "SELECT subscriptionreference, consumerreference, topicquery, "
				+ "policies, additionaluserdata FROM wsnsubscriptions " + "WHERE paused = 0 and publisherresourcekey in ("
				+ joinStringsForInClause(publishers) + ")";

			stmt = connection.prepareStatement(sql);
			rs = stmt.executeQuery();

			while (rs.next()) {
				addSubscriptionFromResults(topic, subscriptions, rs);
			}
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}

		return subscriptions;
	}

	static public void toggleSubscriptionPause(Connection connection, String subscriptionKey, boolean markPaused)
		throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("UPDATE wsnsubscriptions SET paused = ? " + "WHERE subscriptionresourcekey = ?");

			stmt.setShort(1, markPaused ? (short) 1 : (short) 0);
			stmt.setString(2, subscriptionKey);
			stmt.executeUpdate();
		} finally {
			StreamUtils.close(stmt);
		}
	}

	static public Set<String> getIndirectPublishersKeys(String originalPublisher, String query, Connection connection)
	{

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Set<String> indirectPublishers = new HashSet<String>();
		try {
			stmt = connection.prepareStatement(query);
			stmt.setString(1, originalPublisher);
			rs = stmt.executeQuery();
			while (rs.next()) {
				indirectPublishers.add(rs.getString(1));
			}
		} catch (SQLException e) {
			_logger.warn("failed to load indirect publisher keys for notification", e);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
		return indirectPublishers;
	}

	private static void addSubscriptionFromResults(TopicPath topic, Collection<WSNSubscriptionInformation> subscriptions,
		ResultSet rs) throws SQLException
	{
		try {
			EndpointReferenceType subscriptionReference = EPRUtils.fromBlob(rs.getBlob(1));
			EndpointReferenceType consumerReference = EPRUtils.fromBlob(rs.getBlob(2));
			TopicQueryExpression topicFilter = (TopicQueryExpression) DBSerializer.fromBlob(rs.getBlob(3));

			@SuppressWarnings("unchecked")
			Map<SubscriptionPolicyTypes, SubscriptionPolicy> policies = (Map<SubscriptionPolicyTypes, SubscriptionPolicy>) DBSerializer
				.fromBlob(rs.getBlob(4));

			AdditionalUserData additionalUserData = (AdditionalUserData) DBSerializer.fromBlob(rs.getBlob(5));

			if (topic == null || topicFilter == null || topicFilter.matches(topic)) {
				subscriptions.add(new WSNSubscriptionInformation(subscriptionReference, consumerReference, topicFilter,
					policies, additionalUserData));
			}
		} catch (ResourceException e) {
			_logger.warn("Error trying to load subscription from database.", e);
		}
	}

	private static String joinStringsForInClause(Collection<String> listOfStrings)
	{
		StringBuilder buffer = new StringBuilder();
		for (String resourceId : listOfStrings) {
			buffer.append("'");
			buffer.append(resourceId);
			buffer.append("'").append(',');
		}
		int positionOfLastComma = buffer.length() - 1;
		buffer.deleteCharAt(positionOfLastComma);
		return buffer.toString();
	}
}
