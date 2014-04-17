package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request;

import java.nio.ByteBuffer;

public class Commons
{

	/**
	 * This method forms the prefix part of a request between GFFS container and the FileServer.
	 * Each request is unique depending on the type of operation it performs. But the prefix
	 * contains fields which are common to every request. However the length of these fields need
	 * not be the same. This prefix is formed in this method and stored in a passed byte array.
	 * 
	 * @param request
	 *            The byte array where the prefix has to be stored
	 * @param pathtype
	 *            Whether it's a file or directory operation
	 * @param cmd
	 *            Byte representing the command to be performed
	 * @param path
	 *            The filesystem path where the cmd operation has to be performed
	 * @param nonce
	 *            The nonce which goes in every request
	 * @return The number of bytes in the prefix. The length of the prefix varies as it has a
	 *         filesystem path field
	 */
	public static int formPrefix(byte[] request, byte pathtype, byte cmd, String path, byte[] nonce)
	{

		int w_offset = 0;

		// nonce!
		if (nonce != null) {
			System.arraycopy(nonce, 0, request, w_offset, nonce.length);
			w_offset += nonce.length;
		}

		// f/d
		request[w_offset++] = pathtype;
		// cmd
		request[w_offset++] = cmd;

		// pathlength and path
		byte[] b_path = path.getBytes();
		int pathLen = b_path.length;

		// pathlen first
		ByteBuffer bb_pathlen = ByteBuffer.allocate(4);
		// the initial order of a byte buffer is always BIG_ENDIAN.
		bb_pathlen.putInt(pathLen);
		byte[] b_pathLen = bb_pathlen.array();
		System.arraycopy(b_pathLen, 0, request, w_offset, b_pathLen.length);
		w_offset += b_pathLen.length;

		// path
		System.arraycopy(b_path, 0, request, w_offset, b_path.length);
		w_offset += b_path.length;

		return w_offset;
	}

}
