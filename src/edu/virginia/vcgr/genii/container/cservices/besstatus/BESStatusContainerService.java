package edu.virginia.vcgr.genii.container.cservices.besstatus;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;

public class BESStatusContainerService extends AbstractContainerService
{
	static private Log _logger = LogFactory.getLog(BESStatusContainerService.class);
	
	static final public String CONTAINER_SERVICE_NAME = "BES Status Container Service";
	
	public BESStatusContainerService()
	{
		super(CONTAINER_SERVICE_NAME);
	}
	
	// Returns null on a timeout.  If timeout is null, then no timeout.  
	// If 0, then instantaneous.
	public Map<BESName, BESStatusInformation>
		getStatus(BESStatusRequest []requests, Long timeoutMillis)
	{
		// TODO
		return null;
	}
	
	public void getStatus(BESStatusRequest []requests, Long timeoutMillis,
		BESStatusListener listener)
	{
		// TODO
	}
	
	public void noteDownBES(BESName []besNames)
	{
		// TODO
	}
	
	@Override
	protected void loadService()
	{
		_logger.info("BES Status Container Service Loaded.");
	}

	@Override
	protected void startService()
	{
		_logger.info("BES Status Container Service Started.");
	}
}