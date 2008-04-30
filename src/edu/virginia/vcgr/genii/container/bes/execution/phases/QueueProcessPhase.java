package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
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

public class QueueProcessPhase extends AbstractRunProcessPhase 
	implements TerminateableExecutionPhase
{
	static final long serialVersionUID = 0L;
	
	static private final String JOB_TOKEN_PROPERTY = 
		"edu.virginia.vcgr.genii.container.bes.phases.queue.job-token";
	static private final long DEFAULT_LOOP_CYCLE = 1000L * 10;
	
	private String _phaseShiftLock = new String();
	
	transient private NativeQueueState _state = null;
	transient private File _workingDirectory = null;
	
	private String _executable;
	private Collection<String> _arguments;
	private String _stdin;
	private String _stdout;
	private String _stderr;
	private Map<String, String> _environment;
	private Properties _queueProperties;
	
	transient private JobToken _jobToken = null;
	transient private Boolean _terminate = null;
	
	public QueueProcessPhase(
		String executable, Collection<String> arguments, Map<String, String> environment,
		String stdin, String stdout, String stderr, Properties queueProperties)
	{
		super(new ActivityState(
			ActivityStateEnumeration.Running, "Enqueing", false));
		
		_executable = executable;
		_arguments = arguments;
		_environment = environment;
		_queueProperties = queueProperties;
		_stdin = stdin;
		_stdout = stdout;
		_stderr = stderr;
	}
	
	@Override
	public void terminate() throws ExecutionException
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
				
				NativeQueueConnection queue = connectQueue(_workingDirectory);
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
			NativeQueueConnection queue = connectQueue(_workingDirectory);
			
			_jobToken = (JobToken)context.getProperty(JOB_TOKEN_PROPERTY);
			if (_jobToken == null)
			{
				_jobToken = queue.submit(new ApplicationDescription(_executable, _arguments,
					_environment, _stdin, _stdout, _stderr));
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
