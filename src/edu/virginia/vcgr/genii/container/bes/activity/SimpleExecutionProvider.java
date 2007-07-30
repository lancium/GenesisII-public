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
package edu.virginia.vcgr.genii.container.bes.activity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStatusType;
import org.ggf.jsdl.DataStaging_Type;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.appmgr.ApplicationManager;
import edu.virginia.vcgr.genii.container.bes.activity.resource.IStateSaveCallback;
import edu.virginia.vcgr.genii.container.bes.execution.JobExecutor;
import edu.virginia.vcgr.genii.container.bes.execution.ThreadAwareExecutionProvider;
import edu.virginia.vcgr.genii.container.bes.jsdl.HPCProfileApplicationRedux;
import edu.virginia.vcgr.genii.container.bes.jsdl.PosixApplicationRedux;
import edu.virginia.vcgr.genii.container.bes.jsdl.SimpleApplicationRedux;
import edu.virginia.vcgr.genii.container.bes.jsdl.SimpleDataStagingRedux;
import edu.virginia.vcgr.genii.container.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.jsdl.JobPlan;

public class SimpleExecutionProvider extends ThreadAwareExecutionProvider
{
	static private Log _logger = LogFactory.getLog(SimpleExecutionProvider.class);
	
	private IStateSaveCallback _stateSaveCallback;
	private File _baseDirectory;
	
	private String []_commandLine;
	private HashMap<String, String> _environmentOverload;
	private DataStagingItem []_dataStages;
	
	private ActivityState _currentState = null;
	private int _stagingProgress;
	
	private File _stdin;
	private File _stdout;
	private File _stderr;
	
	private boolean _canPrepare = true;
	
	public SimpleExecutionProvider(EndpointReferenceType resourceEPR,
		JobPlan plan, 
		File baseDirectory, IStateSaveCallback stateSave) 
		throws JSDLException
	{
		super(resourceEPR);
		
		_stateSaveCallback = stateSave;
		_currentState = ActivityState.PENDING;
		_baseDirectory = baseDirectory;
	
		SimpleApplicationRedux redux =
			(SimpleApplicationRedux)plan.getApplication();
		PosixApplicationRedux _posix = redux.getPosixApplication();
		if (_posix != null)
			_baseDirectory = _posix.getWorkingDirectory(_baseDirectory);
		HPCProfileApplicationRedux _hpc = redux.getHPCApplication();
		if (_hpc != null)
			_baseDirectory = _hpc.getWorkingDirectory(_baseDirectory);
		
		SimpleDataStagingRedux dataStaging = 
			(SimpleDataStagingRedux)plan.getDataStaging();
		if (dataStaging == null)
			_dataStages = new DataStagingItem[0];
		else
		{
			DataStaging_Type []stagingTypes = dataStaging.getDataStaging();
			if (stagingTypes == null)
				stagingTypes = new DataStaging_Type[0];
			
			_dataStages = new DataStagingItem[stagingTypes.length];
			for (int lcv = 0; lcv < _dataStages.length; lcv++)
			{
				_dataStages[lcv] = new DataStagingItem(stagingTypes[lcv]);
			}
		}
		
		SimpleApplicationRedux app =
			(SimpleApplicationRedux)plan.getApplication();
		String executable = null;
		String []arguments = null;
		
		if (app != null)
		{
			PosixApplicationRedux posix = app.getPosixApplication();
			if (posix != null)
			{
				executable = posix.getExecutable();
				arguments = posix.getArguments();
				_environmentOverload = posix.getEnvironment();
				
				_stdin = posix.getStdin(_baseDirectory);
				_stdout = posix.getStdout(_baseDirectory);
				_stderr = posix.getStderr(_baseDirectory);
			} else
			{
				_canPrepare = false;
				HPCProfileApplicationRedux hpc = app.getHPCApplication();
				if (hpc != null)
				{
					if (hpc.getWorkingDirectory(_baseDirectory) != null)
						_baseDirectory = hpc.getWorkingDirectory(_baseDirectory);
					
					executable = hpc.getExecutable();
					arguments = hpc.getArguments();
					_environmentOverload = hpc.getEnvironment();
					
					_stdin = hpc.getStdin(_baseDirectory);
					_stdout = hpc.getStdout(_baseDirectory);
					_stderr = hpc.getStderr(_baseDirectory);
				}
			}
		}
		
		if (executable == null)
		{
			_commandLine = null;
		} else
		{
			if (arguments == null)
				arguments = new String[0];
		
			_commandLine = new String[arguments.length + 1];
			_commandLine[0] = executable;
			for (int lcv = 0; lcv < arguments.length; lcv++)
				_commandLine[lcv + 1] = arguments[lcv];
			
			if (_environmentOverload == null)
				_environmentOverload = new HashMap<String, String>();
		}
		
		try
		{
			ActivityState savedStatus = _stateSaveCallback.getSavedStatus();
			if (savedStatus != null)
			{
				_currentState = savedStatus;
					
				if (!_currentState.isTerminalState())
					transitionState(ActivityState.KILLED);
			} else
			{
				transitionState(ActivityState.PENDING);
			}
		}
		catch (Throwable t)
		{
			_logger.debug(t);
		}
	}
	
