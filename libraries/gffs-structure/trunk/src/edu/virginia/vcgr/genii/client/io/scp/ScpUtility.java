package edu.virginia.vcgr.genii.client.io.scp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.morgan.util.io.DataTransferStatistics;

public class ScpUtility
{
	static public DataTransferStatistics put(File localFile, String user, String password, String host, int port,
		String remotePath, boolean useSFTP) throws IOException
	{
		DataTransferStatistics stats = DataTransferStatistics.startTransfer();

		Scp copier = new Scp();
		copier.setPort(port);
		copier.setLocalFile(localFile.getAbsolutePath());
		copier.setRemoteTofile(String.format("%s@%s:%s", user, host, remotePath));
		copier.setPassword(password);
		copier.setSftp(useSFTP);
		copier.setTrust(true);
		copier.setProject(new Project());
		copier.execute();

		stats.transfer(localFile.length());
		return stats.finishTransfer();
	}

	static private Pattern FNF_PATTERN = Pattern.compile("scp: .*: No such file or directory");

	static public DataTransferStatistics get(File localFile, String user, String password, String host, int port,
		String remotePath, boolean useSFTP) throws IOException
	{
		try {
			DataTransferStatistics stats = DataTransferStatistics.startTransfer();

			Scp copier = new Scp();
			copier.setPort(port);
			copier.setRemoteFile(String.format("%s@%s:%s", user, host, remotePath));
			copier.setLocalTofile(localFile.getAbsolutePath());
			copier.setPassword(password);
			copier.setSftp(useSFTP);
			copier.setTrust(true);
			copier.setProject(new Project());
			copier.execute();

			stats.transfer(localFile.length());
			return stats.finishTransfer();
		} catch (Throwable cause) {
			Throwable cause2 = cause;
			while (cause2.getCause() != null)
				cause2 = cause2.getCause();

			if (cause2 instanceof IOException) {
				Matcher matcher = FNF_PATTERN.matcher(cause2.getMessage());
				if (matcher.matches())
					throw new FileNotFoundException("Couldn't find remote file \"" + remotePath + "\".");

				throw (IOException) cause2;
			}

			throw new IOException("Unable to copy remote file.", cause2);
		}
	}
}