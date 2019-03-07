package edu.virginia.vcgr.genii.container.resource.db;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.serial.SerialBlob;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.comm.axis.Elementals;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.Rollbackable;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.common.MatchingParameter;
import edu.virginia.vcgr.genii.container.common.notification.SubscriptionsDatabase;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContainerService;
import edu.virginia.vcgr.genii.container.db.ServerDatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.query.ResourceSummary;
import edu.virginia.vcgr.genii.container.security.authz.providers.AclAuthZProvider;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.acl.Acl;
import edu.virginia.vcgr.genii.security.acl.AclEntry;

public class BasicDBResource implements IResource
{
	static protected final String _SPECIAL_SERVICE_KEY_TEMPLATE = "edu.virginia.vcgr.genii.container.resource.db.special-service-key.";

	static private final String _VERIFY_STMT = "SELECT createtime FROM resources WHERE resourceid = ?";
	static private final String _CREATE_STMT = "INSERT INTO resources VALUES(?, ?)";
	static private final String _REMOVE_PROPERTY_STMT = "DELETE FROM properties WHERE resourceid = ? AND propname = ?";
	static private final String _INSERT_PROPERTY_STMT = "INSERT INTO properties VALUES (?, ?, ?)";
	static private final String _GET_PROPERTY_STMT = "SELECT propvalue FROM properties WHERE resourceid = ? AND propname = ?";
	static private final String _DESTROY_KEYS_STMT = "DELETE FROM resources WHERE resourceid = ?";
	static private final String _DESTROY_PROPERTIES_STMT = "DELETE FROM properties WHERE resourceid = ?";
	static private final String _DESTROY_MATCHING_PARAMS_STMT = "DELETE FROM matchingparams WHERE resourceid = ?";
	static private final String _GET_ACL_ITEMS = "SELECT PrincipalEPI, permissions FROM AccessMatrix WHERE ResourceEPI = ?";
	static private final String _DELETE_ACL_ITEMS = "DELETE FROM  AccessMatrix  WHERE ResourceEPI = ?";
	static private final String _GET_PRINCIPAL_STMT = "SELECT ACLEntry FROM X509Identities WHERE PrincipalEPI = ?";
	static private final String _REMOVE_PRINCIPAL_STMT = "DELETE FROM X509Identities WHERE PrincipalEPI = ?";
	static private final String _INSERT_PRINCIPAL_STMT = "INSERT INTO X509Identities VALUES (?, ?)";
	static private final String _REMOVE_ACLENTRY_STMT = "DELETE FROM AccessMatrix WHERE ResourceEPI  = ? AND PrincipalEPI = ?";
	static private final String _INSERT_ACLENTRY_STMT = "INSERT INTO AccessMatrix VALUES (?, ?, ?)";
	static private final int ACL_STRING_CACHE_SIZE = 100000;
	static private Log _logger = LogFactory.getLog(BasicDBResource.class);

	static private TimedOutLRUCache<String, Acl> aclCache = new TimedOutLRUCache<String, Acl>(100, 10000000, "ACL cache");
	static private TimedOutLRUCache<String, String> aclStringCache =
		new TimedOutLRUCache<String, String>(ACL_STRING_CACHE_SIZE, 10000000, "aclstring cache");
	static private TimedOutLRUCache<String, String> rkeyToEPICache =
			new TimedOutLRUCache<String, String>(ACL_STRING_CACHE_SIZE, 10000000, "Resource Key to EPI cache cache");
	static private TimedOutLRUCache<String, AclEntry> principalCache =
		new TimedOutLRUCache<String, AclEntry>(100, 10000000, "auth principal cache");

	protected ServerDatabaseConnectionPool _connectionPool;
	protected Connection _connection;
	protected String _resourceKey;
	protected ResourceKey _parentKey;

	protected BasicDBResource(String parentKey, Connection connection)
	{
		_parentKey = null;
		_resourceKey = parentKey;
		_connection = connection;
		_connectionPool = null;
	}

	public BasicDBResource(ResourceKey parentKey, ServerDatabaseConnectionPool connectionPool) throws SQLException
	{
		_parentKey = parentKey;
		_connectionPool = connectionPool;
		_connection = _connectionPool.acquire(false);
	}

