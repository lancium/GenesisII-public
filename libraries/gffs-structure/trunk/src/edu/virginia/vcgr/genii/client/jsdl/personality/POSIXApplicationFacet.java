package edu.virginia.vcgr.genii.client.jsdl.personality;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface POSIXApplicationFacet extends PersonalityFacet
{
	public void consumeExecutable(Object currentUnderstanding, String filesystemName, String executable) throws JSDLException;

	public void consumeArgument(Object currentUnderstanding, String filesystemName, String argument) throws JSDLException;

	public void consumeInput(Object currentUnderstanding, String filesystemName, String input) throws JSDLException;

	public void consumeOutput(Object currentUnderstanding, String filesystemName, String output) throws JSDLException;

	public void consumeError(Object currentUnderstanding, String filesystemName, String error) throws JSDLException;

	public void consumeWorkingDirectory(Object currentUnderstanding, String filesystemName, String workingDirectory)
		throws JSDLException;

	public void consumeEnvironment(Object currentUnderstanding, String name, String filesystemName, String environment)
		throws JSDLException;

	public void consumeWallTimeLimit(Object currentUnderstanding, long wallTimeLimit) throws JSDLException;

	public void consumeFileSizeLimit(Object currentUnderstanding, long fileSizeLimit) throws JSDLException;

	public void consumeCoreDumpLimit(Object currentUnderstanding, long coreDumpLimit) throws JSDLException;

	public void consumeDataSegmentLimit(Object currentUnderstanding, long dataSegmentLimit) throws JSDLException;

	public void consumeLockedMemoryLimit(Object currentUnderstanding, long lockedMemoryLimit) throws JSDLException;

	public void consumeMemoryLimit(Object currentUnderstanding, long memoryLimit) throws JSDLException;

	public void consumeOpenDescriptorsLimit(Object currentUnderstanding, long openDescriptorsLimit) throws JSDLException;

	public void consumePipeSizeLimit(Object currentUnderstanding, long pipeSizeLimit) throws JSDLException;

	public void consumeStackSizeLimit(Object currentUnderstanding, long stackSizeLimit) throws JSDLException;

	public void consumeCPUTimeLimit(Object currentUnderstanding, long cpuTimeLimit) throws JSDLException;

	public void consumeProcessCountLimit(Object currentUnderstanding, long processCountLimit) throws JSDLException;

	public void consumeVirtualMemoryLimit(Object currentUnderstanding, long virtualMemoryLimit) throws JSDLException;

	public void consumeThreadCountLimit(Object currentUnderstanding, long threadCountLimit) throws JSDLException;

	public void consumeUserName(Object currentUnderstanding, String userName) throws JSDLException;

	public void consumeGroupName(Object currentUnderstanding, String groupName) throws JSDLException;
}
