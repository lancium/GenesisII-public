package edu.virginia.vcgr.smb.server;

import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public abstract class SMBFile
{
	static private Log _logger = LogFactory.getLog(SMBFile.class);

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
		if (_logger.isDebugEnabled())
			_logger.debug("smb read: acc=" + acc + " offset=" + off + " len=" + len);
		
		// check that we won't go past end of file.
		TypeInformation typo = SMBTree.stat(path);
		long fileSize = typo.getByteIOSize();
		if (off >= fileSize) {
			_logger.debug("smb shortcutting read that's past eof (offset).");
			return 0;
		}
		if (off + len > fileSize) {
			// trim the length a bit.
			len = (int)(fileSize - off);
			if (len <= 0) {
				_logger.debug("smb shortcutting read that's past eof (offset+len).");
				return 0;
			}
		}
		
		IO io = getIO();

		ByteBuffer read = acc.slice().prepareBuffer();
		read.limit(len);

		int resLen = io.read(read, off);
		acc.skip(resLen);

		return resLen;
	}

	public void write(SMBBuffer out, long off) throws SMBException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("smb write: out=" + out + " offset=" + off);
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