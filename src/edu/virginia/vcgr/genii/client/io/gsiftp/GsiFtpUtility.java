package edu.virginia.vcgr.genii.client.io.gsiftp;

import java.io.File;
import java.io.IOException;

import org.morgan.util.io.DataTransferStatistics;

public class GsiFtpUtility
{
	static public DataTransferStatistics put(File source, String destinationHost, int destinationPort, String destinationPath)
		throws IOException
	{
		DataTransferStatistics stats = DataTransferStatistics.startTransfer();

		GsiFtp copier = new GsiFtp();
		copier.setSourceFile(source.getAbsolutePath());
		copier.setDestinationfile("gsiftp://" + destinationHost + ":" + String.valueOf(destinationPort) + destinationPath);
		copier.setWorkingDirectory(source.getParentFile());

		copier.execute();
		stats.transfer(source.length());
		return stats.finishTransfer();
	}

	static public DataTransferStatistics get(File destination, String sourceFileHost, int sourcePort, String sourceFilePath)
		throws IOException
	{
		DataTransferStatistics stats = DataTransferStatistics.startTransfer();
		GsiFtp copier = new GsiFtp();
		copier.setSourceFile("gsiftp://" + sourceFileHost + ":" + String.valueOf(sourcePort) + sourceFilePath);
		copier.setDestinationfile(destination.getAbsolutePath());
		copier.setWorkingDirectory(destination.getParentFile());
		copier.execute();
		stats.transfer(destination.length());
		return stats.finishTransfer();
	}
}
