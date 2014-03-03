package edu.virginia.vcgr.genii.client.io.gsiftp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public class GsiFtp
{
	private String sourceFile = "";
	private String destinationFile = "";
	private File workingDirectory;

	static private Log _logger = LogFactory.getLog(GsiFtp.class);

	public void setWorkingDirectory(File workingDirectory)
	{
		this.workingDirectory = workingDirectory;
	}

	public void setSourceFile(String localFile)
	{
		this.sourceFile = localFile;
	}

	public void setDestinationfile(String remoteFile)
	{
		this.destinationFile = remoteFile;
	}

	public void execute()
	{
		writeShellScriptToDisk();
		executeShellScript();
	}

	private void executeShellScript()
	{
		Runtime r = Runtime.getRuntime();
		try {
			Process gsiftpExec = r.exec("sh " + workingDirectory.getAbsolutePath() + "/gsiftp.sh");
			try {
				gsiftpExec.waitFor();

			} catch (InterruptedException e) {
				_logger.fatal("\n\n Shell script execution failing at gridftp stageout\n");
				e.printStackTrace();
			}
		} catch (IOException e) {
			_logger.fatal("\n\n Shell script execution failing at gridftp stageout\n");

		}
	}

	private void writeShellScriptToDisk()
	{
		try {
			FileWriter fstream = new FileWriter(workingDirectory.getAbsolutePath() + "/gsiftp.sh");

			BufferedWriter out = new BufferedWriter(fstream);
			out.write("export X509_USER_PROXY=" + workingDirectory.getAbsolutePath() + GenesisIIConstants.myproxyFilenameSuffix
				+ " \n");
			out.write("chmod 600 " + workingDirectory.getAbsolutePath() + GenesisIIConstants.myproxyFilenameSuffix + " \n");
			out.write("globus-url-copy " + sourceFile + " " + destinationFile + "\n");
			out.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}