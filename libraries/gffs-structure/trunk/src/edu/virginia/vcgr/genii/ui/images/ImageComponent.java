package edu.virginia.vcgr.genii.ui.images;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;

public class ImageComponent extends JComponent
{
	static final long serialVersionUID = 0L;

	private Image _image;

	@Override
	protected void paintComponent(Graphics g)
	{
		g.drawImage(_image, 0, 0, null);
	}

	public ImageComponent(Image image)
	{
		_image = image;

		int height = _image.getHeight(null);
		int width = _image.getWidth(null);

		Dimension d = new Dimension(width, height);
		setMinimumSize(d);
		setPreferredSize(d);
		setMaximumSize(d);
		setSize(d);
	}
}