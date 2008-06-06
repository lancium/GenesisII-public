package edu.virginia.vcgr.genii.client.dialog.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.color.ColorSpace;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.io.FileResource;

public class HelpButton extends JButton
{
	static final long serialVersionUID = 0L;
	
	private Icon _helpIcon;
	private Icon _grayedIcon;
	
	public HelpButton()
	{
		setContentAreaFilled(false);
		
		prepareIcons();
		
		Dimension dim = new Dimension(
			_helpIcon.getIconWidth(), _helpIcon.getIconHeight());
		
		setPreferredSize(dim);
		setMaximumSize(dim);
		setMinimumSize(dim);
		setSize(dim);
	}

	// Paint the round background and label.
	protected void paintComponent(Graphics g) 
	{
		if (getModel().isArmed()) 
		{
			// You might want to make the highlight color
			// a property of the RoundButton class.
			g.setColor(Color.lightGray);
		} else 
		{
			g.setColor(getBackground());
		}
		
		g.fillOval(0, 0, getSize().width-1, getSize().height-1);
		
		if (isEnabled())
			_helpIcon.paintIcon(this, g, 0, 0);
		else
			_grayedIcon.paintIcon(this, g, 0, 0);
	}
	
	// Paint the border of the button using a simple stroke.
	protected void paintBorder(Graphics g) 
	{
	}

	// Hit detection.
	private Shape shape;
	public boolean contains(int x, int y) 
	{
		// If the button has changed size, make a new shape object.
		if (shape == null || !shape.getBounds().equals(getBounds())) 
		{
			shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
		}
		
		return shape.contains(x, y);
	}

	private void prepareIcons()
	{
		ColorConvertOp converter =
			new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		
		BufferedImage original = getHelpImage();
		BufferedImage greyed = converter.filter(original, null);
		
		_helpIcon = new ImageIcon(original);
		_grayedIcon = new ImageIcon(greyed);
	}
	
	static public BufferedImage getHelpImage()
	{
		InputStream in = null;
		
		try
		{
			in = new FileResource("edu/virginia/vcgr/genii/client/dialog/gui/help-icon.png").open();
			return ImageIO.read(in);
		}
		catch (IOException ioe)
		{
			throw new RuntimeException("Unable to load help icon.", ioe);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static public Icon getHelpIcon()
	{
		return new ImageIcon(getHelpImage());
	}
}