package edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles;

import java.io.IOException;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;


public class FileHandle extends AbstractFilesystemHandle
{
	private long _fileHandle;
	
	public FileHandle(GenesisIIFilesystem fs, String []path, long fileHandle)
	{
		super(fs, path);
		
		_fileHandle = fileHandle;
	}
	
	@Override
	public boolean isDirectoryHandle()
	{
		return false;
	}

	@Override
	public void close() throws IOException
	{
		if (_fileHandle < 0)
			return;
		
		try
		{
			_fs.close(_fileHandle);
		}
		catch (FSException fse)
		{
			throw new IOException("Unable to close file handle.", fse);
		}
	}
	
	public byte[] read(long offset, int length) throws FSException
	{
		ByteBuffer target = ByteBuffer.allocate(length);
		_fs.read(_fileHandle, offset, target);
		target.flip();
		byte []dst = new byte[target.remaining()];
		target.get(dst);
		return dst;
	}
	
	public int write(long offset, byte []data) throws FSException
	{
		ByteBuffer source = ByteBuffer.wrap(data);
		_fs.write(_fileHandle, offset, source);
		return source.position();
	}
	
	public int truncAppend(long offset, byte []data) throws FSException
	{
		ByteBuffer source = ByteBuffer.wrap(data);
		_fs.truncate(_path, offset);
		_fs.write(_fileHandle, offset, source);
		return source.position();
	}
}