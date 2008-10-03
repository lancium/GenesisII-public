package edu.virginia.vcgr.genii.container.bes.jsdl.personality.forkexec;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.CommonPosixLikePOSIXApplicationFacet;

class ForkExecPOSIXApplicationFacet extends CommonPosixLikePOSIXApplicationFacet
{
	private FilesystemManager _fsManager;
	private BESWorkingDirectory _workingDirectory;
	
	public ForkExecPOSIXApplicationFacet(FilesystemManager fsManager,
		BESWorkingDirectory workingDirectory)
	{
		_fsManager = fsManager;
		_workingDirectory = workingDirectory;
	}
	
	@Override
	public Object createFacetUnderstanding(Object partentUnderstanding)
		throws JSDLException
	{
		return new ForkExecApplicationUnderstanding(
			_fsManager, _workingDirectory);
	}
}