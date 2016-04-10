package edu.virginia.vcgr.genii.client.comm.axis;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.axis.client.Service;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.feistymeow.process.ethread;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.cache.LRUCache;
import edu.virginia.vcgr.genii.client.mem.LowMemoryWarning;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.security.CertificateValidatorFactory;
import edu.virginia.vcgr.genii.security.x509.CertTool;

/**
 * this class provides helper classes and methods for remembering axis objects. these objects can be relatively slow to create, so reuse can
 * be beneficial although it requires more heap space. service locators are remembered during the entire runtime, because those are very few
 * in number. stubs that are created (using particular service locators) go to different URLs, and URLS can be quite plentiful, so we allow
 * the stubs listed by URL to timeout.
 */
public class AxisServiceAndStubTracking
{
	static Log _logger = LogFactory.getLog(AxisServiceAndStubTracking.class);

	// currently not used, but could be used to close idle connections.
//	public static int CONNECTION_IDLE_TIMEOUT_ms = 10 * 60 * 1000;

	public static boolean enableExtraLogging = false; // code produces more noise if this is enabled.

	/*
	 * if this is not set to true, then the stub caching machinery will not be used. this needs to be enabled for the connection re-use to be
	 * efficient.
	 */
//	public static boolean enableStubCaching = false;
	//hmmm: STUB CACHING IS DISABLED!!!!

	// ................
	// first some constants...
	// ................

	/*
	 * the wsdd file defines all the services, the remote procedure call (RPC) operations available on the services, and the data structures
	 * required to invoke those RPCs.
	 */
	static public String WSDD_CLIENT_CONFIGURATION_FILE = "web-service-client-config.wsdd";

	// ................

//	final static int STUB_CACHE_TIMEOUT = 1000 * 60 * 10; // how long we'll keep around a stub pool for a URL.
	// ten minutes currently. is that long enough? too long?

	// maximum number of stubs to remember (for each EPR, within each service).
//	static public final int STUB_POOL_LIMIT_PER_EPR = 50;

	// we'll actively track this many urls for each service.
//	static public final int MAXIMUM_URLS_PER_SERVICE = 200;

	// ................

	// how many x509 certificates we will remember for the containers we intend to talk to.
	static int VALIDATED_CERT_CACHE_SIZE = 48;

	// ................

	// magic bogus numbers; right now just a metric we need to pick a good value for.
//	static final public int HANDLERS_CREATED_BEFORE_GARBAGE_COLLECTION = 32;

	// how long we'll go without a garbage collection being invoked, regardless of how many handlers were created.
//	static final public long GC_LONGEST_TIME_SLACK_BEFORE_MANDATORY_COLLECTION = 1000 * 30;

	// this is the most frequently we'll garbage collect, even if a bunch of handlers were just created.
//	static final public long GC_SHORTEST_TIME_BETWEEN_COLLECTIONS_TO_REDUCE_FRENETICISM = 1000 * 3;

	// ................

	/*
	 * a sentinel value we add to the properties on a stub to indicate that we already configured it for security. this is done once a stub is
	 * acquired, and this sentinel must be when the stub is returned to the pooling mechanism.
	 */
	static final String STUB_CONFIGURED = "edu.virginia.vcgr.genii.client.security.stub-configured";

	// ................
	// now some static members...
	// ................

	// static list of all locators for our services.
//	private static HashMap<Class<?>, ServiceRecord> _serviceCache = new HashMap<Class<?>, ServiceRecord>();

	// ................

	// cache of signed, serialized delegation assertions.
	static LRUCache<X509Certificate, Boolean> _validatedCerts =
		new LRUCache<X509Certificate, Boolean>(AxisServiceAndStubTracking.VALIDATED_CERT_CACHE_SIZE);

	// ................

	// new gc approach; track how many objects have been instantiated since last collection.
//	static volatile Integer _handlersCreatedSinceLastGC = 0;

	// timestamp for last garbage collection.
//	static volatile Date _lastGarbageCollect = new Date();

	// ................

	/*
	 * a cache of stubs that have been previously instantiated, which we track per service instance because they are relevant only to its
	 * operation. additionally, we have to track stubs per URL, since each stub might be connected to a different host.
	 */
//	private static StubCache _stubsCachedByLocator = new StubCache();

