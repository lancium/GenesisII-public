package edu.virginia.vcgr.genii.container.cleanup;

import java.sql.Connection;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractCleanupHandler implements CleanupHandler
{
	static private Log _logger = LogFactory.getLog(AbstractCleanupHandler.class);

	private void enactCleanup(Connection connection, Map<String, Collection<CleanupReason>> resources)
	{
		for (Map.Entry<String, Collection<CleanupReason>> entry : resources.entrySet()) {
			try {
				_logger.info(String.format("Enacting clean-up on resource %s:  %s", entry.getKey(), entry.getValue()));
				enactCleanup(connection, entry.getKey());
				connection.commit();
			} catch (Throwable cause) {
				_logger.error(String.format("Unable to enact clean-up on resource %s.", entry.getKey()), cause);

				try {
					connection.rollback();
				} catch (Throwable c2) {
					_logger.error(String.format("Unable to rollback clean-up of resource %s.", entry.getKey()), c2);
				}
			}
		}
	}

	protected abstract void detectResourcesToCleanup(Connection connection, CleanupContext context);

	public abstract void enactCleanup(Connection connection, String resourceID) throws Throwable;

	@Override
	final public void doCleanup(Connection connection, boolean enactCleanup)
	{
		CleanupContext context = new CleanupContext();
		detectResourcesToCleanup(connection, context);

		try {
			if (enactCleanup) {
				connection.commit();
				enactCleanup(connection, context.resourcesToClean());
			} else {
				connection.rollback();
				for (Map.Entry<String, Collection<CleanupReason>> entry : context.resourcesToClean().entrySet()) {
					_logger.info(String.format("If we were enacting, we would clean up %s:  %s.", entry.getKey(),
						entry.getValue()));
				}
			}
		} catch (Throwable cause) {
			_logger.error("Unable to enact or rollback cleanup.", cause);
		}
	}
}