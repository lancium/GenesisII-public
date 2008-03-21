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
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.bes.BESPolicy;
import edu.virginia.vcgr.genii.container.bes.BESPolicyActions;
import edu.virginia.vcgr.genii.container.bes.execution.Activity;
import edu.virginia.vcgr.genii.container.bes.execution.ActivityManager;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class DBBESResource extends BasicDBResource implements IBESResource
{
	@Override
	public void destroy() throws ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(
				"DELETE FROM bespolicytable WHERE besid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
			
			ActivityManager.getManager().removeBESPolicy(_resourceKey);
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

	@Override
	public void initialize(HashMap<QName, Object> constructionParams)
			throws ResourceException
	{
		PreparedStatement stmt = null;
		
		super.initialize(constructionParams);
		
		try
		{
			stmt = _connection.prepareStatement(
				"INSERT INTO bespolicytable " +
					"(besid, userloggedinaction, screensaverinactiveaction) " +
				"VALUES (?, ?, ?)");
			stmt.setString(1, _resourceKey);
			stmt.setString(2, BESPolicyActions.NOACTION.name());
			stmt.setString(3, BESPolicyActions.NOACTION.name());
			
			if (stmt.executeUpdate() != 1)
				throw new ResourceException(
					"Unable to create bes policy in database.");
			
			if (!isServiceResource())
				ActivityManager.getManager().setBESPolicy(_resourceKey, 
					new BESPolicy(BESPolicyActions.NOACTION,
							BESPolicyActions.NOACTION));
					
		}
		catch (SQLException sqe)
		{
			throw new ResourceException("Unable to delete resource.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}

	public DBBESResource(ResourceKey parentKey, DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	@Override
	public EndpointReferenceType[] getContainedActivities()
		throws RemoteException
	{
		Collection<Activity> activities = ActivityManager.getManager().getAllActivities(
			_resourceKey);
		
		Collection<EndpointReferenceType> ret = new LinkedList<EndpointReferenceType>();
		
		try
		{
			for (Activity activity : activities)
			{
				ret.add(activity.getActivityEPR());
			}
			
			return ret.toArray(new EndpointReferenceType[0]);
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Internal state error with BES activity.", sqe);
		}
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
			
			ActivityManager.getManager().setBESPolicy(_resourceKey, policy);
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
}
