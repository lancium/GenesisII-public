package org.morgan.dpage;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

class DynamicPagePackage implements Closeable
{
	static final public String PAGE_CONFIGURATION_FILE_PATH = "META-INF/dynamic-pages/page-conf.xml";
	static final public Pattern LIBRARY_PATH_PATTERN = Pattern.compile("^META-INF/lib/([^/]+)$");

	static private File extract(ScratchDirectory scratch, JarFile p, JarEntry entry, String libraryName) throws IOException
	{
		InputStream in = null;
		OutputStream out = null;

		File target = new File(scratch, libraryName);
		try {
			in = p.getInputStream(entry);
			out = new FileOutputStream(target);

			StreamUtils.copy(in, out);

			return target;
		} finally {
			StreamUtils.close(in);
			StreamUtils.close(out);
		}
	}

	static private Collection<File> extractLibraries(JarFile p, ScratchDirectory scratch) throws IOException
	{
		Collection<File> libraries = new LinkedList<File>();

		Enumeration<JarEntry> entries = p.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			Matcher matcher = LIBRARY_PATH_PATTERN.matcher(entry.getName());
			if (matcher.matches())
				libraries.add(extract(scratch, p, entry, matcher.group(1)));
		}

		return libraries;
	}

	static private Map<String, PageContextDescription> parseConfiguration(JarFile p) throws IOException
	{
		InputStream in = null;
		JarEntry entry = p.getJarEntry(PAGE_CONFIGURATION_FILE_PATH);
		if (entry == null)
			throw new FileNotFoundException(String.format("Unable to locate configuration file \"%s\".",
				PAGE_CONFIGURATION_FILE_PATH));
		try {
			in = p.getInputStream(entry);
			return PageConfigurationParser.parse(in);
		} catch (ParserConfigurationException e) {
			throw new IOException("Unable to parse configuration file.", e);
		} catch (SAXException e) {
			throw new IOException("Unable to parse configuration file.", e);
		} finally {
			StreamUtils.close(in);
		}
	}

	private ScratchDirectory _scratchDir;
	private JarFile _package;
	private ClassLoader _loader;
	private Map<String, PageContextDescription> _pageContexts;

	@Override
	protected void finalize() throws Throwable
	{
		close();
	}

	DynamicPagePackage(ScratchSpaceManager manager, File target) throws IOException
	{
		_package = new JarFile(target);

		_pageContexts = parseConfiguration(_package);

		_scratchDir = manager.newScratchDirectory();
		Collection<File> libraries = extractLibraries(_package, _scratchDir);
		Collection<URL> urls = new Vector<URL>(libraries.size());
		for (File library : libraries)
			urls.add(library.toURI().toURL());

		_loader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
		_loader = new URLClassLoader(new URL[] { target.toURI().toURL() }, _loader);
	}

	final Map<String, PageContextDescription> pageContexts()
	{
		return _pageContexts;
	}

	final ClassLoader classLoader()
	{
		return _loader;
	}

	@Override
	synchronized public void close() throws IOException
	{
		StreamUtils.close(_scratchDir);

		_scratchDir = null;
	}

	static public void main(String[] args) throws Throwable
	{
		ScratchSpaceManager manager = new ScratchSpaceManager(new File(System.getProperty("user.home") + "/scratch-space"));
		(new DynamicPagePackage(manager, new File(System.getProperty("user.home") + "/dpage.jar"))).close();
	}
}