	/**
	 * if the "iresource" is one of our objects, we'll return the new ACL format. if it's not our type of object, we'll just return its ACL
	 * property under the old name, which might not exist.
	 * 
	 * @throws SQLException 
	 * @throws ResourceException 
	 */
	static public Acl rationalizeAcl(IResource iresource) throws ResourceException
	{
		Acl toReturn = null;
		if (iresource instanceof BasicDBResource) { 
			BasicDBResource resource = (BasicDBResource) iresource;
			try {
				toReturn = resource.getAcl();
			} catch (SQLException e) {
				String msg = "failed to get ACL string for resource: " + iresource.toString();
				_logger.warn(msg, e);
				throw new ResourceException(msg);
			}
		} else {
			toReturn = (Acl) iresource.getProperty(AclAuthZProvider.GENII_ACL_PROPERTY_NAME);
		}
		return toReturn;
	}
	
	public Connection getConnection()
	{
		return _connection;
	}

	@Override
	protected void finalize() throws Throwable
	{
		try {
			StreamUtils.close(this);
		} finally {
			super.finalize();
		}
	}

	@Override
	public String getKey()
	{
		if (_resourceKey.startsWith(_SPECIAL_SERVICE_KEY_TEMPLATE))
			return null;

		return _resourceKey;
	}

	@Override
	public Object getLockKey()
	{
		return _resourceKey;
	}

	@Override
	public void initialize(GenesisHashMap constructionParams) throws ResourceException
	{
		Boolean b = (Boolean) constructionParams.get(IResource.IS_SERVICE_CONSTRUCTION_PARAM);
		if (b != null && b.booleanValue())
			_resourceKey = _SPECIAL_SERVICE_KEY_TEMPLATE + _parentKey.getServiceName();
		else
			_resourceKey = new GUID().toString();

		PreparedStatement stmt = null;

		try {
			stmt = _connection.prepareStatement(_CREATE_STMT);
			stmt.setString(1, _resourceKey);
			stmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Couldn't create resource.");
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void load(String resourceKey) throws ResourceUnknownFaultType, ResourceException
	{
		_resourceKey = resourceKey;

		if (_resourceKey == null)
			_resourceKey = _SPECIAL_SERVICE_KEY_TEMPLATE + _parentKey.getServiceName();

		ResultSet rs = null;
		PreparedStatement stmt = null;

		try {
			if (_logger.isTraceEnabled())
				_logger.trace("looking up resource: " + _resourceKey);
			stmt = _connection.prepareStatement(_VERIFY_STMT);
			stmt.setString(1, _resourceKey);
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("load time is " + (System.currentTimeMillis() - startTime));

			if (!rs.next()) {
				// the special key is not always found as a database resource.
				if (_logger.isDebugEnabled() && !_resourceKey.contains(_SPECIAL_SERVICE_KEY_TEMPLATE)) {
					_logger.debug("did not find resource '" + _resourceKey + "'.");
				}
				throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Resource \"" + _resourceKey + "\" is unknown.") }, null));
			}
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	static public void setProperty(Connection connection, String resourceKey, String propertyName, Object value) throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement(_REMOVE_PROPERTY_STMT);
			stmt.setString(1, resourceKey);
			stmt.setString(2, propertyName);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;
			if (value == null)
				return;
			stmt = connection.prepareStatement(_INSERT_PROPERTY_STMT);
			stmt.setString(1, resourceKey);
			stmt.setString(2, propertyName);

			Blob b = DBSerializer.toBlob(value, "properties", "propvalue");
			if (b != null) {
				if (_logger.isTraceEnabled())
					_logger.trace("Serializing " + b.length() + " bytes into property database.");
				if (b.length() <= 0) {
					_logger.error("Attempt to serialize property \"" + propertyName + "\" with 0 bytes into the property database.");
				} else if (b.length() >= 128 * 1024) {
					_logger.error(
						"Attempt to serialize property \"" + propertyName + "\" of length " + b.length() + " bytes into a " + "128K space.");
				}
			}

