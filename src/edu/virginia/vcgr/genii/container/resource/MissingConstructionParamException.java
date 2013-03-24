package edu.virginia.vcgr.genii.container.resource;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class MissingConstructionParamException extends ResourceException
{
	static final long serialVersionUID = 0L;

	public MissingConstructionParamException(QName paramName)
	{
		super("Required construction parameter \"" + paramName + "\" is missing.");
	}
}