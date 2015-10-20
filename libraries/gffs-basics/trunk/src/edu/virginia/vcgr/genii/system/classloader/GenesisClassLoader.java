package edu.virginia.vcgr.genii.system.classloader;

import java.util.HashSet;
import java.util.Iterator;

/**
 * A class loader that builds a repository of all the known class loaders, so we can hopefully find stuff across all our jar files.
 */
public class GenesisClassLoader extends ClassLoader
{
	private static GenesisClassLoader _theRealLoaderList = null;

	private HashSet<ClassLoader> _loaders = new HashSet<ClassLoader>();

	/**
	 * the real entre to the class loaders. we use this to load all class loaders.
	 */
	synchronized public static GenesisClassLoader classLoaderFactory()
	{
		if (GenesisClassLoader._theRealLoaderList == null) {
			_theRealLoaderList = new GenesisClassLoader();
			// start by adding the class loader for this class.
			_theRealLoaderList.addLoader(GenesisClassLoader.classLoaderFactory().getClass().getClassLoader());
		}
		return _theRealLoaderList;
	}

	/**
	 * returns true if this is a unique loader that was just added.
	 */
	synchronized public boolean addLoader(ClassLoader newLoader)
	{
		if (!_loaders.contains(newLoader)) {
			_loaders.add(newLoader);
			return true;
		} else {
			// below is just debugging noise and is not a real error condition.
			// System.err.println("classloader was already loaded: " + newLoader.toString());
			return false;
		}
	}

	/**
	 * this is where we do all the work by consulting our table of known loaders.
	 */
	@Override
	synchronized protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		/*
		 * !! important-- this function must not use log4j since loading the log4j classes is sometimes its responsibility. that's why here,
		 * of very few places indeed, we allow the code to log to standard error.
		 */
		Class<?> toReturn = null;
		Iterator<ClassLoader> itty = _loaders.iterator();
		while (itty.hasNext()) {
			ClassLoader curr = itty.next();
			try {
				toReturn = curr.loadClass(name);
				if (toReturn != null)
					return toReturn;
				System.err.println("um, class loader gave us null rather than an exception.  that's not normal.  fail.");
			} catch (Throwable cause) {
				System.err.println("did not find class " + name + " in loader: " + curr.toString());
			}
		}
		System.err.println("failure to find class '" + name + "' in any extant class loader.");
		throw new ClassNotFoundException(name);
	}
}
