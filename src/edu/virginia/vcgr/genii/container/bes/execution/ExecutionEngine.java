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
package edu.virginia.vcgr.genii.container.bes.execution;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

public class ExecutionEngine
{
	static private Log _logger = LogFactory.getLog(ExecutionEngine.class);
	
	static private final String _DEFAULT_NUM_THREADS = "20";
	
	static private ExecutionEngine _instance = null;
	
	static public ExecutionEngine getEngine()
	{
		synchronized(ExecutionEngine.class)
		{
			if (_instance == null)
			{
				String value;
				
				try
				{
					XMLConfiguration conf = ConfigurationManager.getCurrentConfiguration(
						).getContainerConfiguration();
					Properties props;
					
					synchronized(conf)
					{
						props = (Properties)conf.retrieveSection(
							GenesisIIConstants.GLOBAL_PROPERTY_SECTION_NAME);
					}
				
					value = props.getProperty(
						GenesisIIConstants.EXECUTION_ENGINE_THREAD_POOL_SIZE_PROPERTY,
						_DEFAULT_NUM_THREADS);
				}
				catch (Throwable t)
				{
					value = _DEFAULT_NUM_THREADS;
				}
				
				_instance = new ExecutionEngine(Integer.parseInt(value));
			}
		}
			
		return _instance;
	}
		
	private Object _lockObject = new Object();
	
	private LinkedList<IExecutionProvider> _ready =
		new LinkedList<IExecutionProvider>();
	private HashMap<IExecutionProvider, Thread> _running =
		new HashMap<IExecutionProvider, Thread>();

	private int _threadsWaiting = 0;
	private int _yetToCreate;
	
	private ExecutionEngine(int numThreads)
	{
		_yetToCreate = numThreads;
	}

	public void scheduleProvider(IExecutionProvider provider)
	{
		if (provider.needsMoreSteps())
		{
			synchronized(_lockObject)
			{
				if (_yetToCreate > 0 && (_threadsWaiting == 0) )
				{
					_yetToCreate--;
					ExecutionThread th = new ExecutionThread();
					_threadsWaiting++;
					th.start();
				}
				
				_ready.addLast(provider);
				_lockObject.notify();
			}
		}
	}
	
	public boolean cancel(IExecutionProvider provider) 
		throws ResourceUnknownFaultType, ResourceException
	{
		synchronized(_lockObject)
		{
			if (!_running.containsKey(provider))
			{
				_ready.remove(provider);
				return provider.cancel();
			} else
			{
				return provider.cancel();
			}
		}
	}
	
	private class ExecutionThread extends Thread
	{
		public ExecutionThread()
		{
			super("Execution Thread");
			setDaemon(true);
		}
		
		private IExecutionProvider claimProvider()
		{
			IExecutionProvider ret = null;
			
			synchronized(_lockObject)
			{
				while ( true )
				{
					if (_ready.isEmpty())
					{
						try { _lockObject.wait(); } catch (InterruptedException ie) {}
					} else
					{
						ret = _ready.removeFirst();
						_running.put(ret, this);
						_threadsWaiting--;
						return ret;
					}
				}
			}
		}
		
		private void runProvider(IExecutionProvider provider)
			throws ResourceUnknownFaultType, ResourceException
		{
			provider.step();
		}
		
		private void releaseProvider(IExecutionProvider provider)
		{
			synchronized(_lockObject)
			{
				_running.remove(provider);
				_threadsWaiting++;
				scheduleProvider(provider);
			}
		}
		
		public void run()
		{
			while (true)
			{
				IExecutionProvider provider = claimProvider();
				Thread.interrupted();
				try
				{
					runProvider(provider);
				}
				catch (Throwable t)
				{
					_logger.debug(t);
				}
				releaseProvider(provider);
			}
		}
	}
}
