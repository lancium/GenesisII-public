package edu.virginia.vcgr.genii.container.cservices.infomgr;

import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.utils.Duration;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;

/**
 * This is the main container service that is started to hand out
 * information portals.  Technically speaking, this probably didn't
 * have to be a "service" per se, but it works and give a convenient
 * factory mechanism for creating the executor that we are going
 * to use.
 * 
 * @author mmm2a
 */
public class InformationContainerService extends AbstractContainerService
{
	static private Log _logger = LogFactory.getLog(
		InformationContainerService.class);
	
	static private final String NUM_THREADS_PROPERTY =
		"edu.virginia.vcgr.genii.cservices.infomgr.num-threads";
	
	static public final String SERVICE_NAME = "Information Container Service";
	
	private Properties _configurationProperties;
	private Executor _executor = null;

	/**
	 * The public constructor that the container services manager uses to
	 * instantiate this container service.
	 * 
	 * @param infoMgrProperties A properties object representing configuration
	 * properties given when the container service was configured.
	 */
	public InformationContainerService(Properties infoMgrProperties)
	{
		super(SERVICE_NAME);
		
		_configurationProperties = infoMgrProperties;
	}
	
	@Override
	protected void loadService() throws Throwable
	{
		// There is nothing to load at the moment.
	}

	@Override
	protected void startService() throws Throwable
	{
		if (_configurationProperties == null)
			throw new RuntimeException("Information Manager not configured.");
		
		String numThreads = _configurationProperties.getProperty(
			NUM_THREADS_PROPERTY);
		if (numThreads == null)
			throw new RuntimeException("Information Manager not configured.");
	
		_logger.info(String.format("Starting %s with %s threads in the pool.",
			SERVICE_NAME, numThreads));
		_executor = Executors.newFixedThreadPool(Integer.parseInt(numThreads));
	}
	
	/**
	 * Portals are created by parts of the Genesis II system that need to
	 * be able to access information stored and cached about things.  In
	 * general, a single portal should be created once for each type of
	 * information cache (for example, one is created by the Queue class and
	 * shared amongst all queues in the system for all time).
	 * 
	 * @param <InfoType> The data type of the type of information that is
	 * stored.  This need not correspond to a SOAP method data type but can
	 * be some other type that SOAP messages are translated into.
	 * @param persister A persister instance that is used to store the data
	 * that is obtained for caching purposes.
	 * @param resolver A resolver instance that is used to obtain the
	 * information when the cache is not valid.
	 * @param timeout The amount of time to wait before a request for data
	 * is timed out.
	 * @param timeoutUnits The units for the timeout value.
	 * @param cacheWindow The amount of time a piece of data is valid for
	 * before it is considered stale.
	 * @param cacheWindowUnits The time units for the cache window value.
	 * @return A new information portal that can be used to obtain information
	 * either cached or live.
	 */
	public <InfoType> InformationPortal<InfoType> createNewPortal(
		InformationPersister<InfoType> persister,
		InformationResolver<InfoType> resolver,
		Duration defaultTimeout, Duration defaultCacheWindow)
	{
		if (persister == null)
			throw new IllegalArgumentException(
				"The \"persister\" parameter cannot be null.");
		
		if (resolver == null)
			throw new IllegalArgumentException(
				"The \"resolver\" parameter cannot be null.");
		
		if (defaultTimeout == null)
			defaultTimeout = Duration.InfiniteDuration;
		
		if (defaultCacheWindow == null)
			defaultCacheWindow = Duration.InfiniteDuration;
		
		return new InformationPortal<InfoType>(_executor, persister, resolver, 
			defaultTimeout, defaultCacheWindow);
	}
}