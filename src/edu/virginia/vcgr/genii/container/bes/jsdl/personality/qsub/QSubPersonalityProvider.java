package edu.virginia.vcgr.genii.container.bes.jsdl.personality.qsub;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.HPCApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.POSIXApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.SPMDApplicationFacet;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.CommonPersonalityProvider;

public class QSubPersonalityProvider extends CommonPersonalityProvider
{
	private FilesystemManager _fsManager;
	private BESWorkingDirectory _workingDirectory;
	
	public QSubPersonalityProvider(FilesystemManager fsManager,
		BESWorkingDirectory workingDirectory)
	{
		super(fsManager);
		
		_fsManager = fsManager;
		_workingDirectory = workingDirectory;
	}
	
	@Override
	public POSIXApplicationFacet getPOSIXApplicationFacet(
		Object currentUnderstanding) throws JSDLException
	{
		return new QSubPOSIXApplicationFacet(
			_fsManager, _workingDirectory);
	}
	
	@Override
	public HPCApplicationFacet getHPCApplicationFacet(
		Object currentUnderstanding) throws JSDLException
	{
		return new QSubHPCApplicationFacet(
			_fsManager, _workingDirectory);
	}
	
	@Override
	public SPMDApplicationFacet getSPMDApplicationFacet(
		Object currentUnderstanding) throws JSDLException
	{
		return new QSubSPMDApplicationFacet(
			_fsManager, _workingDirectory);
	}
}