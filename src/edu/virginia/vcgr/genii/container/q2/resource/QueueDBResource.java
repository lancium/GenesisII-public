package edu.virginia.vcgr.genii.container.q2.resource;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContainerService;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class QueueDBResource extends BasicDBResource implements IQueueResource
{
	static final public String IS_ACCEPTING_NEW_ACTIVITIES_PROPERTY_NAME = "edu.virginia.vcgr.genii.container.q2.resource.is-accepting-new-activities";

	public QueueDBResource(ResourceKey parentKey, DatabaseConnectionPool connectionPool) throws SQLException
	{
		super(parentKey, connectionPool);
	}

	public void destroy() throws ResourceException
	{
		super.destroy();

		PreparedStatement stmt = null;

		try {
			stmt = _connection.prepareStatement("DELETE FROM q2resources WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();

			stmt.close();
			stmt = null;
			stmt = _connection.prepareStatement("DELETE FROM q2jobs WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();

			stmt.close();
			stmt = null;
			stmt = _connection.prepareStatement("DELETE FROM q2eprs WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();

			stmt.close();
			stmt = null;
			stmt = _connection.prepareStatement("DELETE FROM q2errors WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();

			stmt.close();
			stmt = null;
			stmt = _connection.prepareStatement("DELETE FROM q2logs WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();

			stmt.close();
			stmt = null;
			stmt = _connection.prepareStatement("DELETE FROM q2joblogtargets WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();

			stmt.close();
			stmt = null;
			stmt = _connection.prepareStatement("DELETE FROM q2jobhistorytokens WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();

			HistoryContainerService service = ContainerServices.findService(HistoryContainerService.class);
			service.deleteRecordsLike(_connection, String.format("q2:%s:%%", _resourceKey));
		} catch (SQLException sqe) {
			throw new ResourceException("Error while trying to destroy resource.", sqe);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void setEPR(EndpointReferenceType epr) throws ResourceException
	{
		PreparedStatement stmt = null;

		try {
			stmt = _connection.prepareStatement("INSERT INTO q2eprs (queueid, queueepr) VALUES (?, ?)");
			stmt.setString(1, _resourceKey);
			stmt.setBlob(2, EPRUtils.toBlob(epr, "q2eprs", "queueepr"));
			stmt.executeUpdate();

		} catch (SQLException sqe) {
			throw new ResourceException("Error while trying to set EPR for queue resource.", sqe);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	@Override
	public boolean isAcceptingNewActivites() throws ResourceException
	{
		Boolean value = (Boolean) getProperty(IS_ACCEPTING_NEW_ACTIVITIES_PROPERTY_NAME);
		if (value == null)
			value = Boolean.TRUE;

		return value;
	}

	@Override
	public void isAcceptingNewActivites(boolean isAccepting) throws ResourceException
	{
		setProperty(IS_ACCEPTING_NEW_ACTIVITIES_PROPERTY_NAME, Boolean.valueOf(isAccepting));
	}
}