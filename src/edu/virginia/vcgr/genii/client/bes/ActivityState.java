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
package edu.virginia.vcgr.genii.client.bes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.ggf.bes.factory.ActivityStatusType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

public class ActivityState implements Serializable, Cloneable
{
	static final long serialVersionUID = 0L;

	static final private String _GENII_NS = 
		"http://vcgr.cs.virginia.edu/genesisII/bes/activity-states";
	
	static final private String _GENII_SUS_NS =
		"http://vcgr.cs.virginia.edu/genesisII/bes/activity-states/suspend";
	static final private String _SUSPEND_STATE_ELEMENT_NAME = "suspended";
	static final private QName _SUSPEND_STATE_ELEMENT_QNAME = new QName(
		_GENII_SUS_NS, _SUSPEND_STATE_ELEMENT_NAME);
	
	@XmlAttribute(name = "bes-state", required = true)
	private String _besState;
	
	@XmlAttribute(name = "genii-state", required = false)
	private String _geniiState;
	
	@XmlAttribute(name = "is-suspended", required = false)
	private boolean _isSuspended;
	
	@SuppressWarnings("unused")
	private ActivityState()
	{	
		_besState = null;
		_geniiState = null;
		_isSuspended = false;
	}
	
	public ActivityState(ActivityStateEnumeration besState, 
		String geniiState, boolean isSuspended)
	{
		if (besState == null)
			throw new IllegalArgumentException("BESState cannot be null.");
		
		_besState = besState.getValue();
		_geniiState = geniiState;
		_isSuspended = isSuspended;
	}
	
	public ActivityState(ActivityStatusType wireState)
	{
		_geniiState = null;
		_isSuspended = false;
		
		if (wireState == null)
		{
			_besState = ActivityStateEnumeration._Failed;
		} else
		{
			MessageElement []any = wireState.get_any();
			if (any != null)
			{
				for (MessageElement me : any)
				{
					QName eName = me.getQName();
					if (eName.getNamespaceURI().equals(_GENII_NS))
						_geniiState = eName.getLocalPart();
					else if (eName.equals(_SUSPEND_STATE_ELEMENT_QNAME))
						_isSuspended = true;
				}
			}
			
			ActivityStateEnumeration stateE = wireState.getState();
			if (stateE == null)
			{
				_besState = ActivityStateEnumeration._Failed;
			} else
			{
				_besState = wireState.getState().getValue();
			}
		}
	}
	
	public ActivityState(MessageElement element)
		throws ResourceException
	{
		this(ObjectDeserializer.toObject(element, ActivityStatusType.class));
	}
	
	static private boolean equals(String one, String two)
	{
		if (one == null)
		{
			if (two == null)
				return true;
			else
				return false;
		} else
		{
			if (two == null)
				return false;
		}
		
		return one.equals(two);
	}
	
	public boolean equals(ActivityState other)
	{
		return equals(_besState, other._besState) &&
			equals(_geniiState, other._geniiState) &&
			(_isSuspended == other._isSuspended);
	}
	
	public boolean equals(Object other)
	{
		if (other instanceof ActivityState)
			return equals((ActivityState)other);
		return false;
	}
	
	public boolean isSuspended()
	{
		return _isSuspended;
	}
	
	public void suspend(boolean setSuspended)
	{
		_isSuspended = setSuspended;
	}
	
	public boolean isFinalState()
	{
		if (_besState.equals(ActivityStateEnumeration._Cancelled))
			return true;
		else if (_besState.equals(ActivityStateEnumeration._Failed))
			return true;
		else if (_besState.equals(ActivityStateEnumeration._Finished))
			return true;
		
		return false;
	}
	
	public boolean isFailedState()
	{
		return _besState.equals(ActivityStateEnumeration._Failed);	
	}
	
	public boolean isCancelledState()
	{
		return _besState.equals(ActivityStateEnumeration._Cancelled);	
	}
	
	public boolean isFinishedState()
	{
		return _besState.equals(ActivityStateEnumeration._Finished);	
	}
	
	public boolean isIgnoreable()
	{
		return _geniiState != null && _geniiState.equals("Ignoreable");
	}
	
	public ActivityStatusType toActivityStatusType()
	{
		Collection<MessageElement> anyC = new Vector<MessageElement>(2);
		if (_geniiState != null)
			anyC.add(new MessageElement(new QName(_GENII_NS, _geniiState)));
		if (_isSuspended)
			anyC.add(new MessageElement(_SUSPEND_STATE_ELEMENT_QNAME));
		
		return new ActivityStatusType(
			(anyC.size() > 0) ? anyC.toArray(new MessageElement[0]) : null,
			ActivityStateEnumeration.fromValue(_besState));
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(_besState);
		if (_geniiState != null)
			builder.append(":" + _geniiState);
		if (_isSuspended)
			builder.append("[suspended]");
		
		return builder.toString();
	}
	
	public Object clone()
	{
		return new ActivityState(
			ActivityStateEnumeration.fromValue(_besState),
			_geniiState, _isSuspended);
	}
}