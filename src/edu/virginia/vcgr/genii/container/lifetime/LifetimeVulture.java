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
package edu.virginia.vcgr.genii.container.lifetime;

import org.apache.axis.types.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.morgan.util.alarm.AlarmManager;
import org.morgan.util.event.DefaultEvent;
import org.morgan.util.event.EventDescription;
import org.morgan.util.event.EventException;
import org.morgan.util.event.EventManager;
import org.morgan.util.event.IEvent;
import org.morgan.util.event.IEventHandler;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.context.WorkingContext;

public class LifetimeVulture implements IEventHandler
{
	static private Log _logger = LogFactory.getLog(LifetimeVulture.class);
	
	static private final String _EVENT_NAME_BASE = "LifetimeVultureEvent";
	static private final long _DEFAULT_CHECK_INTERVAL = 1000 * 30;
	
	private HashMap<URI, Date> _nameDateMap = new HashMap<URI, Date>();
	private TreeSet<LifetimePrey> _preySet;
	
	public LifetimeVulture(EventManager eManager, AlarmManager aManager)
		throws EventException
	{
		_logger.info("Lifetime Watcher Initializing.");
		
		_preySet = new TreeSet<LifetimePrey>(
			LifetimePrey.createComparor());
		
		EventDescription ed = eManager.registerEventDescription(
			_EVENT_NAME_BASE + "." + (new GUID()).toString());
		IEvent event = new DefaultEvent(ed);
		eManager.registerHandler(ed, this, 0);
		aManager.addAlarm(_DEFAULT_CHECK_INTERVAL, event);
	}
	
	public void setLifetimeWatch(URI epi, EndpointReferenceType target,
		Date terminationDate)
	{
		_logger.debug("Adding resource for \"" + epi
			+ "\" to lifetime watcher.");

		synchronized(this)
		{
			removePrey(epi);
			if (terminationDate == null)
				return;
			
			_nameDateMap.put(epi, terminationDate);
			_preySet.add(new LifetimePrey(epi, target, terminationDate));
		}
	}
	
	public void checkTermintationStatuses()
	{
		_logger.debug("Checking termintation statuses from lifetime watcher.");
		ArrayList<LifetimePrey> bodies = new ArrayList<LifetimePrey>();
		Date now = new Date();
		
		synchronized(this)
		{
			while (true)
			{
				if (_preySet.isEmpty())
					break;
				LifetimePrey prey = _preySet.first();
				
				if (prey.getTermintationTime().before(now))
				{
					_nameDateMap.remove(prey.getName());
					_preySet.remove(prey);
					bodies.add(prey);
				} else
					break;
			}
		}
		
		try
		{
			WorkingContext.setCurrentWorkingContext(new WorkingContext());
			for (LifetimePrey prey : bodies)
			{
				_logger.debug("Auto-Termintating scheduled resource \""
					+ prey.getName() + "\".");
				
				try
				{
					prey.destroy();
				}
				catch (Throwable t)
				{
					_logger.warn(t.getMessage());
				}
			}
		}
		finally
		{
			WorkingContext.setCurrentWorkingContext(null);
		}
	}

	public boolean handleEvent(IEvent event)
	{
		checkTermintationStatuses();
		return true;
	}
	
	private void removePrey(URI name)
	{
		Date termDate;
		termDate = _nameDateMap.get(name);
		if (termDate == null)
			return;
		
		LifetimePrey prey = new LifetimePrey(name, null, termDate);
		_preySet.remove(prey);
	}
}
