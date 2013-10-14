package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.ApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.CPUArchitectureFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.CandidateHostsFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.DataStagingFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.FileSystemFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.GeniiOrFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.GeniiPropertyFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.HPCApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.JobDefinitionFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.JobDescriptionFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.JobIdentificationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.OperatingSystemFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.OperatingSystemTypeFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.POSIXApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.PersonalityProvider;
import edu.virginia.vcgr.genii.client.jsdl.personality.ResourcesFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.SPMDApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.SourceURIFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.TargetURIFacet;

public class DefaultPersonalityProvider implements PersonalityProvider
{
	@Override
	public Object createNewUnderstanding() throws JSDLException
	{
		return null;
	}

	@Override
	public ApplicationFacet getApplicationFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultApplicationFacet();
	}

	@Override
	public CPUArchitectureFacet getCPUArchitectureFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultCPUArchitectureFacet();
	}

	@Override
	public CandidateHostsFacet getCandidateHostsFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultCandidateHostsFacet();
	}

	@Override
	public DataStagingFacet getDataStagingFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultDataStagingFacet();
	}

	@Override
	public FileSystemFacet getFileSystemFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultFileSystemFacet();
	}

	@Override
	public HPCApplicationFacet getHPCApplicationFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultHPCApplicationFacet();
	}

	@Override
	public SPMDApplicationFacet getSPMDApplicationFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultSPMDApplicationFacet();
	}

	@Override
	public JobDefinitionFacet getJobDefinitionFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultJobDefinitionFacet();
	}

	@Override
	public JobDescriptionFacet getJobDescriptionFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultJobDescriptionFacet();
	}

	@Override
	public JobIdentificationFacet getJobIdentificationFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultJobIdentificationFacet();
	}

	@Override
	public OperatingSystemFacet getOperatingSystemFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultOperatingSystemFacet();
	}

	@Override
	public OperatingSystemTypeFacet getOperatingSystemTypeFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultOperatingSystemTypeFacet();
	}

	@Override
	public POSIXApplicationFacet getPOSIXApplicationFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultPOSIXApplicationFacet();
	}

	@Override
	public ResourcesFacet getResourcesFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultResourcesFacet();
	}

	@Override
	public SourceURIFacet getSourceURIFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultSourceURIFacet();
	}

	@Override
	public TargetURIFacet getTargetURIFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultTargetURIFacet();
	}

	@Override
	public GeniiOrFacet getGeniiOrFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultGeniiOrFacet();
	}

	@Override
	public GeniiPropertyFacet getGeniiPropertyFacet(Object currentUnderstanding) throws JSDLException
	{
		return new DefaultGeniiPropertyFacet();
	}
}
