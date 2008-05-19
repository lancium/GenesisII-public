package edu.virginia.vcgr.genii.ftp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FilePermissions;
import org.morgan.ftp.IBackend;
import org.morgan.ftp.ListEntry;
import org.morgan.ftp.PathAlreadyExistsException;
import org.morgan.ftp.PathDoesNotExistException;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class GeniiBackend implements IBackend
{
	static private Log _logger = LogFactory.getLog(GeniiBackend.class);
	
	private String _username;
	private GeniiBackendConfiguration _configuration;
	
	public GeniiBackend(GeniiBackendConfiguration configuration)
	{
		_configuration = configuration;
	}
	
	@Override
	public boolean authenticate(String username, String password)
			throws FTPException
	{
		_username = username;
		return true;
	}

	@Override
	public void cwd(String path) throws FTPException
	{
		try
		{
			RNSPath newPath = _configuration.getCallingContext().getCurrentPath().lookup(
				path, RNSPathQueryFlags.MUST_EXIST);
			_configuration.getCallingContext().setCurrentPath(newPath);
		}
		catch (RNSException re)
		{
			_logger.debug("Error trying to look up RNS path \"" + path + "\".", re);
			throw new PathDoesNotExistException(path);
		}
	}

	@Override
	public void delete(String entry) throws FTPException
	{
		try
		{
			RNSPath path = _configuration.getCallingContext().getCurrentPath();
			path = path.lookup(entry, RNSPathQueryFlags.MUST_EXIST);
			path.delete();
		}
		catch (RNSPathDoesNotExistException re)
		{
			_logger.debug("Unable to delete rns path \"" + entry + "\".");
			throw new PathDoesNotExistException(entry);
		}
		catch (RNSException re)
		{
			_logger.debug("Unable to delete rns path \"" + entry + "\".");
			throw new FTPException(451, "Unknown internal error.");
		}
	}

	@Override
	public boolean exists(String entry) throws FTPException
	{
		try
		{
			_configuration.getCallingContext().getCurrentPath().lookup(entry, RNSPathQueryFlags.MUST_EXIST);
			return true;
		}
		catch (Throwable t)
		{
			return false;
		}
	}

	@Override
	public String getGreeting()
	{
		return "Genesis II FTP Daemon";
	}

	@Override
	public ListEntry[] list() throws FTPException
	{
		FilePermissions rwx = new FilePermissions(0x7, 0x7, 0x7);
		
		try
		{
			Collection<RNSPath> _paths = _configuration.getCallingContext().getCurrentPath().listContents();
			RNSPath []paths = _paths.toArray(new RNSPath[0]);
			ListEntry []ret = new ListEntry[paths.length];
			for (int lcv = 0; lcv < paths.length; lcv++)
			{
				TypeInformation typeInfo = new TypeInformation(paths[lcv].getEndpoint());
				if (typeInfo.isRNS())
				{
					ret[lcv] = new ListEntry(paths[lcv].getName(), new Date(), 0, _username, "genii", rwx, 1, true);
				} else if (typeInfo.isByteIO())
				{
					String typeDesc = typeInfo.getTypeDescription();
					
					long size = 0;
					try
					{
						size = Long.parseLong(typeDesc);
					}
					catch (NumberFormatException nfe)
					{
					}
					
					ret[lcv] = new ListEntry(paths[lcv].getName(), new Date(), size, _username, "genii", 
						rwx, 1, false);
				} else
				{
					RedirectFile rd = new RedirectFile(paths[lcv].getEndpoint());
					ret[lcv] = new ListEntry(paths[lcv].getName() + ".html", new Date(), rd.getSize(), _username,
						"genii", new FilePermissions(0x5, 0x5, 0x5), 1, false);
				}
			}
			
			return ret;
		}
		catch (RNSException re)
		{
			_logger.debug("Unable to list contents of RNS path.", re);
			throw new FTPException(451, "Unknown internal error.");
		}
	}

	@Override
	public String mkdir(String newDir) throws FTPException
	{
		try
		{
			RNSPath path = _configuration.getCallingContext().getCurrentPath();
			RNSPath newPath = path.lookup(newDir, RNSPathQueryFlags.MUST_NOT_EXIST);
			newPath.mkdir();
			return newPath.pwd();
		}
		catch (RNSPathAlreadyExistsException ae)
		{
			_logger.debug("Unable to create a new RNS directory.", ae);
			throw new PathAlreadyExistsException(newDir);
		}
		catch (RNSException re)
		{
			_logger.debug("Unable to create a new RNS directory.", re);
			throw new FTPException(451, "Unknown error trying to create new RNS directory.");
		}
	}

	@Override
	public String pwd() throws FTPException
	{
		return _configuration.getCallingContext().getCurrentPath().pwd();
	}

	@Override
	public void removeDirectory(String directory) throws FTPException
	{
		delete(directory);
	}

	@Override
	public void rename(String oldEntry, String newEntry) throws FTPException
	{
		try
		{
			RNSPath path = _configuration.getCallingContext().getCurrentPath();
			RNSPath oldPath = path.lookup(oldEntry, RNSPathQueryFlags.MUST_EXIST);
			RNSPath newPath = path.lookup(newEntry, RNSPathQueryFlags.MUST_NOT_EXIST);
			
			newPath.link(oldPath.getEndpoint());
			oldPath.unlink();
		}
		catch (RNSPathAlreadyExistsException ae)
		{
			_logger.debug("Error trying to rename RNS entries.", ae);
			throw new PathAlreadyExistsException(newEntry);
		}
		catch (RNSPathDoesNotExistException dne)
		{
			_logger.debug("Error trying to rename RNS entries.", dne);
			throw new PathDoesNotExistException(oldEntry);
		}
		catch (RNSException re)
		{
			_logger.debug("Error trying to rename RNS entries.", re);
			throw new FTPException(451, "Unknown internal error.");
		}
	}

	@Override
	public InputStream retrieve(String entry) throws FTPException
	{
		try
		{
			RNSPath path = _configuration.getCallingContext().getCurrentPath().lookup(
				entry, RNSPathQueryFlags.DONT_CARE);
			
			if (!path.exists())
			{
				if (entry.endsWith(".html"))
				{
					path = _configuration.getCallingContext().getCurrentPath().lookup(
						entry.substring(0, entry.length() - 5),
						RNSPathQueryFlags.MUST_EXIST);
				} else
					throw new PathDoesNotExistException(entry);
			}
			
			if (new TypeInformation(path.getEndpoint()).isByteIO())
				return ByteIOStreamFactory.createInputStream(path);
			else
				return (new RedirectFile(path.getEndpoint())).getStream();
		}
		catch (RNSPathDoesNotExistException dne)
		{
			_logger.debug("Couldn't find \"" + entry + "\" to retrieve.", dne);
			throw new PathDoesNotExistException(entry);
		}
		catch (Throwable cause)
		{
			_logger.debug("Unable to retrieve grid entry.", cause);
			throw new FTPException(451, "Unable to read entry.");
		}
	}

	@Override
	public long size(String entry) throws FTPException
	{
		try
		{
			RNSPath path = _configuration.getCallingContext().getCurrentPath().lookup(
				entry, RNSPathQueryFlags.DONT_CARE);
			
			if (!path.exists())
			{
				if (entry.endsWith(".html"))
				{
					path = _configuration.getCallingContext().getCurrentPath().lookup(
						entry.substring(0, entry.length() - 5),
						RNSPathQueryFlags.MUST_EXIST);
				} else
					throw new PathDoesNotExistException(entry);
			}
			
			TypeInformation typeInfo = new TypeInformation(path.getEndpoint());
			if (typeInfo.isByteIO())
				return typeInfo.getByteIOSize();
			else
				return (new RedirectFile(path.getEndpoint())).getSize();
		}
		catch (RNSPathDoesNotExistException dne)
		{
			_logger.debug("Couldn't find \"" + entry + "\" to retrieve size.", dne);
			throw new PathDoesNotExistException(entry);
		}
		catch (Throwable cause)
		{
			_logger.debug("Unable to retrieve grid entry size.", cause);
			throw new FTPException(451, "Unable to retrieve entry size.");
		}
	}

	@Override
	public OutputStream store(String entry) throws FTPException
	{
		try
		{
			RNSPath path = _configuration.getCallingContext().getCurrentPath();
			path = path.lookup(entry, RNSPathQueryFlags.DONT_CARE);
			
			if (path.exists())
			{
				if (!(new TypeInformation(path.getEndpoint()).isByteIO()))
					throw new FTPException(451, "Path is not a file.");
			} else
			{
				path.createNewFile();
			}
			
			return ByteIOStreamFactory.createOutputStream(path);
		}
		catch (Throwable cause)
		{
			_logger.debug("Unable to store a file into the grid.", cause);
			throw new FTPException(451, "Unable to store file.");
		}
	}
}