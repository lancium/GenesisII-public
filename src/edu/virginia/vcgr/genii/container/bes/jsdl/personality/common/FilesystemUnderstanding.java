package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.io.IOException;

import org.ggf.jsdl.FileSystemTypeEnumeration;
import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLFileSystem;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.scratchmgr.ScratchFSManagerContainerService;

public class FilesystemUnderstanding
{
	private FileSystemTypeEnumeration _fsType = null;
	private String _fsName = null;
	
	public void setFileSystemType(FileSystemTypeEnumeration fsType)
	{
		_fsType = fsType;
	}
	
	public void setFileSystemName(String fsName)
	{
		_fsName = fsName;
	}
	
	public boolean isScratchFileSystem()
	{
		return (
				(_fsType == null) || 
				(_fsType == FileSystemTypeEnumeration.spool)) 
			&& 
				((_fsName != null) && (_fsName.equals("SCRATCH")));
	}
	
	public JSDLFileSystem createScratchFilesystem(String jobAnnotation)
		throws JSDLException
	{
		if (!isScratchFileSystem())
			throw new JSDLException(String.format(
				"Don't know how to handle file system \"%s\".",
				_fsName));
		
		if (jobAnnotation == null)
			throw new JSDLException(
				"Cannot create SCRATCH file system without a job annotation.");
		
		try
		{
			ScratchFSManagerContainerService service =
				(ScratchFSManagerContainerService)ContainerServices.findService(
					ScratchFSManagerContainerService.SERVICE_NAME);
			return service.reserveSwapFilesystem(GUID.fromRandomBytes(
				jobAnnotation.getBytes()).toString());
		}
		catch (IOException ioe)
		{
			throw new JSDLException("Unable to create scratch space for job.", 
				ioe);
		}
	}
}