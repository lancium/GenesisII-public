package edu.virginia.vcgr.smb.server;


public class SMBHeader {
	public static final int FLAGS_CASE_INSENSITIVE = 0x08;
	public static final int FLAGS_REPLY = 0x80;
	public static final int FLAGS2_LONG_NAMES = 0x0001;
	public static final int FLAGS2_EAS = 0x0002;
	public static final int FLAGS2_SMB_SECURITY_SIGNATURE = 0x0004;
	public static final int FLAGS2_IS_LONG_NAME = 0x0040;
	public static final int FLAGS2_NT_STATUS = 0x4000;
	public static final int FLAGS2_UNICODE = 0x8000;
	
	public int command;
	public int status;
	public int flags;
	public int flags2;
	public int pid;
	public long security;
	public short tid, uid, mid;
	// Not part of the header, but used in AndX chains
	public int fid = 0;
	
	public SMBHeader(int command, int status, int flags, int flags2,
			int pid, long security, short tid, short uid, short mid) {
		super();
		this.command = command;
		this.status = status;
		this.flags = flags;
		this.flags2 = flags2;
		this.pid = pid;
		this.security = security;
		this.tid = tid;
		this.uid = uid;
		this.mid = mid;
	}

	/* Most responses only change the status code */
	public SMBHeader response(int status) {
		/* The flags are hard coded here, but should work for most cases */
		return new SMBHeader(command, status, FLAGS_REPLY, FLAGS2_LONG_NAMES | FLAGS2_NT_STATUS, pid, security, tid, uid, mid);
	}
	
	public static int size() {
		return 8 * 4;
	}
	
	public void encode(SMBBuffer buffer) {
		buffer.put((byte)0xff);
		buffer.put((byte)'S');
		buffer.put((byte)'M');
		buffer.put((byte)'B');
		buffer.put((byte)command);
		buffer.putInt(status);
		buffer.put((byte)flags);
		buffer.putShort((short)flags2);
		buffer.putShort((short)(pid >> 16));
		buffer.putLong(security);
		buffer.putShort((short)0);
		buffer.putShort(tid);
		buffer.putShort((short)pid);
		buffer.putShort(uid);
		buffer.putShort(mid);
	}
	
	public static SMBHeader decode(SMBBuffer buffer) throws SMBException {
		if (buffer.limit() < size()) {
			throw new SMBException(NTStatus.INVALID_SMB);
		}
		
		if (buffer.get() != -1 || buffer.get() != 'S' || buffer.get() != 'M' || buffer.get() != 'B') {
			throw new SMBException(NTStatus.INVALID_SMB);
		}
		
		int command = buffer.get() & 0xff;
		int status = buffer.getInt();
		/* Core dialect doesn't support any flags */
		byte flags = buffer.get();
		short flags2 = buffer.getShort();
		int PID = ((buffer.getShort() & 0xffff) << 16);
		long security = buffer.getLong();
		buffer.getShort();
		short TID = buffer.getShort();
		PID |= (buffer.getShort() & 0xffff);
		short UID = buffer.getShort();
		short MID = buffer.getShort();
		
		return new SMBHeader(command, status, flags, flags2, PID, security, TID, UID, MID);
	}

	public boolean isUnicode() {
		return (flags2 & FLAGS2_UNICODE) != 0;
	}

	public boolean isCaseSensitive() {
		return (flags & FLAGS_CASE_INSENSITIVE) == 0;
	}
}
