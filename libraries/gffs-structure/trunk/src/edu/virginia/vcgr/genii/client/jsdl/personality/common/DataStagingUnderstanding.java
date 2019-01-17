package edu.virginia.vcgr.genii.client.jsdl.personality.common;

import java.net.URI;

import org.ggf.jsdl.CreationFlagEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemRelativePath;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public class DataStagingUnderstanding
{
	private CreationFlagEnumeration _creationFlag = null;
	private boolean _deleteOnTerminate = true;
	private boolean _handleAsArchive = false;
	private boolean _alwaysStageOut = false;
	private String _fileSystemName = null;
	private String _filename = null;
	private URI _sourceURI = null;
	private URI _targetURI = null;
	private UsernamePasswordIdentity _usernamePassword;

	public CreationFlagEnumeration getCreationFlag()
	{
		return _creationFlag;
	}

	public void setCreationFlag(CreationFlagEnumeration flag)
	{
		_creationFlag = flag;
	}

	public boolean isDeleteOnTerminate()
	{
		return _deleteOnTerminate;
	}

	public void setDeleteOnTerminate(boolean onTerminate)
	{
		_deleteOnTerminate = onTerminate;
	}

	public boolean isHandleAsArchive()
	{
		return _handleAsArchive;
	}
	
	public boolean isAlwaysStageOut()
	{
		return _alwaysStageOut;
	}

	public void setHandleAsArchive(boolean handleAsArchive)
	{
		_handleAsArchive = handleAsArchive;
	}
	
	public void setAlwaysStageOut(boolean alwaysStageOut)
	{
		_alwaysStageOut = alwaysStageOut;
	}


	public void setFileSystemName(String fileSystemName)
	{
		_fileSystemName = fileSystemName;
	}

	public FilesystemRelativePath getFilePath()
	{
		return new FilesystemRelativePath(_fileSystemName, _filename);
	}

	public void setFilename(String _filename)
	{
		this._filename = _filename;
	}

	public String getFilename()
	{
		return this._filename;
	}

	public URI getSourceURI()
	{
		return _sourceURI;
	}

	public void setSourceURI(URI _sourceuri)
	{
		_sourceURI = _sourceuri;
	}

	public URI getTargetURI()
	{
		return _targetURI;
	}

	public void setTargetURI(URI _targeturi)
	{
		_targetURI = _targeturi;
	}

	public void setUsernamePassword(UsernamePasswordIdentity upi)
	{
		_usernamePassword = upi;
	}

	public UsernamePasswordIdentity getUsernamePassword()
	{
		return _usernamePassword;
	}
}