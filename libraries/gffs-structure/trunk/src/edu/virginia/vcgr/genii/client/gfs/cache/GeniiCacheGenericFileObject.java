package edu.virginia.vcgr.genii.client.gfs.cache;

import java.nio.ByteBuffer;

import edu.virginia.vcgr.fsii.exceptions.FSException;

/**
 * Generic File Object that can represent any type of file on the GFS
 * 
 * @author cbs6n
 */
public interface GeniiCacheGenericFileObject
{
	public void close() throws FSException;

	public void flush() throws FSException;

	public void read(long offset, ByteBuffer target) throws FSException;

	public void write(long offset, ByteBuffer source) throws FSException;

	public void truncate(long newSize) throws FSException;
}