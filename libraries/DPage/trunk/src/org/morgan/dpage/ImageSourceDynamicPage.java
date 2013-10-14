package org.morgan.dpage;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;

public abstract class ImageSourceDynamicPage implements DynamicPage
{
	static final private String DEFAULT_IMAGE_IO_OUTPUT_FORMAT = "PNG";
	static final private int DEFAULT_WIDTH = 400;
	static final private int DEFAULT_HEIGHT = 400;

	private String _imageIOOutputFormat;

	private int _width;
	private int _height;

	@InjectParameter("width")
	private void widthOverride(String width)
	{
		_width = Integer.parseInt(width);
	}

	@InjectParameter("height")
	private void heightOverride(String height)
	{
		_height = Integer.parseInt(height);
	}

	protected abstract RenderedImage createImage() throws IOException;

	protected ImageSourceDynamicPage(String imageIOOutputFormat)
	{
		this(imageIOOutputFormat, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	protected ImageSourceDynamicPage(String imageIOOutputFormat, int defaultWidth, int defaultHeight)
	{
		_imageIOOutputFormat = (imageIOOutputFormat == null) ? DEFAULT_IMAGE_IO_OUTPUT_FORMAT : imageIOOutputFormat;
		_width = defaultWidth;
		_height = defaultHeight;
	}

	protected ImageSourceDynamicPage(int defaultWidth, int defaultHeight)
	{
		this(null, defaultWidth, defaultHeight);
	}

	protected ImageSourceDynamicPage()
	{
		this(null);
	}

	protected int width()
	{
		return _width;
	}

	protected int height()
	{
		return _height;
	}

	@Override
	final public void generatePage(PrintStream ps) throws IOException
	{
		RenderedImage image = createImage();
		ImageIO.write(image, _imageIOOutputFormat, ps);
	}
}