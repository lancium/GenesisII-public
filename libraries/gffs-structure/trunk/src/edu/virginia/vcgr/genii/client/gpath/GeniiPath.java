package edu.virginia.vcgr.genii.client.gpath;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class GeniiPath implements Serializable
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(GeniiPath.class);
	static final private GeniiPathType DEFAULT_PATH_TYPE = GeniiPathType.Grid;

	private GeniiPathType _pathType;
	private String _path;

	transient private Object _cacheLock = new Object();
	transient private RNSPath _cachedLookup = null;

	private RNSPath lookup()
	{
		synchronized (_cacheLock) {
			if (_cachedLookup == null)
				_cachedLookup = RNSPath.getCurrent().lookup(_path);
		}

		return _cachedLookup;
	}

	public GeniiPath(String path)
	{
		if (path == null)
			path = "";

		int index = path.indexOf(':');
		if (index > 0) {
			String protocol = path.substring(0, index);
			for (GeniiPathType pathType : GeniiPathType.values()) {
				for (String testProtocol : pathType.protocols()) {
					if (testProtocol.equalsIgnoreCase(protocol)) {
						_path = path.substring(index + 1);
						_pathType = pathType;
						return;
					}
				}
			}
		}

		_path = path;
		_pathType = DEFAULT_PATH_TYPE;
	}

	public GeniiPath(RNSPath rnsPath)
	{
		_pathType = GeniiPathType.Grid;
		_path = rnsPath.pwd();
		_cachedLookup = rnsPath;
	}

	public void reset()
	{
		_cachedLookup = null;
	}

	public RNSPath lookupRNS()
	{
		switch (_pathType) {
			case Grid:
				return lookup();
			default:
				return null;
		}
	}

	@Override
	final public int hashCode()
	{
		return _path.hashCode();
	}

	final public boolean equals(GeniiPath other)
	{
		return (_pathType == other._pathType) && (_path.equals(other._path));
	}

	@Override
	final public boolean equals(Object other)
	{
		if (other instanceof GeniiPath)
			return equals((GeniiPath) other);

		return false;
	}

	@Override
	final public String toString()
	{
		return String.format("%s:%s", _pathType, _path);
	}

	final public GeniiPathType pathType()
	{
		return _pathType;
	}

	final public String path()
	{
		return _path;
	}

	final public boolean exists()
	{
		switch (_pathType) {
			case Grid:
				RNSPath gPath = lookup();
				return gPath.exists();

			case Local:
				File lPath = new File(_path);
				return lPath.exists();
		}

		return false;
	}

	final public boolean isFile()
	{
		try {
			switch (_pathType) {
				case Grid:
					RNSPath gPath = lookup();
					TypeInformation info = new TypeInformation(gPath.getEndpoint());
					return info.isByteIO();

				case Local:
					File lPath = new File(_path);
					return lPath.isFile();
			}
		} catch (Throwable cause) {
			// Do nothing
		}

		return false;
	}

	final public boolean isDirectory()
	{
		try {
			switch (_pathType) {
				case Grid:
					RNSPath gPath = lookup();
					TypeInformation info = new TypeInformation(gPath.getEndpoint());
					return info.isRNS();

				case Local:
					File lPath = new File(_path);
					return lPath.isDirectory();
			}
		} catch (Throwable cause) {
			// Do nothing
		}

		return false;
	}

	/**
	 * returns the last component of the path. this doesn't include local: or grid: in the returned
	 * string.
	 */
	final public String getName()
	{
		String basename = path();
		int slash_posn = basename.lastIndexOf('/');
		// ASG September 28, 2013. The code only checked for '/', not '\' as we see in Windows
		if (slash_posn < 0) // there was no '/'
			slash_posn = basename.lastIndexOf('\\');
		if (slash_posn >= 0)
			basename = basename.substring(slash_posn + 1);
		return basename;
	}

	/**
	 * returns everything but the last component of the path. this does not include grid: or local:
	 * in the returned string.
	 */
	final public String getParent()
	{
		String dirname = path();
		int slash_posn = dirname.lastIndexOf('/');
		if (slash_posn >= 1)
			dirname = dirname.substring(0, slash_posn);
		return dirname;
	}

	final public InputStream openInputStream() throws IOException
	{
		try {
			switch (_pathType) {
				case Grid:
					RNSPath gPath = lookup();
					TypeInformation typeInfo = new TypeInformation(gPath.getEndpoint());
					if (!typeInfo.isByteIO())
						throw new IOException(String.format("%s does not refer to a file.", this));
					return ByteIOStreamFactory.createInputStream(gPath.getEndpoint());

				case Local:
					File lPath = new File(_path);
					return new FileInputStream(lPath);

				default:
					throw new IllegalArgumentException("Unexpected path type in GeniiPath.");
			}
		} catch (RNSPathDoesNotExistException e) {
			throw new FileNotFoundException(String.format("Couldn't find file %s!", this));
		}
	}

	final public OutputStream openOutputStream() throws IOException
	{
		try {
			switch (_pathType) {
				case Grid:
					RNSPath gPath = lookup();
					if (gPath.exists()) {
						TypeInformation typeInfo = new TypeInformation(gPath.getEndpoint());
						if (!typeInfo.isByteIO())
							throw new IOException(String.format("%s does not refer to a file.", this));
					}

					return ByteIOStreamFactory.createOutputStream(gPath);

				case Local:
					File lPath = new File(_path);
					return new FileOutputStream(lPath);

				default:
					throw new IllegalArgumentException("Unexpected path type in GeniiPath.");
			}
		} catch (RNSException e) {
			throw new FileNotFoundException(String.format("Couldn't open ouput file %s!", this));
		}
	}

	final public RandomAccessFile openRandomAccessFile() throws IOException
	{
		try {
			switch (_pathType) {
				case Grid:
					RNSPath gPath = lookup();
					if (gPath.exists()) {
						TypeInformation typeInfo = new TypeInformation(gPath.getEndpoint());
						if (!typeInfo.isByteIO())
							throw new IOException(String.format("%s does not refer to a file.", this));
					}

					return null;

				case Local:
					RandomAccessFile lPath = new RandomAccessFile(_path, "rws");
					return lPath;

				default:
					throw new IllegalArgumentException("Unexpected path type in GeniiPath.");
			}
		} catch (RNSException e) {
			throw new FileNotFoundException(String.format("Couldn't open ouput file %s!", this));
		}
	}

	/**
	 * helper class that has room for either Java File object or an RNSPath. This allows a set of
	 * paths to be returned without lots of object casting after the lookups are already done.
	 */
	public static class PathMixIn
	{
		public RNSPath _rns = null;
		public File _file = null;

		PathMixIn(RNSPath rns)
		{
			_rns = rns;
		}

		PathMixIn(File file)
		{
			_file = file;
		}

		public String toString()
		{
			if (_rns != null)
				return _rns.toString();
			if (_file != null)
				return _file.toString();
			return "null";
		}
	}

	/**
	 * returns a list of paths of either local java File or RNSPath objects.
	 */
	public static Collection<PathMixIn> pathExpander(String path)
	{
		Collection<PathMixIn> toReturn = null;

		GeniiPath gp = new GeniiPath(path);
		if (gp.pathType() == GeniiPathType.Grid) {
			// rns path.
			Collection<RNSPath> paths = RNSPath.getCurrent().expand(gp.path());
			if (paths == null) {
				_logger.warn("path " + path + " could not be expanded.");
				return null;
			}
			toReturn = new ArrayList<PathMixIn>();
			for (RNSPath p : paths) {
				toReturn.add(new PathMixIn(p));
			}
		} else {
			File dir = new File(gp.getParent());
			if (dir.isFile()) {
				// if our directory is a file, then there were no directory components in the path.
				dir = new File(".");
			}
			if (!dir.exists()) {
				_logger.warn("directory " + dir + " does not exist.");
				return null;
			}
			String pattern = gp.getName();
			FileFilter fileFilter = new WildcardFileFilter(pattern);
			File[] files = dir.listFiles(fileFilter);
			toReturn = new ArrayList<PathMixIn>();
			for (int i = 0; i < files.length; i++) {
				toReturn.add(new PathMixIn(files[i]));
			}
		}

		if ((toReturn != null) && _logger.isDebugEnabled()) {
			_logger.debug("returning path set:");
			for (PathMixIn p : toReturn) {
				_logger.debug(p.toString());
			}
		} else {
			_logger.debug("did not expand any paths from: " + path);
		}

		return toReturn;
	}
}
