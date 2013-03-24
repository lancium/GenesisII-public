package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.POSIXApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.posix.JSDLPosixConstants;

public class DefaultPOSIXApplicationFacet extends DefaultPersonalityFacet implements POSIXApplicationFacet
{
	@Override
	public void consumeArgument(Object currentUnderstanding, String filesystemName, String argument) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Argument"));
	}

	@Override
	public void consumeCPUTimeLimit(Object currentUnderstanding, long cpuTimeLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "CPUTimeLimit"));
	}

	@Override
	public void consumeCoreDumpLimit(Object currentUnderstanding, long coreDumpLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "CoreDumpLimit"));
	}

	@Override
	public void consumeDataSegmentLimit(Object currentUnderstanding, long dataSegmentLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "DataSegmentLimit"));
	}

	@Override
	public void consumeEnvironment(Object currentUnderstanding, String name, String filesystemName, String environment)
		throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Environment"));
	}

	@Override
	public void consumeError(Object currentUnderstanding, String filesystemName, String error) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Error"));
	}

	@Override
	public void consumeExecutable(Object currentUnderstanding, String filesystemName, String executable) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Executable"));
	}

	@Override
	public void consumeFileSizeLimit(Object currentUnderstanding, long fileSizeLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "FileSizeLimit"));
	}

	@Override
	public void consumeGroupName(Object currentUnderstanding, String groupName) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "GroupName"));
	}

	@Override
	public void consumeInput(Object currentUnderstanding, String filesystemName, String input) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Input"));
	}

	@Override
	public void consumeLockedMemoryLimit(Object currentUnderstanding, long lockedMemoryLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "LockedMemoryLimit"));
	}

	@Override
	public void consumeMemoryLimit(Object currentUnderstanding, long memoryLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "MemoryLimit"));
	}

	@Override
	public void consumeOpenDescriptorsLimit(Object currentUnderstanding, long openDescriptorsLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "OpenDescriptorsLimit"));
	}

	@Override
	public void consumeOutput(Object currentUnderstanding, String filesystemName, String output) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "Output"));
	}

	@Override
	public void consumePipeSizeLimit(Object currentUnderstanding, long pipeSizeLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "PipeSizeLimit"));
	}

	@Override
	public void consumeProcessCountLimit(Object currentUnderstanding, long processCountLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "ProcessCountLimit"));
	}

	@Override
	public void consumeStackSizeLimit(Object currentUnderstanding, long stackSizeLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "StackSizeLimit"));
	}

	@Override
	public void consumeThreadCountLimit(Object currentUnderstanding, long threadCountLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "ThreadCountLimit"));
	}

	@Override
	public void consumeUserName(Object currentUnderstanding, String userName) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "UserName"));
	}

	@Override
	public void consumeVirtualMemoryLimit(Object currentUnderstanding, long virtualMemoryLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "VirtualMemoryLimit"));
	}

	@Override
	public void consumeWallTimeLimit(Object currentUnderstanding, long wallTimeLimit) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "WallTimeLimit"));
	}

	@Override
	public void consumeWorkingDirectory(Object currentUnderstanding, String filesystemName, String workingDirectory)
		throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLPosixConstants.JSDL_POSIX_NS, "WorkingDirectory"));
	}
}
