package edu.virginia.vcgr.genii.container.jsdl.personality.common;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.CPUArchitectureFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.GPUArchitectureFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.DataStagingFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.FileSystemFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.JobIdentificationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.OperatingSystemFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.OperatingSystemTypeFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.ResourcesFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.SourceURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.TargetURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.CommonCPUArchitectureFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.CommonGPUArchitectureFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.CommonOperatingSystemFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.CommonOperatingSystemTypeFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.CommonSourceURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.CommonTargetURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultPersonalityProvider;

public class CommonPersonalityProvider extends DefaultPersonalityProvider
{
	private FilesystemManager _fsManager;

	public CommonPersonalityProvider(FilesystemManager fsManager)
	{
		_fsManager = fsManager;
	}

	@Override
	public Object createNewUnderstanding() throws JSDLException
	{
		return new CommonExecutionUnderstanding(_fsManager);
	}

	@Override
	public FileSystemFacet getFileSystemFacet(Object currentUnderstanding) throws JSDLException
	{
		return new CommonFilesystemFacet();
	}

	@Override
	public ResourcesFacet getResourcesFacet(Object currentUnderstanding) throws JSDLException
	{
		return new CommonResourcesFacet();
	}

	@Override
	public DataStagingFacet getDataStagingFacet(Object currentUnderstanding) throws JSDLException
	{
		return new CommonDataStagingFacet();
	}

	@Override
	public JobIdentificationFacet getJobIdentificationFacet(Object currentUnderstanding) throws JSDLException
	{
		return new CommonJobIdentificationFacet();
	}

	@Override
	public SourceURIFacet getSourceURIFacet(Object currentUnderstanding) throws JSDLException
	{
		return new CommonSourceURIFacet();
	}

	@Override
	public TargetURIFacet getTargetURIFacet(Object currentUnderstanding) throws JSDLException
	{
		return new CommonTargetURIFacet();
	}

	@Override
	public CPUArchitectureFacet getCPUArchitectureFacet(Object currentUnderstanding) throws JSDLException
	{
		return new CommonCPUArchitectureFacet();
	}

	@Override
        public GPUArchitectureFacet getGPUArchitectureFacet(Object currentUnderstanding) throws JSDLException
        {
                return new CommonGPUArchitectureFacet();
        }

	@Override
	public OperatingSystemFacet getOperatingSystemFacet(Object currentUnderstanding) throws JSDLException
	{
		return new CommonOperatingSystemFacet();
	}

	@Override
	public OperatingSystemTypeFacet getOperatingSystemTypeFacet(Object currentUnderstanding) throws JSDLException
	{
		return new CommonOperatingSystemTypeFacet();
	}
}
