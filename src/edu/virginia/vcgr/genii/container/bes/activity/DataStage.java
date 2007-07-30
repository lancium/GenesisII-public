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

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import org.ggf.jsdl.SourceTarget_Type;

public abstract class DataStage implements Serializable
{
	static final long serialVersionUID = 0;
	
	private URI _uri;
	
	protected DataStage(SourceTarget_Type st)
	{
		try
		{
			_uri = new URI(st.getURI().toString());
		}
		catch (URISyntaxException use)
		{
			// Can't really happen.
			throw new RuntimeException("Unexpected exception.", use);
		}
	}

	protected URI getURI()
	{
		return _uri;
	}
}
