package edu.virginia.vcgr.genii.container.jsdl;

import java.io.Serializable;

import org.ggf.jsdl.CreationFlagEnumeration;

import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.UsernamePasswordIdentity;

public class DataStage implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private FilesystemRelative<String> _name;
	private Boolean _deleteOnTerminate;
	private CreationFlagEnumeration _creationFlag;
	private String _sourceURI;
	private String _targetURI;
	private UsernamePasswordIdentity _credentials;
	
	public DataStage(FilesystemRelative<String> name,
		Boolean deleteOnTerminate, CreationFlagEnumeration creationFlag,
		String sourceURI, String targetURI, 
		UsernamePasswordIdentity credentials)
	{
		if (name == null)
			throw new IllegalArgumentException(
				"Data stage name cannot be null.");
		
		_name = name;
		_deleteOnTerminate = deleteOnTerminate;
		_creationFlag = creationFlag;
		_sourceURI = sourceURI;
		_targetURI = targetURI;
		_credentials = credentials;
		
		if (_sourceURI == null && _targetURI == null)
			throw new IllegalArgumentException(String.format(
				"Both source and target URIs cannot be null for stage %s.",
				_name));
	}
	
	final public FilesystemRelative<String> getFileName()
	{
		return _name;
	}
	
	final public boolean deleteOnTerminate(boolean defaultValue)
	{
		return (_deleteOnTerminate == null) ? 
			defaultValue : _deleteOnTerminate.booleanValue();
	}
	
	final public CreationFlagEnumeration getCreationFlag(
		CreationFlagEnumeration defaultValue)
	{
		return (_creationFlag == null) ?
			defaultValue : _creationFlag;
	}
	
	final public String getSourceURI()
	{
		return _sourceURI;
	}
	
	final public String getTargetURI()
	{
		return _targetURI;
	}
	
	final UsernamePasswordIdentity getCredentials()
	{
		return _credentials;
	}
}