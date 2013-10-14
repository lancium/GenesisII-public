package edu.virginia.vcgr.genii.client.resource;

import javax.xml.namespace.QName;

public class MissingConstructionParamException extends ResourceException
{
	static final long serialVersionUID = 0L;

	public MissingConstructionParamException(QName paramName)
	{
		super("Required construction parameter \"" + paramName + "\" is missing.");
	}
}