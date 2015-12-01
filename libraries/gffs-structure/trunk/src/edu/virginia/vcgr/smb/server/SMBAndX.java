package edu.virginia.vcgr.smb.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SMBAndX
{
	static private Log _logger = LogFactory.getLog(SMBAndX.class);

	private int command;
	private int offset;

	private SMBAndX(int command, int offset)
	{
		this.command = command;
		this.offset = offset;
	}

	public int getCommand()
	{
		return command;
	}

	public int getOffset()
	{
		return offset;
	}

	public static SMBAndX decode(SMBBuffer buffer)
	{
		int cmd = buffer.getUShort();

		//hmmm: trying this more to spec.  doesn't work...
//		int cmd = buffer.get();		
//		@SuppressWarnings("unused")
//		int reserved = buffer.get();		

		if (_logger.isDebugEnabled()) {
			if (cmd == 0xff) {
				_logger.debug("saw the NO_ANDX terminator for command batch (0xFF)");
			} else {
				_logger.debug("saw normal command in AND_X: 0x" + Integer.toHexString(cmd));
			}
		}
		

		int offset = buffer.getUShort();

		return new SMBAndX(cmd, offset);
	}

	public static int reserve(SMBBuffer output)
	{
		int loc = output.position();
		output.position(loc + 4);
		return loc;
	}

	public static void encode(SMBBuffer output, int from, int command)
	{
		int to = output.position();
		output.putShort(from, (short) command);

		//hmmm: trying more canonical version.  no worky.
//		output.put(from, (byte)command);
//		// reserved field.
//		output.put(from + 1, (byte)0);
		
		output.putShort(from + 2, (short) to);
	}
}
