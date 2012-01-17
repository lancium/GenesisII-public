package edu.virginia.vcgr.genii.client.jsdl.personality;

import org.ggf.jsdl.CreationFlagEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public interface DataStagingFacet extends PersonalityFacet
{
	public void consumeName(Object currentUnderstanding, 
		String name) throws JSDLException;
	public void consumeFileName(Object currentUnderstanding,
		String fileName) throws JSDLException;
	public void consumeFileSystemName(
		Object currentUnderstanding, String fileSystemName) 
			throws JSDLException;
	public void consumeCreationFlag(Object currentUnderstanding,
		CreationFlagEnumeration creationFlag) throws JSDLException;
	public void consumeDeleteOnTerminateFlag(
		Object currentUnderstanding,
		boolean deleteOnTerminate) throws JSDLException;
	public void consumeCredential(Object currentUnderstanding,
		UsernamePasswordIdentity upi) throws JSDLException;
}
