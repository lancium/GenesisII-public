package edu.virginia.vcgr.genii.container.common.notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.notification.UserDataType;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class DBSubscriptionResource 
	extends BasicDBResource implements ISubscriptionResource
{
	static private Log _logger = LogFactory.getLog(DBSubscriptionResource.class);
	
	static private final String _CREATE_SUBSCRIPTION =
		"INSERT INTO subscriptions VALUES (?, ?, ?, ?, ?)";
	static private final String _DESTROY_SUBSCRIPTION =
		"DELETE FROM subscriptions WHERE subscriptionid = ?";
	static private final String _SELECT_ALL_SUBSCRIPTIONS =
		"SELECT subscriptionid, topic, targetendpoint, userdata " +
		"FROM subscriptions WHERE sourcekey = ?";
	static private final String _SELECT_SUBSCRIPTIONS_BEGIN =
		"SELECT subscriptionid, topic, targetendpoint, userdata " +
		"FROM subscriptions WHERE sourcekey = ? AND ";
	
	public DBSubscriptionResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	public void initialize(HashMap<QName, Object> constructionParams)
		throws ResourceException
	{
		super.initialize(constructionParams);
		if (_resourceKey.startsWith(_SPECIAL_SERVICE_KEY_TEMPLATE + 
			getParentResourceKey().getServiceName()))
			return;
		
		String sourcekey = (String)constructionParams.get(
			ISubscriptionResource.SOURCE_KEY_CONSTRUCTION_PARAMETER);
		if (sourcekey == null)
			throw new ResourceException("Couldn't identify source key.");
		
		String topic = (String)constructionParams.get(
			ISubscriptionResource.TOPIC_CONSTRUCTION_PARAMETER);
		if (topic == null)
			throw new ResourceException("Topic construction parameter not set.");
		
		EndpointReferenceType targetendpoint =
			(EndpointReferenceType)constructionParams.get(
				ISubscriptionResource.TARGET_ENDPOINT_CONSTRUCTION_PARAMTER);
		if (targetendpoint == null)
			throw new ResourceException(
				"Couldn't locate target endpoint for subscription.");
		
		UserDataType userData = (UserDataType)constructionParams.get(
			ISubscriptionResource.USER_DATA_CONSTRUCTION_PARAMETER);
		
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_CREATE_SUBSCRIPTION);
			stmt.setString(1, _resourceKey);
			stmt.setString(2, sourcekey);
			stmt.setString(3, topic);
			stmt.setBlob(4, EPRUtils.toBlob(targetendpoint,
				"subscriptions", "targetendpoint"));
			if (userData == null)
				stmt.setNull(5, Types.VARBINARY);
			else
				stmt.setObject(5, ObjectSerializer.toBytes(userData,
					ISubscriptionResource.USER_DATA_CONSTRUCTION_PARAMETER));

			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Couldn't create resource.");
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	public void destroy() throws ResourceException
	{
		super.destroy();
		
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_DESTROY_SUBSCRIPTION);
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	static public Collection<SubscriptionInformation> matchSubscriptions(
		BasicDBResource sourceResource, String topicExpression)
			throws ResourceException
	{
		String sourceResourceKey = (String)sourceResource.getKey();
		Connection connection = sourceResource.getConnection();
		PreparedStatement stmt = null;
		ResultSet result = null;
	
		try
		{
			if (topicExpression == null || (topicExpression.length() == 0))
			{
				stmt = connection.prepareStatement(_SELECT_ALL_SUBSCRIPTIONS);
				stmt.setString(1, sourceResourceKey);
			} else
			{
				String []topicPieces = topicExpression.split("\\.");
				String expression = _SELECT_SUBSCRIPTIONS_BEGIN;
				expression += "( ";
				for (int lcv = 0; lcv < topicPieces.length; lcv++)
				{
					if (lcv != 0)
						expression += " OR";
					expression += " topic = ?";
				}
				expression += " )";
				stmt = connection.prepareStatement(expression);
				stmt.setString(1, sourceResourceKey);
				String current = "";
				for (int lcv = 0; lcv < topicPieces.length; lcv++)
				{
					if (lcv != 0)
						current += ".";
					current += topicPieces[lcv];
					stmt.setString(2 + lcv, current);
				}
			}
			
			result = stmt.executeQuery();
			ArrayList<SubscriptionInformation> ret = 
				new ArrayList<SubscriptionInformation>();
			while (result.next())
			{
				UserDataType userData = null;
				byte []userDataData = (byte[])result.getObject(4);
				if (userDataData != null)
				{
					userData = ObjectDeserializer.fromBytes(
						UserDataType.class, userDataData);
				}
				
				ret.add(new SubscriptionInformation(
					result.getString(1),
					result.getString(2),
					EPRUtils.fromBlob(result.getBlob(3)),
					userData));
			}
			
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			StreamUtils.close(result);
			StreamUtils.close(stmt);
		}
	}
	
	static public void destroySubscriptions(BasicDBResource sourceResource)
		throws ResourceException
	{
		Collection<SubscriptionInformation> subscriptions = 
			matchSubscriptions(sourceResource, null);
		
		/* Added by Mark Morgan to fix a bug */
		for (SubscriptionInformation subInfo : subscriptions)
		{
			try
			{
				ResourceManager.getTargetResource(
					subInfo.getTarget()).destroy();
			}
			catch (Throwable cause)
			{
				_logger.warn(String.format(
					"Unable to delete subscription %s.",
					subInfo.getSubscriptionKey()), cause);
			}
		}
	}
}