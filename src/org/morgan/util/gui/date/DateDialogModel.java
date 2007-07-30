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
package org.morgan.util.gui.date;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
  * @author Mark Morgan (mark@mark-morgan.org)
  */
public class DateDialogModel
{
	private ArrayList<IDateDialogModelListener> _listeners =
		new ArrayList<IDateDialogModelListener>();
	
	private Calendar _todaysDate;
	private Calendar _currentSelectedDate;
	private Calendar _currentViewedMonth;
	private Calendar _calendar;
	
	public DateDialogModel()
	{
		this(new Date());
	}
	
	public DateDialogModel(Date todaysDate)
	{
		this(todaysDate, todaysDate);
	}
	
	public DateDialogModel(Date todaysDate, Date selectedDate)
	{
		this(Calendar.getInstance(), todaysDate, selectedDate);
	}
	
	public DateDialogModel(Calendar calendar)
	{
		this(calendar, new Date());
	}
	
	public DateDialogModel(Calendar calendar, Date todaysDate)
	{
		this(calendar, todaysDate, todaysDate);
	}
	
	public DateDialogModel(Calendar calendar, Date todaysDate, Date selectedDate)
	{
		_calendar = calendar;
		
		_todaysDate = (Calendar)_calendar.clone();
		_todaysDate.setTime(todaysDate);
		
		_currentSelectedDate = (Calendar)_calendar.clone();
		_currentSelectedDate.setTime(selectedDate);
		
		_currentViewedMonth = (Calendar)_calendar.clone();
		_currentViewedMonth.setTime(selectedDate);
	}
	
	public void addDateModelListener(IDateDialogModelListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(listener);
		}
	}
	
	public void removeDateModelListener(IDateDialogModelListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(listener);
		}
	}
	
	private IDateDialogModelListener[] getListeners()
	{
		IDateDialogModelListener []listeners;
		
		synchronized(_listeners)
		{
			listeners = new IDateDialogModelListener[_listeners.size()];
			_listeners.toArray(listeners);
		}
		
		return listeners;
	}
	
	public void fireSelectedDateChange()
	{
		for (IDateDialogModelListener listener : getListeners())
		{
			listener.selectedDateChange(_currentSelectedDate);
		}
	}
	
	public void fireSelectedMonthChange()
	{
		for (IDateDialogModelListener listener : getListeners())
		{
			listener.selectedMonthChange(_currentViewedMonth);
		}
	}
	
	public Calendar getTodaysDate()
	{
		return _todaysDate;
	}
	
	public Calendar getSelectedDate()
	{
		return _currentSelectedDate;
	}
	
	public Calendar getSelectedMonth()
	{
		return _currentViewedMonth;
	}
	
	public void rollMonth(boolean forward)
	{
		int currentMonth = _currentViewedMonth.get(Calendar.MONTH);
		
		_currentViewedMonth.roll(Calendar.MONTH, forward);
		
		if (forward && (_currentViewedMonth.get(Calendar.MONTH) < currentMonth))
		{
			_currentViewedMonth.roll(Calendar.YEAR, true);
		} else if (!forward && 
			(_currentViewedMonth.get(Calendar.MONTH) > currentMonth))
		{
			_currentViewedMonth.roll(Calendar.YEAR, false);
		}
		
		fireSelectedMonthChange();
	}
	
	public void selectDate(Date newDate)
	{
		_currentSelectedDate.setTime(newDate);
		fireSelectedDateChange();
	}
}
