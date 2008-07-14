package edu.virginia.vcgr.fuse.fs.genii;

import java.util.Calendar;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fuse.exceptions.FuseEntryAlreadyExistsException;
import edu.virginia.vcgr.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.fuse.exceptions.FuseFunctionNotImplementedException;
import edu.virginia.vcgr.fuse.exceptions.FuseNoSuchEntryException;
import edu.virginia.vcgr.fuse.exceptions.FuseUnknownException;
import edu.virginia.vcgr.fuse.fs.FuseFile;
import edu.virginia.vcgr.fuse.fs.FuseFileSystemEntry;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import fuse.FuseException;

public class GeniiFuseEntryFacade implements FuseFileSystemEntry
{
	static private Log _logger = LogFactory.getLog(GeniiFuseEntryFacade.class);
	
	private FuseFileSystemEntry _backendEntry = null;
	
	private GeniiFuseFileSystemContext _fsContext;
	private RNSPath _target;
	private TypeInformation _typeInfo = null;
	
	private void fillInBackend(RNSPath target, 
		GeniiFuseFileSystemContext fsContext)
			throws FuseException
	{
		try
		{
			if (target.exists())
			{
				_typeInfo = new TypeInformation(target.getEndpoint());
				
				if (_typeInfo.isRNS())
					_backendEntry = new GeniiFuseEntryDirectory(
						_target, _typeInfo, _fsContext);
				else if (_typeInfo.isRByteIO())
					_backendEntry = new GeniiFuseEntryRByteIO(
						_target, _typeInfo, _fsContext);
				else if (_typeInfo.isSByteIO())
					_backendEntry = new GeniiFuseEntrySByteIO(
						_target, _typeInfo, _fsContext);
				else
					_backendEntry = new GeniiFuseEntryGenericEndpoint(
						_target, _typeInfo, _fsContext);
				
				return;
			}
		} 
		catch (RNSPathDoesNotExistException e)
		{
			_logger.error(
				"Unexpected exception -- entry should exist, but doesn't.",
				e);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to create backend.", cause);
		}
		
		_typeInfo = null;
		_backendEntry = null;
	}
	
	private void mustExist(boolean mustExist) throws FuseException
	{
		if (!exists())
		{
			if (mustExist)
				throw new FuseNoSuchEntryException(
					"Target entry \"" + _target.getName() + 
					"\" does not exist.");
		} else
		{
			if (!mustExist)
				throw new FuseEntryAlreadyExistsException(
					"Target entry \"" + _target.getName() +
					"\" already exists.");
		}
	}
	
	public GeniiFuseEntryFacade(RNSPath target,
		GeniiFuseFileSystemContext fsContext) throws FuseException
	{
		_target = target;
		_fsContext = fsContext;
		
		fillInBackend(_target, _fsContext);
	}
	
	@Override
	public Calendar accessTime() throws FuseException
	{
		mustExist(true);
		return _backendEntry.accessTime();
	}

	@Override
	public void accessTime(Calendar time) throws FuseException
	{
		mustExist(true);
		_backendEntry.accessTime(time);
	}

	@Override
	public Calendar createTime() throws FuseException
	{
		mustExist(true);
		return _backendEntry.createTime();
	}

