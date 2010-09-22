package edu.virginia.vcgr.externalapp;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.macro.MacroUtils;

import org.w3c.dom.Element;

@XmlRootElement(name = "configuration")
public class DefaultExternalApplicationFactory
	implements ExternalApplicationFactory
{
	static private JAXBContext CONTEXT;
	
	static
	{
		try
		{
			CONTEXT = JAXBContext.newInstance(
				DefaultExternalApplicationConfiguration.class);
		}
		catch (JAXBException e)
		{
			throw new ConfigurationException(
				"Unable to create JAXBContext for " +
				"DefaultExternalApplicationConfiguration.", e);
		}
	}
	
	@Override
	public ExternalApplication createApplication(Element configuration)
	{
		if (configuration == null)
			throw new IllegalArgumentException(
				"Default external application configuration cannot be null.");
		
		try
		{
			Unmarshaller u = CONTEXT.createUnmarshaller();
			DefaultExternalApplicationConfiguration conf = u.unmarshal(
				configuration, 
				DefaultExternalApplicationConfiguration.class).getValue();
			
			Properties variables = new Properties();
			Map<String, String> env = System.getenv();
			for (String key : env.keySet())
				variables.setProperty(key, env.get(key));
			String description = MacroUtils.replaceMacros(
				variables, conf.description());
			Collection<String> argList = conf.arguments();
			String []arguments = new String[argList.size()];
			int lcv = 0;
			for (String arg : argList)
				arguments[lcv++] = MacroUtils.replaceMacros(
					variables, arg);
			
			return new DefaultExternalApplication(description, arguments);
		}
		catch (JAXBException e)
		{
			throw new IllegalArgumentException(
				"Invalid Default external application configuration element.");
		}
	}
}