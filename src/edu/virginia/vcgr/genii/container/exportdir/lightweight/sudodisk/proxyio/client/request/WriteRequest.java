package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.Constants;

public class WriteRequest
{

	/**
	 * Forms a write request and sends it to the proxyserver. A write request is of the form:
	 * |nonce|file/dir|command|path-len|path|seekoffset| len of buffer to write|write buffer
	 */
	public static void send(Socket clientSocket, String path, byte[] wb, long offset, byte[] nonce)
		throws UnknownHostException, IOException
	{

		BufferedOutputStream bos = null;

		bos = new BufferedOutputStream(clientSocket.getOutputStream());

		byte[] request = new byte[1024 + wb.length];
		int w_offset = 0;

		w_offset = Commons.formPrefix(request, Constants.FILE_OPN_BYTE, Constants.FILE_WRITE_CMD_BYTE, path, nonce);

		// filling args!
		ByteBuffer bb_arg1 = ByteBuffer.allocate(8);
		long arg1 = offset;
		bb_arg1.putLong(arg1);
		byte[] b_arg1 = bb_arg1.array();
		System.arraycopy(b_arg1, 0, request, w_offset, b_arg1.length);
		w_offset += b_arg1.length;

		// filling buflen
		int wb_size = wb.length;
		ByteBuffer bb_wbsize = ByteBuffer.allocate(4);
		bb_wbsize.putInt(wb_size);
		byte[] b_wbsize = bb_wbsize.array();
		System.arraycopy(b_wbsize, 0, request, w_offset, b_wbsize.length);
		w_offset += b_wbsize.length;

		// writebuffer
		System.arraycopy(wb, 0, request, w_offset, wb.length);
		w_offset += wb.length;

		bos.write(request, 0, w_offset);
		bos.flush();

	}

	public static DefaultResponse recv(Socket clientSocket) throws IOException
	{
		return DefaultRequest.recv(clientSocket);
	}

}
