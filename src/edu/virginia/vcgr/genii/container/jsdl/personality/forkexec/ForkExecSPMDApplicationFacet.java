package edu.virginia.vcgr.genii.container.jsdl.personality.forkexec;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.jsdl.personality.common.CommonPosixLikeSPMDApplicationFacet;

public class ForkExecSPMDApplicationFacet extends CommonPosixLikeSPMDApplicationFacet{
	private FilesystemManager _fsManager;
	private BESWorkingDirectory _workingDirectory;

	public ForkExecSPMDApplicationFacet(FilesystemManager fsManager, BESWorkingDirectory workingDirectory)
	{
		_fsManager = fsManager;
		_workingDirectory = workingDirectory;
	}

	@Override
	public Object createFacetUnderstanding(Object parentUnderstanding) throws JSDLException
	{
		return new ForkExecApplicationUnderstanding(_fsManager, _workingDirectory);
	}
}
