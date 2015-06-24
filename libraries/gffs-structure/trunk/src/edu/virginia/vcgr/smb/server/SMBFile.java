package edu.virginia.vcgr.smb.server;

import java.nio.ByteBuffer;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public abstract class SMBFile
{
	private RNSPath path;
	private EndpointReferenceType fileEPR;
	private boolean deletePending;

	public interface IO
	{
		public int read(ByteBuffer read, long off) throws SMBException;

		public void write(ByteBuffer write, long off) throws SMBException;

		public void truncate(long size) throws SMBException;
	}

	public SMBFile(RNSPath path, EndpointReferenceType fileEPR)
	{
		this.path = path;
		this.fileEPR = fileEPR;
		this.deletePending = false;
	}

	public abstract IO getIO();

	public int read(SMBBuffer acc, long off, int len) throws SMBException
	{
		IO io = getIO();

		ByteBuffer read = acc.slice().prepareBuffer();
		read.limit(len);

		int resLen = io.read(read, off);
		acc.skip(resLen);

		return resLen;
	}

	public void write(SMBBuffer out, long off) throws SMBException
	{
		IO io = getIO();

		ByteBuffer write = out.prepareBuffer();
		if (write.remaining() == 0) {
			// A write of zero indicates to extend the file size
			io.truncate(off);
			return;
		}

		io.write(write, off);
	}

	public void truncate(long size) throws SMBException
	{
		IO io = getIO();

		io.truncate(size);
	}

	public void truncate() throws SMBException
	{
		truncate(0);
	}

	public EndpointReferenceType getEPR()
	{
		return fileEPR;
	}

	public RNSPath getPath()
	{
		return path;
	}

	public abstract long getCreateTime();

	public abstract void setCreateTime(long millis);

	public abstract long getWriteTime();

	public abstract void setWriteTime(long millis);

	public abstract long getAccessTime();

	public abstract void setAccessTime(long millis);

	public abstract int getAttr();

	public abstract void setAttr(int fileAttr);

	public abstract int getExtAttr();

	public abstract void setExtAttr(int fileAttr);

	public abstract void setSize(long fileSize);

	public abstract long getSize();

	public void setDeletePending(boolean b)
	{
		this.deletePending = b;
	}

	public boolean getDeletePending()
	{
		return deletePending;
	}

	public void close()
	{
		if (deletePending) {
			delete();
		}
	}

	private void delete()
	{
		try {
			path.delete();
		} catch (RNSPathDoesNotExistException e) {

		} catch (RNSException e) {

		}
	}
}