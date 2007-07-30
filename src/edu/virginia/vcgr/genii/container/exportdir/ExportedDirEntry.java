/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.container.exportdir;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;

class ExportedDirEntry
{
	private String _dirId;
	private String _name;
	private EndpointReferenceType _entryReference;
	private String _id;
	private String _type;
	private MessageElement []_attributes;
	
	/* must fit within size of type field in database */
	static public String _FILE_TYPE = "F";
	static public String _DIR_TYPE = "D";
	
	ExportedDirEntry(String dirId, String name, EndpointReferenceType entryReference, 
			String id, String type, MessageElement[] attributes)
	{
		_dirId = dirId;
		_name = name;
		
		_entryReference = entryReference;
		/* TODO:  The following code was used to alleviate a problem where EPRs
		 * maintained references to the original SOAP message which was too large and
		 * forcing unreasonable amounts of memory usage.
		try
		{
			_entryReference = EPRUtils.fromBytes(EPRUtils.toBytes(entryReference));
		}
		catch (Throwable t)
		{
			System.err.println("MOOCH:  Error.");
			t.printStackTrace(System.err);
		}
		*/
		
		_id = id;
		_type = type;
		_attributes = attributes;
	}
	
	ExportedDirEntry(String dirId, String name, EndpointReferenceType entryReference, 
			String id, String type)
	{
		this(dirId, name, entryReference, id, type, null);
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
	
	public void setAttributes(MessageElement [] attributes)
	{
		_attributes = attributes;
	}
	
	public void addAttribute(MessageElement attribute)
	{
		int origSize = 0;
		if (_attributes != null)
			origSize = _attributes.length;
		MessageElement [] newAttributes = new MessageElement[origSize + 1];
		for (int i = 0; i < origSize; i++)
		{
			newAttributes[i] = _attributes[i];
		}
		newAttributes[origSize] = attribute;
		_attributes = newAttributes;
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
