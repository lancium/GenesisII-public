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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class DBBESResource extends BasicDBResource implements IBESResource
{
	static private final String _FIND_ACTIVITIES =
		"SELECT activityepr FROM besactivities WHERE containerkey = ?";
	
	public DBBESResource(ResourceKey parentKey, DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}

	public EndpointReferenceType[] getContainedActivities()
		throws ResourceException
	{
		EndpointReferenceType []ret;
		ArrayList<EndpointReferenceType> tmpRet = new ArrayList<EndpointReferenceType>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = _connection.prepareStatement(_FIND_ACTIVITIES);
			stmt.setString(1, _resourceKey);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				tmpRet.add(EPRUtils.fromBlob(rs.getBlob(1)));
			}
			
			ret = new EndpointReferenceType[tmpRet.size()];
			tmpRet.toArray(ret);
			return ret;
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
		finally
		{
			close(rs);
			close(stmt);
		}
	}
}
