package edu.virginia.vcgr.genii.container.cleanup.besactivity;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.JobDefinition_Type;
import org.morgan.util.Triple;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.container.cleanup.CleanupContext;
import edu.virginia.vcgr.genii.container.cleanup.basicresource.BasicResourceCleanupHandler;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.Identity;

public class BESActivityCleanupHandler extends BasicResourceCleanupHandler
{
	static private Log _logger = LogFactory.getLog(BESActivityCleanupHandler.class);

	static private void evaluateOwners(CleanupContext context, String resourceID, Collection<?> owners)
	{
		int invalidCount = 0;

		for (Object owner : owners) {
			if (owner != null) {
				if (owner instanceof Identity) {
					Identity id = (Identity) owner;
					if (id instanceof X509Identity) {
						X509Identity xid = (X509Identity) id;
						try {
							xid.checkValidity(0, new Date());
						} catch (Throwable cause) {
							invalidCount++;
							_logger.warn("Not counting an expired certificate for " + "cleanup purposes.", cause);
						}
					}
				}
			}
		}

		if (invalidCount > 0)
			context.addResource(resourceID, "Some identities have expired.");
	}

	static private void evaluateBESActivityPropertiesTable(Connection connection, CleanupContext context, String resourceID)
		throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection
				.prepareStatement("SELECT propertyvalue FROM besactivitypropertiestable " + "WHERE activityid = ?");
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

	private void evaluate(Connection connection, CleanupContext context, String activityID, String besID, Blob jsdlBlob,
		Blob ownersBlob, Blob callCtxtBlob, Blob stateBlob, Blob execPlanBlob, Blob actEprBlob, String jobName)
		throws SQLException
	{
		if (besID == null)
			context.addResource(activityID, "No BES associated with activity.");

		validateBlobToXml(context, activityID, jsdlBlob, JobDefinition_Type.class, true);

		Collection<?> owners = validateBlobToJavaSer(context, activityID, ownersBlob, Collection.class, true);
		evaluateOwners(context, activityID, owners);

		validateBlobToJavaSer(context, activityID, callCtxtBlob, ICallingContext.class, true);

		validateBlobToJavaSer(context, activityID, stateBlob, ActivityState.class, false);

		validateBlobToJavaSer(context, activityID, execPlanBlob, Vector.class, true);

		validateEPRFromBlob(context, activityID, actEprBlob, true);

		evaluateBESActivityPropertiesTable(connection, context, activityID);

		evaluateResourcesTable(connection, context, activityID, true);
		evaluateResources2Table(connection, context, activityID, true);
		evaluatePropertiesTable(connection, context, activityID);
	}

	@Override
	protected void detectResourcesToCleanup(Connection connection, CleanupContext context)
	{
		_logger.info("BESActivityCleanupHandler detecting bad resources.");

		Statement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery("SELECT activityid, besid, jsdl, owners, callingcontext, "
				+ "state, executionplan, activityepr, jobname " + "FROM besactivitiestable");

			while (rs.next()) {
				try {
					String activityID = rs.getString(1);
					String besID = rs.getString(2);
					Blob jsdlBlob = rs.getBlob(3);
					Blob ownersBlob = rs.getBlob(4);
					Blob callCtxtBlob = rs.getBlob(5);
					Blob stateBlob = rs.getBlob(6);
					Blob execPlanBlob = rs.getBlob(7);
					Blob actEprBlob = rs.getBlob(8);
					String jobName = rs.getString(9);

					_logger.info(String.format("Evaluating whether or not activity %s with job name %s is good.", activityID,
						jobName));

					evaluate(connection, context, activityID, besID, jsdlBlob, ownersBlob, callCtxtBlob, stateBlob,
						execPlanBlob, actEprBlob, jobName);
				} catch (Throwable cause) {
					_logger.error("Couldn't evaluate a bes activity resource -- " + "threw an exception.", cause);
				}
			}
		} catch (SQLException cause) {
			_logger.error("Couldn't iterate through the bes activities.", cause);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void enactCleanup(Connection connection, String resourceID) throws Throwable
	{
		_logger.info(String.format("BESActivityCleanupHandler enacting cleanup on bad resource %s", resourceID));

		super.enactCleanup(connection, resourceID);

		removeRowsFromTable(connection, new Triple<String, String, String>("besactivityfaultstable", "besactivityid",
			resourceID));
		removeRowsFromTable(connection, new Triple<String, String, String>("besactivitiestable", "activityid", resourceID));
		removeRowsFromTable(connection, new Triple<String, String, String>("besactivitypropertiestable", "activityid",
			resourceID));
	}
}