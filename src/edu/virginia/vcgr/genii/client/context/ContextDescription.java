package edu.virginia.vcgr.genii.client.context;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.morgan.utils.io.IOUtils;

public class ContextDescription
{
	private Map<String, Collection<Serializable>> _properties;
	private Map<String, Serializable> _transientProperties;

	public ContextDescription()
	{
		_properties = new HashMap<String, Collection<Serializable>>();
		_transientProperties = new HashMap<String, Serializable>();
	}

	void setProperty(String propertyName, Collection<Serializable> values)
	{
		_properties.put(propertyName, new Vector<Serializable>(values));
	}

	void setTransientProperty(String propertyName, Serializable value)
	{
		_transientProperties.put(propertyName, value);
	}

	@Override
	public String toString()
	{
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);

		pw.println("Properties:");
		for (String property : _properties.keySet())
			pw.format("\t%s => %s\n", property, _properties.get(property));

		pw.println("Transient Properties:");
		for (String property : _transientProperties.keySet())
			pw.format("\t%s => %s\n", property, _transientProperties.get(property));

		pw.close();
		IOUtils.close(writer);
		return writer.toString();
	}
}