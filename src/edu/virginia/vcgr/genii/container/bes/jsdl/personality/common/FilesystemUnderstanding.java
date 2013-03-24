package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.io.IOException;

import org.ggf.jsdl.FileSystemTypeEnumeration;
import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLFileSystem;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.fuse.FuseFilesystemService;
import edu.virginia.vcgr.genii.container.cservices.scratchmgr.ScratchFSManagerContainerService;

public class FilesystemUnderstanding
{
	private FileSystemTypeEnumeration _fsType = null;
	private String _fsName = null;
	@SuppressWarnings("unused")
	private String _fsSource = null;
	private String _uniqueID = null;

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

	public void setUniqueID(String uniqueID)
	{
		_uniqueID = uniqueID;
	}

	public boolean isScratchFileSystem()
	{
		return ((_fsType == null) || (_fsType == FileSystemTypeEnumeration.spool))
			&& ((_fsName != null) && (_fsName.equals("SCRATCH")));
	}

	public boolean isGridFileSystem()
	{
		return ((_fsType == null) || (_fsType == FileSystemTypeEnumeration.normal)) &&

		((_fsName != null) && (_fsName.equals("GRID")));
	}

	public JSDLFileSystem createScratchFilesystem(String jobAnnotation) throws JSDLException
	{
		if (!isScratchFileSystem())
			throw new JSDLException(String.format("Don't know how to handle file system \"%s\".", _fsName));

		if (_uniqueID == null && jobAnnotation == null)
			throw new JSDLException("Cannot create SCRATCH file system without a job annotation or unique-id.");

		try {
			String uniqueID = _uniqueID;
			if (uniqueID == null)
				uniqueID = jobAnnotation;

			ScratchFSManagerContainerService service = ContainerServices.findService(ScratchFSManagerContainerService.class);
			return service.reserveSwapFilesystem(GUID.fromRandomBytes(uniqueID.getBytes()).toString());
		} catch (IOException ioe) {
			throw new JSDLException("Unable to create scratch space for job.", ioe);
		}
	}

	public JSDLFileSystem createGridFilesystem() throws JSDLException
	{
		if (!isGridFileSystem())
			throw new JSDLException(String.format("Don't know how to handle file system \"%s\".", _fsName));

		try {
			FuseFilesystemService ffs = ContainerServices.findService(FuseFilesystemService.class);
			return ffs.reserveFuseFilesystem();
		} catch (Exception e) {
			throw new JSDLException("Unable to create fuse mount for job.", e);
		}
	}

	static protected String getSandboxFromSource(String source)
	{
		return source.substring(4);
	}
}