	// ................

	// now some class methods and classes...

	// ................

	/**
	 * provides access to the stub pooling mechanism.
	 */
//	public static StubCache getStubCache()
//	{
//		return _stubsCachedByLocator;
//	}

	// ................

	/**
	 * records an instantiated axis service object. we want to reuse these to avoid recreating both the Service and FileProvider objects. Note
	 * that a nasty lesson was taught to us about FileProviders; the FileProvider a Service is instantiated with must remain at same scope of
	 * lifetime as that service, and it cannot be reused by any other service or state corruption will result.
	 */
	public static class ServiceRecord
	{
		Service _service;
		FileProvider _providerConfig;

		public ServiceRecord(Service service, FileProvider providerConfig)
		{
			_service = service;
			_providerConfig = providerConfig;
		}

		@Override
		public boolean equals(Object other)
		{
			boolean toReturn;
			if (other instanceof ServiceRecord) {
				// class to class comparison, so we just check if the services are the same.
				toReturn = _service.equals(((ServiceRecord) other)._service);
			} else if (other instanceof Service) {
				// we allow comparison with Service also, since equals and hashcode are defined by it.
				toReturn = _service.equals(other);
			} else {
				// not a comparable type.
				toReturn = false;
			}
			return toReturn;
		}

		@Override
		public int hashCode()
		{
			return _service.hashCode();
		}
	}

	// ................

	/**
	 * one stub that has been issued to us via the pool, or which we have just created.
	 */
	public static class AcquiredStubRecord
	{
		Stub _stub;
		URL _url;
		ServiceRecord _service;

		AcquiredStubRecord(Stub stub, URL url, ServiceRecord service)
		{
			_stub = stub;
			_url = url;
			_service = service;
		}
	}

	// ................

	/**
	 * a pool of stubs that are associated with a particular URL (which is a host + port + container id for us).
	 */
//	public static class StubPool extends ArrayList<Stub>
//	{
//		private static final long serialVersionUID = 1L;
//	}

	// ................

	/**
	 * keeps around some stub instances per URL so they can be re-used. this has to be done on a per service-locator basis.
	 */
//	public static class InstantiatedStubCache extends TimedOutLRUCache<URL, StubPool>
//	{
//		ServiceRecord _reco;
//
//		public InstantiatedStubCache(ServiceRecord reco, long defaultTimeoutMS)
//		{
//			super(MAXIMUM_URLS_PER_SERVICE, defaultTimeoutMS);
//			_reco = reco;
//		}
//	}

	// ................

	/**
	 * a structure that tracks instantiated stubs so they don't have to be recreated. stubs first have to be considered separately by their
	 * instantiating service, so that's why we map here from a service to a stub cache. then within each service's stub cache, we need to
	 * separate out stubs based on the URL that they're hooked up to, since those rpc calls are on different machines. finally, within a
	 * particular service and for a particular URL, we will cache up to a certain number of stubs. the stubs are checked out of the pool when
	 * they're used and must be checked back in again. the AcquiredStubRecord code can help with this.
	 */
//	public static class StubCache extends HashMap<Service, InstantiatedStubCache>
//	{
//		private static final long serialVersionUID = 1L;
//
//		public StubCache()
//		{
//		}
//
//		synchronized void rememberService(ServiceRecord reco)
//		{
//			// set up a repository for stubs to be re-used.
//			put(reco._service, new InstantiatedStubCache(reco, STUB_CACHE_TIMEOUT));
//		}
//
//		synchronized Stub getStub(Service service, URL url)
//		{
//			Stub stubInstance = null;
//			if (!enableStubCaching)
//				return null; // stub caching is not enabled.
//
//			// we track ServiceRecords, but that object should have compatible equals operation for Service.
//			InstantiatedStubCache cache = get(service);
//			if (cache == null) {
//				throw new RuntimeException("failure to create a stub cache when the service " + service + " was created!");
//			}
//			StubPool pool = cache.get(url);
//			if ((pool != null) && (pool.size() > 0)) {
//				stubInstance = pool.remove(0);
//			} else if (pool == null) {
//				pool = new StubPool();
//				cache.put(url, pool);
//			}
//			// a tweak to keep recently used stubs around; we don't want to recreate them if the epr is still getting hit.
//			cache.refresh(url);
//			return stubInstance;
//		}
//
//		synchronized public void addStub(Stub s, ServiceRecord reco, URL url)
//		{
//			if (!enableStubCaching)
//				return; // stub caching is not enabled.
//
//			InstantiatedStubCache cache = get(reco._service);
//			if (cache == null) {
//				throw new RuntimeException("failure to create a stub cache when the service " + reco._service + " was created!");
//			}
//			StubPool pool = cache.get(url);
//			if (pool == null) {
//				// create a new stub pool since that url hadn't been seen before.
//				pool = new StubPool();
//				cache.put(url, pool);
//			}
//			if (pool.size() < STUB_POOL_LIMIT_PER_EPR) {
//				if (enableExtraLogging && _logger.isDebugEnabled())
//					_logger.debug("adding stub " + s + " in pool for service " + reco._service + " at url " + url);
//				pool.add(s);
//			} else {
//				if (enableExtraLogging && _logger.isDebugEnabled())
//					_logger.debug("dropping stub " + s + " on floor; pool too full for service " + reco._service + " at url " + url);
//			}
//			// a tweak to keep recently used stubs around; we don't want to recreate them if the epr is still getting hit.
//			cache.refresh(url);
//		}
//	}

