package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import org.ggf.jsdl.FileSystemTypeEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultFileSystemFacet;

public class CommonFilesystemFacet extends DefaultFileSystemFacet
{
	@Override
	public Object createFacetUnderstanding(Object parentUnderstanding)
			throws JSDLException
	{
		return new FilesystemUnderstanding();
	}
	
	@Override
	public void consumeDescription(Object currentUnderstanding,
			String description) throws JSDLException
	{
		// No need to do anything with this...just let it go.
	}

	@Override
	public void consumeFileSystemType(Object currentUnderstanding,
			FileSystemTypeEnumeration fileSystemType) throws JSDLException
	{
		((FilesystemUnderstanding)currentUnderstanding).setFileSystemType(
			fileSystemType);
	}

	@Override
	public void consumeName(Object currentUnderstanding, String name)
			throws JSDLException
	{
		((FilesystemUnderstanding)currentUnderstanding).setFileSystemName(
			name);
	}

	@Override
	public void consumeMountSource(Object currentUnderstanding,
			String mountSource) throws JSDLException
	{
		((FilesystemUnderstanding)currentUnderstanding).setFileSystemSource(
			mountSource);
	}

	@Override
	public void completeFacet(Object parentUnderstanding,
			Object currentUnderstanding) throws JSDLException
	{
		((CommonExecutionUnderstanding)parentUnderstanding).addFilesystem(
			(FilesystemUnderstanding)currentUnderstanding);
	}
}