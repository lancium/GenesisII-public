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
package edu.virginia.vcgr.genii.container.appmgr;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.io.FileSystemUtils;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;

public class ApplicationManager
{
	static private Log _logger = LogFactory.getLog(ApplicationManager.class);

	static public File prepareApplication(File executable) throws IOException
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.CreatingActivity);

		history.createTraceWriter("Preparing Activity Executable").format("Preparing executable %s", executable).close();

		if (executable.exists())
			return FileSystemUtils.makeExecutable(executable);

		history.createWarnWriter("Executable Not Found")
			.format("Can't locate executable %s -- we'll keep going just in case.", executable).close();

		_logger.warn(String.format("Executable file \"%s\" does not seem to exist.", executable.getAbsolutePath()));

		return null;
	}
}