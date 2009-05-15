package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.bes.ActivityState;

abstract class AbstractRunProcessPhase extends AbstractExecutionPhase
{
	static final long serialVersionUID = 0L;
	
	public AbstractRunProcessPhase(ActivityState phaseState)
	{
		super(phaseState);
	}
	
	static protected void overloadEnvironment(Map<String, String> processEnvironment,
		Map<String, String> overload)
	{
		if (overload == null || overload.size() == 0)
			return;
		
		OperatingSystemType os = OperatingSystemType.getCurrent();
		
		if (os.isWindows())
			overloadWindowsEnvironment(processEnvironment, overload);
		else
			overloadLinuxEnvironment(processEnvironment, overload);
	}
	
	static private void overloadLinuxEnvironment(
		Map<String, String> processEnvironment,
		Map<String, String> overload)
	{
		for (String variable : overload.keySet())
		{
			String value = overload.get(variable);
			if (variable.equals("PATH") || variable.equals("LD_LIBRARY_PATH"))
				processEnvironment.put(variable,
					mergePaths(processEnvironment.get(variable), value));
			else
				processEnvironment.put(variable, value);
		}
	}
	
	static private void overloadWindowsEnvironment(
		Map<String, String> processEnvironment,
		Map<String, String> overload)
	{
		for (String variable : overload.keySet())
		{
			String value = overload.get(variable);
			if (variable.equalsIgnoreCase("PATH"))
			{
				String trueKey =
					findWindowsVariable(processEnvironment, "PATH");
				if (trueKey == null)
					processEnvironment.put(variable, value);
				else
					processEnvironment.put(trueKey,
						mergePaths(processEnvironment.get(trueKey), value));
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
	
	static protected void resetCommand(ProcessBuilder builder)
	{
		List<String> commandLine = builder.command();
		ArrayList<String> newCommandLine = new ArrayList<String>(commandLine.size());
		
		String command = findCommand(commandLine.get(0), builder.environment());
		if (command == null)
		{
			File f = new File(builder.directory(), commandLine.get(0));
			if (f.exists())
				command = f.getAbsolutePath();
			else
				command = commandLine.get(0);
		}
		
		newCommandLine.add(command);
		newCommandLine.addAll(commandLine.subList(1, commandLine.size()));
		
		builder.command(newCommandLine);
	}
	
	static private String findCommand(String command, Map<String, String> env)
	{
		String path;
		
		if (command.contains(File.separator))
			return command;
		
		if (OperatingSystemType.getCurrent().isWindows())
		{
			String key = findWindowsVariable(env, "PATH");
			path = env.get(key);
		} else
		{
			path = env.get("PATH");
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
		
		return null;
	}
	
	static protected String fileToPath(File file)
	{
		if (file == null)
			return null;
		
		return file.getAbsolutePath();
	}
}