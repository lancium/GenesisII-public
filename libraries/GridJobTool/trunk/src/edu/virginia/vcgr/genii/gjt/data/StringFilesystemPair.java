package edu.virginia.vcgr.genii.gjt.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;

public class StringFilesystemPair
{
	@XmlAttribute(name = "value", required = false)
	private String _value;

	@XmlAttribute(name = "filesystem-type", required = false)
	private FilesystemType _filesystemType;

	public StringFilesystemPair(String value, FilesystemType filesystemType)
	{
		_value = value;
		_filesystemType = filesystemType;

		if (_filesystemType == null)
			_filesystemType = FilesystemType.Default;
	}

	public StringFilesystemPair(String value)
	{
		this(value, null);
	}

	public StringFilesystemPair()
	{
		this(null, null);
	}

	public void set(String value, ParameterizableBroker pBroker, ModificationBroker mBroker)
	{
		pBroker.fireParameterizableStringModified(_value, value);
		_value = value;
		mBroker.fireJobDescriptionModified();
	}

	@XmlTransient
	public void setFilesystemType(FilesystemType filesystemType)
	{
		_filesystemType = filesystemType;
	}

	public String get()
	{
		return _value;
	}

	public FilesystemType getFilesystemType()
	{
		return _filesystemType;
	}
}