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
package edu.virginia.vcgr.genii.ftp;

import java.util.ArrayList;

public class IdleSessionVulture extends Thread
{
	private ArrayList<IdleReapable> _reapables = 
		new ArrayList<IdleReapable>();
	
	public IdleSessionVulture()
	{
		super("Idle Session Vulture");
		setDaemon(true);
	}
	
	public void addReapable(IdleReapable reapable)
	{
		synchronized(_reapables)
		{
			_reapables.add(reapable);
		}
	}
	
	public void run()
	{
		IdleReapable []reapables;
		ArrayList<IdleReapable> removeable = 
			new ArrayList<IdleReapable>(); 
		
		while (true)
		{
			synchronized(_reapables)
			{
				reapables = new IdleReapable[_reapables.size()];
				_reapables.toArray(reapables);
			}
			
			removeable.clear();
			for (IdleReapable reapable : reapables)
			{
				if (reapable.closed())
					removeable.add(reapable);
				else if (reapable.reapable())
				{
					reapable.reap();
					removeable.add(reapable);
				}
			}
			
			synchronized(_reapables)
			{
				for (IdleReapable reapable : removeable)
				{
					_reapables.remove(reapable);
				}
			}
			
			try { Thread.sleep(1000 * 30); }
			catch (InterruptedException ie) {}
		}
	}
}
