package org.morgan.ftp.backends.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.log4j.Logger;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.FilePermissions;
import org.morgan.ftp.IBackend;
import org.morgan.ftp.InternalException;
import org.morgan.ftp.ListEntry;
import org.morgan.ftp.PathAlreadyExistsException;
import org.morgan.ftp.PathDoesNotExistException;
import org.morgan.ftp.UnauthenticatedException;

public class LocalBackend implements IBackend
{
	static private Logger _logger = Logger.getLogger(LocalBackend.class);
	
	private LocalBackendConfiguration _backendConfiguration;
	private UserConfiguration _authenticatedAs = null;
	private File _pwd;
	
	static private boolean isSubdirectoryOf(File root, File subDir)
	{
		try
		{
			String canonicalizedAbsoluteRoot = root.getAbsoluteFile().getCanonicalPath();
			String canonicalizedAbsoluteSubDir = subDir.getAbsoluteFile().getCanonicalPath();
			
			return (canonicalizedAbsoluteSubDir + "/").startsWith(canonicalizedAbsoluteRoot);
		}
		catch (IOException ioe)
		{
			_logger.error("Unexpected IO exception trying to canonicalize file paths.", ioe);
			return false;
		}
	}
	
	static private String reRoot(File root, File file)
	{
		try
		{
			String canonicalizedAbsoluteRoot = root.getAbsoluteFile().getCanonicalPath();
			String canonicalizedAbsoluteSubDir = file.getAbsoluteFile().getCanonicalPath();
			
			String result = canonicalizedAbsoluteSubDir.substring(canonicalizedAbsoluteRoot.length()).trim();
			if (result.length() == 0)
				result = "/";
			
			return result;
		}
		catch (IOException ioe)
		{
			_logger.error("Unexpected IO exception trying to canonicalize file paths.", ioe);
			return "/";
		}
	}
	
	public LocalBackend(LocalBackendConfiguration conf)
	{
		_backendConfiguration = conf;
		_pwd = new File("/");
	}
	
	@Override
	public boolean authenticate(String username, String password)
			throws FTPException
	{
		UserConfiguration userConf = _backendConfiguration.findUser(username);
		if (userConf == null)
			return false;
		
		if (userConf.authenticate(password))
		{
			_authenticatedAs = userConf;
			_pwd = _authenticatedAs.getUserDir();
			return true;
		}
		
		return false;
	}

	private File getRootFile() throws UnauthenticatedException
	{
		if (_authenticatedAs == null)
			throw new UnauthenticatedException();
		
		return _authenticatedAs.getUserDir();
	}
	
	@Override
	public void cwd(String path) throws FTPException
	{
		File root = getRootFile();
		File newDir = new File(path.startsWith("/") ? root : _pwd, path);
		
		if (newDir.exists() && newDir.isDirectory() && isSubdirectoryOf(root, newDir))
		{
			_pwd = newDir;
			return;
		}
		
		throw new PathDoesNotExistException(path);
	}

	@Override
	public void delete(String entry) throws FTPException
	{
		File root = getRootFile();
		File file = new File(entry.startsWith("/") ? root : _pwd, entry);
		
		if (file.exists() && file.isFile() && isSubdirectoryOf(root, file))
		{
			if (!file.delete())
				throw new InternalException("Unable to delete file.");
			
			return;
		}
		
		throw new PathDoesNotExistException(entry);
	}

	@Override
	public boolean exists(String entry) throws FTPException
	{
		File root = getRootFile();
		File newDir = new File(entry.startsWith("/") ? root : _pwd, entry);
		
		return (newDir.exists() && isSubdirectoryOf(root, newDir));
	}

	@Override
	public String getGreeting()
	{
		return "Mark Morgan FTP Server (Local Version)";
	}

