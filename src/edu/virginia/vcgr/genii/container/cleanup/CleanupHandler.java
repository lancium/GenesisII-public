package edu.virginia.vcgr.genii.container.cleanup;

import java.sql.Connection;

public interface CleanupHandler
{
	public void doCleanup(Connection connection, boolean enactCleanup);
}