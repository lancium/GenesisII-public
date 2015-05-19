package edu.virginia.vcgr.smb.server;

import java.nio.ByteBuffer;
import java.util.Calendar;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class SMBGenericIOFile extends SMBFile {
	private class IO implements SMBFile.IO {

		@Override
		public int read(ByteBuffer read, long off) throws SMBException {
			throw new SMBException(NTStatus.NOT_IMPLEMENTED);
		}

		@Override
		public void write(ByteBuffer write, long off) throws SMBException {
			throw new SMBException(NTStatus.NOT_IMPLEMENTED);
		}

		@Override
		public void truncate(long size) throws SMBException {
			throw new SMBException(NTStatus.NOT_IMPLEMENTED);
		}
	}
	
	public SMBGenericIOFile(RNSPath path, EndpointReferenceType fileEPR) {
		super(path, fileEPR);
	}

	@Override
	public IO getIO() {
		// TODO Auto-generated method stub
		return new IO();
	}

	@Override
	public long getCreateTime() {
		return Calendar.getInstance().getTimeInMillis();
	}
	
	@Override
	public void setCreateTime(long millis) {
		
	}

	@Override
	public long getWriteTime() {
		return Calendar.getInstance().getTimeInMillis();
	}

	@Override
	public void setWriteTime(long millis) {
		
	}

	@Override
	public long getAccessTime() {
		return Calendar.getInstance().getTimeInMillis();
	}

	@Override
	public void setAccessTime(long millis) {
		
	}

	@Override
	public int getAttr() {
		return SMBFileAttributes.fromTypeInfo(new TypeInformation(getEPR()));
	}

	@Override
	public void setAttr(int fileAttr) {
		
	}

	@Override
	public long getSize() {
		return 0;
	}
	
	@Override
	public void setSize(long fileSize) {
		
	}

	@Override
	public int getExtAttr() {
		return SMBExtFileAttributes.fromTypeInfo(new TypeInformation(getEPR()));
	}

	@Override
	public void setExtAttr(int fileAttr) {
		
	}
}
