package edu.virginia.vcgr.genii.gjt.gui.icons;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import edu.virginia.vcgr.genii.gjt.util.IOUtils;

public class Icons {
	static private Logger _logger = Logger.getLogger(Icons.class);

	static private final int MINIMUM_POINT_SIZE = 4;

	static final private String NO_FOLDER_24_24 = "no-folder-24x24.png";
	static final private String YES_FOLDER_24_24 = "yes-folder-24x24.png";
	static final private String FOLDER_24_24 = "folder-24x24.png";
	static final private String FOLDER_48_48 = "folder-48x48.png";
	static final private String SMALL_CHECK = "small-check.png";

	static final public Icon EmptyAt16By16 = createEmptyIcon(new Dimension(16,
			16));
	static final public Icon CheckMarkAt16By16 = loadIcon(SMALL_CHECK);
	static final public Icon YesFolderAt24By24 = loadIcon(YES_FOLDER_24_24);
	static final public Icon NoFolderAt24By24 = loadIcon(NO_FOLDER_24_24);
	static final public Icon FolderAt24By24 = loadIcon(FOLDER_24_24);
	static final public Icon FolderAt48By48 = loadIcon(FOLDER_48_48);

	static private Icon loadIcon(String iconName) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream in = null;

		try {
			in = Icons.class.getResourceAsStream(iconName);
			if (in == null)
				throw new FileNotFoundException(String.format(
						"Unable to load icon \"%s\".", iconName));
			IOUtils.copy(in, baos);
			baos.flush();
			return new ImageIcon(baos.toByteArray());
		} catch (Throwable cause) {
			_logger.error(
					String.format("Unable to load icon \"%s\".", iconName),
					cause);
			return null;
		} finally {
			IOUtils.close(in);
			IOUtils.close(baos);
		}
	}

	static public Icon labelIcon(Icon original, String text) {
		BufferedImage newImage = new BufferedImage(original.getIconWidth(),
				original.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = null;
		Rectangle canvas = new Rectangle(0, 0, original.getIconWidth(),
				original.getIconHeight());
		Rectangle2D r;

		try {
			g = newImage.createGraphics();
			original.paintIcon(null, g, 0, 0);

			Font font = g.getFont();
			while (true) {
				r = font.getStringBounds(text, g.getFontRenderContext());
				if (r.getWidth() <= canvas.width
						&& r.getHeight() < canvas.height)
					break;
				float size = font.getSize2D();
				if (size <= MINIMUM_POINT_SIZE)
					break;
				font = font.deriveFont(size - 1.0f);
			}

			g.setFont(font);
			g.setColor(Color.BLACK);
			g.drawString(text, (int) ((canvas.width - r.getWidth()) / 2),
					(int) ((canvas.height - r.getHeight()) / 2 + r.getHeight()));

			return new ImageIcon(newImage);
		} finally {
			g.dispose();
		}
	}

	static public Icon createEmptyIcon(Dimension size) {
		BufferedImage newImage = new BufferedImage(size.width, size.height,
				BufferedImage.TYPE_4BYTE_ABGR);
		return new ImageIcon(newImage);
	}

	static public Icon createGrayedIcon(Icon original) {
		BufferedImage newImage = new BufferedImage(original.getIconWidth(),
				original.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = null;

		try {
			g = newImage.createGraphics();
			original.paintIcon(null, g, 0, 0);

			return new ImageIcon(GrayFilter.createDisabledImage(newImage));
		} finally {
			g.dispose();
		}
	}

	static public Icon createTextIcon(Font font, Color color, Dimension size,
			String text) {
		BufferedImage newImage = new BufferedImage(size.width, size.height,
				BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = null;

		try {
			g = newImage.createGraphics();
			g.setFont(font);
			g.setColor(color);
			Rectangle2D rect = font.getStringBounds(text,
					g.getFontRenderContext());
			g.drawString(text, (float) 5, (float) (rect.getHeight() + 5));

			return new ImageIcon(newImage);
		} finally {
			g.dispose();
		}
	}
}