package edu.virginia.vcgr.genii.gjt.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;

public class StringStringFilesystemTriple
{
	@XmlAttribute(name = "key", required = true)
	private String _key;

	@XmlAttribute(name = "value", required = false)
	private String _value;

	@XmlAttribute(name = "filesystem-type", required = false)
	private FilesystemType _filesystemType;

	public StringStringFilesystemTriple(String key, String value, FilesystemType filesystemType)
	{
		_key = key;
		_value = value;
		_filesystemType = filesystemType;

		if (_filesystemType == null)
			_filesystemType = FilesystemType.Default;
	}

	public StringStringFilesystemTriple(String key, String value)
	{
		this(key, value, FilesystemType.Default);
	}

	public StringStringFilesystemTriple(String key)
	{
		this(key, null);
	}

	public StringStringFilesystemTriple()
	{
		this(null);
	}

	@XmlTransient
	public String getKey()
	{
		return _key;
	}

	@XmlTransient
	public String getValue()
	{
		return _value;
	}

	@XmlTransient
	public FilesystemType getFilesystemType()
	{
		return _filesystemType;
	}

	public void setKey(String key, ParameterizableBroker pBroker, ModificationBroker mBroker)
	{
		pBroker.fireParameterizableStringModified(_key, key);
		_key = key;
		mBroker.fireJobDescriptionModified();
	}

	public void setValue(String value, ParameterizableBroker pBroker, ModificationBroker mBroker)
	{
		pBroker.fireParameterizableStringModified(_value, value);
		_value = value;
		mBroker.fireJobDescriptionModified();
	}

	public void setFilesystemType(FilesystemType filesystemType)
	{
		_filesystemType = filesystemType;
	}
}