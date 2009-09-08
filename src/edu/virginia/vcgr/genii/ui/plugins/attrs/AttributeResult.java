package edu.virginia.vcgr.genii.ui.plugins.attrs;

import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse;

class AttributeResult
{
	private GetResourcePropertyDocumentResponse _properties = null;
	private String _message = null;
	private Throwable _cause = null;
	
	AttributeResult(GetResourcePropertyDocumentResponse properties)
	{
		_properties = properties;
	}
	
	AttributeResult(String message, Throwable cause)
	{
		_message = message;
		_cause = cause;
	}
	
	final public boolean isError()
	{
		return _message != null;
	}
	
	final public String message()
	{
		return _message;
	}
	
	final public Throwable cause()
	{
		return _cause;
	}
	
	final public GetResourcePropertyDocumentResponse properties()
	{
		return _properties;
	}
}