package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.envvarexp.EnvironmentExport;
import edu.virginia.vcgr.genii.client.pwrapper.ResourceUsageDirectory;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;

abstract class AbstractRunProcessPhase extends AbstractExecutionPhase
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(AbstractRunProcessPhase.class);
	static final private int STDERR_SIZE_CAP = 1024*128;

	protected BESConstructionParameters _constructionParameters;

	protected void preDelay() throws InterruptedException
	{
		if (_constructionParameters != null) {
			Duration preDelay = _constructionParameters.preExecutionDelay();
			if (preDelay != null) {
				Thread.sleep((long) preDelay.as(DurationUnits.Milliseconds));
			}
		}
	}

	protected void postDelay() throws InterruptedException
	{
		if (_constructionParameters != null) {
			Duration postDelay = _constructionParameters.postExecutionDelay();
			if (postDelay != null) {
				Thread.sleep((long) postDelay.as(DurationUnits.Milliseconds));
			}
		}
	}

	final protected void generateProperties(ResourceUsageDirectory dir,String userName, String executable, Double memory, int numProcesses, 
			int numProcessesPerHost, int threadsPerProcess, String jobName) {
		File propFile=new File(dir.getAcctDir(),"properties.json");
		try {
			if (propFile.createNewFile()) {
				// We now have the properties file, lets put the user name in
				BufferedWriter output = null;
				try {           
					output = new BufferedWriter(new FileWriter(propFile));
					output.write("{\n");
					output.write("\"userName\": \"" + userName + "\",\n");
					output.write("\"jobName\": \"" + jobName + "\",\n");
					Date today = Calendar.getInstance().getTime();
					output.write("\"date\": \"" + today.toString() + "\",\n");
					output.write("\"executable\": \"" + executable + "\",\n" ); 
					if (memory==null) memory=new Double(2.0*1024.0*1024.0*1024.0);
					output.write("\"memory\": \"" + String.format("%1$,.0f", memory) + "\",\n" ); 

					output.write("\"numProcesses\": \"" + numProcesses + "\",\n");
					output.write("\"NumProcessorsPerHost\": \"" + numProcessesPerHost + "\",\n");
					output.write("\"threadsPerProcess\": \"" + threadsPerProcess + "\"\n");
					output.write("}\n");
				} catch ( IOException e ) {
					e.printStackTrace();
				} finally {
					if ( output != null ) {
						output.close();
					}
				}
			}

			else {

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	final protected void setExportedEnvironment(Map<String, String> environmentMap)
	{
		try {
			EnvironmentExport exp = EnvironmentExport.besExport(_constructionParameters);
			for (String key : exp.keySet()) {
				try {
					environmentMap.put(key, exp.value(key));
				} catch (Throwable t2) {
					_logger.warn(String.format("Unable to set environment variable %s.", key), t2);
				}
			}
		} catch (Throwable t1) {
			_logger.warn("Unable to override exported environment values.", t1);
		}
	}

	public AbstractRunProcessPhase(ActivityState phaseState, BESConstructionParameters constructionParameters)
	{
		super(phaseState);

		_constructionParameters = constructionParameters;
	}

	static protected Map<String, String> overloadEnvironment(Map<String, String> overload)
	{
		Map<String, String> ret = new HashMap<String, String>();

		if (overload == null || overload.size() == 0)
			return ret;

		if (OperatingSystemType.isWindows())
			overloadWindowsEnvironment(ret, overload);
		else
			overloadLinuxEnvironment(ret, overload);

		return ret;
	}

	static private void overloadLinuxEnvironment(Map<String, String> processEnvironment, Map<String, String> overload)
	{
		for (String variable : overload.keySet()) {
			String value = overload.get(variable);
			if (variable.equals("PATH") || variable.equals("LD_LIBRARY_PATH"))
				processEnvironment.put(variable, mergePaths(processEnvironment.get(variable), value));
			else
				processEnvironment.put(variable, value);
		}
	}

	static private void overloadWindowsEnvironment(Map<String, String> processEnvironment, Map<String, String> overload)
	{
		for (String variable : overload.keySet()) {
			String value = overload.get(variable);
			if (variable.equalsIgnoreCase("PATH")) {
				String trueKey = findWindowsVariable(processEnvironment, "PATH");
				if (trueKey == null)
					processEnvironment.put(variable, value);
				else
					processEnvironment.put(trueKey, mergePaths(processEnvironment.get(trueKey), value));
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

	static private String findWindowsVariable(Map<String, String> env, String searchKey)
	{
		for (String trueKey : env.keySet()) {
			if (searchKey.equalsIgnoreCase(trueKey))
				return trueKey;
		}

		return null;
	}

	static protected List<String> resetCommand(List<String> commandLine, File workingDirectory, Map<String, String> environment)
	{
		ArrayList<String> newCommandLine = new ArrayList<String>(commandLine.size());

		String command = findCommand(commandLine.get(0), environment);
		if (command == null) {
			File f = new File(workingDirectory, commandLine.get(0));
			if (f.exists())
				command = f.getAbsolutePath();
			else
				command = commandLine.get(0);
		}

		newCommandLine.add(command);
		newCommandLine.addAll(commandLine.subList(1, commandLine.size()));

		return newCommandLine;
	}

	static private String findCommand(String command, Map<String, String> env)
	{
		String path;

		if (command.contains(File.separator))
			return command;

		if (OperatingSystemType.isWindows()) {
			String key = findWindowsVariable(env, "PATH");
			path = env.get(key);
		} else {
			path = env.get("PATH");
		}

		if (path == null)
			path = "";

		String[] elements = path.split(File.pathSeparator);
		for (String element : elements) {
			File f = new File(element, command);
			if (f.exists())
				return f.getAbsolutePath();
		}

		return null;
	}

	static protected String fileToPath(File file, File workingDirectory)
	{
		if (file == null) {
			if (workingDirectory == null)
				return null;

			return new File(workingDirectory, String.format("%s.stder", new GUID())).getAbsolutePath();
		}

		return file.getAbsolutePath();
	}

	static protected void appendStandardError(HistoryContext history, File stderr)
	{
		if (stderr != null) {
			if (!stderr.exists()) {
				history.createDebugWriter("No Standard Error").format("Standard error path %s does not exist", stderr).close();
			} else if (stderr.length() == 0) {
				history.createDebugWriter("Standard Error Empty").format("Standard error file %s appears empty.", stderr).close();
			} else {
				if (stderr.length() > STDERR_SIZE_CAP)
					history.createDebugWriter("Standard Error Truncated")
						.format("Standard error file %s is too big -- truncating for log.", stderr).close();

				PrintWriter writer = history.createDebugWriter("Standard Error for Job");
				FileReader reader = null;

				try {
					reader = new FileReader(stderr);
					char[] data = new char[STDERR_SIZE_CAP];
					int toRead = STDERR_SIZE_CAP;
					int read;

					while (toRead > 0 && (read = reader.read(data, 0, toRead)) > 0) {
						writer.write(data, 0, read);
						toRead -= read;
					}
				} catch (IOException ioe) {
					writer.format("\nError reading from standard error:  %s.", ioe);
				} finally {
					StreamUtils.close(reader);
					StreamUtils.close(writer);
				}
			}
		}
	}

	static protected void appendStandardError(HistoryContext history, String stderrPath)
	{
		if (stderrPath != null)
			appendStandardError(history, new File(stderrPath));
	}
}