package edu.virginia.vcgr.genii.client.nativeq;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public class FactoryResourceAttributes extends ResourceAttributes
{
	private BasicResourceAttributes _basicResourceAttributes;
	private Collection<ResourceAttributes> _containedResources;
	private URI _localResourceManagerType;

	public FactoryResourceAttributes(BasicResourceAttributes basicResourceAttributes,
		Collection<ResourceAttributes> containedResources, URI localResourceManagerType)
	{
		if (containedResources == null)
			containedResources = new ArrayList<ResourceAttributes>();

		_basicResourceAttributes = basicResourceAttributes;
		_containedResources = containedResources;
		_localResourceManagerType = localResourceManagerType;
	}

	public BasicResourceAttributes getBasicResourceAttributes()
	{
		return _basicResourceAttributes;
	}

	public Collection<ResourceAttributes> getContainedResources()
	{
		return _containedResources;
	}

	public URI getLocalResourceManagerType()
	{
		return _localResourceManagerType;
	}

	protected void describe(StringBuilder builder, String tabPrefix)
	{
		if (_basicResourceAttributes != null) {
			builder.append(String.format("%sBasic Resource Attrs:\n", tabPrefix));
			_basicResourceAttributes.describe(builder, tabPrefix + "\t");
		}

		for (ResourceAttributes attr : _containedResources) {
			builder.append(String.format("%sContained Resource:\n", tabPrefix));
			attr.describe(builder, tabPrefix + "\t");
		}

		if (_localResourceManagerType != null)
			builder.append(String.format("%sLocal Resource Manager Type:  %s\n", tabPrefix, _localResourceManagerType));
	}
}