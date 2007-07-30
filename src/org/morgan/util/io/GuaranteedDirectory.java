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
package org.morgan.util.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * A utility class which acts like a file, but upon successful creation
 * guarantees that the names path is an existing directory.
 *
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class GuaranteedDirectory extends File
{
	static final long serialVersionUID = 0;
	
	private void enforceGuarantee() throws IOException
	{
		if (exists())
		{
			if (!isDirectory())
				throw new IOException("Path \"" + 
					getAbsolutePath() + "\" is not a directory.");
		} else
		{
			if (!mkdirs())
				throw new IOException("Unable to create directory \"" +
					getAbsolutePath() + "\".");
		}
	}
	
	public GuaranteedDirectory(String path) throws IOException
	{
		super(path);
		enforceGuarantee();
	}
	
	public GuaranteedDirectory(String parent, String child) throws IOException
	{
		super(parent, child);
		enforceGuarantee();
	}
	
	public GuaranteedDirectory(File dir) throws IOException
	{
		super(dir.getAbsolutePath());
		enforceGuarantee();
	}
	
	public GuaranteedDirectory(File parent, String child) throws IOException
	{
		super(parent, child);
		enforceGuarantee();
	}
	
	public GuaranteedDirectory(URI path) throws IOException
	{
		super(path);
		enforceGuarantee();
	}
}
