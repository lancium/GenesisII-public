package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import javax.xml.namespace.QName;

import org.ggf.jsdl.CreationFlagEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.def.DefaultDataStagingFacet;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.UsernamePasswordIdentity;

public class SEDataStagingFacet extends DefaultDataStagingFacet
{
	@Override
	public Object createFacetUnderstanding(Object parentUnderstanding)
			throws JSDLException
	{
		return new DataStagingUnderstanding();
	}

	@Override
	public void consumeCreationFlag(Object currentUnderstanding,
		CreationFlagEnumeration creationFlag) throws JSDLException
	{
		if (creationFlag.equals(CreationFlagEnumeration.append))
			throw new UnsupportedJSDLElement(
				"Creation flag \"append\" is not supported.", 
				new QName(JSDLConstants.JSDL_NS, "CreationFlag"));
				
		((DataStagingUnderstanding)currentUnderstanding).setCreationFlag(
			creationFlag);
	}

	@Override
	public void consumeDeleteOnTerminateFlag(Object currentUnderstanding,
			boolean deleteOnTerminate) throws JSDLException
	{
		((DataStagingUnderstanding)currentUnderstanding).setDeleteOnTerminate(
			deleteOnTerminate);
	}

	@Override
	public void consumeFileName(Object currentUnderstanding, String fileName)
			throws JSDLException
	{
		((DataStagingUnderstanding)currentUnderstanding).setFilename(fileName);
	}

	@Override
	public void consumeCredential(Object currentUnderstanding,
			UsernamePasswordIdentity upi) throws JSDLException
	{
		((DataStagingUnderstanding)currentUnderstanding).setCredential(upi);
	}	
	
	@Override
	public void completeFacet(Object parentUnderstanding,
			Object currentUnderstanding) throws JSDLException
	{
		SimpleExecutionUnderstanding parent = 
			(SimpleExecutionUnderstanding)parentUnderstanding;
		DataStagingUnderstanding child = 
			(DataStagingUnderstanding)currentUnderstanding;
		
		parent.addDataStaging(child);
	}
}