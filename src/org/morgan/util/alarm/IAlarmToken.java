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

import java.util.Date;

/**
  * The token created when an alarm is created (giving the programmer access
  * to that alarm instance).
  *
  * @author Mark Morgan (mark@mark-morgan.org)
  */
public interface IAlarmToken
{
	public boolean completed();
	
	public int eventCount();
	
	/**
	 * Retrieve the next occurance of this alarm.  Could be null
	 * if there are no more occurances.
	 * 
	 * @return The next occurance of this alarm or null if there
	 * aren't any more.
	 */
	public Date nextOccurance();
	
	public void cancel();
	
	/**
	 * Only blocks until the next alarm is done.  If the alarm
	 * is a repeater, this method will only block until the very
	 * next one is over.
	 */
	public void block() throws InterruptedException;
	public void block(long timeoutMS) throws InterruptedException;
}
