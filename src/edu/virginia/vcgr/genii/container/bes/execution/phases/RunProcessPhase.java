package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;

public class RunProcessPhase extends AbstractRunProcessPhase 
	implements TerminateableExecutionPhase, Serializable
{
	static final long serialVersionUID = 0L;
	
	static final private String EXECUTING_STAGE = "executing";
	
	private File _executable;
	private String []_arguments;
	private Map<String, String> _environment;
	transient private Process _process = null;
	private String _processLock = new String();
	transient private Boolean _hardTerminate = null;
	
	private StreamRedirectionDescription _redirects;
	
	public RunProcessPhase(File executable, String []arguments, 
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

	protected void finalize() throws Throwable
	{
		synchronized(_processLock)
		{
			if (_process != null)
				_process.destroy();
		}
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		synchronized(_processLock)
		{
			List<String> command = new Vector<String>();
			command.add(_executable.getAbsolutePath());
			for (String arg : _arguments)
				command.add(arg);
			
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(
				context.getCurrentWorkingDirectory().getWorkingDirectory());
			if (_environment == null)
				_environment = new HashMap<String, String>();
			
			if (_environment != null)
			{
				String ogrshConfig = _environment.get("OGRSH_CONFIG");
				if (ogrshConfig != null)
				{
					File f = new File(
						context.getCurrentWorkingDirectory(
							).getWorkingDirectory(), 
						ogrshConfig);
					_environment.put("OGRSH_CONFIG", f.getAbsolutePath());
				}
				
				String geniiUserDir = _environment.get("GENII_USER_DIR");
				if (geniiUserDir != null && !geniiUserDir.startsWith("/"))
				{
					File f = new File(
						context.getCurrentWorkingDirectory(
							).getWorkingDirectory(), 
						geniiUserDir);
					_environment.put("GENII_USER_DIR", f.getAbsolutePath());
				}
				
				overloadEnvironment(builder.environment(), _environment);
			}
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
			synchronized(_processLock)
			{
				_process.destroy();
				_process = null;
			}
		}
		finally
		{
			synchronized(_processLock)
			{
				_process = null;
			}
		}
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
