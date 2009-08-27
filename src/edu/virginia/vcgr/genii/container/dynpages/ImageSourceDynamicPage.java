package edu.virginia.vcgr.genii.container.dynpages;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;

public abstract class ImageSourceDynamicPage implements DynamicPage
{
	static final private String DEFAULT_IMAGE_IO_OUTPUT_FORMAT = "PNG";
	
	private String _imageIOOutputFormat;
	
	protected abstract RenderedImage createImage() throws IOException;
	
	protected ImageSourceDynamicPage(String imageIOOutputFormat)
	{
		_imageIOOutputFormat = (imageIOOutputFormat == null) ?
			DEFAULT_IMAGE_IO_OUTPUT_FORMAT : imageIOOutputFormat;
	}
	
	protected ImageSourceDynamicPage()
	{
		this(null);
	}
	
	@Override
	final public void generate(PrintStream ps) throws IOException
	{
		RenderedImage image = createImage();
		ImageIO.write(image, _imageIOOutputFormat, ps);
	}
}