package edu.virginia.vcgr.genii.ui;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.morgan.util.configuration.ConfigurationException;

public class Icons extends Images
{
	static private final String RESOURCE_PATH_FORMAT =
		"edu/virginia/vcgr/genii/ui/resources/%s";
	
	static private Icon _tearoff;
	
	static
	{
		try
		{
			_tearoff = loadIcon("tearoff.png");
		}
		catch (IOException ioe)
		{
			throw new ConfigurationException("Unable to load icons.", ioe);
		}
	}
	
	static protected ImageIcon loadIcon(String resourceName)
		throws IOException
	{
		return new ImageIcon(loadImage(String.format(
			RESOURCE_PATH_FORMAT, resourceName)));
	}
	
	static public Icon tearoffIcon()
	{
		return _tearoff;
	}
}