package edu.virginia.vcgr.genii.client.pwrapper;

import java.io.File;
import java.util.List;

public class PWrapperTester
{
	static public void main(String[] args) throws Throwable
	{
		ProcessWrapper wrapper = ProcessWrapperFactory.createWrapper(new File("."));
		wrapper.addProcessWrapperListener(new ProcessWrapperListener()
		{
			@Override
			public void processCompleted(ProcessWrapperToken token)
			{
				try {
					System.err.println(token.results());
				} catch (ProcessWrapperException pwe) {
					System.err.println(new ExitResults(143, 0L, 0L, 0L, 0L, "None"));
				}
			}
		});

		File workingDirectory = new File(System.getProperty("user.home"));
		File stdOutRedirect = new File(System.getProperty("user.home") + "/pwd.out");

		File resourceUsageFile = new ResourceUsageDirectory(workingDirectory).getNewResourceUsageFile();

		List<String> command =
			wrapper.formCommandLine(null, null, workingDirectory, null, stdOutRedirect, null, resourceUsageFile, null, "/bin/pwd");

		wrapper.execute(null, null, workingDirectory, null, resourceUsageFile, command);
	}
}