	@Override
	protected void performOperation() 
		throws ResourceUnknownFaultType, ResourceException
	{
		ActivityState currentState;
		synchronized (this)
		{
			currentState = _currentState;
		}
		
		try
		{
			if (currentState.equals(ActivityState.PENDING))
			{
				_stagingProgress = 0;
				transitionState(ActivityState.STAGING_IN);
			} else if (currentState.equals(ActivityState.STAGING_IN))
			{
				if (_stagingProgress >= _dataStages.length)
				{
					transitionState(ActivityState.PREPARING_APPLICATION);
				} else
				{
					stageIn();
				}
			} else if (currentState.equals(ActivityState.PREPARING_APPLICATION))
			{
				if (_commandLine != null)
				{
					if (_canPrepare)
					{
						File prepared = 
							ApplicationManager.prepareApplication(
								_baseDirectory,
								_commandLine[0]);
						if (prepared != null)
							_commandLine[0] = prepared.getAbsolutePath();
					}
				}
				transitionState(ActivityState.EXECUTING);
			} else if (currentState.equals(ActivityState.EXECUTING))
			{
				runJob();
				_stagingProgress = 0;
				transitionState(ActivityState.STAGING_OUT);
			} else if (currentState.equals(ActivityState.STAGING_OUT))
			{
				if (_stagingProgress >= _dataStages.length)
				{
					transitionState(ActivityState.FINISHED);
				} else
				{
					stageOut();
				}
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
			_logger.debug(t);
			transitionState(ActivityState.ERROR);
		}
	}
	
	public boolean cancel() throws ResourceUnknownFaultType, ResourceException
	{
		synchronized(this)
		{
			if (!transitionState(ActivityState.CANCELLED))
				return false;
			if (_executingThread != null)
				_executingThread.interrupt();
		}
		
		return true;
	}

	synchronized public boolean needsMoreSteps()
	{
		return !_currentState.isTerminalState();
	}
	
	public ActivityStatusType getJobState()
	{
		synchronized(this)
		{
			return ActivityState.toActivityStatus(_currentState);
		}
	}
	
	synchronized private boolean transitionState(ActivityState newState)
		throws ResourceUnknownFaultType, ResourceException
	{
		if (_currentState.isTerminalState())
			return false;
		
		_currentState = newState;
		_stateSaveCallback.saveState(_currentState);
		
		return true;
	}
	
	private void stageIn() throws IOException
	{
		_dataStages[_stagingProgress++].stageIn(_baseDirectory);
	}
	
	private void stageOut() throws IOException
	{
		_dataStages[_stagingProgress++].stageOut(_baseDirectory);
	}
	
	private void runJob() throws IOException
	{
		if (_commandLine == null)
			return;
		
		try
		{
			JobExecutor.executeJob(_baseDirectory, _commandLine,
				_environmentOverload, _stdin, _stdout, _stderr);
		}
		catch (InterruptedException ie)
		{
			_logger.debug("Job cancelled.");
		}
	}
}
