package edu.virginia.vcgr.genii.cmdLineManipulator.variation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.cmdLineManipulator.AbstractCmdLineManipulator;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorException;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.PwrapperVariationConfiguration;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorConstants;

public class pwrapperManipulator extends AbstractCmdLineManipulator<PwrapperVariationConfiguration>
{
	static private Log _logger = LogFactory.getLog(pwrapperManipulator.class);

	static final public String MANIPULATOR_TYPE = "pwrapper";

	public pwrapperManipulator()
	{
		super(MANIPULATOR_TYPE, PwrapperVariationConfiguration.class);
	}

	@Override
	public List<String> transform(Map<String, Object> jobProperties, CmdLineManipulatorConfiguration manipulatorConfiguration,
		String variationName) throws CmdLineManipulatorException
	{
		_logger.debug("**Transforming with PWrapper CmdLine Manipulator");

		validateJob(jobProperties);

		// exit if not enough info for pwrapper
		validatePwrapper(jobProperties);

		Map<String, Object> manipProps = new HashMap<String, Object>();

		processManipulatorConfiguration(jobProperties, manipulatorConfiguration, variationName, manipProps);
		tweakCmdLine(jobProperties, manipProps, variationName);
		return formCommandLine(jobProperties);
	}

	/*
	 * Determine if PWrapper can be called
	 */
	private void validatePwrapper(Map<String, Object> jobProperties)
	{
		_logger.debug("**Confirming PWrapper path is provided");

		if (jobWrapperPath(jobProperties) == null)
			throw new IllegalArgumentException("Null pwrapper path in job properties.");
	}

	/*
	 * Transform commandline for PWrapper
	 */
	@Override
	protected void tweakCmdLine(Map<String, Object> jobProperties, Map<String, Object> manipProps, String varName)
	{
		_logger.debug("**Forming PWrapper Specific CmdLine");

		// determine new executable
		String newExec;
		String manipExec = manipulatorExec(manipProps);
		if (manipExec == null)
			newExec = jobWrapperPath(jobProperties).getAbsolutePath();
		else
			newExec = manipExec;

		// list for new args
		List<String> newArgs = new ArrayList<String>();

		// add additional manipulator args
		List<String> manipAdditionalArgs = manipulatorArgs(manipProps);
		if (manipAdditionalArgs != null)
			for (String arg : manipAdditionalArgs)
				newArgs.add(arg);

		// add fuse mount point to args
		File fuseMountPoint = jobFuseMountDirectory(jobProperties);
		if (fuseMountPoint != null) {
			newArgs.add(String.format("-g%s", fuseMountPoint.getAbsolutePath()));
		}

		// add environment specifics to args
		Map<String, String> environment = jobEnvironment(jobProperties);
		if (environment != null)
			for (Map.Entry<String, String> env : environment.entrySet())
				newArgs.add(String.format("-D%s=%s", env.getKey(), env.getValue()));

		// add working dir path to args
		File workingDirectory = jobWorkingDirectory(jobProperties);
		if (workingDirectory != null)
			newArgs.add(String.format("-d%s", workingDirectory.getAbsolutePath()));

		// add resource usage path to args
		File resourceUsagePath = jobResourceUsagePath(jobProperties);
		if (resourceUsagePath != null)
			newArgs.add(String.format("-U%s", resourceUsagePath.getAbsolutePath()));

		// add redirects to args
		File stdinRedirect = jobStdinRedirect(jobProperties);
		if (stdinRedirect != null)
			newArgs.add(String.format("-i%s", stdinRedirect.getAbsolutePath()));

		File stdoutRedirect = jobStdoutRedirect(jobProperties);
		if (stdoutRedirect != null)
			newArgs.add(String.format("-o%s", stdoutRedirect.getAbsolutePath()));

		File stderrRedirect = jobStderrRedirect(jobProperties);
		if (stderrRedirect != null)
			newArgs.add(String.format("-e%s", stderrRedirect.getAbsolutePath()));

		// add job executable to args
		newArgs.add(jobExectuable(jobProperties));

		// add job args
		List<String> jobArguments = jobArguments(jobProperties);
		if (jobArguments != null)
			for (String arg : jobArguments)
				newArgs.add(arg);

		// update exec and args in job properties
		setJobExecutable(newExec, jobProperties, varName);
		setJobArguments(newArgs, jobProperties, varName);
	}

	// Job properties specific to PWrapper cmdline manipulation
	@SuppressWarnings("unchecked")
	private Map<String, String> jobEnvironment(Map<String, Object> jobProperties)
	{
		return (Map<String, String>) jobProperties.get(CmdLineManipulatorConstants.ENVIRONMENT);
	}

	private File jobFuseMountDirectory(Map<String, Object> jobProperties)
	{
		return (File) jobProperties.get(CmdLineManipulatorConstants.FUSE_MOUNT_POINT);
	}

	private File jobWorkingDirectory(Map<String, Object> jobProperties)
	{
		return (File) jobProperties.get(CmdLineManipulatorConstants.WORKING_DIRECTORY);
	}

	private File jobStdinRedirect(Map<String, Object> jobProperties)
	{
		return (File) jobProperties.get(CmdLineManipulatorConstants.STDIN_REDIRECT);
	}

	private File jobStdoutRedirect(Map<String, Object> jobProperties)
	{
		return (File) jobProperties.get(CmdLineManipulatorConstants.STDOUT_REDIRECT);
	}

	private File jobStderrRedirect(Map<String, Object> jobProperties)
	{
		return (File) jobProperties.get(CmdLineManipulatorConstants.STDERR_REDIRECT);
	}

	private File jobResourceUsagePath(Map<String, Object> jobProperties)
	{
		return (File) jobProperties.get(CmdLineManipulatorConstants.RESOURCE_USAGE);
	}

	private File jobWrapperPath(Map<String, Object> jobProperties)
	{
		return (File) jobProperties.get(CmdLineManipulatorConstants.WRAPPER_PATH);
	}

}