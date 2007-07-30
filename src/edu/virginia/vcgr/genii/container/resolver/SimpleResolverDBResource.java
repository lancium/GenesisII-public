package edu.virginia.vcgr.genii.container.resolver;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.morgan.util.GUID;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class SimpleResolverDBResource extends BasicDBResource implements ISimpleResolverResource
{
	static public final String _TARGET_EPI_PROPERTY_NAME =	"simple-resolver-target-epi";
	static public final String _TARGET_EPR_PROPERTY_NAME =	"simple-resolver-target-epr";
	static public final String _MAPPING_VERSION =	"simple-resolver-mapping-version";
	static public final String _SUBSCRIPTION_GUID =	"simple-resolver-subscription-guid";
	static public final String _TERMINATE_SUBSCRIPTION_EPR_PROPERTY_NAME =	"simple-resolver-terminate-subscription-epr";
	static public final String _FACTORY_EPI_PROPERTY_NAME =	"resolver-factory-resource-id";
	static public final String _RESOLVER_EPI_PROPERTY_NAME =	"resolver-epi";
	static public final String _RESOLVER_EPR_PROPERTY_NAME = "simple-resolver-epr";
	
	public SimpleResolverDBResource(ResourceKey parentKey, DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	public void update(SimpleResolverEntry entry) 
		throws ResourceException
	{
		writeEntry(entry);
		
		try
		{	
			updateTerminateSubscription(entry);
		}
		catch(ResourceException re)
		{ }
	}

	public SimpleResolverEntry getEntry() 
		throws ResourceException
	{
		URI targetEPI = (URI) getProperty(_TARGET_EPI_PROPERTY_NAME);
		EndpointReferenceType targetEPR = EPRUtils.fromBytes((byte[])getProperty(_TARGET_EPR_PROPERTY_NAME));
		String versionStr = (String) getProperty(_MAPPING_VERSION);
		String subscriptionGUID = (String) getProperty(_SUBSCRIPTION_GUID);
		URI factoryEPI = (URI) getProperty(_FACTORY_EPI_PROPERTY_NAME);
		URI resolverEPI = (URI) getProperty(_RESOLVER_EPI_PROPERTY_NAME);
		EndpointReferenceType resolverEPR = EPRUtils.fromBytes((byte[])getProperty(_RESOLVER_EPR_PROPERTY_NAME));
		EndpointReferenceType subscriptionEPR = null;
		Object subscriptionObj = getProperty(_TERMINATE_SUBSCRIPTION_EPR_PROPERTY_NAME);
		if (subscriptionObj != null)
			subscriptionEPR = EPRUtils.fromBytes((byte[]) subscriptionObj);
		
		return new SimpleResolverEntry(targetEPI, targetEPR, (Integer.decode(versionStr)).intValue(), subscriptionGUID, subscriptionEPR, factoryEPI, resolverEPI, resolverEPR);
	}

	protected void updateTerminateSubscription(SimpleResolverEntry entry) 
		throws ResourceException
	{
		EndpointReferenceType subscriptionEPR = entry.getTerminateSubscription();
		EndpointReferenceType resolverEPR = entry.getResolverEPR();
		
		/* terminate old subscription */
		if (subscriptionEPR != null)
		{
			SimpleResolverUtils.terminateSubscription(subscriptionEPR);
			entry.setSubscriptionGUID(null);
			entry.setTerminateSubscription(null);
			writeEntry(entry);
		}
		
		/* add new subscription */
		EndpointReferenceType targetEPR = entry.getTargetEPR();
				
		if (targetEPR != null && resolverEPR != null)
		{
			String subscriptionGUID = new GUID().toString();
			entry.setSubscriptionGUID(subscriptionGUID);
			subscriptionEPR = SimpleResolverUtils.createTerminateSubscription(entry, resolverEPR);
			if (subscriptionEPR != null)
			{
				entry.setTerminateSubscription(subscriptionEPR);
				writeEntry(entry);
			}
		}
	}
	
	protected void writeEntry(SimpleResolverEntry entry)
		throws ResourceException
	{
		setProperty(_TARGET_EPI_PROPERTY_NAME, entry.getTargetEPI());
		setProperty(_TARGET_EPR_PROPERTY_NAME, EPRUtils.toBytes(entry.getTargetEPR()));
		setProperty(_MAPPING_VERSION, new Integer(entry.getVersion()).toString());
		setProperty(_SUBSCRIPTION_GUID, entry.getSubscriptionGUID());
		setProperty(_FACTORY_EPI_PROPERTY_NAME, entry.getFactoryEPI());
		setProperty(_RESOLVER_EPI_PROPERTY_NAME, entry.getResolverEPI());
		setProperty(_RESOLVER_EPR_PROPERTY_NAME, EPRUtils.toBytes(entry.getResolverEPR()));
		if (entry.getTerminateSubscription() == null)
			setProperty(_TERMINATE_SUBSCRIPTION_EPR_PROPERTY_NAME, (byte[]) null);
		else
			setProperty(_TERMINATE_SUBSCRIPTION_EPR_PROPERTY_NAME, EPRUtils.toBytes(entry.getTerminateSubscription()));
	}
	
	private static final String _LIST_ALL_RESOLVERS_STMT = 
		"SELECT p1.propvalue, p2.propvalue " +
		"FROM properties p1, properties p2 " +
		"where " +
			"p1.propname = '" + _RESOLVER_EPI_PROPERTY_NAME + 
			"' and " +
			"p1.propvalue is not null" +
			" and " +
			"p1.resourceid = p2.resourceid" +
			" and " +
			"p2.propname = '" + _RESOLVER_EPR_PROPERTY_NAME +
			"' and " +
			"p2.propvalue is not null";
	
	public HashMap<String, EndpointReferenceType> listAllResolvers() 
		throws ResourceException
    {
		ResultSet rs = null;
		PreparedStatement stmt = null;
		HashMap<String, EndpointReferenceType> results = new HashMap<String, EndpointReferenceType>();
		
		// Get list of resource ids for all simple resolvers on this server 
		try
		{
			stmt = _connection.prepareStatement(_LIST_ALL_RESOLVERS_STMT);
			rs = stmt.executeQuery();
			while(rs.next())
			{
				String nextEPI = rs.getString(1);
				EndpointReferenceType nextEPR = EPRUtils.fromBytes((byte[])rs.getObject(2));
				results.put(nextEPI, nextEPR);
			}
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			close(rs);
			close(stmt);
		}
		return results;
    }
}