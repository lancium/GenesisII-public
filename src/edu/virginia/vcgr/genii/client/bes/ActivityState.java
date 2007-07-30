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
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStatusType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public class ActivityState implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(ActivityState.class);
	
	static public ActivityState PENDING;
	static public ActivityState SUSPENDED;
	static public ActivityState STAGING_IN;
	static public ActivityState PREPARING_APPLICATION;
	static public ActivityState EXECUTING;
	static public ActivityState STAGING_OUT;
	static public ActivityState FAILED;
	static public ActivityState ERROR;
	static public ActivityState KILLED;
	static public ActivityState CANCELLED;
	static public ActivityState FINISHED;
	
	static private QName _PENDING = 
		new QName(GenesisIIConstants.BES_FACTORY_NS, "Pending");
	static private QName _RUNNING =
		new QName(GenesisIIConstants.BES_FACTORY_NS, "Running");
	static private QName _SUSPENDED = 
		new QName(GenesisIIConstants.GENESISII_NS, "Suspended");
	static private QName _STAGING_IN =
		new QName(GenesisIIConstants.GENESISII_NS, "Staging-in");
	static private QName _PREPARING_APP =
		new QName(GenesisIIConstants.GENESISII_NS, "Preparing-application");
	static private QName _EXECUTING =
		new QName(GenesisIIConstants.GENESISII_NS, "Executing");
	static private QName _STAGING_OUT =
		new QName(GenesisIIConstants.GENESISII_NS, "Staging-out");
	static private QName _FAILED =
		new QName(GenesisIIConstants.BES_FACTORY_NS, "Failed");
	static private QName _ERROR =
		new QName(GenesisIIConstants.GENESISII_NS, "Error");
	static private QName _KILLED =
		new QName(GenesisIIConstants.GENESISII_NS, "Killed");
	static private QName _CANCELLED =
		new QName(GenesisIIConstants.BES_FACTORY_NS, "Cancelled");
	static private QName _FINISHED =
		new QName(GenesisIIConstants.BES_FACTORY_NS, "Finished");
	
	static
	{
		PENDING = new ActivityState(_PENDING);
		SUSPENDED = new ActivityState(_RUNNING, _SUSPENDED);
		STAGING_IN = new ActivityState(_RUNNING, _STAGING_IN);
		PREPARING_APPLICATION = new ActivityState(_RUNNING, _PREPARING_APP);
		EXECUTING = new ActivityState(_RUNNING, _EXECUTING);
		STAGING_OUT = new ActivityState(_RUNNING, _STAGING_OUT);
		FAILED = new ActivityState(_FAILED);
		ERROR = new ActivityState(_FAILED, _ERROR);
		KILLED = new ActivityState(_FAILED, _KILLED);
		CANCELLED = new ActivityState(_CANCELLED);
		FINISHED = new ActivityState(_FINISHED);
	}

	private ArrayList<QName> _state = new ArrayList<QName>();
	
	private ActivityState(QName... stateList)
	{
		for (QName state : stateList)
		{
			_state.add(state);
		}
	}
	
	private ActivityState(MessageElement message)
	{
		while (message != null)
		{
			_state.add(message.getQName());
			Iterator iter = message.getChildElements();
			message = null;
			if (iter != null)
			{
				while (iter.hasNext())
				{
					Object obj = iter.next();
					if (obj instanceof MessageElement)
					{
						message = (MessageElement)obj;
						break;
					}
				}
			}
		}
	}
	
	public boolean isInState(ActivityState targetState)
	{
		int targetSize = targetState._state.size();
		
		if (_state.size() < targetSize)
			return false;
		
		for (int lcv = 0; lcv < targetSize; lcv++)
		{
			QName myState = _state.get(lcv);
			QName target = targetState._state.get(lcv);
			if (!myState.equals(target))
				return false;
		}
		
		return true;
	}
	
	public boolean equals(ActivityState other)
	{
		int size = other._state.size();
		if (size != _state.size())
			return false;
		
		for (int lcv = 0; lcv < size; lcv++)
		{
			if (!_state.get(lcv).equals(other._state.get(lcv)))
				return false;
		}
		
		return true;
	}
	
	public boolean equals(Object other)
	{
		return equals((ActivityState)other);
	}
	
	public int hashCode()
	{
		int ret = 0;
		
		for (QName state : _state)
		{
			ret <<= 3;
			ret ^= state.hashCode();
		}
		
		return ret;
	}
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		int tab = 0;
		
		for (QName state : _state)
		{
			for (int lcv = 0; lcv < tab; lcv++)
				buffer.append("      ");
			if (tab > 0)
				buffer.append("|-> ");
			buffer.append(state.toString() + "\n");
			tab++;
		}
		
		return buffer.toString();
	}
	
	public boolean isTerminalState()
	{
		return isInState(FAILED) ||
			isInState(FINISHED) || isInState(CANCELLED);
	}
	
	static public ActivityState fromMessage(MessageElement state)
	{
		return new ActivityState(state);
	}
	
	static public ActivityState fromActivityStatus(ActivityStatusType ast)
	{
		return new ActivityState(ast.get_any()[0]);
	}
	
	static public MessageElement toMessage(ActivityState state)
	{
		MessageElement ret = null;
		MessageElement last = null;
		
		try
		{
			for (QName stateName : state._state)
			{
				MessageElement next = new MessageElement(stateName);
				if (ret == null)
					ret = next;
				else
					last.addChild(next);
				
				last = next;
			}
			
			return ret;
		}
		catch (SOAPException se)
		{
			// Shouldn't happen
			_logger.fatal(
				"Unexpected exception:  " + se.getLocalizedMessage(),
				se);
			throw new RuntimeException(se);
		}
	}
	
	static public ActivityStatusType toActivityStatus(ActivityState state)
	{
		return new ActivityStatusType(new MessageElement[] { toMessage(state) });
	}
}