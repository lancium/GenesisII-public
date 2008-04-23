package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import java.net.URI;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class RExportResolverDBResource extends BasicDBResource 
implements IRExportResolverResource
{
	static private Log _logger = LogFactory.getLog(
			RExportResolverDBResource.class);
	
	static public final String _COMMON_EPI_PROPERTY_NAME =
		"rexport-resolver-common-epi";
	static public final String _PRIMARY_EPR_PROPERTY_NAME =	
		"rexport-resolver-primary-epr";
	static public final String _REPLICA_EPR_PROPERTY_NAME = 
		"rexport-resolver-replica-epr";
	static public final String _RESOLVER_EPI_PROPERTY_NAME = 
		"rexport-resolver-epi";
	static public final String _RESOLVER_EPR_PROPERTY_NAME = 
		"rexport-resolver-epr";
	static public final String _LOCAL_PATH_PROPERTY_NAME = 
		"rexport-local-path";
	static public final String _RESOLVER_SERVICE_EPR_PROPERTY_NAME = 
		"rexport-resolver-service-epr";
	
	public RExportResolverDBResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool,
			IResourceKeyTranslater translater)
		throws SQLException
	{
		super(parentKey, connectionPool, translater);
	}
	
	public void update(RExportResolverEntry entry) 
		throws ResourceException
	{
		writeEntry(entry);
	}
	
	protected void writeEntry(RExportResolverEntry entry)
		throws ResourceException
	{
		if (entry.getCommonEPI() != null)
			setProperty(_COMMON_EPI_PROPERTY_NAME, entry.getCommonEPI());
		
		if (entry.getPrimaryEPR() != null)
			setProperty(_PRIMARY_EPR_PROPERTY_NAME, EPRUtils.toBytes(
					entry.getPrimaryEPR()));
		
		if (entry.getReplicaEPR() != null)
			setProperty(_REPLICA_EPR_PROPERTY_NAME, EPRUtils.toBytes(
					entry.getReplicaEPR()));
		
		if (entry.getResolverEPI() != null)
			setProperty(_RESOLVER_EPI_PROPERTY_NAME, entry.getResolverEPI());
		
		if (entry.getResolverEPR() != null)
			setProperty(_RESOLVER_EPR_PROPERTY_NAME, EPRUtils.toBytes(
					entry.getResolverEPR()));
		
		if (entry.getLocalPath() != null)
			setProperty(_LOCAL_PATH_PROPERTY_NAME, entry.getLocalPath());
		
		if (entry.getResolverServiceEPR() != null)
			setProperty(_RESOLVER_SERVICE_EPR_PROPERTY_NAME, EPRUtils.toBytes(
					entry.getResolverServiceEPR()));
		
	}
	
	public RExportResolverEntry getEntry()
		throws ResourceException
	{
		EndpointReferenceType primaryEPR = null;
		EndpointReferenceType replicaEPR = null;
		EndpointReferenceType resolverEPR = null;
		EndpointReferenceType resolverServiceEPR = null;
		
		URI commonEPI = (URI) getProperty(_COMMON_EPI_PROPERTY_NAME);
		
		if (getProperty(_PRIMARY_EPR_PROPERTY_NAME) != null)
			primaryEPR = EPRUtils.fromBytes((byte[])getProperty(
					_PRIMARY_EPR_PROPERTY_NAME));
		
		if (getProperty(_REPLICA_EPR_PROPERTY_NAME) !=null)
			replicaEPR = EPRUtils.fromBytes((byte[])getProperty(
					_REPLICA_EPR_PROPERTY_NAME));
		
		URI resolverEPI = (URI) getProperty(_RESOLVER_EPI_PROPERTY_NAME);
		
		if (getProperty(_RESOLVER_EPR_PROPERTY_NAME) != null)
			resolverEPR = EPRUtils.fromBytes((byte[])getProperty(
					_RESOLVER_EPR_PROPERTY_NAME));
		
		String localPath = (String) getProperty(_LOCAL_PATH_PROPERTY_NAME);
		
		if (getProperty(_RESOLVER_SERVICE_EPR_PROPERTY_NAME) != null)
			resolverServiceEPR = EPRUtils.fromBytes((byte[])getProperty(
					_RESOLVER_SERVICE_EPR_PROPERTY_NAME));
		
		
		return new RExportResolverEntry(commonEPI, primaryEPR, 
				replicaEPR, resolverEPI, resolverEPR, localPath, resolverServiceEPR);
	}
	
	static private final String _CREATE_RESOLVER_INFO =
		"INSERT INTO resolvermapping VALUES(?, ?, ?)";
	
	static private final String _DELETE_RESOLVER_INFO =
		"DELETE FROM resolvermapping WHERE resourceEPI = ?";
	
	static private final String _QUERY_FOR_RESOLVER_ = 
		"SELECT resolverEPR FROM resolvermapping WHERE resourceEPI = ?";
	
	public EndpointReferenceType queryForResourceResolver(String resourceEPI)
		throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<EndpointReferenceType> resolverEPRs = 
			new ArrayList<EndpointReferenceType>();
		
		try{
			stmt = _connection.prepareStatement(_QUERY_FOR_RESOLVER_);
			stmt.setString(1, resourceEPI);
			
			rs = stmt.executeQuery();
			
			while (rs.next()){
				resolverEPRs.add(EPRUtils.fromBlob(rs.getBlob(1)));
			}
			
		}
		catch (SQLException sqe){
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally{
			close(stmt);
		}
		
		//assuming first resolver is the correct one
		//what if no resolvers?
		if (resolverEPRs == null){
			_logger.info("No resolvers found in table for this resource query");
			return null;
		}
		
		return resolverEPRs.get(0);
	}
	
	/**
	 * Update table by creating/deleting mapping of resource to resolver
	 * 
	 * @param isResolverTermination: true if mapping is to be deleted;
	 *	false if mapping is to be created
	 */
	public void updateResolverResourceInfo(String resourceEPI, String resolverEPI, 
			EndpointReferenceType resolverEPR, boolean isResolverTermination)
		throws ResourceException
	{
		
		String updateStmt = null;
		
		if (isResolverTermination){
			updateStmt = _DELETE_RESOLVER_INFO;
			_logger.info("Deleting resolver/resource table mapping.");
		}
		else{
			updateStmt = _CREATE_RESOLVER_INFO;
			_logger.info("Adding resolver/resource table mapping.");
		}
		
		PreparedStatement stmt = null;
		
		try{
			stmt = _connection.prepareStatement(updateStmt);
			if (isResolverTermination){
				stmt.setString(1, resourceEPI);
			}
			else{
				stmt.setString(1, resourceEPI);
				stmt.setString(2, resolverEPI);
				stmt.setBlob(3, EPRUtils.toBlob(resolverEPR));
			}
			if (stmt.executeUpdate() != 1)
				throw new ResourceException(
					"Unable to modify resolver/resource information.");
		}
		catch (SQLException sqe){
			throw new ResourceException(
					"Could not update resolver-resource mapping in table", sqe);
		}
		finally{
			close(stmt);
		}
	}
}