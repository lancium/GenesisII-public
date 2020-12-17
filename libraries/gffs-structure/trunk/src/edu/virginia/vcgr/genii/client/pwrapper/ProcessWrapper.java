package edu.virginia.vcgr.genii.client.pwrapper;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.IOUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.procmgmt.ProcessManager;

public class ProcessWrapper
{
	static private Log _logger = LogFactory.getLog(ProcessWrapper.class);

	static public ExitResults readResults(File file) throws ProcessWrapperException
	{
		try
		{
			FileInputStream fio = new FileInputStream(file);
			String jsonText = IOUtils.toString(fio, "UTF-8");
			ObjectMapper mapper = new ObjectMapper();
			JsonNode obj = mapper.readTree(jsonText).get("location");
			int exitCode = obj.get("exit-code").asInt();
			long userTime = (long)(obj.get("user-time").asDouble() * 1000000L); //need to convert all of these into microseconds
			long systemTime = (long)(obj.get("system-time").asDouble() * 1000000L); //need to convert all of these into microseconds
			long wallclockTime = (long)(obj.get("wallclock-time").asDouble() * 10000000L); //need to convert all of these into microseconds
			long maxrss = obj.get("maximum-rss").asLong();
			String processor = obj.get("processor-id").asText();

			ExitResults ret = new ExitResults(exitCode, userTime, systemTime, wallclockTime, maxrss, processor);
			_logger.debug(ret.toString());
			return ret;
		}
		catch(java.io.FileNotFoundException e)
		{
			_logger.error("Resource file not found: " + e);
			return null;
		}
		catch(java.io.IOException e)
		{
			_logger.error("Failed to parse resource json file with error: " + e);
			return null;
		}
	}

	private Collection<ProcessWrapperListener> _listeners = new LinkedList<ProcessWrapperListener>();

	private ExecutorService _threadPool;
	private File _pathToWrapper;

	public File getPathToWrapper()
	{
		return _pathToWrapper;
	}

	protected void fireProcessCompleted(ProcessWrapperToken token)
	{
		ProcessWrapperListener[] listeners;

		synchronized (_listeners) {
			listeners = _listeners.toArray(new ProcessWrapperListener[_listeners.size()]);
		}

		for (ProcessWrapperListener listener : listeners)
			listener.processCompleted(token);
	}

	ProcessWrapper(ExecutorService threadPool, File pathToWrapper)
	{
		_threadPool = threadPool;
		_pathToWrapper = pathToWrapper;
	}

	final public void addProcessWrapperListener(ProcessWrapperListener listener)
	{
		synchronized (_listeners) {
			_listeners.add(listener);
		}
	}

	final public void removeProcessWrapperListener(ProcessWrapperListener listener)
	{
		synchronized (_listeners) {
			_listeners.remove(listener);
		}
	}

	final public Vector<String> formCommandLine(File fuseMountPoint, Map<String, String> environmentOverload, File workingDirectory,
		File stdinRedirect, File stdoutRedirect, File stderrRedirect, File resourceUsagePath, String executable, String... arguments)
	{
		Vector<String> ret;

		if (executable == null)
			throw new IllegalArgumentException("Executable cannot be null.");

		ret = new Vector<String>(16);
		ret.add(_pathToWrapper.getAbsolutePath());
		if (environmentOverload != null) {
			for (Map.Entry<String, String> env : environmentOverload.entrySet())
				ret.add(String.format("-D%s=%s", env.getKey(), env.getValue()));
		}

		if (workingDirectory != null)
			ret.add(String.format("-d%s", workingDirectory.getAbsolutePath()));

		if (resourceUsagePath != null)
			ret.add(String.format("-U%s", resourceUsagePath.getAbsolutePath()));

		if (fuseMountPoint != null)
			ret.add(String.format("-g%s", fuseMountPoint.getAbsolutePath()));

		if (stdinRedirect != null)
			ret.add(String.format("-i%s", stdinRedirect.getAbsolutePath()));
		if (stdoutRedirect != null)
			ret.add(String.format("-o%s", stdoutRedirect.getAbsolutePath()));
		if (stderrRedirect != null)
			ret.add(String.format("-e%s", stderrRedirect.getAbsolutePath()));

		ret.add(executable);
		for (String arg : arguments)
			ret.add(arg);

		return ret;
	}

