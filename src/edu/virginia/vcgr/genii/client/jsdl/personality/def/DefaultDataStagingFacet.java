package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import javax.xml.namespace.QName;

import org.ggf.jsdl.CreationFlagEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.DataStagingFacet;

public class DefaultDataStagingFacet extends DefaultPersonalityFacet implements
		DataStagingFacet
{
	@Override
	public void consumeCreationFlag(Object currentUnderstanding,
			CreationFlagEnumeration creationFlag) throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLConstants.JSDL_NS, "CreationFlag"));
	}

	@Override
	public void consumeDeleteOnTerminateFlag(
			Object currentUnderstanding, boolean deleteOnTerminate)
			throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLConstants.JSDL_NS, "DeleteOnTerminateFlag"));
	}

	@Override
	public void consumeFileName(Object currentUnderstanding,
			String fileName) throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLConstants.JSDL_NS, "FileName"));
	}

	@Override
	public void consumeFileSystemName(Object currentUnderstanding,
			String fileSystemName) throws JSDLException
	{
		throw new UnsupportedJSDLElement(
			new QName(JSDLConstants.JSDL_NS, "FileSystemName"));
	}

	@Override
	public void consumeName(Object currentUnderstanding, String name)
			throws JSDLException
	{
	}
}
