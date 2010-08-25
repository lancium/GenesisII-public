package edu.virginia.vcgr.genii.container.cservices.eprmapper;

import java.sql.Connection;
import java.sql.SQLException;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.resource.db.query.ResourceSummary;

public class EPRMapperService extends AbstractContainerService
{
	static final public String SERVICE_NAME = "EPI-to-EPR Mapping Service";
	
	private EndpointReferenceType internalLookup(String epi) 
		throws ResourceException
	{
		Connection connection = null;

		try
		{
			connection = getConnectionPool().acquire(true);
			return ResourceSummary.getEPRFromEPI(connection, epi);
		}
		catch (SQLException e)
		{
			throw new ResourceException(String.format(
				"Unable to lookup resource \"%s\".", epi),
				e);
		}
		finally
		{
			StreamUtils.close(connection);
		}
	}
	
	@Override
	protected void loadService() throws Throwable
	{
		// Nothing to do
	}

	@Override
	protected void startService() throws Throwable
	{
		// Nothing to do
	}
	
	public EPRMapperService()
	{
		super(SERVICE_NAME);
	}
	
	static public EndpointReferenceType lookup(String epi) 
		throws ResourceException
	{
		EPRMapperService service =
			ContainerServices.findService(EPRMapperService.class);
		
		if (service == null)
			throw new RuntimeException(String.format(
				"Couldn't find service \"%s\".", SERVICE_NAME));
		
		return service.internalLookup(epi);
	}
}