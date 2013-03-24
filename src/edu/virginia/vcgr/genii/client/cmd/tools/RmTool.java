package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.rns.JavaFileHierarchyHelper;
import edu.virginia.vcgr.genii.client.rns.PathDisposal;
import edu.virginia.vcgr.genii.client.rns.PathOutcome;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathHierarchyHelper;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class RmTool extends BaseGridTool
{
	static private final String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/drm";
	static private final String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/urm";
	static private final String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/rm";

	static private Log _logger = LogFactory.getLog(RmTool.class);

	private boolean _recursive = false;
	private boolean _force = false;

	@Option({ "recursive", "r" })
	public void setRecursive()
	{
		_recursive = true;
	}

	@Option({ "force", "f" })
	public void setForce()
	{
		_force = true;
	}

	public RmTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new FileResource(_MANPAGE));
	}

	/**
	 * implements the actual activity of rm, once all parameters have been grabbed.
	 */
	@Override
	protected int runCommand() throws Throwable
	{
		boolean recursive = _recursive;
		boolean force = _force;

		RNSPath path = RNSPath.getCurrent();
		int toReturn = 0;
		for (int lcv = 0; lcv < numArguments(); lcv++) {
			GeniiPath gPath = new GeniiPath(getArgument(lcv));
			PathOutcome ret = PathOutcome.OUTCOME_ERROR;
			if (gPath.pathType() == GeniiPathType.Grid) {
				ret = rm(path, gPath.path(), recursive, force);
			} else {
				File fPath = new File(gPath.path());
				ret = rm(fPath, recursive, force);
			}
			if (ret.differs(PathOutcome.OUTCOME_SUCCESS)) {
				String msg = "Failed to remove " + gPath.toString() + " because " + PathOutcome.outcomeText(ret) + ".";
				stderr.println(msg);
				_logger.error(msg);
				toReturn = 1;
			}
		}

		return toReturn;
	}

	/**
	 * checks that the arguments seem appropriate.
	 */
	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException();
	}

	/**
	 * removes a path pointed at by a java File object.
	 */
	private PathOutcome rm(File path, boolean recursive, boolean force)
	{
		if (path == null)
			return PathOutcome.OUTCOME_NOTHING;
		if (_logger.isDebugEnabled())
			_logger.debug("entered into rm on Java File: path=" + path.toString() + " recurs=" + recursive + " force=" + force);
		if (!path.exists()) {
			if (force)
				return PathOutcome.OUTCOME_SUCCESS; // no error for this case with force enabled.
			stderr.println(path.getName() + " does not exist.");
			return PathOutcome.OUTCOME_NOTHING;
		}
		if (recursive)
			// recursive case will traverse into directories and eat all contents.
			return PathDisposal.recursiveDelete(path);
		else
			// not recursive, just remove the thing itself, if we can.
			return PathDisposal.removeAppropriately(path, new JavaFileHierarchyHelper(), null);
	}

	/**
	 * removes a "filePath" in RNS space using the "currentPath" as an entre.
	 */
	public PathOutcome rm(RNSPath currentPath, String filePath, boolean recursive, boolean force)
	{
		if ((currentPath == null) || (filePath == null))
			return PathOutcome.OUTCOME_NOTHING;
		if (_logger.isDebugEnabled())
			_logger.debug("entered into rm on RNSPath + String: currpath=" + currentPath.toString() + " filepath="
				+ filePath.toString() + " recurs=" + recursive + " force=" + force);
		for (RNSPath file : currentPath.expand(filePath)) {
			PathOutcome ret = rm(file, recursive, force);
			if (ret.differs(PathOutcome.OUTCOME_SUCCESS))
				return ret;
		}
		return PathOutcome.OUTCOME_SUCCESS;
	}

	/**
	 * removes an RNS "path" from RNS space.
	 */
	public PathOutcome rm(RNSPath path, boolean recursive, boolean force)
	{
		if (path == null)
			return PathOutcome.OUTCOME_NOTHING;
		if (_logger.isDebugEnabled())
			_logger.debug("entered into rm on RNSPath: path=" + path.toString() + " recurs=" + recursive + " force=" + force);
		PathOutcome ret = PathOutcome.OUTCOME_ERROR;
		if (recursive) {
			// do directory traversal into rns space and destroy contents.
			ret = PathDisposal.recursiveDelete(path);
		} else {
			// not recursive, so just clean the item itself.
			ret = PathDisposal.removeAppropriately(path, new RNSPathHierarchyHelper(), null);
		}
		if (ret.same(PathOutcome.OUTCOME_SUCCESS))
			return ret;
		if (force) {
			String msg = "Forcing removal via unlink after exception.";
			stderr.println(msg);
			_logger.warn(msg);
			try {
				path.unlink();
				return PathOutcome.OUTCOME_SUCCESS;
			} catch (Throwable cause) {
				_logger.error("Failed to unlink path: " + path.pwd(), cause);
				return ret;
			}
		} else {
			return ret;
		}
	}
}
