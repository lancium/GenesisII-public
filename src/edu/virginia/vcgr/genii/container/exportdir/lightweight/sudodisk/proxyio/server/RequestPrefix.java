package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.Constants;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.PathType;

/**
 * The prefix part of a request between 
 * GFFS container and the FileServer. Each request is unique depending
 * on the type of operation it performs. But the prefix contains fields
 * which are common to every request. However the length of these fields
 * need not be the same. 
 * 
 * Request prefix is of the form:
 * |nonce|file/dir|command|path-len|path
 *
 */
public class RequestPrefix {

	//----------------------------------------------------
	//IMPORTANT :if any field is added or type is modified! Modify getSize()!
	//----------------------------------------------------

	private byte[] _nonce = new byte[Constants.NONCE_SIZE];
	private PathType _pathType;
	private String _cmd;
	private int _pathNamelen;

	public RequestPrefix(byte[] nonce, PathType pathType, String cmd,
			int pathNamelen) {
		this._nonce = nonce;
		this._pathType = pathType;
		this._cmd = cmd;
		this._pathNamelen = pathNamelen;
	}

	public static int getSize() {
		//16+1+1+4
		//sizeof(nonce) + sizeof(pathType) 
		// + sizeof(cmd) +sizeof(pathNameLen)
		return Constants.NONCE_SIZE + 6;
	}

	public int getPathLength() {
		return _pathNamelen;
	}

	public byte[] getNonce() {
		return _nonce;
	}

	public PathType getPathType() {
		return _pathType;
	}

	public String getCmd() {
		return _cmd;
	}

	public boolean isReadReq() {
		return (_cmd.equals(Constants.FILE_READ_CMD));
	}

	public boolean isWriteReq() {
		return (_cmd.equals(Constants.FILE_WRITE_CMD));

	}

	public boolean isAppendReq() {
		return (_cmd.equals(Constants.FILE_TRUNCAPPEND_CMD));
	}
}
