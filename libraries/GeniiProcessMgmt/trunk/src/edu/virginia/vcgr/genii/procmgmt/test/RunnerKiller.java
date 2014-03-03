package edu.virginia.vcgr.genii.procmgmt.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import edu.virginia.vcgr.genii.procmgmt.ProcessManager;

public class RunnerKiller {
	static private class StreamCopier extends Thread {
		private InputStream _in;
		private OutputStream _out;

		private StreamCopier(InputStream in, OutputStream out) {
			super("Stream Copier");

			_in = in;
			_out = out;

			setDaemon(true);
		}

		@Override
		public void run() {
			byte[] data = new byte[1024];
			int read;

			try {
				while ((read = _in.read(data)) > 0) {
					_out.write(data, 0, read);
					_out.flush();
				}
			} catch (Throwable cause) {
				System.err.println("Error while copying stream.");
			}
		}
	}

	static public void main(String[] args) throws Throwable {
		ProcessBuilder builder = new ProcessBuilder(
				"C:\\Documents and Settings\\Mark Morgan\\longrun\\LongRunner\\Debug\\RunnerRunner.bat");
		builder.directory(new File(
				"C:\\Documents and Settings\\Mark Morgan\\longrun\\LongRunner\\Debug"));
		Process p = builder.start();

		new StreamCopier(new FileInputStream("build.xml"), p.getOutputStream())
				.start();
		new StreamCopier(p.getInputStream(), System.out).start();
		new StreamCopier(p.getErrorStream(), System.err).start();

		ProcessManager.kill(p);
	}
}