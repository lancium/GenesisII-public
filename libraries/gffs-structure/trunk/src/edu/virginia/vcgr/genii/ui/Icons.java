package edu.virginia.vcgr.genii.ui;

import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.morgan.util.configuration.ConfigurationException;

public class Icons extends Images {
	static private Icon _tearoff;

	static {
		try {
			_tearoff = loadIcon("tearoff.png");
		} catch (IOException ioe) {
			throw new ConfigurationException("Unable to load icons.", ioe);
		}
	}

	static protected ImageIcon loadIcon(String resourceName) throws IOException {
		return new ImageIcon(loadImage(resourceName));
	}

	static public Icon tearoffIcon() {
		return _tearoff;
	}
}