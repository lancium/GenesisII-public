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
package edu.virginia.vcgr.genii.container.bes.activity.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStatusType;
import org.ggf.jsdl.JobDefinition_Type;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.bes.activity.SimpleExecutionProvider;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionEngine;
import edu.virginia.vcgr.genii.container.bes.execution.IExecutionProvider;
import edu.virginia.vcgr.genii.container.bes.jsdl.SimpleJobPlanProvider;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.jsdl.JobIdentificationRedux;
import edu.virginia.vcgr.genii.container.jsdl.JobPlan;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class DBBESActivityResource extends BasicDBResource implements
		IBESActivityResource
{
	static final private String _ADD_CONTAINER_ASSOCIATION_STMT =
		"INSERT INTO besactivities VALUES (?, ?, ?)";
	
	static private Log _logger = LogFactory.getLog(DBBESActivityResource.class);
	
	static private final String _JOB_DEF_PROPERTY = "job-definition";
	static private final String _BASE_DIRECTORY_PROPERTY = "job-directory";
	static private final String _EPR_PROPERTY = "resource-epr";
	
	private transient SimpleExecutionProvider _provider = null;
	
	static private HashMap<String, SimpleExecutionProvider> _providers =
		new HashMap<String, SimpleExecutionProvider>();
	
	public DBBESActivityResource(ResourceKey rKey, DatabaseConnectionPool pool) 
		throws SQLException
	{
		super(rKey, pool);
	}
	
	public JobDefinition_Type getJobDefinition() throws ResourceException
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(
			(byte[])getProperty(_JOB_DEF_PROPERTY));
		return (JobDefinition_Type)ObjectDeserializer.deserialize(
			new InputSource(bais), JobDefinition_Type.class);
	}
	
	public void associateWithContainer(
		EndpointReferenceType activityEPR, String containerID)
			throws ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(_ADD_CONTAINER_ASSOCIATION_STMT);
			stmt.setString(1, containerID);
			stmt.setString(2, (String)getKey());
			stmt.setBlob(3, EPRUtils.toBlob(activityEPR));
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update database.");
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.toString(), sqe);
		}
		finally
		{
			close(stmt);
		}
	}
	
	public void createProcess(EndpointReferenceType epr,
		File basedir, JobDefinition_Type jobDef) 
			throws ResourceException, JSDLException
	{
		setBaseDirectory(basedir);
		setJobDefinition(jobDef);
		
		setProperty(_EPR_PROPERTY, EPRUtils.toBytes(epr));
		
		JobPlan plan = new JobPlan(new SimpleJobPlanProvider(), jobDef);
		_provider = new SimpleExecutionProvider(epr, plan, basedir,
			new StateSaveCallback(epr));
		
		if (plan != null)
		{
			JobIdentificationRedux ident = plan.getJobIdentification();
			if (ident != null)
			{
				String name = ident.getJobName();
				if (name != null)
					setProperty(IBESActivityResource.ACTIVITY_NAME_PROPERTY, name);
			}
		}
	}
	
	public void restartProcessing() throws ResourceException, JSDLException
	{
		File baseDir = getBaseDirectory();
		JobDefinition_Type jobDef = getJobDefinition();
		EndpointReferenceType epr = 
			EPRUtils.fromBytes((byte[])getProperty(_EPR_PROPERTY));
		
		JobPlan plan = new JobPlan(new SimpleJobPlanProvider(), jobDef);
		SimpleExecutionProvider provider = 
			new SimpleExecutionProvider(epr, plan, baseDir, new StateSaveCallback(epr));
		
		synchronized(getParentResourceKey().getLockObject())
		{
			if (_providers.get(getKey()) != null)
				return;
			
			ExecutionEngine.getEngine().scheduleProvider(provider);
			_providers.put((String)getKey(), provider);
		}
	}

	public ActivityStatusType getOverallStatus() throws ResourceException
	{
		String key = (String)getKey();
		
		ActivityState state = (ActivityState)getProperty(
			"stored-activity-status");
		if (state != null)
			return ActivityState.toActivityStatus(state);
		
		SimpleExecutionProvider provider = null;
		
		synchronized(getParentResourceKey().getLockObject())
		{
			provider = _providers.get(key);
		}
		
		if (provider == null)
		{
			// We must have been restarted
			try
			{
				restartProcessing();
			}
			catch (JSDLException je)
			{
				// SHouldn't happen.
				_logger.error(je);
			}
			
			synchronized(getParentResourceKey().getLockObject())
			{
				provider = _providers.get(key);
			}
			
			if (provider == null)
				throw new ResourceException("Unable to restart job.");
		}
		
		ActivityStatusType status = provider.getJobState();
		state = ActivityState.fromActivityStatus(status);
		if (state.isTerminalState())
		{
			setProperty("stored-activity-status", state);
			synchronized(getParentResourceKey().getLockObject())
			{
				_providers.remove(key);
			}
		}
		
		return status;
	}
	
	static private final String _DESTROY_ACTIVITY_ASSOC_STMT =
		"DELETE FROM besactivities WHERE activitykey = ?";
	
	public void destroy() throws ResourceException
	{
		IExecutionProvider provider;
		PreparedStatement stmt = null;
		
		try
		{
			synchronized(getParentResourceKey().getLockObject())
			{
				provider = _providers.remove(getKey());
			}
			
			if (provider != null)
				provider.cancel();
			
			destroyDirectory(getBaseDirectory());
			stmt = _connection.prepareStatement(_DESTROY_ACTIVITY_ASSOC_STMT);
			stmt.setString(1, (String)getKey());
			stmt.executeUpdate();
			stmt.close();
			_connection.commit();
			
			try { super.destroy(); } catch (Throwable t) {}
		}
		catch (ResourceUnknownFaultType ruft)
		{
			_logger.error(ruft.getLocalizedMessage());
			throw new ResourceException(ruft.getLocalizedMessage(), ruft);
		}
		catch (SQLException sqe)
		{
			_logger.error(sqe.getLocalizedMessage());
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			close(stmt);
		}
	}

	private void setBaseDirectory(File baseDirectory) throws ResourceException
	{
		setProperty(_BASE_DIRECTORY_PROPERTY, baseDirectory);
	}

	private File getBaseDirectory() throws ResourceException
	{
		return (File)getProperty(_BASE_DIRECTORY_PROPERTY);
	}
	
	private void setJobDefinition(JobDefinition_Type jobDefinition)
		throws ResourceException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(baos);
		
		ObjectSerializer.serialize(writer, jobDefinition,
			new QName(GenesisIIConstants.GENESISII_NS, "job-definition"));
		try
		{
			writer.close();
			
			setProperty(_JOB_DEF_PROPERTY, baos.toByteArray());
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.getMessage(), ioe);
		}
	}

	static private void destroyDirectory(File dir)
	{
		for (File file : dir.listFiles())
		{
			if (file.isDirectory())
				destroyDirectory(file);
			file.delete();
		}
		
		dir.delete();
	}
	
	public void commit() throws ResourceException
	{
		ResourceKey parentKey = getParentResourceKey();
		if (parentKey == null)
			_logger.error("Parent Key is null!");
		Object lObj = parentKey.getLockObject();
		if (lObj == null)
			_logger.error("Lock object is null!");
		synchronized(lObj)
		{
			if (_provider != null)
			{
				ExecutionEngine.getEngine().scheduleProvider(_provider);
				_providers.put((String)getKey(), _provider);
				_provider = null;
			}
		}

		super.commit();
	}
	
	public void rollback()
	{
		super.rollback();
		_provider = null;
	}

	public boolean terminateActivity() 
		throws ResourceException, ResourceUnknownFaultType
	{
		IExecutionProvider provider;
		synchronized(getParentResourceKey().getLockObject())
		{
			provider = _providers.get(getKey());
		}
		
		if (provider == null)
			return false;
		
		return provider.cancel();
	}
	
	static private class StateSaveCallback implements IStateSaveCallback
	{
		static private final String _STATE_PROPERTY = "private:state";
		
		private EndpointReferenceType _target;

		public StateSaveCallback(EndpointReferenceType target)
		{
			_target = target;
		}
		
		public void saveState(ActivityState state)
			throws ResourceUnknownFaultType, ResourceException
		{
			ResourceKey rKey = ResourceManager.getTargetResource(_target);
			IResource resource = rKey.dereference();
			synchronized(rKey.getLockObject())
			{
				resource.setProperty(_STATE_PROPERTY, state);
				resource.commit();
			}
		}
		
		public ActivityState getSavedStatus() 
			throws ResourceException, ResourceUnknownFaultType
		{
			ResourceKey rKey = ResourceManager.getTargetResource(_target);
			IResource resource = rKey.dereference();
			synchronized(rKey.getLockObject())
			{
				return (ActivityState)resource.getProperty(_STATE_PROPERTY);
			}
		}
	}
}
