package edu.virginia.vcgr.genii.container.bes.jsdl.personality.qsub;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.CommonPosixLikeHPCApplicationFacet;

class QSubHPCApplicationFacet extends CommonPosixLikeHPCApplicationFacet
{
	private FilesystemManager _fsManager;
	private BESWorkingDirectory _workingDirectory;
	
	public QSubHPCApplicationFacet(FilesystemManager fsManager, 
		BESWorkingDirectory workingDirectory)
	{
		_fsManager = fsManager;
		_workingDirectory = workingDirectory;
	}
	
	@Override
	public Object createFacetUnderstanding(Object partentUnderstanding)
		throws JSDLException
	{
		return new QSubApplicationUnderstanding(
			_fsManager, _workingDirectory);
	}
}