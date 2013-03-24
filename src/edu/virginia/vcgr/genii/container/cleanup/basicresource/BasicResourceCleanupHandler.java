package edu.virginia.vcgr.genii.container.cleanup.basicresource;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Triple;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.container.cleanup.AbstractCleanupHandler;
import edu.virginia.vcgr.genii.container.cleanup.CleanupContext;
import edu.virginia.vcgr.genii.container.cleanup.wsnsubscription.WSNSubscriptionCleanupHandler;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContainerService;

public class BasicResourceCleanupHandler extends AbstractCleanupHandler
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(BasicResourceCleanupHandler.class);

	static private void cleanupSubscriptions(Connection connection, String publisherKey) throws Throwable
	{
		ResultSet rs = null;
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("SELECT subscriptionresourcekey FROM wsnsubscriptions "
				+ "WHERE publisherresourcekey = ?");
			stmt.setString(1, publisherKey);
			rs = stmt.executeQuery();

			while (rs.next()) {
				WSNSubscriptionCleanupHandler handler = new WSNSubscriptionCleanupHandler();
				handler.enactCleanup(connection, rs.getString(1));
			}
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	static protected void removeRowsFromTable(Connection connection, Triple<String, String, String> tableResourceTriple)
		throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement(String.format("DELETE FROM %s WHERE %s = ?", tableResourceTriple.first(),
				tableResourceTriple.second()));
			stmt.setString(1, tableResourceTriple.third());
			stmt.executeUpdate();
		} finally {
			StreamUtils.close(stmt);
		}
	}

	static protected void validateEPRFromBlob(CleanupContext context, String resourceID, Blob blob, boolean mustExist)
	{
		if (blob == null && mustExist) {
			context.addResource(resourceID, "Blob for EPR was null.");
			return;
		}

		// We don't actually try and parse it because:
		// A) EPRs haven't really changed, so they shouldn't be bad
		// B) It would be hard to determine if the cause of an exception
		// was SQL, or other.
		return;
	}

	static protected <Type> Type validateBlobToXml(CleanupContext context, String resourceID, Blob blob, Class<Type> type,
		boolean mustExist) throws SQLException
	{
		if (blob == null && mustExist) {
			context.addResource(resourceID, "Blob for type %s was null.", type);
			return null;
		}

		try {
			return DBSerializer.xmlFromBlob(type, blob);
		} catch (IOException ioe) {
			throw new SQLException("IO Exception thrown during BLOB read.", ioe);
		} catch (Throwable cause) {
			context.addResource(resourceID, "Couldn't derserialize %s from blob:  %s", type, cause);
			return null;
		}
	}

	static protected <Type> Type validateBlobToJavaSer(CleanupContext context, String resourceID, Blob blob, Class<Type> type,
		boolean mustExist) throws SQLException
	{
		if (blob == null && mustExist) {
			context.addResource(resourceID, "Blob for type %s was null.", type);
			return null;
		}

		try {
			Object obj = DBSerializer.fromBlob(blob);
			if (obj != null && !(type.isAssignableFrom(obj.getClass()))) {
				context.addResource(resourceID, "Tried to deserialize to %s, but got %s.", type, obj.getClass());
			}

			return type.cast(obj);
		} catch (SQLException sqe) {
			Throwable cause = sqe.getCause();
			if (cause == null || cause instanceof SQLException)
				throw sqe;

			context.addResource(resourceID, "Exception thrown while parsing Java serializeable from the database into %s:  %s",
				type, cause);
			return null;
		} catch (Throwable cause) {
			context.addResource(resourceID, "Exceptino thrown while parsing Java serializable from the database into %s:  %s",
				type, cause);
			return null;
		}
	}

	static protected void evaluatePropertiesTable(Connection connection, CleanupContext context, String resourceID)
		throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT propvalue FROM properties WHERE resourceid = ?");
			stmt.setString(1, resourceID);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Blob blob = rs.getBlob(1);
				validateBlobToJavaSer(context, resourceID, blob, Object.class, false);
			}
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	static protected void evaluateResourcesTable(Connection connection, CleanupContext context, String resourceID,
		boolean mustExist) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT resourceid FROM resources WHERE resourceid = ?");
			stmt.setString(1, resourceID);

			rs = stmt.executeQuery();

			boolean wasItThere = rs.next();

			if (!wasItThere && mustExist)
				context.addResource(resourceID, "Resource missing from resources table.");
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	static protected void evaluateResources2Table(Connection connection, CleanupContext context, String resourceID,
		boolean mustExist) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT epr FROM resources2 WHERE resourceid = ?");
			stmt.setString(1, resourceID);

			rs = stmt.executeQuery();

			boolean wasItThere = rs.next();

			if (!wasItThere && mustExist)
				context.addResource(resourceID, "Resource missing from resources2 table.");
			else if (wasItThere)
				validateEPRFromBlob(context, resourceID, rs.getBlob(1), true);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	@Override
	protected void detectResourcesToCleanup(Connection connection, CleanupContext context)
	{
		// At the moment, we have nothing to detect.
	}

	@Override
	public void enactCleanup(Connection connection, String resourceID) throws Throwable
	{
		removeRowsFromTable(connection, new Triple<String, String, String>("resources", "resourceid", resourceID));
		removeRowsFromTable(connection, new Triple<String, String, String>("resources2", "resourceid", resourceID));
		removeRowsFromTable(connection, new Triple<String, String, String>("properties", "resourceid", resourceID));
		removeRowsFromTable(connection, new Triple<String, String, String>("persistedproperties", "resourceid", resourceID));
		removeRowsFromTable(connection, new Triple<String, String, String>("matchingparams", "resourceid", resourceID));

		HistoryContainerService service = new HistoryContainerService();
		service.deleteRecords(connection, resourceID);

		cleanupSubscriptions(connection, resourceID);
	}
}