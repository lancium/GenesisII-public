/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.genii.client.context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.utils.flock.FileLock;

public class ClientContextResolver implements IContextResolver
{
	static private Log _logger = LogFactory.getLog(ClientContextResolver.class);

	static final public String USER_CONTEXT_FILENAME = "user-context.xml";
	static final public String USER_TRANSIENT_FILENAME = "user-transient.dat";
	static final public String COMBINED_FILENAME = "user-combined.xml";

	public File getContextFile() throws IOException
	{
		return new File(ConfigurationManager.getCurrentConfiguration().getUserDirectory(), USER_CONTEXT_FILENAME);
	}

	public File getContextTransientFile() throws IOException
	{
		return new File(ConfigurationManager.getCurrentConfiguration().getUserDirectory(), USER_TRANSIENT_FILENAME);
	}

	public File getCombinedFile() throws IOException
	{
		return new File(ConfigurationManager.getCurrentConfiguration().getUserDirectory(), COMBINED_FILENAME);
	}

	@Override
	public ICallingContext resolveContext() throws FileNotFoundException, IOException
	{
		ICallingContext toReturn = null;
		if (_logger.isTraceEnabled())
			_logger.trace("<into calling context load>");
		File contextFile = getContextFile();
		FileLock fl = null;
		try {
			fl = FileLock.lockFile(contextFile);

			// if we don't have split context files, we will try to load a combined one.
			if (contextFile == null || !contextFile.exists() || contextFile.length() == 0) {
				File combinedFile = getCombinedFile();
				// make sure we could actually try loading the combined one.
				if (combinedFile == null || combinedFile.length() == 0) {
					_logger.info("unable to find a context to load, either split or combined style.");
					return null;
				}
				// we must unlock the file again before letting the context load occur (since it also locks).
				StreamUtils.close(fl);
				// now load the combined context.
				toReturn = ContextFileSystem.load(combinedFile);
			} else {
				// we must unlock the file again before letting the context load occur (since it also locks).
				StreamUtils.close(fl);
				// load the more modern split context from two files.
				toReturn = ContextFileSystem.load(contextFile, getContextTransientFile());
			}

		} finally {
			StreamUtils.close(fl);
			if (_logger.isTraceEnabled())
				_logger.trace(">out of calling context load<");
		}

		return toReturn;
	}

	@Override
	public void storeCurrentContext(ICallingContext ctxt) throws FileNotFoundException, IOException
	{
		ContextFileSystem.store(getContextFile(), getContextTransientFile(), ctxt);
	}

	@Override
	public Object clone()
	{
		return new ClientContextResolver();
	}
}
