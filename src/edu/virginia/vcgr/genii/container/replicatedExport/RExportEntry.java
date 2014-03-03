package edu.virginia.vcgr.genii.container.replicatedExport;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

class RExportEntry
{
	private String _dirId;
	private String _name;
	private EndpointReferenceType _entryReference;
	private String _id;
	private String _type;
	private MessageElement[] _attributes;
	/* must fit within size of type field in database */
	static public String _FILE_TYPE = "F";
	static public String _DIR_TYPE = "D";

	RExportEntry(String dirId, String name, EndpointReferenceType entryReference, String id, String type,
		MessageElement[] attributes)
	{
		_dirId = dirId;
		_name = name;
		_entryReference = entryReference;
		_id = id;
		_type = type;
		_attributes = attributes;

	}

	public String getDirId()
	{
		return _dirId;
	}

	public String getName()
	{
		return _name;
	}

	public EndpointReferenceType getEntryReference()
	{
		return _entryReference;
	}

	public String getId()
	{
		return _id;
	}

	public String getType()
	{
		return _type;
	}

	public MessageElement[] getAttributes()
	{
		return _attributes;
	}

	public void setAttributes(MessageElement[] attributes)
	{
		_attributes = attributes;
	}

	public boolean isDirectory()
	{
		if (_type != null && _type.equals(_DIR_TYPE))
			return true;
		return false;
	}

	public boolean isFile()
	{
		if (_type != null && _type.equals(_FILE_TYPE))
			return true;
		return false;
	}
}