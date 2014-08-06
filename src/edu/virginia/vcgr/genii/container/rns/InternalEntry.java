/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.virginia.vcgr.genii.container.rns;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;

public class InternalEntry
{
	private String _name;
	private EndpointReferenceType _entryReference;
	private MessageElement[] _attributes;
	private boolean _doesExist; // added by ak3ka.
	ResourceForkInformation _rif; // Added by ASG, May 11, 2014

	public InternalEntry(String name, EndpointReferenceType entryReference, MessageElement[] attributes,
		ResourceForkInformation rif, boolean isExists)
	{
		_name = name;
		_entryReference = entryReference;
		_attributes = attributes;
		_doesExist = isExists;
		_rif = rif;
	}

	public InternalEntry(String name, EndpointReferenceType entryReference, MessageElement[] attributes, boolean isExists)
	{
		_name = name;
		_entryReference = entryReference;
		_attributes = attributes;
		_doesExist = isExists;
		_rif = null;
	}

	public InternalEntry(String name, EndpointReferenceType entryReference, MessageElement[] attributes)
	{
		this(name, entryReference, attributes, null, true);
	}

	public InternalEntry(String name, EndpointReferenceType entryReference)
	{
		this(name, entryReference, null);
	}

	public String getName()
	{
		return _name;
	}

	public EndpointReferenceType getEntryReference()
	{
		return _entryReference;
	}

	public void setAttributes(MessageElement[] attr)
	{
		_attributes = attr;
	}

	public MessageElement[] getAttributes()
	{
		return _attributes;
	}

	public ResourceForkInformation getResourceInformation()
	{
		return _rif;
	}

	public void setResourceInformation(ResourceForkInformation rif)
	{
		_rif = rif;
	}

	public boolean isExistent()
	{
		return _doesExist;
	}

}
