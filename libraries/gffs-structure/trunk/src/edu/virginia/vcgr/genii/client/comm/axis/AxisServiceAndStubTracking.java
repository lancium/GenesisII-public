package edu.virginia.vcgr.genii.client.comm.axis;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.axis.client.Service;
import org.apache.axis.client.Stub;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.LRUCache;
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

	public static boolean enableExtraLogging = false; // code produces more noise if this is enabled.

	// first some constants...

	/*
	 * the wsdd file defines all the services, the remote procedure call (RPC) operations available on the services, and the data structures
	 * required to invoke those RPCs.
	 */
	static public String WSDD_CLIENT_CONFIGURATION_FILE = "web-service-client-config.wsdd";

	// how many x509 certificates we will remember for the containers we intend to talk to.
	static int VALIDATED_CERT_CACHE_SIZE = 48;

	/*
	 * a sentinel value we add to the properties on a stub to indicate that we already configured it for security. this is done once a stub is
	 * acquired, and this sentinel must be when the stub is returned to the pooling mechanism.
	 */
	static final String STUB_CONFIGURED = "edu.virginia.vcgr.genii.client.security.stub-configured";

	// ................

	// now some static members...

	// cache of signed, serialized delegation assertions.
	static LRUCache<X509Certificate, Boolean> _validatedCerts =
		new LRUCache<X509Certificate, Boolean>(AxisServiceAndStubTracking.VALIDATED_CERT_CACHE_SIZE);

	// ................

	// now some class methods and classes...

	/**
	 * records an instantiated axis service object. we want to reuse these to avoid recreating both the Service and FileProvider objects. Note
	 * that a nasty lesson was taught to us about FileProviders; the FileProvider a Service is instantiated with must remain at same scope of
	 * lifetime as that service, and it cannot be reused by any other service or state corruption will result.
	 * 
	 * this pairing is still kept, although now we don't reuse services at all, because that has been shown to be even more subtly problematic
	 * than reusing the file provider.
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
}
