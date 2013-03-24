package edu.virginia.vcgr.genii.client.bes;

import java.net.URI;

import org.apache.axis.types.URI.MalformedURIException;

public enum ResourceManagerType {
	Unknown("http://tempuri.org/unknown"), Simple(BESConstants.LOCAL_RESOURCE_MANAGER_TYPE_SIMPLE), PBS(
		BESConstants.LOCAL_RESOURCE_MANAGER_TYPE_PBS), SGE(BESConstants.LOCAL_RESOURCE_MANAGER_TYPE_SGE), GridQueue(
		BESConstants.LOCAL_RESOURCE_MANAGER_TYPE_GRID_QUEUE);

	private URI _uri;

	private ResourceManagerType(String uri)
	{
		this(URI.create(uri));
	}

	private ResourceManagerType(URI uri)
	{
		_uri = uri;
	}

	final public URI toURI()
	{
		return _uri;
	}

	final public org.apache.axis.types.URI toApacheAxisURI()
	{
		try {
			return new org.apache.axis.types.URI(_uri.toString());
		} catch (MalformedURIException mue) {
			// Can't happen
			return null;
		}
	}

	static public ResourceManagerType fromURI(URI uri)
	{
		for (ResourceManagerType type : ResourceManagerType.values())
			if (type._uri.equals(uri))
				return type;

		return Unknown;
	}

	static public ResourceManagerType fromURI(String uri)
	{
		return fromURI(URI.create(uri));
	}

	static public ResourceManagerType fromURI(org.apache.axis.types.URI uri)
	{
		return fromURI(uri.toString());
	}
}