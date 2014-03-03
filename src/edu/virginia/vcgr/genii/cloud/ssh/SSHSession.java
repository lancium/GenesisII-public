package edu.virginia.vcgr.genii.cloud.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import edu.virginia.vcgr.genii.cloud.ResourceController;

public class SSHSession implements ResourceController
{

	private String _user;
	private String _host;
	private String _pass;
	private int _port;
	private String _privKey = null;
	private Boolean _strictHostCheck = true;
	private String _keyStore;

	static private Log _logger = LogFactory.getLog(SSHSession.class);

	public SSHSession(String username, int port, String host, String password, Boolean strictHostKeyChecking,
		String keyStorePath)
	{

		_user = username;
		_port = port;
		_host = host;
		_pass = password;
		_strictHostCheck = strictHostKeyChecking;
		_keyStore = keyStorePath;
	}

	public SSHSession(String username, int port, String host, String password)
	{
		this(username, port, host, password, false, null);
	}

	private Session setupCon() throws JSchException
	{
		JSch jsch = new JSch();
		Session session = jsch.getSession(_user, _host, _port);

		if (!_strictHostCheck)
			session.setConfig("StrictHostKeyChecking", "no");
		else
			jsch.setKnownHosts(_keyStore);

		if (_privKey == null)
			session.setPassword(_pass);
		else
			jsch.addIdentity(_privKey, _pass);

		return session;
	}

	public void setPrivateKeyAuth(String privateKeyPath)
	{
		_privKey = privateKeyPath;
	}

	public boolean sendFileTo(String localPath, String remotePath) throws Exception
	{

		OutputStream out = null;
		Session session = null;
		Channel channel = null;

		try {
			session = setupCon();
			session.connect(30000);

			// exec 'scp -t rfile' remotely
			String command = "scp" + " -t " + remotePath;
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if (checkAck(in) != 0) {
				return false;
			}

			File _lfile = new File(localPath);

			// send "C0644 filesize filename", where filename should not include '/'
			long filesize = _lfile.length();
			command = "C0644 " + filesize + " " + trimSlash(localPath) + "\n";

			out.write(command.getBytes());
			out.flush();

			if (checkAck(in) != 0) {
				return false;
			}

			// send a content of lfile
			FileInputStream fis = new FileInputStream(localPath);
			byte[] buf = new byte[1024];
			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len);
			}
			fis.close();
			fis = null;
			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			if (checkAck(in) != 0) {
				return false;
			}

			return true;
		} finally {
			if (out != null)
				out.close();
			if (channel != null)
				channel.disconnect();
			if (session != null)
				session.disconnect();
		}
	}

	public boolean recieveFileFrom(String localPath, String remotePath) throws Exception
	{

		OutputStream out = null;
		Session session = null;
		Channel channel = null;

		try {
			session = setupCon();
			session.connect(30000);

			// exec 'scp -f rfile' remotely
			String command = "scp -f " + remotePath;
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			byte[] buf = new byte[1024];

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			while (true) {
				int c = checkAck(in);
				if (c != 'C') {
					break;
				}

				// read '0644 '
				in.read(buf, 0, 5);

				long filesize = 0L;
				while (true) {
					if (in.read(buf, 0, 1) < 0) {
						// error
						break;
					}
					if (buf[0] == ' ')
						break;
					filesize = filesize * 10L + (long) (buf[0] - '0');
				}

				String file = null;
				for (int i = 0;; i++) {
					in.read(buf, i, 1);
					if (buf[i] == (byte) 0x0a) {
						file = new String(buf, 0, i);
						break;
					}
				}

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();

				// read a content of lfile
				String prefix = null;
				if (new File(localPath).isDirectory()) {
					prefix = localPath + File.separator;
				}

				FileOutputStream fos = new FileOutputStream(prefix == null ? localPath : prefix + file);
				int foo;
				while (true) {
					if (buf.length < filesize)
						foo = buf.length;
					else
						foo = (int) filesize;
					foo = in.read(buf, 0, foo);
					if (foo < 0) {
						// error
						break;
					}
					fos.write(buf, 0, foo);
					filesize -= foo;
					if (filesize == 0L)
						break;
				}
				fos.close();
				fos = null;

				if (checkAck(in) != 0) {
					return false;
				}

				// send '\0'
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
			}

			return true;
		} finally {
			if (out != null)
				out.close();
			if (channel != null)
				channel.disconnect();
			if (session != null)
				session.disconnect();
		}

	}

	public int sendCommand(String command, OutputStream out, OutputStream err) throws Exception
	{

		Channel channel = null;
		Session session = null;

		try {
			int exitStatus;
			String buffer = "";

			session = setupCon();
			session.connect(30000);

			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			channel.setInputStream(null);
			channel.setOutputStream(out);
			((ChannelExec) channel).setErrStream(err);

			InputStream in = channel.getInputStream();

			PrintWriter pw;
			if (out != null)
				pw = new PrintWriter(out);
			else
				pw = new PrintWriter(buffer);

			channel.connect();

			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					pw.print(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					exitStatus = channel.getExitStatus();
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}

			return exitStatus;
		} finally {
			if (channel != null)
				channel.disconnect();
			if (session != null)
				session.disconnect();
		}
	}

	static String trimSlash(String path)
	{
		if (path.lastIndexOf('/') > 0) {
			return path.substring(path.lastIndexOf('/') + 1);
		} else {
			return path;
		}
	}

	static int checkAck(InputStream in) throws IOException
	{
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				_logger.error(sb.toString());
			}
			if (b == 2) { // fatal error
				_logger.fatal(sb.toString());
			}
		}
		return b;
	}

	public boolean fileExists(String path) throws JSchException
	{

		Channel channel = null;
		Session session = null;
		ChannelSftp c = null;

		try {

			session = setupCon();
			session.connect(30000);

			// Create sftp channel
			channel = session.openChannel("sftp");
			channel.connect();
			c = (ChannelSftp) channel;

			// Fix to not sink to system.out
			c.get(path, System.out);

		} catch (SftpException e) {
			return false;
		}

		finally {
			if (c != null)
				c.disconnect();
			if (channel != null)
				channel.disconnect();
			if (session != null)
				session.disconnect();
		}
		return true;
	}

	@Override
	public void setAuthorizationFile(String path)
	{
		this.setPrivateKeyAuth(path);

	}

}
