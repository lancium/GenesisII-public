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
package edu.virginia.vcgr.genii.container.byteio;

import java.io.IOException;
import java.sql.SQLException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class RByteIOResourceFactory extends BasicDBResourceFactory
{
	public RByteIOResourceFactory(DatabaseConnectionPool pool)
		throws IOException, SQLException, ConfigurationException
	{
		super(pool);
	}

	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new RByteIOResource(parentKey, _pool);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
}