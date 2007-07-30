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

import java.util.Date;

import org.morgan.util.alarm.AlarmManager;
import org.morgan.util.alarm.IAlarmToken;
import org.morgan.util.event.DefaultEvent;
import org.morgan.util.event.EventDescription;
import org.morgan.util.event.EventManager;
import org.morgan.util.event.IEvent;
import org.morgan.util.event.IEventHandler;


import junit.framework.TestCase;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class AlarmTest extends TestCase implements IEventHandler
{
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
	
	private EventManager _eManager;
	private AlarmManager _aManager;
	
	protected void setUp() throws Exception
	{
		super.setUp();
		
		_eManager = new EventManager();
		_aManager = new AlarmManager(_eManager);
	}

	protected void tearDown() throws Exception
	{
		_aManager = null;
		_eManager = null;
		
		super.tearDown();
	}

	public void testSimpleAlarm() throws Exception
	{
		EventDescription desc = _eManager.registerEventDescription(
				"my-event", TestEvent.class);
			
		_eManager.registerHandler(desc, this, 0);
		
		TestEvent te = new TestEvent(desc);
		
		IAlarmToken rToken = _aManager.addAlarm(
			new Date(new Date().getTime() + 1000), te);
		rToken.block();
		TestCase.assertTrue(rToken.completed());
		TestCase.assertEquals(1, rToken.eventCount());
		TestCase.assertEquals(1, te.getCount());
	}
	
	public void testRepeatingAlarm() throws Exception
	{
		EventDescription desc = _eManager.registerEventDescription(
				"my-event2", TestEvent.class);
			
		_eManager.registerHandler(desc, this, 0);
		
		TestEvent te = new TestEvent(desc);
		
		IAlarmToken rToken = _aManager.addAlarm(1000, te);
		rToken.block();
		TestCase.assertFalse(rToken.completed());
		TestCase.assertEquals(1, rToken.eventCount());
		TestCase.assertEquals(1, te.getCount());
		rToken.block();
		TestCase.assertFalse(rToken.completed());
		TestCase.assertEquals(2, rToken.eventCount());
		TestCase.assertEquals(2, te.getCount());
		rToken.block();
		TestCase.assertFalse(rToken.completed());
		TestCase.assertEquals(3, rToken.eventCount());
		TestCase.assertEquals(3, te.getCount());
		rToken.cancel();
		TestCase.assertTrue(rToken.completed());
	}
	
	public boolean handleEvent(IEvent iEvent)
	{
		TestEvent event = (TestEvent)iEvent;
		event.inrement();
		
		return true;
	}
}
