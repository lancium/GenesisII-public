package edu.virginia.vcgr.genii.client.gpath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class GeniiPath implements Serializable
{
	static final long serialVersionUID = 0L;

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
}
