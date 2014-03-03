package edu.virginia.vcgr.genii.cmdLineManipulator;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CommonVariationConfiguration;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.VariationConfiguration;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorException;

public abstract class AbstractCmdLineManipulator<ConfigType> implements CmdLineManipulator<ConfigType>,
	CmdLineManipulatorConstants
{
	static private Log _logger = LogFactory.getLog(AbstractCmdLineManipulator.class);

	private String _manipulatorType;
	private Class<ConfigType> _variationConfigurationType;

	protected AbstractCmdLineManipulator(String manipulatorType, Class<ConfigType> variationConfigurationType)
	{
		_manipulatorType = manipulatorType;
		_variationConfigurationType = variationConfigurationType;
	}

	@Override
	public String getManipulatorType()
	{
		return _manipulatorType;
	}

	@Override
	public Class<ConfigType> variationConfigurationType()
	{
		return _variationConfigurationType;
	}

	abstract protected void tweakCmdLine(Map<String, Object> jobProperties, Map<String, Object> manipulatorProps,
		String variationName);

	protected void validateJob(Map<String, Object> jobProperties)
	{

		_logger.debug("**Confirming job executable provided");

		if (jobExectuable(jobProperties) == null)
			throw new IllegalArgumentException("Null job executable passed to manipulator.");
	}

	/*
	 * Ensure correct tweaker has been called based on type Extract executable/args if specified in
	 * configuration
	 */
	protected void processManipulatorConfiguration(Map<String, Object> jobProps, CmdLineManipulatorConfiguration manipConfig,
		String varName, Map<String, Object> manipProps) throws CmdLineManipulatorException
	{
		_logger.debug("**Processing General Manipulator Construction Configuration");

		CommonVariationConfiguration commonConfig =
			(CommonVariationConfiguration) getVariationConfiguration(manipConfig, varName);

		if (commonConfig != null) {

			// Extract manipulator executable
			String executable = commonConfig.execCmd();
			if (executable != null) {
				manipProps.put(MANIP_EXEC, executable);
				_logger.debug(String.format("\tManipulator executable from configuration: %s", executable));
			}

			// Extract additional arguments executable
			List<String> additionalArgs = commonConfig.additionalArgs();
			if (additionalArgs != null) {
				manipProps.put(MANIP_ARGS, additionalArgs);
				_logger.debug(String.format("\tManipulator arguments from configuration: %s.", additionalArgs));
			}
		}
	}

	/*
	 * Ensure that the correct tweaker has been called based on its type
	 */
	protected Object getVariationConfiguration(CmdLineManipulatorConfiguration manipConfig, String variationName)
		throws CmdLineManipulatorException
	{
		_logger.debug("**Acquiring Variation Configuration");

		if (manipConfig == null) {
			throw new IllegalArgumentException("Null cmdLine manipulator configuration.");
		}

		Set<VariationConfiguration> variations = manipConfig.variationSet();

		for (VariationConfiguration variation : variations) {

			if (variation.variationName().equals(variationName)) {

				// check manipulator type
				String manipulatorType = variation.variationType();

				if (!manipulatorType.equals(_manipulatorType)) {
					throw new CmdLineManipulatorException(String.format("Loaded manipulator type \"%s\" "
						+ "does not match requested type \"%s\".", manipulatorType, _manipulatorType));
				}

				// extract configuration
				return variation.variationConfigurtaion();
			}
		}

		throw new CmdLineManipulatorException("No tweaker found with name: " + variationName);
	}

	protected static List<String> formCommandLine(Map<String, Object> jobProperties) throws CmdLineManipulatorException
	{
		List<String> commandLine = new ArrayList<String>();

		// get job executable
		String jobExectuable = jobExectuable(jobProperties);

		// add executable to commandLine
		commandLine.add(jobExectuable);

		// get job arguments
		List<String> jobArguments = jobArguments(jobProperties);

		// add args to commandLine
		if (jobArguments != null)
			for (String arg : jobArguments)
				commandLine.add(arg);

		return commandLine;
	}

	protected static String jobExectuable(Map<String, Object> jobProps)
	{
		return (String) jobProps.get(JOB_EXECUTABLE_NAME);
	}

	protected static void setJobExecutable(String executable, Map<String, Object> jobProps, String varName)
	{
		jobProps.put(JOB_EXECUTABLE_NAME, executable);
		_logger.debug(String.format("**%s manipulator transformed exec: %s", varName, executable));
	}

	@SuppressWarnings("unchecked")
	protected static List<String> jobArguments(Map<String, Object> jobProps)
	{
		return (List<String>) jobProps.get(JOB_ARGUMENTS);
	}

	protected static void setJobArguments(List<String> args, Map<String, Object> jobProps, String varName)
	{
		jobProps.put(CmdLineManipulatorConstants.JOB_ARGUMENTS, args);
		_logger.debug(String.format("**%s manipulator transformed args: %s", varName, args));
	}

	protected static String manipulatorExec(Map<String, Object> manipProps)
	{
		return (String) manipProps.get(MANIP_EXEC);
	}

	@SuppressWarnings("unchecked")
	protected static List<String> manipulatorArgs(Map<String, Object> manipProps)
	{
		return (List<String>) manipProps.get(MANIP_ARGS);
	}
}