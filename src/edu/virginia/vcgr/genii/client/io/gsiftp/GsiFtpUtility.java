package edu.virginia.vcgr.genii.client.io.gsiftp;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.io.DataTransferStatistics;

public class GsiFtpUtility
{
	static public DataTransferStatistics put(File source, String destinationHost, int destinationPort, String destinationPath)
		throws IOException
	{
		DataTransferStatistics stats = DataTransferStatistics.startTransfer();

		GsiFtp copier = new GsiFtp();
		copier.setPort(destinationPort);
		copier.setSourceFile(source.getAbsolutePath());
		copier.setDestinationfile("gsiftp://" + destinationHost + destinationPath);
		copier.setWorkingDirectory(source.getParentFile());

		copier.execute();
		stats.transfer(source.length());
		return stats.finishTransfer();
	}

	static private Pattern FNF_PATTERN = Pattern.compile("scp: .*: No such file or directory");

	static public DataTransferStatistics get(File destination, String sourceFileHost, int port, String sourceFilePath)
		throws IOException
	{
		DataTransferStatistics stats = DataTransferStatistics.startTransfer();
		GsiFtp copier = new GsiFtp();
		copier.setPort(port);
		copier.setSourceFile("gsiftp://" + sourceFileHost + sourceFilePath);
		copier.setDestinationfile(destination.getAbsolutePath());
		copier.setWorkingDirectory(destination.getParentFile());
		copier.execute();
		stats.transfer(destination.length());
		return stats.finishTransfer();
	}
}