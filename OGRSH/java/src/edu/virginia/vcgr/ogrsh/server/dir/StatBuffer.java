package edu.virginia.vcgr.ogrsh.server.dir;

import java.io.IOException;
import java.util.Date;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHReadBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IOGRSHWriteBuffer;
import edu.virginia.vcgr.ogrsh.server.packing.IPackable;
import edu.virginia.vcgr.ogrsh.server.util.StatUtils;
import edu.virginia.vcgr.ogrsh.server.util.TimeUtils;

public class StatBuffer implements IPackable
{
	public static final int S_IFMT = 00170000;
	public static final int S_IFSOCK = 0140000;
	public static final int S_IFLNK = 0120000;
	public static final int S_IFREG = 0100000;
	public static final int S_IFBLK = 0060000;
	public static final int S_IFDIR = 0040000;
	public static final int S_IFCHR = 0020000;
	public static final int S_IFIFO = 0010000;
	public static final int S_ISUID = 0004000;
	public static final int S_ISGID = 0002000;
	public static final int S_ISVTX = 0001000;

	public static final int S_IRWXU = 00700;
	public static final int S_IRUSR = 00400;
	public static final int S_IWUSR = 00200;
	public static final int S_IXUSR = 00100;

	public static final int S_IRWXG = 00070;
	public static final int S_IRGRP = 00040;
	public static final int S_IWGRP = 00020;
	public static final int S_IXGRP = 00010;

	public static final int S_IRWXO = 00007;
	public static final int S_IROTH = 00004;
	public static final int S_IWOTH = 00002;
	public static final int S_IXOTH = 00001;
	
	private int _st_mode;
	private long _st_size;
	private int _st_blocksize;
	private long _st_atime;
	private long _st_mtime;
	private long _st_ctime;
	private long _st_ino;

	private StatBuffer(long st_ino, int mode, long size, int blocksize, 
		long atime, long mtime, long ctime)
	{
		_st_ino = st_ino;
		_st_mode = mode;
		_st_blocksize = blocksize;
		_st_size = size;
		_st_atime = atime;
		_st_mtime = mtime;
		_st_ctime = ctime;
	}

	public StatBuffer(IOGRSHReadBuffer buffer) throws IOException
	{
		unpack(buffer);
	}
	
	public void pack(IOGRSHWriteBuffer buffer) throws IOException
	{
		buffer.writeObject(_st_ino);
		buffer.writeObject(_st_mode);
		buffer.writeObject(_st_size);
		buffer.writeObject(_st_blocksize);
		buffer.writeObject(_st_atime);
		buffer.writeObject(_st_mtime);
		buffer.writeObject(_st_ctime);
	}

	public void unpack(IOGRSHReadBuffer buffer) throws IOException
	{
		_st_ino = (Long)buffer.readObject();
		_st_mode = (Integer)buffer.readObject();
		_st_size = (Long)buffer.readObject();
		_st_blocksize = (Integer)buffer.readObject();
		_st_atime = (Long)buffer.readObject();
		_st_mtime = (Long)buffer.readObject();
		_st_ctime = (Long)buffer.readObject();
	}
	
	static public StatBuffer fromTypeInformation(TypeInformation ti)
	{
		long st_ino = StatUtils.generateInodeNumber(ti.getEndpoint());
		if (ti.isRNS())
		{
			return new StatBuffer(st_ino,
				StatBuffer.S_IFDIR | StatBuffer.S_IRUSR | StatBuffer.S_IXUSR |
				StatBuffer.S_IRGRP | StatBuffer.S_IXGRP | StatBuffer.S_IROTH |
				StatBuffer.S_IXOTH,
				0, 1024 * 4, 
				TimeUtils.getSeconds(new Date()), 
				TimeUtils.getSeconds(new Date()),
				TimeUtils.getSeconds(new Date()));
		} else if (ti.isByteIO())
		{
			return new StatBuffer(st_ino,
				StatBuffer.S_IFREG | StatBuffer.S_IRUSR | StatBuffer.S_IWUSR |
				StatBuffer.S_IRGRP | StatBuffer.S_IROTH,
				ti.getByteIOSize(), 1024 * 512, 
				TimeUtils.getSeconds(new Date()), 
				TimeUtils.getSeconds(new Date()),
				TimeUtils.getSeconds(new Date()));
		} else if (ti.isTool())
		{
			return new StatBuffer(st_ino,
				StatBuffer.S_IFREG,
				0, 1024 * 4, 
				TimeUtils.getSeconds(new Date()), 
				TimeUtils.getSeconds(new Date()),
				TimeUtils.getSeconds(new Date()));
		} else
		{
			return new StatBuffer(st_ino,
				StatBuffer.S_IFREG, 0, 1024 * 4, 
				TimeUtils.getSeconds(new Date()), 
				TimeUtils.getSeconds(new Date()),
				TimeUtils.getSeconds(new Date()));
		}
	}
}