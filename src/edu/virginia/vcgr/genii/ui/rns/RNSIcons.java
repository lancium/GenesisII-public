package edu.virginia.vcgr.genii.ui.rns;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.morgan.util.Pair;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.ui.EndpointType;
import edu.virginia.vcgr.genii.ui.Icons;

public class RNSIcons extends Icons
{
	static private final String RESOURCE_PATH_PATTERN =
		"edu/virginia/vcgr/genii/ui/rns/icon-resources/%s";
	
	static private EnumMap<EndpointType, Pair<Icon, Icon>> _endpointIcons =
		new EnumMap<EndpointType, Pair<Icon,Icon>>(EndpointType.class);
	static private Icon _questionIcon;
	static private Icon _errorIcon;
	
	static
	{
		try
		{
			_endpointIcons.put(EndpointType.DIRECTORY, loadIconPairs("folder.png"));
			_endpointIcons.put(EndpointType.FILE, loadIconPairs("file.png"));
			_endpointIcons.put(EndpointType.BES, loadIconPairs("bes.png"));
			_endpointIcons.put(EndpointType.QUEUE, loadIconPairs("queue.png"));
			_endpointIcons.put(EndpointType.USER, loadIconPairs("idp.png"));
			_endpointIcons.put(EndpointType.HEAVY_EXPORT, loadIconPairs("export.png"));
			_endpointIcons.put(EndpointType.LIGHT_EXPORT, loadIconPairs("light-export.png"));
			_endpointIcons.put(EndpointType.UNKNOWN, loadIconPairs("file.png"));
			
			_questionIcon = new ImageIcon(loadImage(
				String.format(RESOURCE_PATH_PATTERN, "question.png")));
			_errorIcon = new ImageIcon(loadImage(
				String.format(RESOURCE_PATH_PATTERN, "error.png")));
		}
		catch (IOException ioe)
		{
			throw new ConfigurationException(
				"Unable to load icon resources.", ioe);
		}
	}
	
	static private BufferedImage createL(int width, int height)
	{
		Graphics2D g = null;
		BufferedImage ret = new BufferedImage(
			width, height, BufferedImage.TYPE_INT_ARGB);
		try
		{
			g = ret.createGraphics();
			
			g.setColor(Color.GREEN);
			g.fillOval(0, 0, width - 1, height - 1);
			g.setColor(Color.BLACK);
			g.drawString("L", 1, height - 2);
			
			return ret;
		}
		finally
		{
			if (g != null)
				g.dispose();
		}
	}
	
	static private Pair<Icon, Icon> loadIconPairs(String resourceName) 
		throws IOException
	{
		Graphics2D g = null;
		
		try
		{
			int width;
			int height;
			
			BufferedImage original = loadImage(
				String.format(RESOURCE_PATH_PATTERN, resourceName));
			
			width = original.getWidth();
			height = original.getHeight();
			BufferedImage local = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
			g = local.createGraphics();
			g.drawImage(original, 0, 0, null);
			g.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.8f));
			
			BufferedImage lImage = createL(width * 2 / 3, height * 2 / 3);
			g.drawImage(lImage, width - lImage.getWidth() - 1, 1,
				lImage.getWidth(), lImage.getHeight(), null);
			
			return new Pair<Icon, Icon>(
				new ImageIcon(original), new ImageIcon(local));
		}
		finally
		{
			if (g != null)
				g.dispose();
		}
	}
	
	static public Icon getIcon(EndpointType type, boolean isLocal)
	{
		Pair<Icon, Icon> pair = _endpointIcons.get(type);
		return (isLocal ? pair.second() : pair.first());
	}
	
	static public Icon getQuestionIcon()
	{
		return _questionIcon;
	}
	
	static public Icon getErrorIcon()
	{
		return _errorIcon;
	}
}