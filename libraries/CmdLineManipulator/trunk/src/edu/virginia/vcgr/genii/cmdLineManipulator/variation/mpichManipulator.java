package edu.virginia.vcgr.genii.cmdLineManipulator.variation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.cmdLineManipulator.AbstractCmdLineManipulator;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorException;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.MpichVariationConfiguration;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorConstants;

public class mpichManipulator extends
		AbstractCmdLineManipulator<MpichVariationConfiguration> {
	static private Log _logger = LogFactory.getLog(mpichManipulator.class);

	static public final String MANIPULATOR_TYPE = "mpich";

	static public final String MPICH_PROCESSNUM_FLAG = "mpich-processNum-flag";

	public mpichManipulator() {
		super(MANIPULATOR_TYPE, MpichVariationConfiguration.class);
	}

	@Override
	public List<String> transform(Map<String, Object> jobProperties,
			CmdLineManipulatorConfiguration manipulatorConfig,
			String variationName) throws CmdLineManipulatorException {
		_logger.debug("**Transforming with Mpich CmdLine Manipulator");

		validateJob(jobProperties);

		// exit if not SPMD job
		if (confirmSPMDJob(jobProperties)) {
			Map<String, Object> manipProps = new HashMap<String, Object>();

			processManipulatorConfiguration(jobProperties, manipulatorConfig,
					variationName, manipProps);
			tweakCmdLine(jobProperties, manipProps, variationName);
		}
		return formCommandLine(jobProperties);
	}

	/*
	 * Determine if this is a SPMD job based on properties If not, return
	 * without transformation
	 */
	private boolean confirmSPMDJob(Map<String, Object> jobProperties) {
		if (jobSPMDVariation(jobProperties) == null) {
			_logger.debug(" Skipping mpich manipulator: Job is not SPMD.");
			return false;
		}
		return true;
	}

	/*
	 * Transform commandline for MPICH manipulation
	 */
	@Override
	protected void tweakCmdLine(Map<String, Object> jobProps,
			Map<String, Object> manipProps, String varName) {
		_logger.debug("**Forming MPICH Specific CmdLine");

		// list for new job args
		List<String> newArgs = new ArrayList<String>();

		// specify number of processes
		Integer numProcesses = jobProcessNum(jobProps);
		String processNumFlag = manipulatorProcessNumFlag(manipProps);
		if ((processNumFlag != null) && (numProcesses != null))
			newArgs.add(String.format("%s %d", processNumFlag, numProcesses));

		// add additional args from construction param
		List<String> manipAdditionalArgs = manipulatorArgs(manipProps);
		if (manipAdditionalArgs != null)
			for (String arg : manipAdditionalArgs)
				newArgs.add(arg);

		// add job executable to args
		newArgs.add(jobExectuable(jobProps));

		// add job args
		List<String> jobArguments = jobArguments(jobProps);
		if (jobArguments != null)
			for (String arg : jobArguments)
				newArgs.add(arg);

		// save new cmdline in jobProps
		setJobExecutable(manipulatorExec(manipProps), jobProps, varName);
		setJobArguments(newArgs, jobProps, varName);

	}

	/*
	 * extract construction properties associated with MPICH tweaker (mpich
	 * executable and additional arguments)
	 */
	@Override
	protected void processManipulatorConfiguration(
			Map<String, Object> jobProps,
			CmdLineManipulatorConfiguration manipConfig, String variationName,
			Map<String, Object> manipProps) throws CmdLineManipulatorException {
		super.processManipulatorConfiguration(jobProps, manipConfig,
				variationName, manipProps);

		_logger.debug("**Processing MPICH Specific Manipulator "
				+ "Construction Configuration");

		if (manipulatorExec(manipProps) == null)
			throw new IllegalArgumentException(
					"Null MPICH executable configuration parameter.");

		// get mpich specific configuration
		MpichVariationConfiguration mpichConfiguration = (MpichVariationConfiguration) getVariationConfiguration(
				manipConfig, variationName);

		if (mpichConfiguration != null) {
			// confirm requested spmd variation supported by BES
			boolean matchingSupport = false;
			for (String supportedVariation : mpichConfiguration
					.spmdVariations())
				if (jobSPMDVariation(jobProps).toString().equals(
						supportedVariation))
					matchingSupport = true;

			if (!matchingSupport)
				throw new CmdLineManipulatorException(String.format(
						"Requested SPMD variation not supported "
								+ "by manipulator \"%s\"", variationName));

			// extract processNum flag
			String processNumFlag = mpichConfiguration.processNumFlag();

			if (processNumFlag != null) {
				manipProps.put(MPICH_PROCESSNUM_FLAG, processNumFlag);
				_logger.debug(String.format(
						"\tNumber of processes will be specified on "
								+ "cmdLine with flag \"%s\"", processNumFlag));
			}
		}

	}

	private URI jobSPMDVariation(Map<String, Object> jobProperties) {
		return (URI) jobProperties
				.get(CmdLineManipulatorConstants.SPMD_VARIATION);
	}

	private Integer jobProcessNum(Map<String, Object> jobProperties) {
		return (Integer) jobProperties
				.get(CmdLineManipulatorConstants.NUMBER_OF_PROCESSES);
	}

	protected static String manipulatorProcessNumFlag(
			Map<String, Object> manipProps) {
		return (String) manipProps.get(MPICH_PROCESSNUM_FLAG);
	}
}