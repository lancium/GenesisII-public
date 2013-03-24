package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.CopyMachine;
import edu.virginia.vcgr.genii.client.rns.PathOutcome;

public class CopyTool extends BaseGridTool
{
	static private final String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dcp";
	static private final String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/ucp";
	static private final String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/cp";
	static private Log _logger = LogFactory.getLog(CopyTool.class);

	boolean isRecursive = false;
	boolean isForced = false;

	@Option({ "recursive", "r" })
	public void setRecursive()
	{
		isRecursive = true;
	}

	@Option({ "force", "f" })
	public void setForce()
	{
		isForced = true;
	}

	public CopyTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new FileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws Throwable
	{
		// we want the last argument extracted, because that's the target.
		String argLast = getArgument(numArguments() - 1);
		int toReturn = 0;
		for (int i = 0; i < numArguments() - 1; i++) {
			if (_logger.isDebugEnabled())
				_logger.debug("CopyTool: copying from " + getArgument(i) + " to " + argLast);
			PathOutcome ret = copy(getArgument(i), argLast, isRecursive, isForced, stderr);
			if (ret.differs(PathOutcome.OUTCOME_SUCCESS)) {
				String msg = "Failed to copy from " + getArgument(i) + " to " + argLast + " because "
					+ PathOutcome.outcomeText(ret) + ".";
				stderr.println(msg);
				_logger.error(msg);
				toReturn = 1;
			}
		}
		return toReturn;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 2)
			throw new InvalidToolUsageException();
	}

	/**
	 * performs a copy operation from a source to a target. if the source or target mention grid: or
	 * local:, then those are used. otherwise this assumes both are in grid: space.
	 */
	public static PathOutcome copy(String sourcePath, String targetPath, boolean recursive, boolean force, PrintWriter stderr)
	{
		if ((sourcePath == null) || (targetPath == null))
			return PathOutcome.OUTCOME_NOTHING;
		PathOutcome toReturn = PathOutcome.OUTCOME_ERROR; // until we know more specifically.
		if (!recursive) {
			toReturn = CopyMachine.copyOneFile(sourcePath, targetPath);
		} else {
			// do a recursive copy by traversing source.
			CopyMachine mimeo = new CopyMachine(sourcePath, targetPath, null, force, stderr);
			toReturn = mimeo.copyTree();
		}
		return toReturn;
	}
}
