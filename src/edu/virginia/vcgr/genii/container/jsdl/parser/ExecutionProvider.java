package edu.virginia.vcgr.genii.container.jsdl.parser;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.CPUArchitectureFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.DataStagingFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.FileSystemFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.HPCApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.JobIdentificationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.OperatingSystemFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.OperatingSystemTypeFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.POSIXApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.ResourcesFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.SPMDApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.SourceURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.TargetURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultPersonalityProvider;
import edu.virginia.vcgr.genii.container.jsdl.JobRequest;

public class ExecutionProvider extends DefaultPersonalityProvider
{
	@Override
	public Object createNewUnderstanding()
	{
		return new JobRequest();
	}

	@Override
	public DataStagingFacet getDataStagingFacet(Object currentUnderstanding)
	{
		return new ExecutionDataStagingFacet();
	}

	@Override
	public FileSystemFacet getFileSystemFacet(Object currentUnderstanding)
	{
		return new ExecutionFilesystemFacet();
	}

	@Override
	public JobIdentificationFacet getJobIdentificationFacet(
		Object currentUnderstanding)
	{
		return new ExecutionJobIdentificationFacet();
	}

	@Override
	public SourceURIFacet getSourceURIFacet(Object currentUnderstanding)
	{
		return new ExecutionSourceURIFacet();
	}

	@Override
	public TargetURIFacet getTargetURIFacet(Object currentUnderstanding)
	{
		return new ExecutionTargetURIFacet();
	}
	
	@Override
	public ResourcesFacet getResourcesFacet(Object currentUnderstanding)
	{
		return new ExecutionResourcesFacet();
	}
	
	@Override
	public POSIXApplicationFacet getPOSIXApplicationFacet(
		Object currentUnderstanding)
	{
		return new ExecutionPOSIXApplicationFacet();
	}
	
	@Override
	public HPCApplicationFacet getHPCApplicationFacet(
		Object currentUnderstanding)
	{
		return new ExecutionHPCApplicationFacet();
	}
	
	@Override
	public SPMDApplicationFacet getSPMDApplicationFacet(
		Object currentUnderstanding)
	{
		return new ExecutionSPMDApplicationFacet();
	}

	@Override
	public CPUArchitectureFacet getCPUArchitectureFacet(
			Object currentUnderstanding) throws JSDLException
	{
		return new ExecutionCPUArchitectureFacet();
	}

	@Override
	public OperatingSystemFacet getOperatingSystemFacet(
			Object currentUnderstanding) throws JSDLException
	{
		return new ExecutionOperatingSystemFacet();
	}

	@Override
	public OperatingSystemTypeFacet getOperatingSystemTypeFacet(
			Object currentUnderstanding) throws JSDLException
	{
		return new ExecutionOperatingSystemTypeFacet();
	}
}