package edu.virginia.vcgr.smb.server.queryfs;

import java.io.UnsupportedEncodingException;

import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;

public class SMBInfoVolume
{
	public static void encode(SMBBuffer output, boolean unicode) throws SMBException
	{
		output.putInt(SMBQueryFs.SERIAL_NUMBER);

		// TODO: this is very broken

		// output.put((byte)output.strlen(SMBQueryFs.VOLUME_LABEL, unicode));
		// output.putString(SMBQueryFs.VOLUME_LABEL, unicode);

		try {
			byte[] ascii = SMBQueryFs.VOLUME_LABEL.getBytes("US-ASCII");
			output.put((byte) ascii.length);
			for (int i = 0; i < ascii.length; i++)
				output.put(ascii[i]);
		} catch (UnsupportedEncodingException e) {
			throw new SMBException(NTStatus.NOT_IMPLEMENTED);
		}
	}
}
