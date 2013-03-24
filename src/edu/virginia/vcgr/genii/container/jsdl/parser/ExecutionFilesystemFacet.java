package edu.virginia.vcgr.genii.container.jsdl.parser;

import org.ggf.jsdl.FileSystemTypeEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultFileSystemFacet;
import edu.virginia.vcgr.genii.container.jsdl.FilesystemFactory;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;

public class ExecutionFilesystemFacet extends DefaultFileSystemFacet
{
	private String _filesystemName = null;
	private String _mountSource = null;
	private FileSystemTypeEnumeration _type = null;

	@Override
	public Object createFacetUnderstanding(Object parentUnderstanding)
	{
		return this;
	}

	@Override
	public void consumeDescription(Object currentUnderstanding, String description)
	{
		// No need to do anything with this...just drop it.
	}

	@Override
	public void consumeFileSystemType(Object currentUnderstanding, FileSystemTypeEnumeration fileSystemType)
	{
		_type = fileSystemType;
	}

	@Override
	public void consumeName(Object currentUnderstanding, String name)
	{
		_filesystemName = name;
	}

	@Override
	public void consumeMountSource(Object currentUnderstanding, String mountSource)
	{
		_mountSource = mountSource;
	}

	@Override
	public void completeFacet(Object parentUnderstanding, Object currentUnderstanding) throws JSDLException
	{
		JobRequest jr = (JobRequest) parentUnderstanding;
		jr.addFilesystem(FilesystemFactory.getFilesystem(_filesystemName, _type, null, _mountSource));
	}
}