	@Override
	public String mkdir(String path) throws FTPException
	{
		File root = getRootFile();
		File newDir = new File(path.startsWith("/") ? root : _pwd, path);
		
		if (newDir.exists())
			throw new PathAlreadyExistsException(path);
		if (!isSubdirectoryOf(root, newDir))
			throw new PathDoesNotExistException(path);

		if (!newDir.mkdir())
			throw new FTPException(451, "Unable to create directory.");
		
		return reRoot(root, newDir);
	}

	@Override
	public String pwd() throws FTPException
	{
		return reRoot(getRootFile(), _pwd);
	}

	@Override
	public void removeDirectory(String directory) throws FTPException
	{
		File root = getRootFile();
		File file = new File(directory.startsWith("/") ? root : _pwd, directory);
		
		if (file.exists() && file.isDirectory() && isSubdirectoryOf(root, file))
		{
			if (!file.delete())
				throw new FTPException(550, "Directory not empty.");
			
			return;
		}
		
		throw new PathDoesNotExistException(directory);
	}

	@Override
	public void rename(String oldEntry, String newEntry) throws FTPException
	{
		File root = getRootFile();
		File oldFile = new File(oldEntry.startsWith("/") ? root : _pwd, oldEntry);
		File newFile = new File(newEntry.startsWith("/") ? root : _pwd, newEntry);
		
		if (!oldFile.exists() || !isSubdirectoryOf(root, oldFile))
			throw new PathDoesNotExistException(oldEntry);
		if (newFile.exists())
			throw new PathAlreadyExistsException(newEntry);
		if (!isSubdirectoryOf(root, newFile))
			throw new PathDoesNotExistException(newEntry);
		
		if (!oldFile.renameTo(newFile))
			throw new InternalException("Unable to rename file.");
	}

	@Override
	public InputStream retrieve(String entry) throws FTPException
	{
		File root = getRootFile();
		File file = new File(entry.startsWith("/") ? root : _pwd, entry);
		
		try
		{
			if (file.exists() && file.isFile() && isSubdirectoryOf(root, file))
				return new FileInputStream(file);
			
			throw new PathDoesNotExistException(entry);
		}
		catch (FileNotFoundException fnfe)
		{
			// Can't really happen
			_logger.error("Internal error.", fnfe);
			throw new InternalException("Internal error.", fnfe);
		}
	}

	@Override
	public long size(String path) throws FTPException
	{
		File root = getRootFile();
		File file = new File(path.startsWith("/") ? root : _pwd, path);
		
		if (file.exists() && file.isFile() && isSubdirectoryOf(root, file))
				return file.length();
			
		throw new PathDoesNotExistException(path);
	}

	@Override
	public OutputStream store(String entry) throws FTPException
	{
		File root = getRootFile();
		File file = new File(entry.startsWith("/") ? root : _pwd, entry);
		
		try
		{
			if (file.exists())
				throw new PathAlreadyExistsException(entry);
			
			if (!isSubdirectoryOf(root, file))
				throw new PathDoesNotExistException(entry);
			
			return new FileOutputStream(file);
		}
		catch (FileNotFoundException fnfe)
		{
			// Can't really happen
			_logger.error("Internal error.", fnfe);
			throw new InternalException("Internal error.", fnfe);
		}
	}

	@Override
	public ListEntry[] list() throws FTPException
	{
		File []files = _pwd.listFiles();
		if (files == null)
			return new ListEntry[0];
		
		ListEntry []ret = new ListEntry[files.length];
		for (int lcv = 0; lcv < files.length; lcv++)
		{
			File file = files[lcv];
			
			ret[lcv] = new ListEntry(file.getName(),
				new Date(file.lastModified()), file.length(),
				_authenticatedAs.getUser(), "unknown",
				new FilePermissions(
					(file.canRead() ? FilePermissions.READ_PERM : 0) 	|
					(file.canWrite() ? FilePermissions.WRITE_PERM : 0)	|
					(file.canExecute() ? FilePermissions.EXEC_PERM : 0),
					0, 0), 1, file.isDirectory()); 
		}
		
		return ret;
	}
}