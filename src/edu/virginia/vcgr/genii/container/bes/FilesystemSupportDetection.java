package edu.virginia.vcgr.genii.container.bes;

import java.util.HashSet;
import java.util.Set;

import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.fuse.FuseFilesystemService;

final public class FilesystemSupportDetection
{
	static final private String SCRATCH_FILESYSTEM = "SCRATCH";
	static final private String FUSE_FILESYSTEM = "GRID";

	static public Set<String> supportedFilesystemTypes()
	{
		Set<String> ret = new HashSet<String>();
		ret.add(SCRATCH_FILESYSTEM);
		if (ContainerServices.hasService(FuseFilesystemService.class))
			ret.add(FUSE_FILESYSTEM);

		return ret;
	}
}