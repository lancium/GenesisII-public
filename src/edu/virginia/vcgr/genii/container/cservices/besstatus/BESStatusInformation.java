package edu.virginia.vcgr.genii.container.cservices.besstatus;

import org.ggf.bes.factory.FactoryResourceAttributesDocumentType;

public class BESStatusInformation
{
	private FactoryResourceAttributesDocumentType _attributes;
	private long _statusStaleness;
	
	public BESStatusInformation(
		FactoryResourceAttributesDocumentType attributes,
		long statusStaleness)
	{
		_attributes = attributes;
		_statusStaleness = statusStaleness;
	}
	
	public FactoryResourceAttributesDocumentType getFactoryAttributes()
	{
		return _attributes;
	}
	
	public long getStatusStaleness()
	{
		return _statusStaleness;
	}
}