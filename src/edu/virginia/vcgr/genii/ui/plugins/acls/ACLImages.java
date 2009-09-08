package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.ui.Images;

public class ACLImages extends Images
{
	static final private String BASE_RESOURCE_PATH =
		"edu/virginia/vcgr/genii/ui/plugins/acls/icon-resources";
	
	static private BufferedImage _everyoneImage;
	static private BufferedImage _personImage;
	static private BufferedImage _emptyPatternImage;
	static private BufferedImage _filledPatternImage;
	
	static
	{
		try
		{
			_everyoneImage = loadImage(String.format(
				"%s/everyone.png", BASE_RESOURCE_PATH));
			_personImage = loadImage(String.format(
				"%s/person.png", BASE_RESOURCE_PATH));
			_emptyPatternImage = loadImage(String.format(
				"%s/empty-pattern.png", BASE_RESOURCE_PATH));
			_filledPatternImage = loadImage(String.format(
				"%s/filled-pattern.png", BASE_RESOURCE_PATH));
			addLabels(_everyoneImage, Color.WHITE, "Everyone");
			addLabels(_emptyPatternImage, Color.BLACK, "Pattern");
			addLabels(_filledPatternImage, Color.BLACK, "Pattern");
		}
		catch (IOException e)
		{
			throw new ConfigurationException("Unable to load ACL icons.", e);
		}
	}
	
	static private Font findFont(Font exemplar, Graphics2D g,
		String...labels)
	{
		FontMetrics metrics = g.getFontMetrics(exemplar);
		double maxWidth = 0.0;
		Rectangle2D r;
		int imageWidth = g.getDeviceConfiguration().getBounds().width;
		
		for (String label : labels)
		{
			r = metrics.getStringBounds(label, g);
			maxWidth = Math.max(maxWidth, r.getWidth());
		}
		
		if (maxWidth >= imageWidth)
			return findFont(exemplar.deriveFont(exemplar.getSize() - 1.0f),
				g, labels);
		return exemplar;
	}
	
	static private void addLabels(BufferedImage image, Color fontColor, String...labels)
	{
		Graphics2D g = null;
		int gWidth;
		
		try
		{
			g = image.createGraphics();
			gWidth = g.getDeviceConfiguration().getBounds().width;
			
			if (fontColor != null)
				g.setColor(fontColor);
			
			g.setFont(findFont(g.getFont(), g, labels));
			
			int baseline = g.getDeviceConfiguration().getBounds().height;
			
			for (int lcv = labels.length - 1; lcv >= 0; lcv--)
			{
				String label = labels[lcv];
				Rectangle2D r = g.getFontMetrics().getStringBounds(label, g);
				g.drawString(label,
					(float)(r.getX() + (gWidth - r.getWidth()) / 2.0),
					(float)(baseline + r.getY()));
				baseline = (int)(baseline - r.getHeight());
			}
		}
		finally
		{
			if (g != null)
				g.dispose();
		}
	}
	
	static public BufferedImage everyone()
	{
		return _everyoneImage;
	}
	
	static public BufferedImage person()
	{
		return _personImage;
	}
	
	static public BufferedImage emptyPattern()
	{
		return _emptyPatternImage;
	}
	
	static public BufferedImage filledPattern()
	{
		return _filledPatternImage;
	}
}