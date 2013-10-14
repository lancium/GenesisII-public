package edu.virginia.vcgr.genii.cmdLineManipulator.variation;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import edu.virginia.vcgr.genii.cmdLineManipulator.AbstractCmdLineManipulator;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorConstants;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorException;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.AprunVariationConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class aprunManipulator extends AbstractCmdLineManipulator<AprunVariationConfiguration>
{
	static private Log _logger = LogFactory.getLog(aprunManipulator.class);
	static final public String MANIPULATOR_TYPE = "aprun";
	static final public String DEFAULT_APRUN_EXECUTABLE = "aprun";

	public aprunManipulator()
	{
		super(MANIPULATOR_TYPE, AprunVariationConfiguration.class);
	}

	@Override
	public List<String> transform(Map<String, Object> jobProperties, CmdLineManipulatorConfiguration manipConfig,
		String variationName) throws CmdLineManipulatorException
	{
		_logger.debug("**Transforming with Aprun CmdLine Manipulator");

		validateJob(jobProperties);

		Map<String, Object> manipProps = new HashMap<String, Object>();

		processManipulatorConfiguration(jobProperties, manipConfig, variationName, manipProps);
		tweakCmdLine(jobProperties, manipProps, variationName);
		return formCommandLine(jobProperties);
	}

	/*
	 * Transform commandline for aprun manipulation
	 */
	@Override
	protected void tweakCmdLine(Map<String, Object> jobProperties, Map<String, Object> manipProps, String varName)
	{
		_logger.debug("**Forming Aprun Specific CmdLine");

		// update executable with aprun call
		String newExecutable;
		String manipExec = manipulatorExec(manipProps);
		if (manipExec == null)
			newExecutable = DEFAULT_APRUN_EXECUTABLE;
		else
			newExecutable = manipExec;

		// list for new args
		List<String> newArgs = new ArrayList<String>();

		// add aprun specific args
		Integer numProcessingElements = numProcessingElements(jobProperties);
		if (numProcessingElements == null) {
			_logger.debug("\tNull number of processing elements: assuming 1");
			numProcessingElements = 1;
		}
		newArgs.add(String.format("-n %d", numProcessingElements));

		Integer numProcessingElementsPerNode = numProcessingElementsPerNode(jobProperties);
		if (numProcessingElementsPerNode == null) {
			_logger.debug("\tNull processing elements/node: assuming 1");
			numProcessingElementsPerNode = 1;
		}
		newArgs.add(String.format("-N %d", numProcessingElementsPerNode));

		// add additional args from construction params
		List<String> manipulatorAdditionalArgs = manipulatorArgs(manipProps);
		if (manipulatorAdditionalArgs != null)
			for (String arg : manipulatorAdditionalArgs)
				newArgs.add(arg);

		// add job executable
		newArgs.add(jobExectuable(jobProperties));

		// add job args
		List<String> jobArguments = jobArguments(jobProperties);
		if (jobArguments != null)
			for (String arg : jobArguments)
				newArgs.add(arg);

		// save new cmdline in jobProps
		setJobExecutable(newExecutable, jobProperties, varName);
		setJobArguments(newArgs, jobProperties, varName);
	}

	// Job properties specific to aprun cmdline manipulation
	private Integer numProcessingElements(Map<String, Object> jobProperties)
	{
		return (Integer) jobProperties.get(CmdLineManipulatorConstants.NUMBER_OF_PROCESSES);
	}

	private Integer numProcessingElementsPerNode(Map<String, Object> jobProperties)
	{
		return (Integer) jobProperties.get(CmdLineManipulatorConstants.NUMBER_OF_PROCESSES_PER_HOST);
	}
}