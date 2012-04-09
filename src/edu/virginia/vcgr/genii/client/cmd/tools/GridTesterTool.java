package edu.virginia.vcgr.genii.client.cmd.tools;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class GridTesterTool extends BaseGridTool
{
	static final private int LINE_WIDTH = 75;
	static final private int RESULT_WIDTH = 20;
	static final private int MIN_DOT_WIDTH = 5;
	static final private int MAX_DESCRIPTION_WIDTH = LINE_WIDTH - 
		RESULT_WIDTH - MIN_DOT_WIDTH;
	
	static final private String DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dgrid-tester";
	static final private FileResource _MANPAGE = new FileResource("edu/virginia/vcgr/genii/client/cmd/tools/man/grid-tester");
	
	/*
	static final private String USAGE_PATTERN = "grid-tester { %s }";
	
	static private String formatUsage(String pattern)
	{
		StringBuilder builder = new StringBuilder();
		for (JUnitTestTypes testType : JUnitTestTypes.values())
		{
			if (testType.isValid())
			{
				if (builder.length() > 0)
					builder.append(" | ");
				builder.append(testType);
			}
		}
		return String.format(pattern, builder);
	}
	*/
	
	private boolean _showStackTraces = false;
	
	@SuppressWarnings("unused")
	@Option("show-stack-traces")
	private void setShowStackTraces()
	{
		_showStackTraces = true;
	}
	
	private class InternalRunListener
		extends RunListener
	{
		private boolean _finishedOutputting = true;
		
		@Override
		public void testStarted(Description description) throws Exception
		{
			_finishedOutputting = false;
			
			String desc = description.getMethodName();
			stdout.print(desc);
			int dots = MIN_DOT_WIDTH + (MAX_DESCRIPTION_WIDTH - desc.length());
			if (dots < MIN_DOT_WIDTH)
				dots = MIN_DOT_WIDTH;
			while (dots-- > 0)
				stdout.print('.');
			stdout.flush();
		}

		@Override
		public void testFinished(Description description) throws Exception
		{
			if (!_finishedOutputting)
				stdout.println("[FINISHED]");
			stdout.flush();
			_finishedOutputting = true;
		}

		@Override
		public void testFailure(Failure failure) throws Exception
		{
			if (!_finishedOutputting)
			{
				stdout.println("[FAILED]");
				String header = failure.getMessage();
				if (header != null)
					stdout.format("\tTest:  %s\n", header);
				if (_showStackTraces)
				{
					stdout.flush();
					Throwable cause = failure.getException();
					if (cause != null)
						cause.printStackTrace(stdout);
				}
			}
			
			stdout.flush();
			_finishedOutputting = true;
		}

		@Override
		public void testAssumptionFailure(Failure failure)
		{
			if (!_finishedOutputting)
			{
				stdout.println("[ASSUMPTION FAILED]");
				String header = failure.getMessage();
				if (header != null)
					stdout.format("\tTest:  %s\n", header);
				if (_showStackTraces)
				{
					stdout.flush();
					Throwable cause = failure.getException();
					if (cause != null)
						cause.printStackTrace(System.err);
				}
			}
			
			stdout.flush();
			_finishedOutputting = true;
		}

		@Override
		public void testIgnored(Description description) throws Exception
		{
			if (!_finishedOutputting)
				stdout.println("[SKIPPED]");
			
			stdout.flush();
			_finishedOutputting = true;
		}
	}
	
	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException(
				"Wrong number of arguments!");
		
		String arg = getArgument(0);
		
		try
		{
			if (!JUnitTestTypes.valueOf(arg).isValid())
				throw new IllegalArgumentException();
		}
		catch (Throwable cause)
		{
			throw new InvalidToolUsageException(String.format(
				"Argument \"%s\" is unrecognized!", arg));
		}
	}

	@Override
	protected int runCommand() throws Throwable
	{
		String arg = getArgument(0);
		JUnitTestTypes testType = JUnitTestTypes.valueOf(arg);
		
		JUnitCore core = new JUnitCore();
		core.addListener(new InternalRunListener());
		Result result = core.run(testType.classes());
		stdout.println();
		stdout.format("Tests Completed in %.2f seconds.\n", 
			result.getRunTime() / 1000.0);
		stdout.format("%d/%d Tests Completed Successfully, %d Tests Ignored.\n",
			result.getRunCount() - result.getFailureCount(), result.getRunCount(),
			result.getIgnoreCount());
		return result.wasSuccessful() ? 0 : 1;
	}

	public GridTesterTool()
	{
		/*
		super(new FileResource(DESCRIPTION), formatUsage(USAGE_PATTERN), true,
				ToolCategory.INTERNAL); */
		super(new FileResource(DESCRIPTION), null, true,
				ToolCategory.INTERNAL);
		
		addManPage(_MANPAGE);
	}
}