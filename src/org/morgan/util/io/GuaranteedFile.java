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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * A utility class which acts like a file, but upon successful creation
 * guarantees that the named path is an existing file (creating it with a
 * given resource if necessary).
 *
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class GuaranteedFile extends File
{
	private static final int _BLOCK_SIZE = 1024 * 4;
	static final long serialVersionUID = 0;
	
	private void enforceGuarantee(String resourceName) throws IOException
	{
		if (exists())
		{
			if (!isFile())
				throw new IOException("Path \"" + getAbsolutePath() +
					"\" is not a file.");
		} else
		{
			ClassLoader loader;
			FileOutputStream out = null;
			InputStream in = null;
			
			try
			{
				out = new FileOutputStream(this);
				loader = Thread.currentThread().getContextClassLoader();
				in = loader.getResourceAsStream(resourceName);
				
				byte []block = new byte[_BLOCK_SIZE];
				int read;
				
				while ( (read = in.read(block)) >= 0)
				{
					out.write(block, 0, read);
				}
			}
			finally
			{
				StreamUtils.close(in);
				StreamUtils.close(out);
			}
		}
	}
	
	public GuaranteedFile(String path, String defaultResourceName)
		throws IOException
	{
		super(path);
		enforceGuarantee(defaultResourceName);
	}
	
	public GuaranteedFile(String parent, String child, String defaultResourceName) 
		throws IOException
	{
		super(parent, child);
		enforceGuarantee(defaultResourceName);
	}
	
	public GuaranteedFile(File dir, String defaultResourceName) 
		throws IOException
	{
		super(dir.getAbsolutePath());
		enforceGuarantee(defaultResourceName);
	}
	
	public GuaranteedFile(File parent, String child, 
		String defaultResourceName) throws IOException
	{
		super(parent, child);
		enforceGuarantee(defaultResourceName);
	}
	
	public GuaranteedFile(URI path, String defaultResourceName)
		throws IOException
	{
		super(path);
		enforceGuarantee(defaultResourceName);
	}
}
