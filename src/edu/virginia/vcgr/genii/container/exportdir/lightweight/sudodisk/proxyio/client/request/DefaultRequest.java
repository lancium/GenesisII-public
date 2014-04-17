package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.Constants;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.Conversions;

/**
 * A default send is a request which just has a request prefix For example, mkdir is a default
 * request |nonce|file/dir|command|path-len|path while read, write/truncappend aren't default as
 * they have arguments for offset and numbytes respectively
 * 
 */
public class DefaultRequest
{

	public static void send(Socket clientSocket, byte pathtype, byte cmd, String path, byte[] nonce)
		throws UnknownHostException, IOException
	{

		BufferedOutputStream bos = null;

		bos = new BufferedOutputStream(clientSocket.getOutputStream());

		byte[] request = new byte[1024];
		int w_offset = 0;

		w_offset = Commons.formPrefix(request, pathtype, cmd, path, nonce);

		bos.write(request, 0, w_offset);
		bos.flush();

	}

	/**
	 * A default recv will have just the |error-code||len of err msg|error msg| It basically doesn't
	 * expect a response like stat or read. Examples are mkdir (where error-code indicates success
	 * of operation), and error msg indicating any failure in operation. Note that file system
	 * operations can have defaultrequest.send (eg. stat) but someothertype.recv
	 * 
	 * @param clientSocket
	 *            The socket which is going to get a response from server
	 * @return An object having information on the error-code, and error msg if any
	 * @throws IOException
	 */
	public static DefaultResponse recv(Socket clientSocket) throws IOException
	{
		BufferedInputStream bin = null;
		bin = new BufferedInputStream(clientSocket.getInputStream());

		// just read the prefix
		byte[] prefix = new byte[5];
		int bytes_read = bin.read(prefix, 0, prefix.length);
		if (bytes_read != prefix.length) {
			if (bytes_read == 0) {
				return new DefaultResponse(-1, Constants.NO_RESPONSE_MSG);
			}
			return new DefaultResponse(-1, Constants.SMALL_RESPONSE_ERROR);
		}

		int retVal = Conversions.getIntFromBytes((byte) 0x00, (byte) 0x00, (byte) 0x00, prefix[0]);

		// Is it a success response?
		if (retVal == 0) {
			return new DefaultResponse(0);
		}

		// Now read the response!
		int lenOfErrorMsg = Conversions.getIntFromBytes(prefix[1], prefix[2], prefix[3], prefix[4]);

		// read lenOfErrorMsg bytes from stream!
		// default buffered i/o size is 8k in java
		byte[] contents = new byte[512 * 1024];
		byte[] read_buff = new byte[lenOfErrorMsg];

		int readOffset = 0;

		// reading the error msg
		while (true) {
			if (readOffset == lenOfErrorMsg) {
				return new DefaultResponse(retVal, read_buff);
			}

			bytes_read = bin.read(contents, 0, contents.length);
			if (bytes_read == -1) {
				// XXX: Server could 've sent a smaller response!
				return new DefaultResponse(retVal, read_buff);
			}

			// Server sending response larger than it should -
			// shouldn't happen actually!
			if (readOffset + bytes_read > lenOfErrorMsg) {
				return new DefaultResponse(-1, Constants.LARGE_RESPONSE_ERROR);
			}

			System.arraycopy(contents, 0, read_buff, readOffset, bytes_read);
			readOffset += bytes_read;

		}

	}
}
