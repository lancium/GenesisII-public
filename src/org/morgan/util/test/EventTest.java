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
package org.morgan.util.test;

import org.morgan.util.event.DefaultEvent;
import org.morgan.util.event.EventDescription;
import org.morgan.util.event.EventException;
import org.morgan.util.event.EventManager;
import org.morgan.util.event.IEvent;
import org.morgan.util.event.IEventDoneHandler;
import org.morgan.util.event.IEventHandler;
import org.morgan.util.event.IEventHandlerToken;
import org.morgan.util.event.IEventProgress;

import junit.framework.TestCase;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class EventTest extends TestCase
	implements IEventHandler, IEventDoneHandler
{
	private EventManager _manager = null;
	private Object _key = new Object();
	
	static private class TestEvent extends DefaultEvent
	{
		static final long serialVersionUID = 0;
		
		private int _count;
		
		public TestEvent(EventDescription desc)
		{
			super(desc);
			
			_count = 0;
		}
		
		public void inrement()
		{
			_count++;
		}
		
		public int getCount()
		{
			return _count;
		}
	}
	
	protected void setUp() throws Exception
	{
		super.setUp();
		
		_manager = new EventManager();
	}

	protected void tearDown() throws Exception
	{
		_manager = null;
		
		super.tearDown();
	}
	
	public void testEvents() throws EventException, InterruptedException
	{
		EventDescription desc = _manager.registerEventDescription(
			"my-event", TestEvent.class);
		
		_manager.registerHandler(desc, this, 0);
		_manager.registerHandler(desc, this, 10);
		IEventHandlerToken tokenGamma = _manager.registerHandler(desc, this, -10);
		
		IEventProgress progress;
		TestEvent te = new TestEvent(desc);
		
		progress = _manager.raise(te);
		progress.block();
		
		TestCase.assertTrue(progress.completed());
		TestCase.assertEquals(3, te.getCount());
		
		tokenGamma.setActive(false);
		te = new TestEvent(desc);
		
		progress = _manager.raise(te);
		progress.block();
		
		TestCase.assertTrue(progress.completed());
		TestCase.assertEquals(2, te.getCount());
		
		tokenGamma.setActive(true);
		te = new TestEvent(desc);
		
		progress = _manager.raise(te);
		progress.block();
		
		TestCase.assertTrue(progress.completed());
		TestCase.assertEquals(3, te.getCount());
		
		tokenGamma.remove();
		te = new TestEvent(desc);
		
		progress = _manager.raise(te);
		progress.block();
		
		TestCase.assertTrue(progress.completed());
		TestCase.assertEquals(2, te.getCount());
	}
	
	public void testEventDoneHandler() throws Exception
	{
		EventDescription desc = _manager.registerEventDescription(
				"my-event2", TestEvent.class);
			
		_manager.registerHandler(desc, this, 0);
		_manager.registerHandler(desc, this, 10);
		_manager.registerHandler(desc, this, -10);
		
		TestEvent te = new TestEvent(desc);
		
		synchronized(_key)
		{
			IEventProgress progress = _manager.raise(te, this);
			_key.wait();
			TestCase.assertTrue(progress.completed());
		}
		
		TestCase.assertEquals(3, te._count);
	}
	
	public boolean handleEvent(IEvent event)
	{
		TestEvent te = (TestEvent)event;
		te.inrement();
		return true;
	}
	
	public void eventDoneCallback(IEventProgress prog)
	{
		synchronized(_key)
		{
			_key.notifyAll();
		}
	}
}
