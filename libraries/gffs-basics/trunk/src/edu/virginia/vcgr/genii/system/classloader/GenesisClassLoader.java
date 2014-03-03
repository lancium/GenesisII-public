package edu.virginia.vcgr.genii.system.classloader;

/**
 * A class loader that builds a repository of all the known class loaders, so we can hopefully find
 * stuff across all our jar files.
 */
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GenesisClassLoader extends ClassLoader
{
	static private Log _logger = LogFactory.getLog(GenesisClassLoader.class);

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

			/*
			 * nope //temp design truncation to see if we are causing osgi problems. if
			 * (_loaders.size() > 0) { if (_logger.isDebugEnabled())
			 * _logger.debug("skipping classloader add; single instance mode: " +
			 * newLoader.toString()); return false; }
			 */

			_loaders.add(newLoader);
			return true;
		} else {
			if (_logger.isTraceEnabled())
				_logger.trace("classloader was already loaded: " + newLoader.toString());
			return false;
		}
	}

	/**
	 * this is where we do all the work by consulting our table of known loaders.
	 */
	@Override
	synchronized protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		Class<?> toReturn = null;
		Iterator<ClassLoader> itty = _loaders.iterator();
		while (itty.hasNext()) {
			ClassLoader curr = itty.next();
			try {
				toReturn = curr.loadClass(name);
				if (toReturn != null)
					return toReturn;
				_logger.debug("um, class loader gave us null rather than an exception.  that's not normal.  fail.");
			} catch (Throwable cause) {
				_logger.debug("did not find class " + name + " in loader: " + curr.toString());
			}
		}
		_logger.error("failure to find class '" + name + "' in any extant class loader.");
		throw new ClassNotFoundException(name);
	}
}
