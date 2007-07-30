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
package edu.virginia.vcgr.genii.client.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.jni.JNIClientBaseClass;

public class FileSystemUtils extends JNIClientBaseClass
{
	static public final int MODE_SET_UID = 04000;
	static public final int MODE_SET_GID = 02000;
	static public final int MODE_STICKY = 01000;
	static public final int MODE_USER_READ = 00400;
	static public final int MODE_USER_WRITE = 00200;
	static public final int MODE_USER_EXECUTE = 00100;
	static public final int MODE_GROUP_READ = 00040;
	static public final int MODE_GROUP_WRITE = 00020;
	static public final int MODE_GROUP_EXECUTE = 00010;
	static public final int MODE_WORLD_READ = 00004;
	static public final int MODE_WORLD_WRITE = 00002;
	static public final int MODE_WORLD_EXECUTE = 00001;

	static private Pattern _EXTENSION_PATTERN = Pattern.compile(
		"\\.\\w{3}(?=\\.)");
	
	static private native void chmod(String filePath, int mode)
		throws IOException;
	
	static private File makeWindowsExecutable(File file)
		throws FileNotFoundException, IOException
	{
		String filepath = file.getAbsolutePath();
		
		int index = filepath.lastIndexOf('.');
		if (index > 0)
		{
			String subString = filepath.substring(index + 1);
			if (	subString.equalsIgnoreCase("exe")		||
					subString.equalsIgnoreCase("com")		||
					subString.equalsIgnoreCase("cmd")		||
					subString.equalsIgnoreCase("bat")	)
				return file;
		}
		
		String guess = guessMostLikelyExtension(filepath);
		File ret = new File(file.getAbsolutePath() + "." + guess);
		if (!file.renameTo(ret))
			throw new IOException("Unable to rename file from \"" + file
				+ "\" to \"" + ret + "\".");
		return ret;
	}
	
	static private String guessMostLikelyExtension(String filepath)
	{
		Matcher matcher = _EXTENSION_PATTERN.matcher(filepath);
		while (matcher.find())
		{
			String possibleMatch = matcher.group().substring(1, 4);
			if (	possibleMatch.equalsIgnoreCase("exe")	||
					possibleMatch.equalsIgnoreCase("com")	||
					possibleMatch.equalsIgnoreCase("cmd")	||
					possibleMatch.equalsIgnoreCase("bat")	)
				return possibleMatch;
		}
		
		return "exe";
	}
	
	static public File makeExecutable(File filepath)
		throws FileNotFoundException, IOException
	{
		if (!filepath.exists())
			throw new FileNotFoundException("Couldn't find file \"" +
				filepath.getAbsolutePath() + "\".");

		String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows"))
			return makeWindowsExecutable(filepath);
		else
		{
			chmod(filepath.getAbsolutePath(),
				MODE_USER_READ | MODE_USER_WRITE | MODE_USER_EXECUTE);
			return filepath;
		}
	}
}
