package edu.virginia.g3.fsview;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import edu.virginia.g3.fsview.utils.IOUtils;

public class FSViewFactories
{
	static private Set<FSViewFactory> _factories = new HashSet<FSViewFactory>();

	static private Map<String, FSViewFactory> _factoryMap = new HashMap<String, FSViewFactory>();

	static {
		for (FSViewFactory factory : ServiceLoader.load(FSViewFactory.class)) {
			_factories.add(factory);

			for (String scheme : factory.supportedURISchemes())
				_factoryMap.put(scheme, factory);
		}
	}

	static public FSViewFactory[] factories()
	{
		return _factories.toArray(new FSViewFactory[_factories.size()]);
	}

	static public FSViewFactory factory(String scheme)
	{
		FSViewFactory factory = _factoryMap.get(scheme);
		if (factory == null)
			throw new IllegalArgumentException(String.format("No known FSView factory for scheme %s.", scheme));

		return factory;
	}

	static public FSViewFactory factory(URI fsRoot)
	{
		return factory(fsRoot.getScheme());
	}

	static public FSViewFactory factory(URL fsRoot) throws URISyntaxException
	{
		return factory(fsRoot.toURI());
	}

	static private void displayEntry(String prefix, FSViewEntry entry)
	{
		if (entry.entryType() == FSViewEntryType.Directory)
			System.out.format("%s[Directory]\t%s\n", prefix, entry.entryName());
		else
			System.out.format("%s%d\t%s\n", prefix, ((FSViewFileEntry) entry).size(), entry.entryName());
	}

	static public void main(String[] args) throws Throwable
	{
		URI fsRoot;
		FSViewFactory factory;
		FSViewSession session = null;

		// fsRoot = new File("/Users/morgan").toURI();
		fsRoot = URI.create("smb://athena.cs.virginia.edu/mmm2a/");
		// fsRoot = URI.create("ssh://mark-imac.cs.virginia.edu/Users/morgan");
		// fsRoot = URI.create("ftp://mark-imac.cs.virginia.edu:2121");

		factory = factory(fsRoot);

		try {
			/*
			 * session = factory.openSession(fsRoot, new AnonymousAuthenticationInformation(),
			 * true);
			 */
			session =
				factory.openSession(fsRoot, new UsernamePasswordAuthenticationInformation("griduser", "!!griduser"), true);

			FSViewEntry root = session.root();

			root = session.lookup("/public_html");

			displayEntry("", root);
			if (root.entryType() == FSViewEntryType.Directory) {
				for (FSViewEntry entry : ((FSViewDirectoryEntry) root).listEntries()) {
					displayEntry("\t", entry);

					if (entry.entryType() == FSViewEntryType.Directory) {
						for (FSViewEntry entry2 : ((FSViewDirectoryEntry) entry).listEntries()) {
							displayEntry("\t\t", entry2);

							if (entry2.entryType() == FSViewEntryType.Directory) {
								for (FSViewEntry entry3 : ((FSViewDirectoryEntry) entry2).listEntries()) {
									displayEntry("\t\t\t", entry3);
								}
							}
						}
					}
				}
			}
		} finally {
			IOUtils.close(session);
		}
	}
}