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
package org.morgan.util.updater;

import java.io.IOException;
import java.util.ArrayList;

import org.morgan.util.Version;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
class UpdateListenerHandler
{
	private ArrayList<IUpdateListener> _listeners =
		new ArrayList<IUpdateListener>();
	
	protected UpdateListenerHandler()
	{
	}

	public void addUpdateListener(IUpdateListener listener)
	{
		synchronized (_listeners)
		{
			_listeners.add(listener);
		}
	}

	public void removeUpdateListener(IUpdateListener listener)
	{
		synchronized (_listeners)
		{
			_listeners.remove(listener);
		}
	}
	
	private IUpdateListener[] getListeners()
	{
		IUpdateListener []listeners;
		
		synchronized(_listeners)
		{
			listeners = new IUpdateListener[_listeners.size()];
			_listeners.toArray(listeners);
		}
		
		return listeners;
	}
	
	protected void fireExceptionOccurred(String msg, IOException ioe)
	{
		IUpdateListener []listeners = getListeners();
		for (IUpdateListener listener : listeners)
		{
			listener.exceptionOccurred(msg, ioe);
		}
	}
	
	protected void fireStartingUpdate(int filesToUpdate)
	{
		IUpdateListener []listeners = getListeners();
		for (IUpdateListener listener : listeners)
		{
			listener.startingUpdate(filesToUpdate);
		}
	}
	
	protected void fireFinishedUpdate()
	{
		IUpdateListener []listeners = getListeners();
		for (IUpdateListener listener : listeners)
		{
			listener.finishedUpdate();
		}
	}
	
	protected void fireStartingFileUpdate(
		String fileName, Version oldVersion, Version newVersion)
	{
		IUpdateListener []listeners = getListeners();
		for (IUpdateListener listener : listeners)
		{
			listener.startingFileUpdate(fileName, oldVersion, newVersion);
		}
	}
	
	protected void fireFinishedFileUpdate(String fileName)
	{	
		IUpdateListener []listeners = getListeners();
		for (IUpdateListener listener : listeners)
		{
			listener.finishedFileUpdate(fileName);
		}
	}
	
	protected void fireStartingCommit()
	{
		IUpdateListener []listeners = getListeners();
		for (IUpdateListener listener : listeners)
		{
			listener.startingCommit();
		}
	}
	
	protected void fireFinishedCommit()
	{
		IUpdateListener []listeners = getListeners();
		for (IUpdateListener listener : listeners)
		{
			listener.finishedCommit();
		}
	}
}