			stmt.setBlob(3, b);
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update property \"" + propertyName + "\".");
		} finally {
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void setProperty(String propertyName, Object value) throws ResourceException
	{
		try {
			setProperty(_connection, _resourceKey, propertyName, value);
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}

	public String setAclMatrix(Acl acl, boolean updateDB) throws ResourceException, SQLException
	{
		HashSet<AclEntry> ids = new HashSet<AclEntry>(); // unique set of AclEntries
		ids.addAll(acl.executeAcl);
		ids.addAll(acl.readAcl);
		ids.addAll(acl.writeAcl);
		String aclString = "";
		// First, if we are updating the database, remove all the old entries
		if (updateDB) {
			removeAclMatrix(_connection,_resourceKey);
			// Now dump the cache entries for this resource
			String epi = getEPI(_connection, _resourceKey);
			aclCache.remove(epi);
			aclStringCache.remove(epi);
		}

		// Now go ahead and update.
		for (AclEntry entry : ids) {
			AclEntry temp = null;
			String epi = null;
			if (entry == null) {
				// This is a bizzare case. In the old implementation a null was used in an access control list
				// to indicate "everyone". I find this strange. ASG
				epi = "EVERYONE";
				if (_logger.isTraceEnabled())
					_logger.debug("Translating an entry for EVERYONE");
			} else {
				if (_logger.isTraceEnabled())
					_logger.debug("examining entry: " + entry.describe(VerbosityLevel.HIGH));

				// Now check if there is already an entry in the X509 database, if so, skip
				// Else, create an entry for this AclEntry
				epi = entry.getEPI(false);
				if (_logger.isTraceEnabled())
					_logger.debug("The EPI  :" + epi.toString());
				/*
				 * Note that the EPI returned will be formed in one of several ways: if from an X509 with an EPI in the DN, then the EPI if
				 * from an X509 without an EPI, then "SN:issuerDN:serial_number" if a username/password, then with the false flag it will have
				 * the (hashed) password embedded. If an X.509 pattern, then an EPI with a new GUID will be returned.
				 */
				if (updateDB) {
					temp = getPrincipalfromDB(epi);
					if (temp == null) {
						// The entry is not in the DB, put it there
						createPrincipalinDB(epi, entry);
					}
				}
			}
			// Ok, now the principal is in the database, need to determine what that principal can do
			String permissions = " ";
			if (acl.readAcl.contains(entry)) {
				// It is in the read list, add "R"
				permissions = permissions + "r";
			}
			if (acl.writeAcl.contains(entry)) {
				// It is in the read list, add "R"
				permissions = permissions + "w";
			}
			if (acl.executeAcl.contains(entry)) {
				// It is in the read list, add "R"
				permissions = permissions + "x";
			}
			// now add the access matrix entry
			if (updateDB)
				addACLMatrixEntry(epi, permissions);
			aclString = aclString + ";" + epi + permissions;
		}
		// System.out.println("SetACLMatrix done :");
		return aclString;
	}

	public String ACLtoAclString(Acl acl)
	{
		try {
			return setAclMatrix(acl, false);
		} catch (ResourceException | SQLException e) {
			return null;
		}
	}

	public Acl aclStringToAcl(String aclString)
	{
		/*
		 * This constructor takes an aclString and parses it, looking up the principals in the principal DB, and building up the read write,
		 * and execute access control lists.
		 */
		// While there are elements in the acl list, break them out;
		Acl rval = new Acl();
		String current = aclString.substring(0, aclString.indexOf(';'));
		String remainder = aclString.substring(aclString.indexOf(';') + 1);
		while (current != "") {
			// current will be of the form "principalID permissions"
			String principal = current.substring(0, current.indexOf(' '));
			String permissions = current.substring(current.indexOf(' ') + 1);
			// Now get the principal from the database
			boolean found = false;
			AclEntry aclPrincipal = null;
			if (principal.equalsIgnoreCase("EVERYONE")) {
				found = true;
			} else {
				aclPrincipal = getPrincipalfromDB(principal);
				// if is not there, something is not right. it should never happen.
				if (aclPrincipal == null) {
					found = false;
					_logger
						.error("Trying to reconstruct AclEntry from aclString, principal \"" + principal + "\" not in principal database. ");
					// This should not happen - EVER - but yet it has. So what to do. We'll just skip it
				} else
					found = true;
			}
			if (found) {
				if ((permissions.indexOf('R') >= 0) || (permissions.indexOf('r') >= 0))
					rval.readAcl.add(aclPrincipal);
				if ((permissions.indexOf('W') >= 0) || (permissions.indexOf('w') >= 0))
					rval.writeAcl.add(aclPrincipal);
				if ((permissions.indexOf('X') >= 0) || (permissions.indexOf('x') >= 0))
					rval.executeAcl.add(aclPrincipal);
			}
			if (remainder.indexOf(';') >= 0) {
				current = remainder.substring(0, remainder.indexOf(';'));
				remainder = remainder.substring(remainder.indexOf(';') + 1);
			} else // end the loop
				current = "";
		}
		return rval;
	}

	public boolean translateOldAcl() throws ResourceException, SQLException
	{
		Acl acl = (Acl) getProperty(AclAuthZProvider.GENII_ACL_PROPERTY_NAME);
		if (acl == null)
			return false;
		String result = setAclMatrix(acl, true);
		if (_logger.isTraceEnabled())
			_logger.debug("acl translation result: " + result);
		return true;
	}

	public Acl getAcl() throws ResourceException, SQLException
	{

		Acl acl = null;
		/*
		 * So now instead we need to resource.getAclString(); if null - translate aclstring, then getaclstring Acl acl = new Acl(aclString)
		 */
		String EPI=rkeyToEPICache.get(_resourceKey);
		if (EPI==null) {
			if (_logger.isDebugEnabled())
				_logger.debug("getAcl epi cache miss for resource: " + _resourceKey);
			EPI = getEPI(_connection, _resourceKey);
			rkeyToEPICache.put(_resourceKey, EPI);
		}
		acl = aclCache.get(EPI);
		if (acl != null) {
			return acl;
		}
		String aclString = getACLString(false);
		if (aclString == null) {
			_logger.debug("translating an old ACL into new form");
			translateOldAcl();
			aclString = getACLString(false);
		}
		if (aclString != null) {
			acl = aclStringToAcl(aclString);
			aclCache.put(EPI, acl);
		} else {
			String msg = "utterly failed to find the ACL for this resource: " + _resourceKey;
			_logger.debug(msg);
			throw new ResourceException(msg);
		}
		if (_logger.isDebugEnabled())
			_logger.debug("getAcl succeeded for resource: " + _resourceKey);
		return acl;
	}

	public void addACLMatrixEntry(String principalEPI, String permission) throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		// Will need to first look up the EPI of the resource
		try {
			String EPI = getEPI(_connection, _resourceKey);
			// Then we remove an entry if it is there
			stmt = _connection.prepareStatement(_REMOVE_ACLENTRY_STMT);
			stmt.setString(1, EPI);
			stmt.setString(2, principalEPI);
			if (stmt.execute()) {
				// worked ok, there was something there
			}
			stmt.close();
			// Then we insert it
			stmt = _connection.prepareStatement(_INSERT_ACLENTRY_STMT);
			stmt.setString(1, EPI);
			stmt.setString(2, principalEPI);
			stmt.setString(3, permission);
			stmt.executeUpdate();
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	static public void removeAclMatrix(Connection connection, String resourceKey) throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String EPI = getEPI(connection, resourceKey);
			stmt = connection.prepareStatement(_DELETE_ACL_ITEMS);
			stmt.setString(1, EPI);
			stmt.executeUpdate();

		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}

	}

	@Override
	public String getACLString(boolean sanitize) throws ResourceException
	{
		// ASG 12/23/2015
		String ACLString = null;

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String EPI = getEPI(_connection, _resourceKey);
			ACLString = aclStringCache.get(EPI);
			if (ACLString != null) {
				return ACLString;
			}
			stmt = _connection.prepareStatement(_GET_ACL_ITEMS);
			stmt.setString(1, EPI);
			rs = stmt.executeQuery();
			// if (!rs.next())
			// return null;
			while (rs.next()) {
				String principal = rs.getString(1);
				String permissions = rs.getString(2);
				if (ACLString == null)
					ACLString = "";
				ACLString = ACLString + principal + " " + permissions + " ;";
			}
			aclStringCache.put(EPI, ACLString);
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}

		return ACLString;
	}

	static public Object getProperty(Connection connection, String resourceKey, String propertyName) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement(_GET_PROPERTY_STMT);
			stmt.setString(1, resourceKey);
			stmt.setString(2, propertyName);
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("getProperty " + propertyName + ": time is " + (System.currentTimeMillis() - startTime));

			if (!rs.next())
				return null;

			Blob blob = rs.getBlob(1);
			if (blob == null)
				return null;

			return DBSerializer.fromBlob(blob);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	@Override
	public Object getProperty(String propertyName) throws ResourceException
	{
		boolean exceptionOccurred = true;

		try {
			if (_connection == null) {
				_connection = _connectionPool.acquire(false);
				exceptionOccurred = false;
			}

			return getProperty(_connection, _resourceKey, propertyName);
		}

		catch (SQLException sqe) {
			throw new ResourceException("Unable to get property.", sqe);
		}

		finally {
			if (exceptionOccurred == false) {
				_connectionPool.release(_connection);
				_connection = null;
			}
		}
	}

	@Override
	public void destroy() throws ResourceException
	{
		PreparedStatement stmt = null;
		// ASG 2019-03-06. Need to update this to also remove accessMatrix entries, Resources entries, and resources2 entries.
		try {
			stmt = _connection.prepareStatement(_DESTROY_PROPERTIES_STMT);
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
			stmt.close();
			stmt = _connection.prepareStatement(_DESTROY_KEYS_STMT);
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
			stmt.close();
			stmt = _connection.prepareStatement(_DESTROY_MATCHING_PARAMS_STMT);
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
			stmt.close();
			stmt = _connection.prepareStatement("DELETE FROM persistedproperties WHERE resourceid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
			ResourceSummary.removeResources(_connection, _resourceKey);

			SubscriptionsDatabase.destroyMySubscriptions(_connection, _resourceKey);

			HistoryContainerService service = ContainerServices.findService(HistoryContainerService.class);
			service.deleteRecords(_resourceKey);
		} catch (SQLException sqe) {
			throw new ResourceException("Error while trying to destroy resource.", sqe);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	@Override
	synchronized public void commit() throws ResourceException
	{
		if (_connection == null) {
			// It's already been closed
			return;
		}

		try {
			_connection.commit();
		} catch (SQLException sqe) {
			_logger.warn(sqe);
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}

	@Override
	public void rollback()
	{
		if (_connection == null) {
			// It's already been closed.
			return;
		}

		try {
			_connection.rollback();
		} catch (SQLException sqe) {
			_logger.error(sqe);
		}
	}

	@Override
	synchronized public void close() throws IOException
	{
		if (_connection != null && _connectionPool != null) {
			_connectionPool.release(_connection);
			_connection = null;
		}
	}

	@Override
	public Rollbackable getParentResourceKey()
	{
		return (Rollbackable) _parentKey;
	}

	static protected void destroyAll(Connection connection, Collection<String> keys) throws ResourceException
	{
		PreparedStatement destroyKeyStmt = null;
		PreparedStatement destroyPropertiesStmt = null;

		try {
			destroyKeyStmt = connection.prepareStatement(_DESTROY_KEYS_STMT);
			destroyPropertiesStmt = connection.prepareStatement(_DESTROY_PROPERTIES_STMT);

			for (String key : keys) {
				destroyKeyStmt.setString(1, key);
				destroyKeyStmt.executeUpdate();

				destroyPropertiesStmt.setString(1, key);
				destroyPropertiesStmt.executeUpdate();
			}
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(destroyKeyStmt);
			StreamUtils.close(destroyPropertiesStmt);
		}
	}

	/**
	 * Return whether or not the resource is a service resource
	 */
	@Override
	public boolean isServiceResource()
	{
		if (_resourceKey.startsWith(_SPECIAL_SERVICE_KEY_TEMPLATE)) {
			return true;
		}
		return false;
	}

	@Override
	public Collection<MatchingParameter> getMatchingParameters() throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Collection<MatchingParameter> ret = new LinkedList<MatchingParameter>();

		try {
			stmt = _connection.prepareStatement("SELECT paramname, paramvalue FROM matchingparams " + "WHERE resourceid = ?");
			stmt.setString(1, _resourceKey);
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("getMatchingParameters time is " + (System.currentTimeMillis() - startTime));

			while (rs.next()) {
				ret.add(new MatchingParameter(rs.getString(1), rs.getString(2)));
			}

			return ret;
		} catch (SQLException sqe) {
			throw new ResourceException("Unable to get matching parameters.", sqe);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void addMatchingParameter(MatchingParameter... parameters) throws ResourceException
	{
		PreparedStatement stmt = null;

		try {
			stmt = _connection.prepareStatement("INSERT INTO matchingparams" + "(resourceid, paramname, paramvalue) " + "VALUES (?, ?, ?)");

			for (MatchingParameter param : parameters) {
				stmt.setString(1, _resourceKey);
				stmt.setString(2, param.getName());
				stmt.setString(3, param.getValue());
				stmt.addBatch();
			}

			stmt.executeBatch();
		} catch (SQLException sqe) {
			throw new ResourceException("Unable to add matching parameters.", sqe);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void removeMatchingParameter(MatchingParameter... parameters) throws ResourceException
	{
		PreparedStatement stmt = null;

		try {
			stmt = _connection
				.prepareStatement("DELETE FROM matchingparams " + "WHERE resourceid = ? AND paramname = ? " + "AND paramvalue = ?");

			for (MatchingParameter param : parameters) {
				stmt.setString(1, _resourceKey);
				stmt.setString(2, param.getName());
				stmt.setString(3, param.getValue());
				stmt.addBatch();
			}

			stmt.executeBatch();
		} catch (SQLException sqe) {
			throw new ResourceException("Unable to delete matching parameters.", sqe);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	public static AclEntry getPrincipalfromDB(String epi)
	{
		ServerDatabaseConnectionPool pool = BasicDBResourceProvider.createConnectionPool();
		Connection connection = null;

		PreparedStatement stmt = null;
		ResultSet rs = null;
		AclEntry retval;
		retval = principalCache.get(epi);
		if (retval != null) {
			return retval;
		}
		try {
			connection = pool.acquire(false);
			stmt = connection.prepareStatement(_GET_PRINCIPAL_STMT);
			stmt.setString(1, epi);
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("getPrincipalfromDB " + epi + ": is " + (System.currentTimeMillis() - startTime));

			if (!rs.next())
				return null;

			Blob blob = rs.getBlob(1);
			if (blob == null)
				return null;

			retval = (AclEntry) DBSerializer.fromBlob(blob);
			principalCache.put(epi, retval);
			return retval;
		} catch (SQLException sqe) {
			return null;

		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
			pool.release(connection);
		}
	}

	public void createPrincipalinDB(String epi, AclEntry entry) throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			// Remove the old one, though there should not an existing one
			stmt = _connection.prepareStatement(_REMOVE_PRINCIPAL_STMT);
			stmt.setString(1, epi);
			stmt.executeUpdate();
			stmt.close();
			stmt = null;

			// Make sure they gave us something
			if (entry == null)
				return;
			stmt = _connection.prepareStatement(_INSERT_PRINCIPAL_STMT);
			stmt.setString(1, epi);

			Blob b = DBSerializer.toBlob(entry, "X509Identities", "ACLEntry");
			if (b != null) {
				if (_logger.isTraceEnabled())
					_logger.trace("Serializing " + b.length() + " bytes into X509Indenties database.");
				if (b.length() <= 0) {
					_logger.error("Attempt to serialize property \"" + "entry" + "\" with 0 bytes into the X509Indenties database.");
				} else if (b.length() >= 128 * 1024) {
					_logger.error(
						"Attempt to serialize property \"" + "entry" + "\" of length " + b.length() + " bytes into a " + "128K space.");
				}
			}

			stmt.setBlob(2, b);
			// Put it in the database
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to update X509Identitites \"" + epi + "\".");
		} finally {
			StreamUtils.close(stmt);
		}
	}

	static public String getEPI(Connection connection, String resourceID) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT epi FROM resources2 WHERE resourceid = ?");
			stmt.setString(1, resourceID);
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("getEPI time is " + (System.currentTimeMillis() - startTime));
			if (rs.next())
				return rs.getString(1);

			rs.close();
			rs = null;

			stmt.close();
			stmt = null;

			stmt = connection.prepareStatement("SELECT propvalue FROM properties " + "WHERE resourceid = ? AND propname = ?");
			stmt.setString(1, resourceID);
			stmt.setString(2, IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME);

			rs = stmt.executeQuery();
			if (rs.next()) {
				Object obj = DBSerializer.fromBlob(rs.getBlob(1));
				if (obj != null)
					return obj.toString();

				return null;
			}

			throw new SQLException(String.format("Unable to find EPI for resource \"%s\".", resourceID));
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	/**
	 * The inverse of getEPI().
	 */
	static public String getResourceID(Connection connection, String epi) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement("SELECT resourceid FROM resources2 WHERE epi = ?");
			stmt.setString(1, epi);
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("getResourceIDtime is " + (System.currentTimeMillis() - startTime));

			if (rs.next())
				return rs.getString(1);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
		return null;
	}

	@Override
	public Calendar createTime() throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = _connection.prepareStatement("SELECT createtime FROM resources WHERE resourceid = ?");
			stmt.setString(1, _resourceKey);
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("createTime time is " + (System.currentTimeMillis() - startTime));

			if (!rs.next())
				return null;
			Timestamp ts = rs.getTimestamp(1);
			Calendar c = Calendar.getInstance();
			c.setTime(ts);
			return c;
		} catch (SQLException sqe) {
			throw new ResourceException("Unable to get create time.", sqe);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	static public ConstructionParameters constructionParameters(Connection connection, Class<?> serviceClass, String resourceid)
		throws SQLException
	{
		ConstructionParameters cParams =
			(ConstructionParameters) getProperty(connection, resourceid, ConstructionParameters.CONSTRUCTION_PARAMETERS_QNAME.toString());

		if (cParams == null)
			cParams = ConstructionParameters.instantiateDefault(serviceClass);

		return cParams;
	}

	@Override
	public ConstructionParameters constructionParameters(Class<?> serviceClass) throws ResourceException
	{
		ConstructionParameters cParams =
			(ConstructionParameters) getProperty(ConstructionParameters.CONSTRUCTION_PARAMETERS_QNAME.toString());

		if (cParams == null)
			cParams = ConstructionParameters.instantiateDefault(serviceClass);

		return cParams;
	}

	static public void constructionParameters(Connection connection, String resourceid, ConstructionParameters parameters) throws SQLException
	{
		setProperty(connection, resourceid, ConstructionParameters.CONSTRUCTION_PARAMETERS_QNAME.toString(), parameters);
	}

	@Override
	public void constructionParameters(ConstructionParameters parameters) throws ResourceException
	{
		setProperty(ConstructionParameters.CONSTRUCTION_PARAMETERS_QNAME.toString(), parameters);
	}

	@Override
	public Collection<MessageElement> getUnknownAttributes() throws ResourceException
	{
		Collection<MessageElement> ret = new LinkedList<MessageElement>();
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = _connection.prepareStatement("SELECT attrvalues FROM unknownattrs WHERE resourceid = ?");
			stmt.setString(1, getKey());
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("get unkownattributes time is " + (System.currentTimeMillis() - startTime));

			while (rs.next()) {
				Blob blob = rs.getBlob(1);
				long blobLength = blob.length();
				MessageElement[] any = ObjectDeserializer.anyFromBytes(blob.getBytes(1L, (int) blobLength));
				if (any != null) {
					for (MessageElement value : any)
						ret.add(value);
				}
			}

			return ret;
		} catch (SQLException e) {
			throw new ResourceException("Unable to retrieve unknown attributes!", e);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void setUnknownAttributes(Map<QName, Collection<MessageElement>> newAttrs) throws ResourceException
	{
		PreparedStatement stmt = null;
		deleteUnknownAttributes(newAttrs.keySet());

		try {
			stmt = _connection.prepareStatement("INSERT INTO unknownattrs(resourceid, attrname, attrvalues) VALUES (?, ?, ?)");

			for (Map.Entry<QName, Collection<MessageElement>> entry : newAttrs.entrySet()) {
				stmt.setString(1, _resourceKey);
				stmt.setString(2, entry.getKey().toString());

				Collection<MessageElement> any = entry.getValue();
				stmt.setBlob(3, new SerialBlob(ObjectSerializer.anyToBytes(Elementals.toArray(any))));

				stmt.addBatch();
			}

			stmt.executeBatch();
		} catch (SQLException e) {
			throw new ResourceException("Unable to serialize unknown attributes into database!", e);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void deleteUnknownAttributes(Set<QName> names) throws ResourceException
	{
		PreparedStatement stmt = null;

		try {
			stmt = _connection.prepareStatement("DELETE FROM unknownattrs WHERE resourceid = ? AND attrname = ?");

			for (QName name : names) {
				stmt.setString(1, _resourceKey);
				stmt.setString(2, name.toString());
				stmt.addBatch();
			}

			stmt.executeBatch();
		} catch (SQLException e) {
			throw new ResourceException("Unable to delete unknown attributes into database!", e);
		} finally {
			StreamUtils.close(stmt);
		}
	}

}