	@Override
	public void delete(boolean onlyUnlink) throws FuseException
	{
		mustExist(true);
		
		try
		{
			if (onlyUnlink)
				_target.unlink();
			else
				_target.delete();
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to delete/unlink entry.", 
				cause);
		}
	}

	@Override
	public boolean exists()
	{
		try
		{
			if (_target.exists())
			{
				if (_backendEntry == null)
					fillInBackend(_target, _fsContext);
				return true;
			} else
			{
				_backendEntry = null;
				return false;
			}
		}
		catch (Throwable cause)
		{
			_logger.error("Unexpected exception checking for existance.", 
				cause);
			return false;
		}
	}

	@Override
	public void flush() throws FuseException
	{
		if (exists())
			_backendEntry.flush();
	}

	@Override
	public int getPermissions() throws FuseException
	{
		mustExist(true);
		return _backendEntry.getPermissions();
	}

	@Override
	public long inode() throws FuseException
	{
		mustExist(true);
		return _backendEntry.inode();
	}

	@Override
	public boolean isDirectory()
	{
		if (!exists())
			return false;
		
		return _backendEntry.isDirectory();
	}

	@Override
	public boolean isFile()
	{
		if (!exists())
			return false;
		
		return _backendEntry.isFile();
	}

	@Override
	public boolean isSymlink()
	{
		if (!exists())
			return false;
		
		return _backendEntry.isSymlink();
	}

	@Override
	public long length() throws FuseException
	{
		mustExist(true);
		return _backendEntry.length();
	}

	@Override
	public void link(FuseFileSystemEntry source) throws FuseException
	{
		mustExist(false);
		
		if (!(source instanceof GeniiFuseEntryFacade))
			throw new FuseUnknownException("Cross device link attempted.");
	
		try
		{
			GeniiFuseEntryFacade sourceFacade = (GeniiFuseEntryFacade)source;
			_target.link(sourceFacade._target.getEndpoint());
			fillInBackend(_target, _fsContext);
		}
		catch (Throwable t)
		{
			throw FuseExceptions.translate("Unable to create link.", t);
		}
	}

	@Override
	public Collection<FuseFileSystemEntry> listContents() throws FuseException
	{
		mustExist(true);
		return _backendEntry.listContents();
	}

	@Override
	public void mkdir(int mode) throws FuseException
	{
		mustExist(false);
		
		try
		{
			_target.mkdir();
			fillInBackend(_target, _fsContext);
			_backendEntry.setPermissions(mode);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to make directory.", cause);
		}
	}

	@Override
	public Calendar modificationTime() throws FuseException
	{
		mustExist(true);
		return _backendEntry.modificationTime();
	}

	@Override
	public void modificationTime(Calendar time) throws FuseException
	{
		mustExist(true);
		_backendEntry.modificationTime(time);
	}

	@Override
	public String name()
	{
		return _target.getName();
	}

	@Override
	public FuseFile open(boolean create, boolean exclusiveCreate,
			boolean readable, boolean writable, boolean append,
			Long truncateLength) throws FuseException
	{
		try
		{
			if (exists())
			{
				if (create && exclusiveCreate)
					mustExist(false);
			} else
			{
				if (!create)
					mustExist(true);
				
				_target.createNewFile();
				fillInBackend(_target, _fsContext);
			}
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to open file.", cause);
		}
		
		return _backendEntry.open(create, exclusiveCreate, readable, 
			writable, append, truncateLength);
	}

	@Override
	public String pwd()
	{
		return _target.pwd();
	}

	@Override
	public FuseFileSystemEntry readlink() throws FuseException
	{
		mustExist(true);
		return _backendEntry.readlink();
	}

	@Override
	public void rename(FuseFileSystemEntry source) throws FuseException
	{
		mustExist(false);
		if (!(source instanceof GeniiFuseEntryFacade))
			throw new FuseUnknownException("Cross device link attempted.");
		
		try
		{
			GeniiFuseEntryFacade sourceFacade = (GeniiFuseEntryFacade)source;
			_target = sourceFacade._target;
			source.delete(true);
			source = null;
		}
		finally
		{
			if (source != null)
				_target = null;
			
			fillInBackend(_target, _fsContext);
		}
	}

	@Override
	public void setPermissions(int mode) throws FuseException
	{
		mustExist(true);
		_backendEntry.setPermissions(mode);
	}

	@Override
	public void symlink(FuseFileSystemEntry source) throws FuseException
	{
		mustExist(false);
		
		throw new FuseFunctionNotImplementedException(
			"symlink functionallity not implemented.");
	}
}