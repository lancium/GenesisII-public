package edu.virginia.vcgr.externalapp;

import java.awt.Image;
import java.io.File;

import javax.imageio.ImageIO;

import edu.virginia.vcgr.genii.ui.images.PictureViewer;

public class PictureViewerApplication extends AbstractExternalApplication
{
	@Override
	protected void doRun(File content) throws Throwable
	{
		Image image = ImageIO.read(content);
		if (image == null)
			throw new IllegalArgumentException(String.format(
				"Don't know how to read image format from %s.", 
				content.getName()));
		
		PictureViewer.displayPicture(image, content.getName());
	}
}