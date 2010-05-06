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
package edu.virginia.vcgr.genii.container.bes.resource;

import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.ggf.bes.factory.UnknownActivityIdentifierFaultType;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.GeniiBESConstants;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.bes.BES;
import edu.virginia.vcgr.genii.container.bes.BESPolicy;
import edu.virginia.vcgr.genii.container.bes.BESPolicyActions;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class DBBESResource extends BasicDBResource implements IBESResource
{	
	@Override
	public void initialize(HashMap<QName, Object> constructionParams)
			throws ResourceException
	{
		super.initialize(constructionParams);
		
		try
		{
			if (!isServiceResource())
				BES.createBES(_resourceKey,
					new BESPolicy(BESPolicyActions.NOACTION, 
						BESPolicyActions.NOACTION));
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
				"Unable to create resource -- database error.", sqe);
		}
	}

	@Override
	public void destroy() throws ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			BES.deleteBES(_resourceKey);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to delete resource.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
		
		super.destroy();
	}

	public DBBESResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}

	@Override
	public BESPolicy getPolicy() throws RemoteException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = _connection.prepareStatement(
				"SELECT userloggedinaction, screensaverinactiveaction " +
					"FROM bespolicytable WHERE besid = ?");
			stmt.setString(1, _resourceKey);
			
			rs = stmt.executeQuery();
			if (!rs.next())
				throw FaultManipulator.fillInFault(
					new ResourceUnknownFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription("Resource is unknown.")
					}, null));
			
			return new BESPolicy(
				BESPolicyActions.valueOf(rs.getString(1)),
				BESPolicyActions.valueOf(rs.getString(2)));
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to get BES policy.", sqe);
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	public BES getBES() throws RemoteException
	{
		BES bes = BES.getBES(_resourceKey);
		if (bes == null)
			throw new RemoteException("Couldn't find active BES entity.");
		return bes;
	}
	
	@Override
	public void setPolicy(BESPolicy policy) throws RemoteException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(
				"UPDATE bespolicytable " +
					"SET userloggedinaction = ?, " +
						"screensaverinactiveaction = ? " +
				"WHERE besid = ?");
			stmt.setString(1, policy.getUserLoggedInAction().name());
			stmt.setString(2, policy.getScreenSaverInactiveAction().name());
			stmt.setString(3, _resourceKey);
			if (stmt.executeUpdate() != 1)
				throw new ResourceException("Unable to update bes policy.");
			
			getBES().getPolicyEnactor().setPolicy(policy);
		}
		catch (SQLException sqe)
		{
			throw new RemoteException(
				"Unable to update bes policy in database.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	@Override
	public Collection<BESActivity> getContainedActivities() 
		throws RemoteException
	{
		BES bes = BES.getBES(_resourceKey);
		return bes.getContainedActivities();
	}
	
	@Override
	public BESActivity getActivity(String activityid)
		throws RemoteException, UnknownActivityIdentifierFaultType
	{
		BES bes = BES.getBES(_resourceKey);
		BESActivity activity = bes.findActivity(activityid);
		if (activity == null)
			throw new UnknownActivityIdentifierFaultType("Unknown activity \"" 
				+ activityid + "\".", null);
		return activity;
	}
	
	@Override
	public BESActivity getActivity(EndpointReferenceType activity)
		throws RemoteException, UnknownActivityIdentifierFaultType
	{
		AddressingParameters ap = new AddressingParameters(
			activity.getReferenceParameters());
		String id = ap.getResourceKey();
		return getActivity(id);
	}
	
	public boolean isAcceptingNewActivities()
		throws RemoteException
	{
		Boolean storedAccepting = (Boolean)getProperty(
			IBESResource.STORED_ACCEPTING_NEW_ACTIVITIES);
		if (storedAccepting != null && !storedAccepting.booleanValue())
			return false;
		
		Integer threshold = (Integer)getProperty(
			IBESResource.THRESHOLD_DB_PROPERTY_NAME);
		
		return getBES().isAcceptingActivites(threshold);
	}
	
	@Override
	public void nativeQProperties(Properties props) throws RemoteException
	{
		replacePersistedProperties(
			GeniiBESConstants.NATIVE_QUEUE_CONF_CATEGORY, props);
	}
	@Override
	public Properties nativeQProperties() throws RemoteException
	{
		return getPersistedProperties(
			GeniiBESConstants.NATIVE_QUEUE_CONF_CATEGORY);
	}
}