package edu.virginia.vcgr.genii.container.cservices.executionmgr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;

public class ExecutionManagerService extends AbstractContainerService
{
	static private Log _logger = LogFactory.getLog(ExecutionManagerService.class);
	
	static final public String SERVICE_NAME = "Execution Manager Service";
	
	public ExecutionManagerService()
	{
		super(SERVICE_NAME);
	}
	
	@Override
	protected void loadService() throws Throwable
	{
		_logger.info("Execution Manager Service being loaded.");
	}

	@Override
	protected void startService() throws Throwable
	{
		_logger.info("Execution Manager Service being started.");
	}
	
	public void launch(ICallingContext callingContext, JobRequest request)
	{
	}
}