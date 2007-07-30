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
package edu.virginia.vcgr.genii.container.bes.execution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

public class JobExecutor
{
	static private Log _logger = LogFactory.getLog(JobExecutor.class);
	
	static public int executeJob(
		File workingDirectory, String []commandLine, 
		HashMap<String, String> environmentOverload,
		File stdin, File stdout, File stderr)
		throws InterruptedException, IOException
	{
		InputStream in = null;
		OutputStream out = null;
		
		if (commandLine == null)
			throw new IllegalArgumentException("Command Line cannot be null.");
		
		ProcessBuilder builder = new ProcessBuilder(commandLine);
		
		if (workingDirectory != null)
			builder.directory(workingDirectory);
		
		overloadEnvironment(builder.environment(), environmentOverload);
		resetCommand(builder);
		
		Process proc = null;
		try
		{
			debugProcessBuilder(builder);
			proc = builder.start();
			in = proc.getInputStream();
			if (stdout != null)
				redirect(in, stdout);
			else
				StreamUtils.close(in);
			
			out = proc.getOutputStream();
			if (stdin != null)
				redirect(stdin, out);
			else
				StreamUtils.close(out);
			
			in = proc.getErrorStream();
			if (stderr != null)
				redirect(in, stderr);
			else
				StreamUtils.close(in);
			return proc.waitFor();
		}
		catch (InterruptedException ie)
		{
			proc.destroy();
			throw ie;
		}
	}
	
	static private ExecutorService _streamThreadPool = 
		Executors.newCachedThreadPool();
	
	static private void redirect(File source, OutputStream target)
		throws IOException
	{
		boolean successful = false;
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(source);
			redirect(fin, target);
			successful = true;
		}
		finally
		{
			if (!successful)
			{
				StreamUtils.close(target);
				StreamUtils.close(fin);
			}
		}
	}
	
	static private void redirect(InputStream source, File target)
		throws IOException
	{
		boolean successful = false;
		FileOutputStream fout = null;
		
		try
		{
			fout = new FileOutputStream(target);
			redirect(source, fout);
			successful = true;
		}
		finally
		{
			if (!successful)
			{
				StreamUtils.close(source);
				StreamUtils.close(fout);
			}
		}
	}
	
	static private void redirect(InputStream source, OutputStream target)
	{
		_streamThreadPool.execute(new Redirector(source, target));
	}
	
	static private class Redirector implements Runnable
	{
		private InputStream _in;
		private OutputStream _out;
		
		public Redirector(InputStream in, OutputStream out)
		{
			_in = in;
			_out = out;
		}
		
		public void run()
		{
			byte []data = new byte[1024 * 4];
			int read;
			
			try
			{
				while ( (read = _in.read(data)) >= 0)
				{
					_out.write(data, 0, read);
				}
			}
			catch (IOException ioe)
			{
				_logger.debug(ioe);
			}
			finally
			{
				StreamUtils.close(_in);
				StreamUtils.close(_out);

				_logger.debug("Stream redirector closing.");
			}
		}
	}
	
	static private void debugProcessBuilder(ProcessBuilder builder)
	{
		StringBuilder sBuilder = new StringBuilder();
		
		sBuilder.append("About to create a process...\n\tCommand Line:");
		List<String> cLine = builder.command();
		for (String s : cLine)
		{
			sBuilder.append(" \"" + s + "\"");
		}
		sBuilder.append("\n\tWorking Directory:  \"" + builder.directory() 
			+ "\"\n\tEnvironment:\n");
		Map<String, String> env = builder.environment();
		for (String key : env.keySet())
		{
			if (key != null)
			{
				String value = env.get(key);
				sBuilder.append("\t\t\"" + key + "=" + value + "\"\n");
			}
		}

		_logger.debug(sBuilder.toString());
	}
	
	static private void overloadEnvironment(
		Map<String, String> processEnvironment,
		HashMap<String, String> overload)
	{
		if (overload == null || overload.size() == 0)
			return;
		
		String osName = System.getProperty("os.name");
		if (osName.contains("Windows"))
		{
			// Handle Windows
			overloadWindowsEnvironment(processEnvironment, overload);
		} else if (osName.contains("Linux"))
		{
			// Handle Linux
			overloadLinuxEnvironment(processEnvironment, overload);
		} else
		{
			throw new RuntimeException("Don't know how to handle \"" +
				osName + "\" platform.");
		}
	}
	
	static private void overloadLinuxEnvironment(
		Map<String, String> processEnvironment,
		HashMap<String, String> overload)
	{
		for (String variable : overload.keySet())
		{
			String value = overload.get(variable);
			if (variable.equals("PATH"))
				processEnvironment.put(variable, 
					mergePaths(processEnvironment.get(variable), value));
			else if (variable.equals("LD_LIBRARY_PATH"))
				processEnvironment.put(variable,
					mergePaths(processEnvironment.get(variable), value));
			else
				processEnvironment.put(variable, value);
		}
	}
	
	static private void overloadWindowsEnvironment(
		Map<String, String> processEnvironment,
		HashMap<String, String> overload)
	{
		for (String variable : overload.keySet())
		{
			String value = overload.get(variable);
			if (value.equalsIgnoreCase("PATH"))
			{
				String trueKey = 
					findWindowsVariable(processEnvironment, "PATH");
				if (trueKey == null)
					processEnvironment.put(variable, value);
				else
					processEnvironment.put(trueKey,
						mergePaths(processEnvironment.get(trueKey),	value));
			} else
				processEnvironment.put(variable, value);
		}
	}
	
	static private String mergePaths(String original, String newValue)
	{
		if (original == null || original.length() == 0)
			return newValue;
		if (newValue == null || newValue.length() == 0)
			return original;
		
		return original + File.pathSeparator + newValue;
	}
	
	static private String findWindowsVariable(Map<String, String> env,
		String searchKey)
	{
		for (String trueKey : env.keySet())
		{
			if (searchKey.equalsIgnoreCase(trueKey))
				return trueKey;
		}
		
		return null;
	}
	
	static private void resetCommand(ProcessBuilder builder)
	{
		List<String> commandLine = builder.command();
		ArrayList<String> newCommandLine = new ArrayList<String>(
			commandLine.size());
		
		newCommandLine.add(findCommand(commandLine.get(0),
			builder.environment()));
		newCommandLine.addAll(commandLine.subList(1, commandLine.size()));
		
		builder.command(newCommandLine);
	}
	
	static private String findCommand(String command, Map<String, String> env)
	{
		String path;
		
		if (command.contains(File.separator))
			return command;
		
		String osName = System.getProperty("os.name");
		if (osName.contains("Windows"))
		{
			String key = findWindowsVariable(env, "PATH");
			if (key == null)
				path = "";
			path = env.get(key);
		} else if (osName.contains("Linux"))
		{
			path = env.get("PATH");
		} else
		{
			throw new RuntimeException("Don't know how to handle \"" +
				osName + "\" platform.");
		}
		
		if (path == null)
			path = "";
		
		String []elements = path.split(File.pathSeparator);
		for (String element : elements)
		{
			File f = new File(element, command);
			if (f.exists())
				return f.getAbsolutePath();
		}
		
		_logger.warn("Found a command without a path, " +
			"but couldn't find the command in the path.");
		return command;
	}
}