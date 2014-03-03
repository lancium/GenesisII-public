package edu.virginia.vcgr.genii.cmdLineManipulator;

public interface CmdLineManipulatorConstants
{
	static final public String NAMESPACE = "http://vcgr.cs.virginia.edu/cmdline-manipulators";

	// job properties

	// general job properties
	static public final String FUSE_MOUNT_POINT = "fuse-mount-point";
	static public final String JOB_EXECUTABLE_NAME = "job-executable-name";
	static public final String JOB_ARGUMENTS = "job-arguments";
	static public final String PROCESS_SPECIFICATION_PROPERTY = "specify-processes";

	// spmd job properties
	static public final String SPMD_VARIATION = "spmd-variation";
	static public final String NUMBER_OF_PROCESSES = "number-of-processes";
	static public final String NUMBER_OF_PROCESSES_PER_HOST = "number-of-processes-per-host";

	// environment job properties
	static public final String ENVIRONMENT = "environment";
	static public final String WORKING_DIRECTORY = "working-directory";
	static public final String STDIN_REDIRECT = "stdin-redirect";
	static public final String STDOUT_REDIRECT = "stdout-redirect";
	static public final String STDERR_REDIRECT = "stderr-redirect";
	static public final String RESOURCE_USAGE = "resource-usage";
	static public final String WRAPPER_PATH = "wrapper-path";
	static public final String TARGET_USER = "target-user";

	// general manipulator properties
	static public final String MANIP_EXEC = "manipulator-executable-name";
	static public final String MANIP_ARGS = "manipulator-additional-arguments";
}