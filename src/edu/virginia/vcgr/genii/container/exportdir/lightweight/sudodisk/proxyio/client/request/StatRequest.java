package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.Constants;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.StatAttributes;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.Conversions;

public class StatRequest
{

	public static void send(Socket clientSocket, byte pathtype, byte cmd, String path, byte[] nonce)
		throws UnknownHostException, IOException
	{
		DefaultRequest.send(clientSocket, pathtype, cmd, path, nonce);
	}

	/**
	 * 
	 * @param clientSocket
	 * @return
	 * @throws IOException
	 */
	public static StatResponse recv(Socket clientSocket) throws IOException
	{

		BufferedInputStream bis = null;

		bis = new BufferedInputStream(clientSocket.getInputStream());

		// status code + sizeof(read buffer)
		// the response buffer has the directory listing or
		// an error msg depending on the error-code

		byte[] response_pfix = new byte[5];
		int bytes_read = bis.read(response_pfix, 0, response_pfix.length);
		if (bytes_read != response_pfix.length) {
			if (bytes_read == 0) {
				return new StatResponse(-1, Constants.NO_RESPONSE_MSG);
			}
			return new StatResponse(-1, Constants.SMALL_RESPONSE_ERROR);
		}

		// Error!
		int errorCode = Conversions.getIntFromBytes((byte) 0x00, (byte) 0x00, (byte) 0x00, response_pfix[0]);

		boolean successResponse = true;
		if (errorCode != 0) {
			successResponse = false;
		}

		// contents can be:
		// success response or error msg as indicated by above boolean!
		int read_buf_size = Conversions.getIntFromBytes(response_pfix[1], response_pfix[2], response_pfix[3], response_pfix[4]);

		byte[] contents = new byte[512 * 1024];
		byte[] read_buff = new byte[read_buf_size];

		int readOffset = 0;
		while (true) {

			// I 've read how much I am supposed to read
			if (readOffset == read_buf_size) {
				if (successResponse) {
					// return the stat attributes for the queried filesystem path
					StatAttributes sr = StatAttributes.deserialize(read_buff);
					return new StatResponse(0, sr);
				} else {
					// return errormsg
					return new StatResponse(errorCode, new String(read_buff));
				}
			}

			bytes_read = bis.read(contents, 0, contents.length);
			if (bytes_read == -1) {
				return new StatResponse(-1, Constants.SMALL_RESPONSE_ERROR);
			}

			// Server sending response larger than it should -
			// shouldn't happen actually!
			if (readOffset + bytes_read > read_buf_size) {
				return new StatResponse(-1, Constants.LARGE_RESPONSE_ERROR);
			}

			System.arraycopy(contents, 0, read_buff, readOffset, bytes_read);
			readOffset += bytes_read;

		}

	}

}
