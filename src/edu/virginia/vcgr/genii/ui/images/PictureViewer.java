package edu.virginia.vcgr.genii.ui.images;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import org.morgan.utils.gui.GUIUtils;

public class PictureViewer extends JDialog
{
	static final long serialVersionUID = 0L;
	
	private PictureViewer(Window owner, String imageName, Image image)
	{
		super(owner, (imageName == null) ? "Image Viewer" : String.format(
			"Image Viewer (%s)", imageName));
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		JScrollPane scroller = new JScrollPane(new ImageComponent(image));
		
		content.add(scroller, new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	static public void displayPicture(Window owner, Image image,
		String imageName)
	{
		PictureViewer viewer = new PictureViewer(owner, imageName, image);
		viewer.pack();
		viewer.setModalityType(ModalityType.MODELESS);
		GUIUtils.centerWindow(viewer);
		viewer.setVisible(true);
	}
	
	static public void displayPicture(Window owner, Image image)
	{
		displayPicture(owner, image, null);
	}
	
	static public void displayPicture(Image image, String imageName)
	{
		displayPicture(null, image, imageName);
	}
	
	static public void displayPicture(Image image)
	{
		displayPicture(null, image, null);
	}
}