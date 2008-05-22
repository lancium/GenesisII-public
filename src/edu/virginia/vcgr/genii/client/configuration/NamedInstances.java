package edu.virginia.vcgr.genii.client.configuration;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.morgan.util.configuration.XMLConfiguration;

public class NamedInstances
{
	static private QName _instancesSectionName =
		new QName("http://vcgr.cs.virginia.edu/Genesis-II", "configured-instances");
	
	static private NamedInstances _clientSide = null;
	static private NamedInstances _serverSide = null;
	
	private HashMap<String, Object> _instances = new HashMap<String, Object>();
	
	@SuppressWarnings("unchecked")
	private NamedInstances(XMLConfiguration configuration)
	{
		for (Object obj : configuration.retrieveSections(_instancesSectionName))
		{
			Map<String, Object> instances = (Map<String, Object>)obj;
			for (String name : instances.keySet())
			{
				_instances.put(name, instances.get(name));
			}
		}
	}
	
	public Object lookup(String name)
	{
		return _instances.get(name);
	}
	
	synchronized static public NamedInstances getClientInstances()
	{
		if (_clientSide == null)
			_clientSide = new NamedInstances(
				ConfigurationManager.getCurrentConfiguration().getClientConfiguration());
		
		return _clientSide;
	}
	
	synchronized static public NamedInstances getServerInstances()
	{
		if (_serverSide == null)
			_serverSide = new NamedInstances(
				ConfigurationManager.getCurrentConfiguration().getContainerConfiguration());
		
		return _serverSide;
	}
	
	static public NamedInstances getRoleBasedInstances()
	{
		if (ConfigurationManager.getCurrentConfiguration().isClientRole())
			return getClientInstances();
		else
			return getServerInstances();
	}
}