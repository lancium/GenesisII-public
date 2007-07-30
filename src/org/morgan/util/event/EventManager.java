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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class EventManager
{
	private HashMap<String, EventDescription> _descriptions =
		new HashMap<String, EventDescription>();
	private HashMap<String, TreeSet<EventHandlerBundle> > _handlers;
	
	private ExecutorService _eventExecutor;
	
	public EventManager()
	{
		this(Executors.newCachedThreadPool());
	}
	
	public EventManager(ExecutorService eventExecutor)
	{
		if (eventExecutor == null)
			throw new IllegalArgumentException(
				"Executor service cannot be null.");
		
		_eventExecutor = eventExecutor;
		
		_handlers = new HashMap<String, TreeSet<EventHandlerBundle> >();
	}
	
	public EventDescription registerEventDescription(String eventName)
		throws EventException
	{
		EventDescription desc;
		
		synchronized(_descriptions)
		{
			if (_descriptions.containsKey(eventName))
				throw new EventException("Event \"" + eventName 
					+ "\" already exists.");
			_descriptions.put(eventName, desc = new EventDescription(eventName));
		}
		
		
		return desc;
	}
	
	public EventDescription registerEventDescription(String eventName,
		Class<? extends IEvent> requiredBase) throws EventException
	{
		EventDescription desc;
		
		synchronized(_descriptions)
		{
			if (_descriptions.containsKey(eventName))
				throw new EventException("Event \"" + eventName 
					+ "\" already exists.");
			_descriptions.put(eventName, 
				desc = new EventDescription(eventName, requiredBase));
		}
		
		
		return desc;
	}
	
	public EventDescription registerEventDescription(String eventName,
		Class<? extends IEvent> requiredBase,
		boolean stopOnException) throws EventException
	{
		EventDescription desc;
		
		synchronized(_descriptions)
		{
			if (_descriptions.containsKey(eventName))
				throw new EventException("Event \"" + eventName 
					+ "\" already exists.");
			_descriptions.put(eventName, 
				desc = new EventDescription(eventName, 
					requiredBase, stopOnException));
		}
		
		
		return desc;
	}
	
	public Collection<EventDescription> getRegisteredEvents()
	{
		synchronized(_descriptions)
		{
			return _descriptions.values();
		}
	}
	
	public EventDescription lookupEvent(String eventName)
	{
		synchronized(_descriptions)
		{
			return _descriptions.get(eventName);
		}
	}
	
	public IEventProgress raise(IEvent event)
	{
		return raise(event, null);
	}
	
	public IEventProgress raise(IEvent event, IEventDoneHandler doneHandler)
	{
		EventDescription desc = event.getDescription();
		if (!desc.matchesRequiredBase(event))
			throw new RuntimeException("Event doesn't match the required base.");
		
		TreeSet<EventHandlerBundle> set;
		EventHandlerBundle []handlers;
		synchronized(_handlers)
		{
			set = _handlers.get(desc.getEventName());
		}
		
		if (set != null)
		{
			synchronized(set)
			{
				handlers = new EventHandlerBundle[set.size()];
				set.toArray(handlers);
			}
		} else
		{
			handlers = new EventHandlerBundle[0];
		}
		
		EventRaiseWorker worker = new EventRaiseWorker(
			doneHandler, handlers, event);
		_eventExecutor.submit(worker);
		return worker;
	}
	
	public IEventHandlerToken registerHandler(EventDescription description,
		IEventHandler handler, int priority)
	{
		EventHandlerBundle bundle;
		TreeSet<EventHandlerBundle> bundles;
		
		synchronized(_handlers)
		{
			bundles = _handlers.get(description.getEventName());
			if (bundles == null)
			{
				_handlers.put(description.getEventName(),
					bundles = new TreeSet<EventHandlerBundle>());
			}
		}
		
		synchronized(bundles)
		{
			bundles.add(bundle = new EventHandlerBundle(handler, priority));
		}

		return new HandlerToken(description.getEventName(), bundle);
	}
	
	private class EventHandlerBundle implements Comparable<EventHandlerBundle>
	{
		private IEventHandler _handler;
		private int _priority;
		private boolean _isActive;
		
		public EventHandlerBundle(IEventHandler handler, int priority)
		{
			_handler = handler;
			_priority = priority;
			_isActive = true;
		}
		
		public IEventHandler getHandler()
		{
			return _handler;
		}
		
		public int getPriority()
		{
			return _priority;
		}
		
		public boolean isActive()
		{
			return _isActive;
		}
		
		public void setActive(boolean active)
		{
			_isActive = active;
		}
		
		public boolean handleEvent(IEvent event)
		{
			if (_isActive)
				return _handler.handleEvent(event);
			else
				return true;
		}
		
		public int compareTo(EventHandlerBundle other)
		{
			if (_priority == other._priority)
				return _handler.hashCode() - other._handler.hashCode();
			
			return _priority - other._priority;
		}
		
		public boolean equals(EventHandlerBundle other)
		{
			return compareTo(other) == 0;
		}
		
		public boolean equals(Object other)
		{
			return equals((EventHandlerBundle)other);
		}
		
		public int hashCode()
		{
			return _priority;
		}
	}
	
	private class HandlerToken implements IEventHandlerToken
	{
		private String _eventName;
		private EventHandlerBundle _bundle;
		
		HandlerToken(String eventName, EventHandlerBundle bundle)
		{
			_eventName = eventName;
			_bundle = bundle;
		}
		
		public IEventHandler getHandler()
		{
			return _bundle.getHandler();
		}
		
		public int getPriority()
		{
			return _bundle.getPriority();
		}
		
		public EventDescription getEventDescription()
		{
			synchronized(_descriptions)
			{
				return _descriptions.get(_eventName);
			}
		}
		
		public void setActive(boolean active)
		{
			_bundle.setActive(active);
		}
		
		public boolean isActive()
		{
			return _bundle.isActive();
		}
		
		public void remove()
		{
			TreeSet<EventHandlerBundle> set;
			
			synchronized(_handlers)
			{
				set = _handlers.get(_eventName);
			}
			
			if (set != null)
			{
				synchronized(set)
				{
					set.remove(_bundle);
				}
			}
		}
	}
	
	private class EventRaiseWorker implements Runnable, IEventProgress
	{
		private IEventDoneHandler _doneHandler;
		private boolean _cancelled = false;
		private ArrayList<Throwable> _exceptions = new ArrayList<Throwable>();
		private int _handlersCalled = 0;
		private IEvent _event;
		private EventHandlerBundle []_bundles;
		private boolean _completed = false;
		private Object _msg = new Object();
		
		EventRaiseWorker(IEventDoneHandler doneHandler,
			EventHandlerBundle []bundles, IEvent event)
		{
			_doneHandler = doneHandler;
			_bundles = bundles;
			_event = event;
		}
		
		public void run()
		{
			synchronized(_msg)
			{
				for (int lcv = 0; lcv < _bundles.length; lcv++)
				{
					if (_cancelled)
						break;
					
					EventHandlerBundle bundle = _bundles[lcv];
					if (bundle.isActive())
					{
						try
						{
							boolean continueHandling = bundle.handleEvent(_event);
							_handlersCalled++;
							if (!continueHandling)
								break;
						}
						catch (Throwable t)
						{
							_handlersCalled++;
							synchronized(_exceptions)
							{
								_exceptions.add(t);
							}
							
							if (_event.getDescription().getStopOnException())
								break;
						}
					}
				}
				
				_completed = true;
				_msg.notifyAll();
			}

			if (_doneHandler != null)
				_doneHandler.eventDoneCallback(this);
		}
		
		public IEvent getEvent()
		{
			return _event;
		}
		
		public void cancel()
		{
			_cancelled = true;
		}
		
		/**
		 * The result of this method is thread safe.
		 */
		public Throwable[] exceptionsRaised()
		{
			Throwable []ret;
			synchronized(_exceptions)
			{
				ret = new Throwable[_exceptions.size()];
				_exceptions.toArray(ret);
			}
			
			return ret;
		}
		
		public boolean completed()
		{
			return _completed;
		}
		
		public int numHandlersCalled()
		{
			return _handlersCalled;
		}
		
		public void block() throws InterruptedException
		{
			synchronized(_msg)
			{
				while (!_completed)
				{
					_msg.wait();
				}
			}
		}
		
		public void block(long timeoutMS) throws InterruptedException
		{
			synchronized(_msg)
			{
				if (!_completed)
				{
					_msg.wait(timeoutMS);
				}
			}
		}
	}
}
