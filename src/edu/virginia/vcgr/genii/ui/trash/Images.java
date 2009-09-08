package edu.virginia.vcgr.genii.ui.trash;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.morgan.util.configuration.ConfigurationException;


public class Images extends edu.virginia.vcgr.genii.ui.Images
{
	static private final String RESOURCE_PATH_FORMAT =
		"edu/virginia/vcgr/genii/ui/trash/resources/%s";
	
	static private BufferedImage _emptyTrashcan;
	static private BufferedImage _fullTrashcan;
	
	static
	{
		try
		{
			_emptyTrashcan = loadImage(String.format(
				RESOURCE_PATH_FORMAT, "empty-trashcan.png"));
			_fullTrashcan = loadImage(String.format(
				RESOURCE_PATH_FORMAT, "full-trashcan.png"));
		}
		catch (IOException ioe)
		{
			throw new ConfigurationException(
				"Unable to load trashcan image resources.", ioe);
		}
	}
	
	static public BufferedImage emptyTrashcan()
	{
		return _emptyTrashcan;
	}
	
	static public BufferedImage fullTrashcan()
	{
		return _fullTrashcan;
	}
}