	// ................

	/**
	 * tracks a set of stub instances that a proxy mechanism has checked out (temporarily). these must be returned after they are done being
	 * called.
	 */
	public static class AcquiredStubsList extends ArrayList<AcquiredStubRecord>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * retrieves the service that was used to create the previously acquired stub "lookup". this expects that all the stubs are present
		 * already in the acquired stubs list.
		 */
		public AxisServiceAndStubTracking.ServiceRecord getServiceRecordForStub(Stub lookup)
		{
			for (AxisServiceAndStubTracking.AcquiredStubRecord reco : this) {
				if (reco._stub == lookup) {
					return reco._service;
				}
			}
			// this is a real problem, since the caller needs to know this!
			return null;
		}

		public void releaseAllStubs()
		{
			if (this.size() > 0) {
				if (enableExtraLogging && _logger.isDebugEnabled())
					_logger.debug("about to release " + this.size() + " stubs to the pool.");
			}

			// shift all the items into a temporary list and out of the main list, just so no one can release them twice.
			ArrayList<AcquiredStubRecord> snagStubs = new ArrayList<AcquiredStubRecord>();
			snagStubs.addAll(this.subList(0, this.size()));
			this.clear();

			// return all the acquired stubs to the pool.
			for (AcquiredStubRecord r : snagStubs) {
				try {
					/*
					 * be sure to mark the stub as no longer configured, since we're not its owner any more. an empty string is a miniature
					 * standard within this code currently; might be better to add a conditional method.
					 */
					r._stub._setProperty(STUB_CONFIGURED, "");
					// drop the thing back into the cache.
//					AxisServiceAndStubTracking.getStubCache().addStub(r._stub, r._service, r._url);
				} catch (Throwable t) {
					_logger.error("failure when returning stub to pool", t);
				}
			}
		}

	}

	// ................

	/**
	 * instantiates a service based on a class used as a locator for it.
	 */
	static public ServiceRecord createServiceInstance(Class<?> locatorClass) throws ResourceException
	{
		Service instantiatedService = null;
		ServiceRecord reco = null;
		try {
			/*
			 * we will check the cache to see if we already created that type of service, since we want to re-use the connection pooling
			 * mechanism associated with the locator.
			 */
//			synchronized (_serviceCache) {
//				reco = _serviceCache.get(locatorClass);
//				if (reco != null) {
//					instantiatedService = reco._service;
//					if (_logger.isTraceEnabled())
//						_logger.debug("looked up service using " + locatorClass + " and got this guy: " + instantiatedService);
//					return reco;
//				}
//			}

			/*
			 * we need to make up a new service and file provider now. painful axis knowledge here! the file provider cannot be shared
			 * statically by all service locators when service locators are reused. this causes nasty bugs when interleaving of rpcs occurs.
			 * instead, the file providers must be instantiated per service locator (even though it doesn't visibly refer to the file
			 * provider), and that file provider must not be destroyed until the service is thrown out.
			 */
			reco = new ServiceRecord(null, new FileProvider(AxisServiceAndStubTracking.WSDD_CLIENT_CONFIGURATION_FILE));

			Constructor<?> cons = locatorClass.getConstructor(org.apache.axis.EngineConfiguration.class);
			instantiatedService = (Service) cons.newInstance(reco._providerConfig);
			// remember the service in our record now that it exists.
			reco._service = instantiatedService;

//			synchronized (_serviceCache) {
//				// stash the service in our cache.
//				//_serviceCache.put(locatorClass, reco);
//				
//				_logger.info("NOT CACHING SERVICE THIS IS DISABLED YO HELLO!!!!");
//			}
//			// set up a repository for stubs to be re-used.
//			getStubCache().rememberService(reco);

			if (_logger.isTraceEnabled())
				_logger.debug("created service using " + locatorClass + " and got this guy: " + instantiatedService.toString());

			return reco;
		} catch (NoSuchMethodException nsme) {
			throw new ResourceException("Class " + locatorClass.getName() + " does not refer to a known locator class type.", nsme);
		} catch (Exception e) {
			throw new ResourceException("Unable to create locator instance: " + e.getMessage(), e);
		}
	}

	// ................

	/**
	 * ensures that the certificate chain "serverCert" has already been validated or that it is known to our trust store. if we had to check
	 * the trust store, then we'll cache it for future lookups.
	 */
	public static void validateCertificateChain(EndpointReferenceType _epr) throws Exception
	{
		X509Certificate[] chain = EPRUtils.extractCertChain(_epr);
		if ((chain != null) && _logger.isTraceEnabled()) {
			int which = 0;
			for (X509Certificate cert : chain) {
				if (_logger.isTraceEnabled())
					_logger.debug("validating chain[" + which++ + "] for epr with: " + cert.getSubjectDN());
			}
		}

		URI epi = EPRUtils.extractEndpointIdentifier(_epr);

		if (chain == null) {
			throw new GenesisIISecurityException("EPR for " + _epr.getAddress().toString() + " does not contain a certificate chain.");
		}

		synchronized (_validatedCerts) {
			if (!_validatedCerts.containsKey(chain[0])) {
				// make sure the epi's match
				String certEpi = CertTool.getSN(chain[0]);
				if (!certEpi.equals(epi.toString())) {
					throw new GenesisIISecurityException("EPI for " + _epr.getAddress().toString() + " (" + epi.toString()
						+ ") does not match that in the certificate (" + certEpi + ")");
				}

				// run it through the trust manager
				boolean okay = CertificateValidatorFactory.getValidator().validateIsTrustedResource(chain);
				if (!okay) {
					/*
					 * we throw an exception so that we can either bail out, or just warn about it.
					 */
					// but we warn very quietly these days.
					throw new AuthZSecurityException("failed to validate cert chain: " + chain[0].getSubjectDN());
				}

				// insert into valid certs cache
				_validatedCerts.put(chain[0], Boolean.TRUE);
			}
		}

	}

	// ................

	// hmmm: if the garbage collection on a thread shows any promise in improving reliability, then we could drop this method entirely.

	/**
	 * perform a garbage collection if we have created several clients; this will prime the reuse pump. it would be better for us to
	 * programmatically release the handlers when they're done being used, but that is quite difficult due to the way we create proxies (which
	 * depend on the handler) and then use them sometime later. the resolution code in AxisClientInvocationHandler also confuses the issue.
	 */
