package edu.virginia.vcgr.ogrsh.server.file;

import java.io.Closeable;

import edu.virginia.vcgr.ogrsh.server.dir.StatBuffer;
import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;

public interface IFileDescriptor extends Closeable
{
	static public final int SEEK_SET = 0;
	static public final int SEEK_CUR = 1;
	static public final int SEEK_END = 2;
	
	public byte[] read(int length) throws OGRSHException;
	public int write(byte []data) throws OGRSHException;
	
	public long lseek64(long offset, int whence) throws OGRSHException;
	
	public StatBuffer fxstat() throws OGRSHException;
}