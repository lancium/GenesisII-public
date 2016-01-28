package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.PathOutcome;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class RmdirTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(RmdirTool.class);

	static final private String _DESCRIPTION = "config/tooldocs/description/drmdir";
	static final private String _USAGE = "config/tooldocs/usage/urmdir";
	static final private String _MANPAGE = "config/tooldocs/man/rmdir";

	public RmdirTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		return removeDirectory(getArguments(), stderr);
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException();
	}

	public static int removeDirectory(List<String> pathsToRemove, PrintWriter stderr)
		throws RNSException, InvalidToolUsageException, FileNotFoundException, IOException
	{
		RNSPath path = RNSPath.getCurrent();
		int toReturn = 0;
		for (String sPath : pathsToRemove) {
			GeniiPath gPath = new GeniiPath(sPath);

			PathOutcome ret = PathOutcome.OUTCOME_ERROR;
			if (gPath.pathType() == GeniiPathType.Grid) {
				ret = RmTool.rm(path, gPath.path(), false, false, stderr);
			} else {
				File fPath = new File(gPath.path());
				ret = RmTool.rm(fPath, false, false, stderr);
			}
			if (ret.differs(PathOutcome.OUTCOME_SUCCESS)) {
				String msg = "Failed to remove " + gPath.toString() + " because " + PathOutcome.outcomeText(ret) + ".";
				if (stderr != null)
					stderr.println(msg);
				_logger.error(msg);
				toReturn = 1;
			}
		}
		return toReturn;
	}

}