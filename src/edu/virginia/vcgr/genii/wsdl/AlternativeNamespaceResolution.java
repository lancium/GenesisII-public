package edu.virginia.vcgr.genii.wsdl;

import org.w3c.dom.Document;

public class AlternativeNamespaceResolution
{
	static private ThreadLocal<Document> _alternativeResolver =
		new ThreadLocal<Document>();
	
	static public void setAlternativeResolver(Document resolver)
	{
		_alternativeResolver.set(resolver);
	}
	
	static public Document getAlternativeResolver()
	{
		return _alternativeResolver.get();
	}
}