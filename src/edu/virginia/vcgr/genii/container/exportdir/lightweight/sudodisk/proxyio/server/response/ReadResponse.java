package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.response;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.ErrorCode;

public class ReadResponse
{

	public static void send(Socket socket, byte[] b)
	{
		if (socket == null || b == null) {
			return;
		}

		try {
			BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

			// one byte for response code, and
			// one int for size of response
			byte[] prefix = new byte[5];
			prefix[0] = ErrorCode.SUCCESS_CODE;

			ByteBuffer bb_buflen = ByteBuffer.allocate(4);
			// the initial order of a byte buffer is always BIG_ENDIAN.
			bb_buflen.putInt(b.length);
			byte[] b_buflen = bb_buflen.array();
			System.arraycopy(b_buflen, 0, prefix, 1, b_buflen.length);

			// prefix now has 5 bytes of content!

			// let's send out the prefix!
			bos.write(prefix);

			if (b.length != 0) {
				bos.write(b);
			}

			bos.close();

		} catch (IOException ioe) {
			System.err.println("Error writing out to client");
			ioe.printStackTrace();
		}

	}

}
