package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;
import edu.virginia.vcgr.genii.container.sysinfo.SupportedOperatingSystems;

public class RunProcessPhase extends AbstractExecutionPhase 
	implements TerminateableExecutionPhase, Serializable
{
	static final long serialVersionUID = 0L;
	
	static final private String EXECUTING_STAGE = "executing";
	
	private String _executable;
	private String []_arguments;
	private Map<String, String> _environment;
	transient private Process _process = null;
	private String _processLock = new String();
	transient private Boolean _hardTerminate = null;
	
	private StreamRedirectionDescription _redirects;
	
	public RunProcessPhase(String executable, String []arguments, 
		Map<String, String> environment,
		StreamRedirectionDescription redirects)
	{
		super(new ActivityState(ActivityStateEnumeration.Running,
			EXECUTING_STAGE, false));
		
		if (executable == null)
			throw new IllegalArgumentException(
				"Argument \"executable\" cannot be null.");
		if (arguments == null)
			arguments = new String[0];
		
		if (redirects == null)
			redirects = new StreamRedirectionDescription(null, null, null);
		
		_executable = executable;
		_arguments = arguments;
		_environment = environment;
		
		_redirects = redirects;
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		synchronized(_processLock)
		{
			List<String> command = new Vector<String>();
			command.add(_executable);
			for (String arg : _arguments)
				command.add(arg);
			
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(context.getCurrentWorkingDirectory());
			if (_environment != null)
				overloadEnvironment(builder.environment(), _environment);
			resetCommand(builder);
			
			_process = builder.start();
			_redirects.enact(context,
				_process.getOutputStream(),
				_process.getInputStream(),
				_process.getErrorStream());
		}
		
		try
		{
			int eValue = _process.waitFor();
			
			if (_hardTerminate != null && _hardTerminate.booleanValue())
				return;
			
			if (eValue != 0)
				throw new ContinuableExecutionException(
					"Process exited with non-zero value:  " + eValue); 
		}
		catch (InterruptedException ie)
		{
			_process.destroy();
			throw ie;
		}
	}
	
	static private void overloadEnvironment(Map<String, String> processEnvironment,
		Map<String, String> overload)
	{
		if (overload == null || overload.size() == 0)
			return;
		
		SupportedOperatingSystems os = SupportedOperatingSystems.current();
		if (os.equals(SupportedOperatingSystems.WINDOWS))
			overloadWindowsEnvironment(processEnvironment, overload);
		else if (os.equals(SupportedOperatingSystems.LINUX))
			overloadLinuxEnvironment(processEnvironment, overload);
		else
			throw new RuntimeException("Don't know how to handle \"" +
				os + "\" platform.");
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
	
	static private void resetCommand(ProcessBuilder builder)
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
		
		SupportedOperatingSystems os = SupportedOperatingSystems.current();
		if (os.equals(SupportedOperatingSystems.WINDOWS))
		{
			String key = findWindowsVariable(env, "PATH");
			path = env.get(key);
		} else if (os.equals(SupportedOperatingSystems.LINUX))
		{
			path = env.get("PATH");
		} else
			throw new RuntimeException("Dont' know how to handle \"" + os + "\" platform.");
		
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
	
	@Override
	public void terminate() throws ExecutionException
	{
		synchronized(_processLock)
		{
			_hardTerminate = Boolean.TRUE;
			_process.destroy();
		}
	}
}
