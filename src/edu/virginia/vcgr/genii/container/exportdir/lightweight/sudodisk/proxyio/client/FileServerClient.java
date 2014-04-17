package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils.PathType;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.commons.Constants;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.mapping.Mapper;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.TruncAppendRequest;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.DefaultRequest;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.DefaultResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.DirListRequest;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.DirListResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.ReadRequest;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.ReadResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.StatRequest;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.StatResponse;
import edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.client.request.WriteRequest;

public class FileServerClient
{

	public static ReadResponse read(String path, long offset, long numBytes, byte[] nonce, int port)
		throws UnknownHostException, IOException
	{

		if (path == null) {
			throw new NullPointerException("Read path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);
		try {
			ReadRequest.send(clientSocket, path, offset, numBytes, nonce);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		// return read response
		ReadResponse rr = null;
		try {
			rr = ReadRequest.recv(clientSocket, (int) numBytes);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		return rr;
	}

	public static DefaultResponse write(String path, byte[] wb, long offset, byte[] nonce, int port)
		throws UnknownHostException, IOException
	{

		if (path == null) {
			throw new NullPointerException("Write path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);

		try {
			WriteRequest.send(clientSocket, path, wb, offset, nonce);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);
		// read response
		try {
			DefaultResponse retVal = WriteRequest.recv(clientSocket);
			return retVal;
		} catch (IOException ioe) {
			clientSocket.close();
			throw ioe;
		}
	}

	public static DefaultResponse truncAppend(String path, byte[] wb, long offset, byte[] nonce, int port)
		throws UnknownHostException, IOException
	{

		if (path == null) {
			throw new NullPointerException("Append path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);
		try {
			TruncAppendRequest.send(clientSocket, path, wb, offset, nonce);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		// read response
		try {
			DefaultResponse retVal = TruncAppendRequest.recv(clientSocket);
			return retVal;
		} catch (IOException ioe) {
			clientSocket.close();
			throw ioe;
		}
	}

	public static StatResponse stat(String path, byte[] nonce, int port) throws UnknownHostException, IOException
	{
		if (path == null) {
			throw new NullPointerException("delete path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);

		StatResponse sr = null;
		try {
			StatRequest.send(clientSocket, Constants.FILE_OPN_BYTE, Constants.FILE_STAT_CMD_BYTE, path, nonce);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		try {
			sr = StatRequest.recv(clientSocket);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		return sr;
	}

	public static DefaultResponse rm(String path, byte[] nonce, int port) throws UnknownHostException, IOException
	{

		if (path == null) {
			throw new NullPointerException("delete path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);

		try {
			DefaultRequest.send(clientSocket, Constants.FILE_OPN_BYTE, Constants.FILE_DELETE_CMD_BYTE, path, nonce);
		} catch (Exception e) {
			clientSocket.close();
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		// read response
		try {
			DefaultResponse retVal = DefaultRequest.recv(clientSocket);
			return retVal;
		} catch (IOException ioe) {
			clientSocket.close();
			throw ioe;
		}

	}

	public static DefaultResponse mkdir(String path, byte[] nonce, int port) throws UnknownHostException, IOException
	{

		if (path == null) {
			throw new NullPointerException("Mkdir path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);
		try {
			DefaultRequest.send(clientSocket, Constants.DIR_OPN_BYTE, Constants.DIR_CREATE_CMD_BYTE, path, nonce);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		// read response
		try {
			DefaultResponse retVal = DefaultRequest.recv(clientSocket);
			return retVal;

		} catch (IOException ioe) {
			clientSocket.close();
			throw ioe;
		}
	}

	public static DefaultResponse rmdir(String path, byte[] nonce, int port) throws UnknownHostException, IOException
	{

		if (path == null) {
			throw new NullPointerException("Rmdir path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);
		try {
			DefaultRequest.send(clientSocket, Constants.DIR_OPN_BYTE, Constants.DIR_DELETE_CMD_BYTE, path, nonce);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		// read response
		try {
			DefaultResponse retVal = DefaultRequest.recv(clientSocket);
			return retVal;
		} catch (IOException ioe) {
			clientSocket.close();
			throw ioe;
		}
	}

	public static DefaultResponse isDir(String path, byte[] nonce, int port) throws UnknownHostException, IOException
	{

		if (path == null) {
			throw new NullPointerException("path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);
		try {
			DefaultRequest.send(clientSocket, Constants.DIR_OPN_BYTE, Constants.DIR_ISDIR_CMD_BYTE, path, nonce);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		// read response
		try {
			DefaultResponse retVal = DefaultRequest.recv(clientSocket);
			return retVal;
		} catch (IOException ioe) {
			clientSocket.close();
			throw ioe;
		}
	}

	public static DefaultResponse creat(String path, byte[] nonce, int port) throws UnknownHostException, IOException
	{

		if (path == null) {
			throw new NullPointerException("Creat path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);
		try {
			DefaultRequest.send(clientSocket, Constants.FILE_OPN_BYTE, Constants.FILE_CREATE_CMD_BYTE, path, nonce);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		// read response
		try {
			DefaultResponse retVal = DefaultRequest.recv(clientSocket);
			return retVal;
		} catch (IOException ioe) {
			clientSocket.close();
			throw ioe;
		}
	}

	public static DefaultResponse canRead(String path, byte[] nonce, int port, PathType pt) throws UnknownHostException,
		IOException
	{

		if (path == null) {
			throw new NullPointerException("path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);
		try {
			if (pt == PathType.FILE) {
				DefaultRequest.send(clientSocket, Constants.FILE_OPN_BYTE, Constants.FILE_CAN_READ_CMD_BYTE, path, nonce);
			} else if (pt == PathType.DIRECTORY) {
				DefaultRequest.send(clientSocket, Constants.DIR_OPN_BYTE, Constants.DIR_CAN_READ_CMD_BYTE, path, nonce);
			} else {
				clientSocket.close();
				throw new IOException("Cannot check can read on " + "non file/dir");
			}

		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		// read response
		try {
			DefaultResponse retVal = DefaultRequest.recv(clientSocket);
			return retVal;
		} catch (IOException ioe) {
			clientSocket.close();
			throw ioe;
		}

	}

	public static DefaultResponse exists(String path, byte[] nonce, int port, PathType pt) throws UnknownHostException,
		IOException
	{

		if (path == null) {
			throw new NullPointerException("path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);
		try {
			if (pt == PathType.FILE) {
				DefaultRequest.send(clientSocket, Constants.FILE_OPN_BYTE, Constants.FILE_DOESEXIST_CMD_BYTE, path, nonce);
			} else if (pt == PathType.DIRECTORY) {
				DefaultRequest.send(clientSocket, Constants.DIR_OPN_BYTE, Constants.DIR_DOESEXIST_CMD_BYTE, path, nonce);
			} else {
				clientSocket.close();
				throw new IOException("Cannot check existance on " + "non file/dir");
			}

		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		// read response
		try {
			DefaultResponse retVal = DefaultRequest.recv(clientSocket);
			return retVal;
		} catch (IOException ioe) {
			clientSocket.close();
			throw ioe;
		}

	}

	public static DefaultResponse canWrite(String path, byte[] nonce, int port, PathType pt) throws UnknownHostException,
		IOException
	{

		if (path == null) {
			throw new NullPointerException(" path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);
		try {
			if (pt == PathType.FILE) {
				DefaultRequest.send(clientSocket, Constants.FILE_OPN_BYTE, Constants.FILE_CAN_WRITE_CMD_BYTE, path, nonce);
			} else if (pt == PathType.DIRECTORY) {
				DefaultRequest.send(clientSocket, Constants.DIR_OPN_BYTE, Constants.DIR_CAN_WRITE_CMD_BYTE, path, nonce);
			} else {
				clientSocket.close();
				throw new IOException("Cannot check can write on " + "non file/dir");
			}

		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		// read response
		try {
			DefaultResponse retVal = DefaultRequest.recv(clientSocket);
			return retVal;
		} catch (IOException ioe) {
			clientSocket.close();
			throw ioe;
		}

	}

	public static DirListResponse listlong(String path, byte[] nonce, int port) throws UnknownHostException, IOException
	{

		if (path == null) {
			throw new NullPointerException("list path cannot be null");
		}

		Socket clientSocket = new Socket("localhost", port);

		try {
			DirListRequest.send(clientSocket, Constants.DIR_OPN_BYTE, Constants.DIR_LISTLONG_CMD_BYTE, path, nonce);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		clientSocket.setSoTimeout(Constants.READ_TIMEOUT_VAL);

		// read response!
		DirListResponse dlr = null;
		try {
			dlr = DirListRequest.recv(clientSocket);
		} catch (Exception e) {
			clientSocket.close();
			throw e;
		}

		return dlr;
	}

	// Invoked to start the file server!
	// returns the <nonce,port, serverprocess> where the server was started!
	public static synchronized FileServerID start(String uname)
	{

		// To Handle race!
		FileServerID fsid = Mapper.getClientMapping(uname);

		// Has someone else already created a mapping?
		if (fsid != null) {
			boolean isChildAlive = false;
			try {
				fsid.getFSProcess().exitValue();
			} catch (IllegalThreadStateException itse) {
				// Exception is thrown if process is still running!
				isChildAlive = true;
			}
			if (isChildAlive) {
				return fsid;
			}
		}

		String install_dir_sys_prop = "edu.virginia.vcgr.genii.install-base-dir";

		List<String> cmd = new ArrayList<String>();
		cmd.add("sudo");
		cmd.add("-u");
		cmd.add(uname);
		cmd.add(System.getProperty(install_dir_sys_prop) + "/fs_wrapper.sh");

		// cmd.add(Constants.FILE_SERVER_ROOT_PATH + "/fs_wrapper.sh");
		// cmd.add(Constants.FILE_SERVER_ROOT_PATH);

		Process p = null;
		ProcessBuilder pb = new ProcessBuilder(cmd);

		byte[] nonce = new byte[Constants.NONCE_SIZE];

		try {
			pb.redirectOutput(Redirect.PIPE);
			p = pb.start();
		} catch (Exception e) {
			return null;
		}

		BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));

		BufferedOutputStream stdin = new BufferedOutputStream(p.getOutputStream());

		try {
			new Random().nextBytes(nonce);
			stdin.write(nonce);
			stdin.flush();
		} catch (Exception ioe) {
			// kill the bad process we created!
			p.destroy();
			return null;
		}

		int port = 0;
		try {
			String line = stdout.readLine();
			port = Integer.parseInt(line);

			if (port == 0) {
				p.destroy();
				return null;
			}
		} catch (Exception e) {
			p.destroy();
			return null;
		}

		fsid = new FileServerID(nonce, port, p);
		Mapper.setClientMapping(uname, fsid);
		return fsid;
	}

	public static synchronized void stop(String uname)
	{

		// To Handle race!
		FileServerID fsid = Mapper.getClientMapping(uname);

		// Has someone else already created a mapping?
		if (fsid != null) {
			boolean isChildAlive = false;
			try {
				fsid.getFSProcess().exitValue();
			} catch (IllegalThreadStateException itse) {
				// Exception is thrown if process is still running!
				isChildAlive = true;
			}
			if (isChildAlive) {
				fsid.getFSProcess().destroy();
			}
		}
	}
}