	final public ProcessWrapperToken execute(File fuseMountPoint, Map<String, String> environmentOverload, File workingDirectory,
		File stdinRedirect, File resourceUsageFile, List<String> command) throws ProcessWrapperException
	{
		if (!_pathToWrapper.exists())
			throw new ProcessWrapperException(String.format("Cannot find required Process Wrapper binary \"%s\"!", _pathToWrapper));
		if (!_pathToWrapper.isFile())
			throw new ProcessWrapperException(String.format("Process Wrapper binary \"%s\" is not a file!", _pathToWrapper));
		if (!_pathToWrapper.canExecute())
			throw new ProcessWrapperException(String.format("Path to process wrapper binary \"%s\" is not executable!", _pathToWrapper));

		if (workingDirectory != null) {
			if (!workingDirectory.exists())
				throw new ProcessWrapperException(String.format("Working directory %s does not exist!", workingDirectory));
			if (!workingDirectory.isDirectory())
				throw new ProcessWrapperException(String.format("Working directory %s is not a directory!", workingDirectory));
		}

		if (stdinRedirect != null) {
			if (!stdinRedirect.exists())
				throw new ProcessWrapperException(String.format("Stdin source file %s does not exist!", stdinRedirect));
			if (!stdinRedirect.isFile())
				throw new ProcessWrapperException(String.format("Stdin source file %s is not a file!", stdinRedirect));
		}

		ProcessBuilder builder = new ProcessBuilder(new Vector<String>(command));

		ProcessWrapperWorker worker = new ProcessWrapperWorker(resourceUsageFile, builder);
		_threadPool.execute(worker);
		return worker;
	}

	private class ProcessWrapperWorker implements Runnable, ProcessWrapperToken
	{
		private File _resourceUsageFile;
		private ProcessBuilder _builder;
		private Process _process = null;

		private ExitResults _results = null;
		private ProcessWrapperException _exception = null;
		volatile private boolean _done = false;
		private Object _lockObject = new Object();

		private ProcessWrapperWorker(File resourceUsageFile, ProcessBuilder builder)
		{
			_resourceUsageFile = resourceUsageFile;
			_builder = builder;
		}

		@Override
		public void run()
		{
			try {
				synchronized (_lockObject) {
					if (!_done)
						_process = _builder.start();
				}

				_process.waitFor();
				_results = readResults(_resourceUsageFile);
			} catch (ProcessWrapperException pwe) {
				synchronized (_lockObject) {
					_exception = pwe;
				}
			} catch (Throwable cause) {
				synchronized (_lockObject) {
					_exception = new ProcessWrapperException("Unable to execute process!", cause);
				}
			} finally {
				synchronized (_lockObject) {
					_done = true;
					_process = null;
					_lockObject.notifyAll();
				}

				fireProcessCompleted(this);
			}
		}

		@Override
		public void cancel()
		{
			if (_process == null || _process.isAlive() == false)
			{
				_logger.debug("Cancel called on a fork/exec job that is not running. This job was probably terminated before it reached execution.");
				return;
			}

			synchronized (_lockObject) {
				_done = true;
				if (_process != null) {
					try {
						if (OperatingSystemType.isWindows())
							ProcessManager.kill(_process);
					} catch (Throwable cause) {
						_logger.warn("There was a problem killing a windows process.");
					}

					_process.destroy();
				}
			}
		}

		@Override
		public ExitResults results() throws ProcessWrapperException
		{
			synchronized (_lockObject) {
				if (!_done)
					return null;
			}

			if (_exception != null)
				throw _exception;

			if (_results == null)
				throw new ProcessWrapperException("Internal error -- process doesn't have results.");

			return _results;
		}

		@Override
		public void join() throws InterruptedException
		{
			synchronized (_lockObject) {
				while (!_done)
					_lockObject.wait();
			}
		}
	}
}