package edu.virginia.vcgr.genii.container.replicatedExport;

import java.sql.Connection;

import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

public interface IRExportEntryResource extends IResource {
	public void destroy(Connection connection, boolean hardDestroy)
			throws ResourceException, ResourceUnknownFaultType;

	public void destroy(boolean hardDestroy) throws ResourceException,
			ResourceUnknownFaultType;
}
