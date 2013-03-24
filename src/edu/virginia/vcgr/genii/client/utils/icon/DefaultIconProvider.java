package edu.virginia.vcgr.genii.client.utils.icon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

public abstract class DefaultIconProvider implements IconProvider
{
	static private class FileIconProvider extends DefaultIconProvider
	{
		private File _iconSource;

		private FileIconProvider(File iconSource)
		{
			_iconSource = iconSource;
		}

		@Override
		final public Icon createIcon()
		{
			FileInputStream fin = null;

			try {
				fin = new FileInputStream(_iconSource);
				return new ImageIcon(ImageIO.read(fin));
			} catch (IOException ioe) {
				throw new ConfigurationException(String.format("Unable to read image file %s.", _iconSource), ioe);
			} finally {
				StreamUtils.close(fin);
			}
		}
	}

	static abstract private class ResourceBasedIconProvider extends DefaultIconProvider
	{
		private String _resourceName;

		protected abstract InputStream openStream(String resourceName);

		private ResourceBasedIconProvider(String resourceName)
		{
			_resourceName = resourceName;
		}

		final public Icon createIcon()
		{
			InputStream in = null;

			try {
				in = openStream(_resourceName);
				if (in == null)
					throw new FileNotFoundException(String.format("Can't find resource %s.", _resourceName));
				return new ImageIcon(ImageIO.read(in));
			} catch (IOException ioe) {
				throw new ConfigurationException(String.format("Unable to read image from resource %s.", _resourceName), ioe);
			} finally {
				StreamUtils.close(in);
			}
		}
	}

	static private class ClassLoaderBasedIconProvider extends ResourceBasedIconProvider
	{
		private ClassLoader _loader;

		private ClassLoaderBasedIconProvider(ClassLoader loader, String resourceName)
		{
			super(resourceName);

			_loader = loader;
		}

		@Override
		final protected InputStream openStream(String resourceName)
		{
			return _loader.getResourceAsStream(resourceName);
		}
	}

	static private class ClassBasedIconProvider extends ResourceBasedIconProvider
	{
		private Class<?> _class;

		private ClassBasedIconProvider(Class<?> cl, String resourceName)
		{
			super(resourceName);

			_class = cl;
		}

		@Override
		final protected InputStream openStream(String resourceName)
		{
			return _class.getResourceAsStream(resourceName);
		}
	}

	static public IconProvider createIconProvider(File sourceFile)
	{
		return new FileIconProvider(sourceFile);
	}

	static public IconProvider createIconProvider(ClassLoader loader, String resourceName)
	{
		if (loader == null)
			loader = ClassLoader.getSystemClassLoader();

		return new ClassLoaderBasedIconProvider(loader, resourceName);
	}

	static public IconProvider createIconProvider(Class<?> relativeClass, String resourceName)
	{
		return new ClassBasedIconProvider(relativeClass, resourceName);
	}

	static public IconProvider createIconProvider(String resourceName)
	{
		return createIconProvider((ClassLoader) null, resourceName);
	}
}