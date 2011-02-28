package edu.virginia.vcgr.genii.client.cmdLineManipulator;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulator;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulators;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorConstants;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorException;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.VariationConfiguration;
import edu.virginia.vcgr.genii.cmdLineManipulator.variation.pwrapperManipulator;

public class CmdLineManipulatorUtils
{
	static private Log _logger = LogFactory.getLog(CmdLineManipulatorUtils.class);
	
	static public void addBasicJobProperties(Map<String, Object> jobProperties, 
			String executable, Collection<String> arguments)
	{		
		jobProperties.put(CmdLineManipulatorConstants.JOB_EXECUTABLE_NAME,
				executable);
		jobProperties.put(CmdLineManipulatorConstants.JOB_ARGUMENTS,
				arguments);
		return;
	}
		
	static public void addSPMDJobProperties(Map<String, Object> jobProperties, 
			URI variation, Integer numProcesses, Integer numProcessesPeHost)
	{		
		if (variation != null){
			jobProperties.put(CmdLineManipulatorConstants.SPMD_VARIATION, variation);
			jobProperties.put(CmdLineManipulatorConstants.NUMBER_OF_PROCESSES,
					numProcesses);
			jobProperties.put(CmdLineManipulatorConstants.NUMBER_OF_PROCESSES_PER_HOST,
					numProcessesPeHost);
		}
	}
		
	static public void addEnvProperties(Map<String, Object> jobProperties,
			Map<String, String> environment, File workingDirectory, 
			File stdinRedirect, File stdoutRedirect, File stderrRedirect, 
			File resourceUsagePath,	File pathToWrapper)
	{
			
		jobProperties.put(CmdLineManipulatorConstants.ENVIRONMENT,
				environment);
		jobProperties.put(CmdLineManipulatorConstants.WORKING_DIRECTORY,
				workingDirectory);
		jobProperties.put(CmdLineManipulatorConstants.STDIN_REDIRECT,
				stdinRedirect);
		jobProperties.put(CmdLineManipulatorConstants.STDOUT_REDIRECT,
				stdoutRedirect);
		jobProperties.put(CmdLineManipulatorConstants.STDERR_REDIRECT,
				stderrRedirect);
		jobProperties.put(CmdLineManipulatorConstants.RESOURCE_USAGE,
				resourceUsagePath);
		jobProperties.put(CmdLineManipulatorConstants.WRAPPER_PATH,
				pathToWrapper);
		return;
	}
		
	static public List<String> callCmdLineManipulators(Map<String, Object> jobProperties, 
			CmdLineManipulatorConfiguration cmdLineManipulatorConf)
		throws CmdLineManipulatorException
	{
		//new manip conf should have been created on nativeq connection
		if (cmdLineManipulatorConf == null)
			throw new IllegalArgumentException(
					"Null cmdLine manipulator construction params.");
		
		List<String> cmdLine = new ArrayList<String>();
		
		//get set of manipulator variations defined in construction file
		Set<VariationConfiguration> manipulatorVariations = 
			cmdLineManipulatorConf.variationSet();
		
		//new var set should have been initialized on creation
		if (manipulatorVariations == null)
			throw new IllegalArgumentException(
					"Null cmdLine manipulator variations in construction params.");
		
		//determine if SPMD job
		boolean spmdJob = false;
		if (jobProperties.get(CmdLineManipulatorConstants.SPMD_VARIATION) != null)
			spmdJob = true;		
		
		//get set of manipulators to be called as specified in construction file
		List<String> callChain = cmdLineManipulatorConf.callChain();
		
		//new call chain should have been initialized on creation
		if (callChain == null)
			throw new IllegalArgumentException(
					"Null manipulator call chain in construction params.");
		
		//keep track if mpi manipulator called
		boolean mpiManipulatorCalled = false;
		
		//for each manipulator in set
		for (String manipulatorName : callChain){
			
			boolean foundManipulator = false; 
			int manipulatorCount = 0;
			
			//get manipulator in construction parameters that matches by name
			for (VariationConfiguration manipulatorVariation : manipulatorVariations){
				
				if (manipulatorVariation.variationName().equals(manipulatorName)){
					
					String manipulatorType = manipulatorVariation.variationType();
					
					if (manipulatorType.equals("mpich"))
						mpiManipulatorCalled = true;
					
					//load manipulator of that type
					CmdLineManipulator<?> manipulator = 
						CmdLineManipulators.getCmdLineManipulator(manipulatorType);
					
					_logger.debug(String.format(
							"  calling cmdLine manipulator \"%s\"'s transform",
							manipulatorName));
					
					//call manipulator to transform cmdLine
					cmdLine = manipulator.transform(jobProperties, 
							cmdLineManipulatorConf, manipulatorName);	
					_logger.debug(String.format("  new cmdLine:\n %s", 
							cmdLine.toString()));
				
					foundManipulator = true;
					manipulatorCount++;
					break;
				}
			}
			if (!foundManipulator)
				throw new CmdLineManipulatorException(String.format(
						"Construction params lack a manipulator named \"%s\".", 
						manipulatorName));
			if (manipulatorCount>1)
				throw new IllegalArgumentException("Construction params contain " +
						"mulitple manipulator variations with same name.");
			if (spmdJob && (!mpiManipulatorCalled))
				throw new IllegalArgumentException("No MPI manipulator " +
						"in manipulator call chain: Attempting to run SPMD job " +
						"on BES with no SPMD support.");
			
		}
		
		return cmdLine;
	}
	
	
	static public void addPwrapperManipulator(
			CmdLineManipulatorConfiguration manipulatorConf) 
	{			
		_logger.debug("Adding default pwrapper cmdline manipulator.");
		
		Set<VariationConfiguration> variationConfigSet = 
			manipulatorConf.variationSet();
		
		List<String> callChainConf = 
			manipulatorConf.callChain();
			
		if (variationConfigSet == null)
			variationConfigSet = new HashSet<VariationConfiguration>();
		
		//add pwrapper manipulator
		VariationConfiguration pwrapperVariationConfig = 
			new VariationConfiguration();
		String variationName = String.format("default_pwrapper");
		pwrapperVariationConfig.variationName(variationName);
		pwrapperVariationConfig.variationType(
				pwrapperManipulator.MANIPULATOR_TYPE);
		
		//save new variation and call chain configs
		variationConfigSet.add(pwrapperVariationConfig);
		callChainConf.add(0, variationName);
		//set new configs in passed-in construction params
		manipulatorConf.variationSet(variationConfigSet);
		manipulatorConf.callChain(callChainConf);
	}
	
		
					

}