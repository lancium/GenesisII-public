package edu.virginia.vcgr.genii.container.resource;

import java.io.Closeable;
import java.io.IOException;
import org.apache.axis.types.URI;
import java.security.GeneralSecurityException;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.x509.CertCreationSpec;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

/**
 * This class implements an abstract notion of resource key.  It can translate
 * between WS-Addressing and internal key representations and also manages the
 * lifetime of the java object for the resource.
 * 
 * @author Mark Morgan (mmm2a@cs.virginia.edu)
 */
public class ResourceKey implements Closeable
{
	static private HashMap<Object, ReferenceCounter> _lockTable
		= new HashMap<Object, ReferenceCounter>();
	static private Log _logger = LogFactory.getLog(ResourceKey.class);
	
	private boolean _closed = false;
	
	private IResourceFactory _factory;
	private IResource _cachedResource = null;
	private IResourceProvider _provider = null;
	private String _serviceName;
	private boolean _incrementedCounter = false;
	
	static private void incrementCounter(IResource resource)
	{
		Object key = resource.getLockKey();
		synchronized(_lockTable)
		{
			ReferenceCounter counter = _lockTable.get(key);
			if (counter == null)
			{
				counter = new ReferenceCounter();
				_lockTable.put(key, counter);
			}
			counter.increment();
		}
	}
	
	static private void decrementCounter(IResource resource)
	{
		Object key = resource.getLockKey();
		synchronized(_lockTable)
		{
			ReferenceCounter counter = _lockTable.get(key);
			if (counter != null)
			{
				if (counter.decrement() <= 0)
					_lockTable.remove(key);
			}
		}
	}
	
	protected void finalize() throws Throwable
	{
		try
		{
			close();
		}
		catch (IOException ioe)
		{
			_logger.error(ioe);
		}
		finally
		{
			super.finalize();
		}
	}
	
	/**
	 * Create a new resource key for the named service using the given
	 * construction parameters.
	 * 
	 * @param serviceName The name of the service to create a key for.
	 * @param constructionParameters The construction parameters to initialize
	 * the new resource.
	 * @throws ResourceException If anything goes wrong.
	 */
	ResourceKey(String serviceName, 
		HashMap<QName, Object> constructionParameters) 
			throws ResourceException
	{
		boolean noExceptions = false;
		_serviceName = serviceName;
		
		try
		{
			IResourceProvider provider = ResourceProviders.getProvider(serviceName);
			_factory = provider.getFactory();
			_cachedResource = _factory.instantiate(this);
			translateConstructionParameters(serviceName,
				constructionParameters);
			_cachedResource.initialize(constructionParameters);
			incrementCounter(_cachedResource);
			_incrementedCounter = true;
			RequiredConstructionParamWorker.setRequiredConstructionParameters(
				_cachedResource, constructionParameters);
			noExceptions = true;
		}
		finally
		{
			if (!noExceptions)
			{
				_logger.warn("An exception occured, so closing the resource.");
				StreamUtils.close(_cachedResource);
			}
		}	
	}
	
	/**
	 * Create a resource class to handle an existing resource's state.
	 * 
	 * @param serviceName The name of the service to which the state belongs.
	 * @param refParams The WS-Addressing ReferenceParmaeters which address the
	 * target resource.
	 * @throws ResourceUnknownFaultType If the addressing information refers to
	 * a resource which can't be found.
	 * @throws ResourceException If anything else goes wrong.
	 */
	ResourceKey(String serviceName, ReferenceParametersType refParams)
		throws ResourceUnknownFaultType, ResourceException
	{
		boolean noExceptions = false;
		_serviceName = serviceName;
		
		try
		{
			_provider = ResourceProviders.getProvider(serviceName);
			_factory = _provider.getFactory();
			_cachedResource = _factory.instantiate(this);
			_cachedResource.load(refParams);
			incrementCounter(_cachedResource);
			_incrementedCounter = true;
			noExceptions = true;
		}
		finally
		{
			if (!noExceptions)
			{
				StreamUtils.close(_cachedResource);
			}
		}
	}
	
	/**
	 * Retrieve the actual resource reference by this key.
	 * 
	 * @return The actual resource.
	 */
	public IResource dereference()
	{
		return _cachedResource;
	}
	
	/**
	 * Retrieve the WS-Addressing ReferenceParameters that match this key.
	 * 
	 * @return The Addressing information for WS-Addressing.
	 * @throws ResourceException If anything goes wrong.
	 */
	public ReferenceParametersType getResourceParameters()
		throws ResourceException
	{
		return dereference().getResourceParameters(); 
	}
	
	/**
	 * Request that the resource destroy itself.
	 * 
	 * @throws ResourceException If anything goes wrong.
	 */
	public void destroy() throws ResourceException
	{
		dereference().destroy();
	}
	
	/**
	 * Because resource can be created multiple times for a given resource,
	 * you can't simply lock on the resource.  Rather, you can get this object
	 * and lock on it.  This method assumes that the internal key representation
	 * supports a reasonable hashcode and equals semantic.
	 * 
	 * @return An object which can be locked on.
	 */
	public Object getLockObject()
	{
		synchronized(_lockTable)
		{
			return _lockTable.get(_cachedResource.getLockKey());
		}
	}
	
	/**
	 * Retrieve the provider associated with this key.
	 * 
	 * @return The associated provider.
	 */
	public IResourceProvider getProvider()
	{
		return _provider;
	}
	
	/**
	 * Return resources used by this key (and it's underlying resource) to
	 * the system.  This method should ALWAYS be called when the user is done with
	 * the resource.
	 * 
	 * @throws IOException if something goes wrong with the close.
	 */
	synchronized public void close() throws IOException
	{
		if (_closed)
			return;
		
		_closed = true;
		
		if (_incrementedCounter)
			decrementCounter(_cachedResource);
		_cachedResource.close();
	}
	
	static private class ReferenceCounter
	{
		private int _count;
		
		public ReferenceCounter()
		{
			_count = 0;
		}
		
		public void increment()
		{
			_count++;
		}
		
		public int decrement()
		{
			return --_count;
		}
	}
	
	private void translateConstructionParameters(String serviceName,
		HashMap<QName, Object> consParms) throws ResourceException
	{
		URI epi = (URI)consParms.get(
			IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM);
		if (epi == null)
		{
			throw new MissingConstructionParamException(
				IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM);
		}
		
		CertCreationSpec spec = (CertCreationSpec)consParms.get(
			IResource.CERTIFICATE_CREATION_SPEC_CONSTRUCTION_PARAM);
		if (spec != null)
		{	
			try
			{
				consParms.put(IResource.CERTIFICATE_CHAIN_CONSTRUCTION_PARAM,
						CertTool.createResourceCertChain(epi.toString(), 
								serviceName, spec));
			}
			catch (GeneralSecurityException gse)
			{
				throw new ResourceException(gse.getLocalizedMessage(), gse);
			}
		}
	}
	
	public String getServiceName()
	{
		return _serviceName;
	}
	
	public Object getKey()
	{
		if (_cachedResource == null) {
			return null;
		}
		return _cachedResource.getKey();
	}
}
