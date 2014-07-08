package edu.virginia.vcgr.genii.client.utils.icon;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.system.classloader.GenesisClassLoader;

public class StandardIconProvider implements IconProvider
{
	static private Log _logger = LogFactory.getLog(StandardIconProvider.class);

	static private final int DEFAULT_ICON_SIZE = 16;

	private String _resourceName = null;
	private int _iconSize;

	public StandardIconProvider(String resourceName, int iconSize)
	{
		if (resourceName == null) _resourceName = null;
		else _resourceName = String.format("%s%s", GenesisIIConstants.IMAGE_RELATIVE_LOCATION, resourceName);
		_iconSize = iconSize;
	}

	public StandardIconProvider(String resourceName)
	{
		if (resourceName == null) _resourceName = null;
		else _resourceName = String.format("%s%s", GenesisIIConstants.IMAGE_RELATIVE_LOCATION, resourceName);
		_iconSize = DEFAULT_ICON_SIZE;
	}

	public InputStream openStream(String resourceName)
	{
		if (_resourceName == null) return null;
		return GenesisClassLoader.getSystemResourceAsStream(resourceName);
	}

	public Icon loadIcon()
	{
		InputStream in = null;

		if (_resourceName == null) return null;
		
		try {
			in = openStream(_resourceName);
			if (in == null)
				throw new FileNotFoundException(String.format("Can't find resource %s.", _resourceName));
			return new ImageIcon(ImageIO.read(in));
		} catch (IOException ioe) {
			throw new ConfigurationException(String.format("Unable to read image from resource %s.", _resourceName), ioe);
		} finally {
			StreamUtils.close(in);
		}
	}

	@Override
	public Icon createIcon()
	{
		try {
			if (_resourceName != null) {
				return loadIcon();
			}
		} catch (Exception e) {
			_logger.error("failed to load icon resource: " + _resourceName);
		}
		return new Icon()
		{
			@Override
			final public void paintIcon(Component c, Graphics g, int x, int y)
			{
				final Color ICON_COLOR = Color.blue;

				Graphics2D g2 = (Graphics2D) (g.create());
				g2.setColor(ICON_COLOR);

				g2.fillOval(x + 2, y + 2, _iconSize - 4, _iconSize - 4);
				g2.dispose();
			}

			@Override
			final public int getIconWidth()
			{
				return _iconSize;
			}

			@Override
			final public int getIconHeight()
			{
				return _iconSize;
			}
		};

	}
}