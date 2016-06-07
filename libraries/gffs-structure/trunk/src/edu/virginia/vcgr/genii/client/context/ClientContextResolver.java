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

	public static final String UNICORE_COMBINED_CONTEXT_LOADED = "edu.virginia.vcgr.unicore-context";

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

				/*
				 * hmmm: we are never seeing this message be printed for the unicore usage of the grid. as a result, we're never setting this
				 * unicore flag in the context, so for any unicore job that uses the grid, the first time the job does anything it will always
				 * incur a 7 second or so penalty while updating certs. it would be nice to figure out where the unicore context actually gets
				 * loaded and fix this.
				 */
				// _logger.debug("loaded combined context for unicore...");
				/*
				 * this should only be set in a context using the old combined method, which has only one current client: unicore. note that
				 * we never set this property in the context unless it's unicore, so no one had better check for 'false'. if there's anything
				 * in the context listed with this key, then it's a combined-style unicore context.
				 */
				// toReturn.setSingleValueProperty(UNICORE_COMBINED_CONTEXT_LOADED, "true");

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
