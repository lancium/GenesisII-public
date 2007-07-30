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
package org.morgan.util.alarm;

import java.util.Comparator;
import java.util.Date;
import java.util.PriorityQueue;

import org.morgan.util.event.EventManager;
import org.morgan.util.event.IEvent;
import org.morgan.util.event.IEventDoneHandler;
import org.morgan.util.event.IEventFactory;
import org.morgan.util.event.IEventProgress;
import org.morgan.util.event.SingletonEventFactory;


/**
  * A manager for all alarms.  This manager is responsible for raising the
  * alarms when the time is right.
  *
  * @author Mark Morgan (mark@mark-morgan.org)
  */
public class AlarmManager extends Thread
{
	private EventManager _eventManager;
	private PriorityQueue<AlarmDescription> _alarms;
	
	public AlarmManager(EventManager eventManager)
	{
		super("Alarm Thread");
		
		_eventManager = eventManager;
		_alarms = new PriorityQueue<AlarmDescription>(
			32, DescriptionComparator.COMPARATOR);
		
		setDaemon(true);
		start();
	}
	
	private void insert(AlarmDescription desc)
	{
		synchronized(_alarms)
		{
			_alarms.add(desc);
			_alarms.notifyAll();
		}
	}
	
	private IAlarmToken addAlarm(Date nextOccurance, 
		Long interval, IEventFactory factory)
	{
		AlarmDescription desc = new AlarmDescription(nextOccurance,
			interval, factory);
		insert(desc);
		return desc;
	}
	
	public IAlarmToken addAlarm(Date occurance, IEvent event)
	{
		return addAlarm(
			occurance, new SingletonEventFactory(event));
	}
	
	public IAlarmToken addAlarm(
		Date occurance, IEventFactory factory)
	{
		return addAlarm(occurance, null, factory);
	}
	
	public IAlarmToken addAlarm(long interval, IEvent event)
	{
		return addAlarm(interval, 
			new SingletonEventFactory(event));
	}
	
	public IAlarmToken addAlarm(long interval, 
		IEventFactory factory)
	{
		return addAlarm(new Date(new Date().getTime() + interval),
			new Long(interval), factory);
	}
	
	public void run()
	{
		try
		{
			synchronized(_alarms)
			{
				while (true)
				{
					AlarmDescription desc = _alarms.peek();
					if ( (desc != null) &&
							((desc._cancelled) ||
							 (desc._nextOccurance.compareTo(new Date()) <= 0)))
					{
						if (desc._cancelled)
							continue;
						
						// raise the alarm
						desc = _alarms.remove();
						desc.raise();
					} else
					{
						if (desc != null)
						{
							long timeToWait = desc._nextOccurance.getTime() -
								(new Date().getTime());
							if (timeToWait < 0)
								continue;
							_alarms.wait(timeToWait);
						} else
						{
							_alarms.wait();
						}
					}
				}
			}
		}
		catch (InterruptedException ie)
		{
		}
	}
	
	private class AlarmDescription implements IAlarmToken, IEventDoneHandler
	{
		private Date _nextOccurance;
		private Long _repeatInterval;
		private IEventFactory _factory;
		private int _occurances = 0;
		private boolean _cancelled = false;
		
		AlarmDescription( 
			Date nextOccurance, Long repeatInterval, IEventFactory factory)
		{
			_nextOccurance = nextOccurance;
			_repeatInterval = repeatInterval;
			_factory = factory;
		}
		
		void raise()
		{
			_eventManager.raise(_factory.create(), this);
		}
		
		public void eventDoneCallback(IEventProgress progress)
		{
			_occurances++;
			if (!_cancelled && (_repeatInterval != null))
			{
				_nextOccurance = new Date(
					new Date().getTime() + _repeatInterval.longValue());

				insert(this);
			} else
			{
				_nextOccurance = null;
			}
			
			synchronized(this)
			{
				notifyAll();
			}
		}
		
		public boolean completed()
		{
			return _cancelled || (_nextOccurance == null);
		}

		public int eventCount()
		{
			return _occurances;
		}

		public Date nextOccurance()
		{
			return _nextOccurance;
		}

		public void cancel()
		{
			_cancelled = true;
		}

		public void block() throws InterruptedException
		{
			int start = _occurances;
			while (true)
			{
				if (_occurances > start)
					return;
				if (_nextOccurance == null)
					return;
				if (_cancelled)
					return;
				synchronized(this)
				{
					wait();
				}
			}
		}

		public void block(long timeoutMS) throws InterruptedException
		{
			int start = _occurances;
			if (_occurances > start)
				return;
			if (_nextOccurance == null)
				return;
			if (_cancelled)
				return;
			
			synchronized(this)
			{
				wait(timeoutMS);
			}
		}
	}
	
	static private class DescriptionComparator
		implements Comparator<AlarmDescription>
	{
		public int compare(AlarmDescription one, AlarmDescription two)
		{
			return one._nextOccurance.compareTo(two._nextOccurance);
		}
		
		static public DescriptionComparator COMPARATOR =
			new DescriptionComparator();
	}
}