//	public static void recordHandlerCreationAndTakeOutTrashIfAppropriate()
//	{
//		synchronized (_handlersCreatedSinceLastGC) {
//			_handlersCreatedSinceLastGC++;
//			int saveHandlers = _handlersCreatedSinceLastGC;
//			Date startGC = new Date();
//
//			long millisecondsSinceLastGC = startGC.getTime() - _lastGarbageCollect.getTime();
//
//			/*
//			 * is it too soon to garbage collect? the client is generally very frenetic and makes large numbers of outcalls, so if we only
//			 * track how many outcalls were created, the client will be collecting garbage every second (or multiple times a second). so this
//			 * code institutes an interval we'll call... must not collect trash more frequently than this.
//			 */
//			if (millisecondsSinceLastGC < GC_SHORTEST_TIME_BETWEEN_COLLECTIONS_TO_REDUCE_FRENETICISM) {
//				return;
//			}
//
//			// did we create enough handlers to justify collecting?
//			boolean createdEnoughHandlers = _handlersCreatedSinceLastGC >= HANDLERS_CREATED_BEFORE_GARBAGE_COLLECTION;
//
//			/*
//			 * we check if it's been too long since cleaning out the garbage, since the container is usually too lax hardly ever empties out
//			 * the trash (because it doesn't create very many outcalls). so this code institutes an interval we'll call... must collect trash
//			 * at least this often.
//			 */
//			boolean enforcedRunBecauseTooSlack = millisecondsSinceLastGC >= GC_LONGEST_TIME_SLACK_BEFORE_MANDATORY_COLLECTION;
//
//			// now decide if it's appropriate to clean out the trash.
//			if (enforcedRunBecauseTooSlack || createdEnoughHandlers) {
//				// yes, collect the trash, finally, joy joy...
//				LowMemoryWarning.performGarbageCollection();
//
//				// HttpConnectionManager connMgr = CommonsHTTPSender.getConnectionManager();
//				// connMgr.show connections? can't do it!
//
//				// hmmm: this code seems to be deadly. we get a container that no longer can be connected to somehow!?
//				// try dropping any connections that have been closed or idle too long.
//				// connMgr.closeIdleConnections(CONNECTION_IDLE_TIMEOUT_ms);
//
//				// reset how many handlers were created; the handler our caller is about to create (or just created) is already counted.
//				_handlersCreatedSinceLastGC = 0;
//				// currently we always log this; it shows up only as frequently as the shortest time between collections.
//				_logger.info("GC (last ran " + (millisecondsSinceLastGC / 100) / 10.0 + " sec ago) cleaned " + saveHandlers
//					+ " handlers and took " + ((new Date()).getTime() - startGC.getTime()) + " ms");
//				// update our last recorded collection time.
//				_lastGarbageCollect = new Date();
//			}
//		}
//	}

	/*
	 * this handy bash script shows the number of new stubs created vs. stubs reused when the stub debugging is enabled:
	 */
	// while true ; do
	// echo "##############"
	// grep "creating new stub instance" ~/.GenesisII/grid-client.log >$TMP/stub-creating-new.txt
	// grep "reusing stub instance" ~/.GenesisII/grid-client.log >$TMP/stub-reusing.txti
	// wc -l $TMP/stub-* 2>&1 | head -n 2
	// sleep 10
	// done

	public static class GarbageManThread extends ethread
	{
		public static final int THREAD_INTERVAL_ms = 1000 * 60;

		public GarbageManThread()
		{
			// run periodically at the interval we've picked above.
			super(THREAD_INTERVAL_ms);
			start();
		}

		@Override
		public boolean performActivity()
		{
			// hmmm: neither the idle connection closing nor the garbage collection seems to help at all.

			// we don't want to timeout a connection until the read timeout period has at least elapsed.
			// hmmm: this may actually be irrelevant, since a "checked out" connection will not automatically get closed,
			// and an "owned" connection shouldn't have any reads occurring on it.
			// Integer readTimeout =
			// Integer.parseInt(AxisProperties.getProperty(DefaultCommonsHTTPClientProperties.CONNECTION_DEFAULT_SO_TIMEOUT_KEY))
			// + 1000 * 5;
			//
			// // hmmm: DEFINITELY remove this debug.
			// _logger.debug("*** closing connections idle for " + readTimeout + "ms...");
			//
			// List<HttpConnectionManager> connMgrs = CommonsHTTPSender.getConnectionManagers();
			// for (HttpConnectionManager connMgr : connMgrs) {
			// if (connMgr != null) {
			// connMgr.closeIdleConnections(readTimeout);
			// }
			// }

			// // hmmm: DEFINITELY remove this debug.
			// _logger.debug("*** running garbage collection from thread...");
			//
			// Date startTime = new Date();
			// LowMemoryWarning.performGarbageCollection();
			//
			// long duration = (new Date()).getTime() - startTime.getTime();
			// if (duration > 200) {
			// if (_logger.isDebugEnabled())
			// _logger.debug("GC took " + duration + " ms");
			// }

			return true; // keep going.
		}
	}

	// set up the GC thread to run periodically.
	// static GarbageManThread _cleaner = new GarbageManThread();
}
