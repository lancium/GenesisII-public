package edu.virginia.vcgr.genii.ui.xml;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.ui.Icons;

public class XMLTreeIcons extends Icons
{
	static private final String RESOURCE_PATH_PATTERN = "edu/virginia/vcgr/genii/ui/xml/icon-resources/%s";

	static private Icon _elementIcon;
	static private Icon _attributeIcon;
	static private Icon _errorIcon;

	static {
		try {
			_elementIcon = new ImageIcon(loadImage(String.format(RESOURCE_PATH_PATTERN, "element.png")));
			_attributeIcon = new ImageIcon(loadImage(String.format(RESOURCE_PATH_PATTERN, "attribute.png")));
			_errorIcon = new ImageIcon(loadImage(String.format(RESOURCE_PATH_PATTERN, "error.png")));
		} catch (IOException ioe) {
			throw new ConfigurationException("Unable to load icon resources.", ioe);
		}
	}

	static public Icon errorIcon()
	{
		return _errorIcon;
	}

	static public Icon elementIcon()
	{
		return _elementIcon;
	}

	static public Icon attributeIcon()
	{
		return _attributeIcon;
	}
}