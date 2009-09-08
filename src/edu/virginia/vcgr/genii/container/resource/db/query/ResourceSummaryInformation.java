package edu.virginia.vcgr.genii.container.resource.db.query;

public class ResourceSummaryInformation
{
	private String _resourceID;
	private String _humanName;
	private String _epi;
	private String _implementingClassString;
	private Class<?> _implementingClass = null;
	
	ResourceSummaryInformation(String resourceID, String humanName, 
		String epi, String implementingClassString)
	{
		_resourceID = resourceID;
		_humanName = humanName;
		_epi = epi;
		_implementingClassString = implementingClassString;
	}
	
	final public String resourceID()
	{
		return _resourceID;
	}
	
	final public String humanName()
	{
		return _humanName;
	}
	
	final public String epi()
	{
		return _epi;
	}
	
	final public String implementingClassString()
	{
		return _implementingClassString;
	}
	
	final public Class<?> implementingClass()
		throws ClassNotFoundException
	{
		synchronized(_implementingClassString)
		{
			if (_implementingClass == null)
			{
				_implementingClass = Thread.currentThread(
					).getContextClassLoader().loadClass(
						_implementingClassString);
			}
		}
		
		return _implementingClass;
	}
	
	@Override
	final public String toString()
	{
		if (_humanName == null)
			return String.format("[%s] %s@%s", _implementingClassString,
				_resourceID, _epi);
		else
			return String.format("%s [%s] %s@%s", _humanName,
				_implementingClassString, _resourceID, _epi);
	}
}