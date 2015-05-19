package edu.virginia.vcgr.smb.server;

public class SMBAndX {
	private int command;
	private int offset;

	private SMBAndX(int command, int offset) {
		this.command = command;
		this.offset = offset;
	}

	public int getCommand() {
		return command;
	}

	public int getOffset() {
		return offset;
	}
	
	public static SMBAndX decode(SMBBuffer buffer) {
		int cmd = buffer.getUShort();
		int offset = buffer.getUShort();
		
		return new SMBAndX(cmd, offset);
	}

	public static int reserve(SMBBuffer output) {
		int loc = output.position();
		output.position(loc + 4);
		return loc;
	}

	public static void encode(SMBBuffer output, int from, int command) {
		int to = output.position();
		output.putShort(from, (short)command);
		output.putShort(from + 2, (short)to);
	}
}
