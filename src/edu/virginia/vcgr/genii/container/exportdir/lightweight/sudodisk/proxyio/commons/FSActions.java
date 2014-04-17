package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons;

import java.io.File;
import java.net.Socket;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.server.response.DefaultResponse;

// contains list of file system actions that are common to files&dirs
public class FSActions
{

	// returns 0 if readable!
	public static int canRead(String path, Socket socket)
	{
		if (path == null) {
			DefaultResponse.send(socket, ErrorCode.BAD_PATH_CODE, ErrorCode.getErrorMsgFromErrorCode(ErrorCode.BAD_PATH_CODE));
			return ErrorCode.BAD_PATH_CODE;
		}

		File f = new File(path);
		if (f.canRead()) {
			DefaultResponse.send(socket, ErrorCode.SUCCESS_CODE, null);
			return ErrorCode.SUCCESS_CODE;
		} else {
			DefaultResponse.send(socket, ErrorCode.CANNOT_READ_CODE, null);
			return ErrorCode.CANNOT_READ_CODE;
		}
	}

	// returns 0 if writeable!
	public static int canWrite(String path, Socket socket)
	{
		if (path == null) {
			DefaultResponse.send(socket, ErrorCode.BAD_PATH_CODE, ErrorCode.getErrorMsgFromErrorCode(ErrorCode.BAD_PATH_CODE));
			return ErrorCode.BAD_PATH_CODE;
		}

		File f = new File(path);
		if (f.canWrite()) {
			DefaultResponse.send(socket, ErrorCode.SUCCESS_CODE, null);
			return ErrorCode.SUCCESS_CODE;
		} else {
			DefaultResponse.send(socket, ErrorCode.CANNOT_WRITE_CODE, null);
			return ErrorCode.CANNOT_WRITE_CODE;
		}
	}

	public static int isExists(String path, Socket socket)
	{

		if (path == null) {
			DefaultResponse.send(socket, ErrorCode.BAD_PATH_CODE, ErrorCode.getErrorMsgFromErrorCode(ErrorCode.BAD_PATH_CODE));
			return ErrorCode.BAD_PATH_CODE;
		}

		File f = new File(path);
		if (f.exists()) {
			DefaultResponse.send(socket, ErrorCode.SUCCESS_CODE, null);
			return ErrorCode.SUCCESS_CODE;
		} else {
			DefaultResponse.send(socket, ErrorCode.FNF_CODE, null);
			return ErrorCode.FNF_CODE;
		}

	}
}
