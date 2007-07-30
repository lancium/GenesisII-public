package edu.virginia.vcgr.genii.client.rcreate;

public class ResourceCreationContext
{
	static private final String _DEFAULT_CONTAINER_PATH = 
		"/containers/BootstrapContainer";
	static private final String _DEFAULT_SERVICE_RELATIVE_PATH = "Services";
	
	private String _defaultContainerPath;
	private String _serviceRelativePath;
	
	public ResourceCreationContext()
	{
		this(null, null);
	}
	
	public ResourceCreationContext(String defaultContainerPath,
		String serviceRelativePath)
	{
		if (defaultContainerPath == null)
			defaultContainerPath = _DEFAULT_CONTAINER_PATH;
		
		if (serviceRelativePath == null)
			serviceRelativePath = _DEFAULT_SERVICE_RELATIVE_PATH;
		
		_defaultContainerPath = defaultContainerPath;
		_serviceRelativePath = serviceRelativePath;
	}
	
	public String getServiceRelativePath()
	{
		return _serviceRelativePath;
	}
	
	public void setServiceRelativePath(String serviceRelativePath)
	{
		if (serviceRelativePath == null)
			serviceRelativePath = _DEFAULT_SERVICE_RELATIVE_PATH;
		
		_serviceRelativePath = serviceRelativePath;
	}
	
	public String getDefaultContainerPath()
	{
		return _defaultContainerPath;
	}
	
	public void setDefaultContainerPath(String defaultContainerPath)
	{
		if (defaultContainerPath == null)
			defaultContainerPath = _DEFAULT_CONTAINER_PATH;
		
		_defaultContainerPath = defaultContainerPath;
	}
	// This is where one might eventually consider putting schedulers, etc.
}