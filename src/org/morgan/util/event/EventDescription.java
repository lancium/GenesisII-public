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
package org.morgan.util.event;

import java.io.Serializable;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class EventDescription implements Serializable
{
	static final long serialVersionUID = 0;
	
	private Class<? extends IEvent> _requiredBase;
	private String _eventName;
	private boolean _stopOnException;
	
	EventDescription(String eventName)
	{
		this(eventName, IEvent.class, false);
	}
	
	EventDescription(String eventName, 
		Class<? extends IEvent> requiredBase)
	{
		this(eventName, requiredBase, false);
	}
	
	EventDescription(String eventName, 
		Class<? extends IEvent> requiredBase, boolean stopOnException)
	{
		if (eventName == null)
			throw new IllegalArgumentException("Event Name cannot be null.");
		if (requiredBase == null)
			throw new IllegalArgumentException(
				"Required base class cannot be null.");
		
		_eventName = eventName;
		_requiredBase = requiredBase;
		_stopOnException = stopOnException;
	}
	
	public boolean matchesRequiredBase(IEvent event)
	{
		return _requiredBase.isAssignableFrom(event.getClass());
	}
	
	public Class<? extends IEvent> getRequiredBaseClass()
	{
		return _requiredBase;
	}
	
	public String getEventName()
	{
		return _eventName;
	}
	
	public boolean getStopOnException()
	{
		return _stopOnException;
	}
}
