package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

public interface ISByteIOResource extends IRByteIOResource
{
	static public final String POSITION_PROPERTY =
		"edu.virginia.vcgr.genii.byteio.streamable.position";
	
	public File chooseFile(HashMap<QName, Object> creationProperties)
		throws ResourceException;
	public File getCurrentFile() throws ResourceException;
	public void destroy() throws ResourceException;
	public void setCreateTime(Calendar tm) throws ResourceException;
	public Calendar getCreateTime()	throws ResourceException;
	public void setModTime(Calendar tm)	throws ResourceException;
	public Calendar getModTime() throws ResourceException;
	public void setAccessTime(Calendar tm) throws ResourceException;
	public Calendar getAccessTime()	throws ResourceException;
}