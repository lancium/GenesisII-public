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
package org.morgan.util.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.HashMap;

import org.morgan.util.Version;
import org.morgan.util.io.StreamUtils;

/**
 * Reads and stores a file whose format is line after line of:
 * 		version:relative-path
 * with a hash symbol meaning comment to end of line
 *
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class VersionList
{
	private HashMap<String, Version> _versions = new HashMap<String, Version>();
	
	public VersionList()
	{
	}
	
	public VersionList(InputStream in) throws IOException
	{
		this(new BufferedReader(new InputStreamReader(in)));
	}
	
	public VersionList(Reader reader) throws IOException
	{
		this(new BufferedReader(reader));
	}
	
	public VersionList(BufferedReader reader) throws IOException
	{
		String origLine;
		String line;
		
		while ( (origLine = reader.readLine()) != null)
		{
			line = origLine;
			int index = line.indexOf("#");
			if (index >= 0)
				line = line.substring(0, index);
			line = line.trim();
			if (line.length() == 0)
				continue;
			int firstBracket = line.indexOf("[");
			if (firstBracket < 0)
				throw new IOException("Can't parse version line \"" + origLine + "\".");
			int secondBracket = line.indexOf("]", firstBracket);
			if (secondBracket < 0)
				throw new IOException("Can't parse version line \"" + origLine + "\".");
			
			String relativePath = line.substring(0, firstBracket).trim();
			Version v = new Version(
				line.substring(firstBracket + 1, secondBracket).trim());
			_versions.put(relativePath, v);
		}
	}

	public String[] getRelativeFiles()
	{
		String []ret = new String[_versions.size()];
		_versions.keySet().toArray(ret);
		return ret;
	}
	
	public Version getVersion(String relativeFile)
	{
		return _versions.get(relativeFile);
	}
	
	public void store(File outputFile) throws IOException
	{
		PrintStream ps = null;
		
		try
		{
			ps = new PrintStream(outputFile);
			store(ps);
		}
		finally
		{
			StreamUtils.close(ps);
		}
	}
	
	public void store(PrintStream ps)
	{
		ps.println("#");
		ps.println("# Auto generated");
		ps.println("#");
		
		for (String relPath : _versions.keySet())
		{
			Version v = _versions.get(relPath);
			ps.println(relPath + "[" + v + "]");
		}
	}
}
