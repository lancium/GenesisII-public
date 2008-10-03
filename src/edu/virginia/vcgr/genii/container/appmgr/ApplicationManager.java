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

import edu.virginia.vcgr.genii.client.io.FileSystemUtils;

public class ApplicationManager
{
	static public File prepareApplication(File executable) throws IOException
	{
		if (executable.exists())
			return FileSystemUtils.makeExecutable(executable);
		
		return null;
	}
}