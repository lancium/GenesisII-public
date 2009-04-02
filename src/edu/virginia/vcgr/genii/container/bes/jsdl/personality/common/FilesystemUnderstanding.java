package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.io.File;
import java.io.IOException;

import org.ggf.jsdl.FileSystemTypeEnumeration;
import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.client.jsdl.GridFileSystem;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLFileSystem;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.scratchmgr.ScratchFSManagerContainerService;

public class FilesystemUnderstanding
{
	private FileSystemTypeEnumeration _fsType = null;
	private String _fsName = null;
	private String _fsSource = null;
	
	public void setFileSystemType(FileSystemTypeEnumeration fsType)
	{
		_fsType = fsType;
	}
	
	public void setFileSystemName(String fsName)
	{
		_fsName = fsName;
	}
	
	public String getFileSystemName()
	{
		return _fsName;
	}
	
	public void setFileSystemSource(String fsSource)
	{
		_fsSource = fsSource;
	}
	
	public boolean isScratchFileSystem()
	{
		return (
				(_fsType == null) || 
				(_fsType == FileSystemTypeEnumeration.spool)) 
			&& 
				((_fsName != null) && (_fsName.equals("SCRATCH")));
	}
	
	public boolean isGridFileSystem()
	{
		return (
				(_fsType == null) ||
				(_fsType == FileSystemTypeEnumeration.normal))
			&&
			(
				(_fsSource != null) &&
				(_fsSource.startsWith("rns:")));
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
	
	public JSDLFileSystem createGridFilesystem(File mountPoint)
		throws JSDLException
	{
		if (!isGridFileSystem())
			throw new JSDLException(String.format(
				"Don't know how to handle file system \"%s\".",
				_fsName));
		
		return new GridFileSystem(mountPoint,
			getSandboxFromSource(_fsSource));
	}
	
	static private String getSandboxFromSource(String source)
	{
		return source.substring(4);
	}
}