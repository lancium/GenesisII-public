package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.GeniiBESConstants;
import edu.virginia.vcgr.genii.client.nativeq.ApplicationDescription;
import edu.virginia.vcgr.genii.client.nativeq.JobToken;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueue;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueues;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;

public class QueueProcessPhase extends AbstractRunProcessPhase 
	implements TerminateableExecutionPhase
{
	static final long serialVersionUID = 0L;
	
	static private final String JOB_TOKEN_PROPERTY = 
		"edu.virginia.vcgr.genii.container.bes.phases.queue.job-token";
	static private final long DEFAULT_LOOP_CYCLE = 1000L * 10;
	
	private String _phaseShiftLock = new String();
	
	transient private NativeQueueState _state = null;
	transient private BESWorkingDirectory _workingDirectory = null;
	
	private URI _spmdVariation;
	private Integer _numProcesses;
	private File _executable;
	private Collection<String> _arguments;
	private File _stdin;
	private File _stdout;
	private File _stderr;
	private Map<String, String> _environment;
	private Properties _queueProperties;
	
	transient private JobToken _jobToken = null;
	transient private Boolean _terminate = null;
	
	public QueueProcessPhase(URI spmdVariation, Integer numProcesses,
		File executable, Collection<String> arguments, Map<String, String> environment,
		File stdin, File stdout, File stderr, Properties queueProperties)
	{
		super(new ActivityState(
			ActivityStateEnumeration.Running, "Enqueing", false));
	
		_spmdVariation = spmdVariation;
		_numProcesses = numProcesses;
		_executable = executable;
		_arguments = arguments;
		_environment = environment;
		_queueProperties = queueProperties;
		_stdin = stdin;
		_stdout = stdout;
		_stderr = stderr;
	}
	
	@Override
	public void terminate(boolean countAsFailedAttempt)
		throws ExecutionException
	{
		try
		{
			synchronized(_phaseShiftLock)
			{
				if (_workingDirectory == null || _jobToken == null)
				{
					_terminate = Boolean.TRUE;
					return;
				}
				
				NativeQueueConnection queue = connectQueue(
					_workingDirectory.getWorkingDirectory());
				queue.cancel(_jobToken);
				
				_phaseShiftLock.notifyAll();
			}
		}
		catch (NativeQueueException nqe)
		{
			throw new ExecutionException("Unable to cancel job in queue.", 
				nqe);
		}
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		synchronized(_phaseShiftLock)
		{
			if (_terminate != null && _terminate.booleanValue())
				return;
			
			_workingDirectory = context.getCurrentWorkingDirectory();
			NativeQueueConnection queue = connectQueue(
				_workingDirectory.getWorkingDirectory());
			
			_jobToken = (JobToken)context.getProperty(JOB_TOKEN_PROPERTY);
			if (_jobToken == null)
			{
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
				}
				
				_jobToken = queue.submit(new ApplicationDescription(
					_spmdVariation, _numProcesses, _executable.getAbsolutePath(),
					_arguments,
					_environment, fileToPath(_stdin),
					fileToPath(_stdout), fileToPath(_stderr)));
				context.setProperty(JOB_TOKEN_PROPERTY, _jobToken);
			}
			
			while (true)
			{
				_state = queue.getStatus(_jobToken);
				context.updateState(new ActivityState(
					ActivityStateEnumeration.Running, 
					_state.toString(), false));
				if (_state.isFinalState())
					break;
				
				_phaseShiftLock.wait(DEFAULT_LOOP_CYCLE);
			}
			context.setProperty(JOB_TOKEN_PROPERTY, null);
		}
	}
	
	@Override
	public ActivityState getPhaseState()
	{
		synchronized(_phaseShiftLock)
		{
			if (_state == null)
				return super.getPhaseState();
			
			return new ActivityState(ActivityStateEnumeration.Running,
				_state.toString(), false);
		}
	}
	
	private NativeQueueConnection connectQueue(File workingDirectory)
	{
		if (workingDirectory == null)
			throw new IllegalArgumentException("Working directory cannot be null.");
		
		String providerName = _queueProperties.getProperty(
			GeniiBESConstants.NATIVEQ_PROVIDER_PROPERTY);
		
		try
		{
			NativeQueue queue = NativeQueues.getNativeQueue(providerName);
			return queue.connect(workingDirectory, _queueProperties);
		}
		catch (NativeQueueException nqe)
		{
			throw new RuntimeException(
				"Unable to acquire connection to native queue.", nqe);
		}
	}
}
