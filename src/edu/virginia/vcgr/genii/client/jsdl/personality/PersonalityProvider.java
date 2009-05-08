package edu.virginia.vcgr.genii.client.jsdl.personality;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public interface PersonalityProvider
{
	public Object createNewUnderstanding() throws JSDLException;
	
	public JobDefinitionFacet getJobDefinitionFacet(
		Object currentUnderstanding) throws JSDLException;
	public JobDescriptionFacet getJobDescriptionFacet(
		Object currentUnderstanding) throws JSDLException;
	public JobIdentificationFacet getJobIdentificationFacet(
		Object currentUnderstanding) throws JSDLException;
	public ApplicationFacet getApplicationFacet(
		Object currentUnderstanding) throws JSDLException;
	public POSIXApplicationFacet getPOSIXApplicationFacet(
		Object currentUnderstanding) throws JSDLException;
	public HPCApplicationFacet getHPCApplicationFacet(
		Object currentUnderstanding) throws JSDLException;
	public SPMDApplicationFacet getSPMDApplicationFacet(
		Object currentUnderstanding) throws JSDLException;
	public ResourcesFacet getResourcesFacet(
		Object currentUnderstanding) throws JSDLException;
	public CandidateHostsFacet getCandidateHostsFacet(
		Object currentUnderstanding) throws JSDLException;
	public FileSystemFacet getFileSystemFacet(
		Object currentUnderstanding) throws JSDLException;
	public OperatingSystemFacet getOperatingSystemFacet(
		Object currentUnderstanding) throws JSDLException;
	public OperatingSystemTypeFacet getOperatingSystemTypeFacet(
		Object currentUnderstanding) throws JSDLException;
	public CPUArchitectureFacet getCPUArchitectureFacet(
		Object currentUnderstanding) throws JSDLException;
	public GeniiPropertyFacet getGeniiPropertyFacet(
		Object currentUnderstanding) throws JSDLException;
	public GeniiOrFacet getGeniiOrFacet(
		Object currentUnderstanding) throws JSDLException;
	public DataStagingFacet getDataStagingFacet(
		Object currentUnderstanding) throws JSDLException;
	public SourceURIFacet getSourceURIFacet(
		Object currentUnderstanding) throws JSDLException;
	public TargetURIFacet getTargetURIFacet(
		Object currentUnderstanding) throws JSDLException;
}
