package edu.virginia.vcgr.genii.client.jsdl.personality;

import org.ggf.jsdl.FileSystemTypeEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.range.RangeExpression;

public interface FileSystemFacet extends PersonalityFacet
{
	public void consumeName(Object currentUnderstanding, 
		String name) throws JSDLException;
	public void consumeDescription(Object currentUnderstanding, 
		String description) throws JSDLException;
	public void consumeMountPoint(Object currentUnderstanding,
		String mountPoint) throws JSDLException;
	public void consumeMountSource(Object currentUnderstanding,
		String mountSource) throws JSDLException;
	public void consumeDiskSpace(Object currentUnderstanding,
		RangeExpression diskSpace) throws JSDLException;
	public void consumeFileSystemType(
		Object currentUnderstanding,
		FileSystemTypeEnumeration fileSystemType) throws JSDLException;
	public void consumeUniqueID(Object currentUnderstanding, String uniqueID)
		throws JSDLException;
}
