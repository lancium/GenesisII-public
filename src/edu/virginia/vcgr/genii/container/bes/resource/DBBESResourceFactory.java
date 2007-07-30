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

import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.bes.SharedBESTableBase;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class DBBESResourceFactory extends SharedBESTableBase
{
	public DBBESResourceFactory(DatabaseConnectionPool connectionPool) 
		throws SQLException
	{
		super(connectionPool);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new DBBESResource(parentKey, _pool);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
}
