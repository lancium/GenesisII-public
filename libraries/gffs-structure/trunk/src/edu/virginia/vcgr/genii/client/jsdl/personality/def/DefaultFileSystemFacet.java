package edu.virginia.vcgr.genii.client.jsdl.personality.def;

import javax.xml.namespace.QName;

import org.ggf.jsdl.FileSystemTypeEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLConstants;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.UnsupportedJSDLElement;
import edu.virginia.vcgr.genii.client.jsdl.personality.FileSystemFacet;
import edu.virginia.vcgr.genii.client.jsdl.range.RangeExpression;

public class DefaultFileSystemFacet extends DefaultPersonalityFacet implements FileSystemFacet
{
	@Override
	public void consumeDescription(Object currentUnderstanding, String description) throws JSDLException
	{
	}

	@Override
	public void consumeDiskSpace(Object currentUnderstanding, RangeExpression diskSpace) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "DiskSpace"));
	}

	@Override
	public void consumeFileSystemType(Object currentUnderstanding, FileSystemTypeEnumeration fileSystemType)
		throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "FileSystemType"));
	}

	@Override
	public void consumeMountPoint(Object currentUnderstanding, String mountPoint) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "MountPoint"));
	}

	@Override
	public void consumeMountSource(Object currentUnderstanding, String mountSource) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "MountSource"));
	}

	@Override
	public void consumeName(Object currentUnderstanding, String name) throws JSDLException
	{
		throw new UnsupportedJSDLElement(new QName(JSDLConstants.JSDL_NS, "Name"));
	}

	@Override
	public void consumeUniqueID(Object currentUnderstanding, String uniqueID) throws JSDLException
	{
		// Ignore
	}
}
