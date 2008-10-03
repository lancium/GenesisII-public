package edu.virginia.vcgr.genii.container.cservices.besstatus;

public class BESStatusRequest
{
	private BESName _name;
	private Long _cacheCoherenceWindow;
	
	public BESStatusRequest(BESName name, Long cacheCoherenceWindow)
	{
		_name = name;
		_cacheCoherenceWindow = cacheCoherenceWindow;
	}
	
	public BESStatusRequest(BESName name)
	{
		this(name, null);
	}
	
	public BESName getName()
	{
		return _name;
	}
	
	public Long getCacheCoherenceWindow()
	{
		return _cacheCoherenceWindow;
	}
}