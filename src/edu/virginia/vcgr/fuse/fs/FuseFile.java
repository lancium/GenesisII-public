package edu.virginia.vcgr.fuse.fs;

import java.io.Closeable;
import java.nio.ByteBuffer;

import fuse.FuseException;

public interface FuseFile extends Closeable
{
	public void read(long offset, ByteBuffer buffer) throws FuseException;
	public void write(long offset, ByteBuffer buffer) throws FuseException;
	
	public void release() throws FuseException;
	public void flush() throws FuseException;
}