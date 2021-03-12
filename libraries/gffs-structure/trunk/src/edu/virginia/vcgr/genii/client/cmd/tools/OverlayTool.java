package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.CopyMachine;
import edu.virginia.vcgr.genii.client.rns.PathOutcome;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class OverlayTool extends BaseGridTool
{
	static private final String _DESCRIPTION = "config/tooldocs/description/doverlay";
	static private final String _USAGE = "config/tooldocs/usage/uoverlay";
	static private final String _MANPAGE = "config/tooldocs/man/overlay";
	static private Log _logger = LogFactory.getLog(OverlayTool.class);

	String _logLocationStr = null;
	RNSPath _logLocation = null;

	public OverlayTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		// arguments will be src, dest, offset (in src, appending to end of dest file)
		// we want the penultimate argument extracted, because that's the target.
		String argSrc = getArgument(0);
		String argTarget = getArgument(1);
		String argOffset = getArgument(2);
			_logger.debug("Overlay Tool: appending bytes from " + argSrc + " to " + argTarget + " with offset " + argOffset);
		PathOutcome ret = overlay(argSrc, argTarget, argOffset, _logLocation, stderr);

		if (ret.differs(PathOutcome.OUTCOME_SUCCESS)) {
			String msg = "Failed to overlay from " + argSrc + " to " + argTarget + " because " + PathOutcome.outcomeText(ret) + ".";
			stderr.println(msg);
			_logger.error(msg);
			return 1;
		}
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 3)
			throw new InvalidToolUsageException();
		String argOffset = getArgument(2);
		if (!StringUtils.isNumeric(argOffset)) {
			throw new InvalidToolUsageException();
		}
	}

	/**
	 * performs a copy operation from a source to a target. if the source or target mention grid: or local:, then those are used. otherwise
	 * this assumes both are in grid: space.
	 */
	public static PathOutcome overlay(String sourcePath, String targetPath, String offset, RNSPath logLocation,
		PrintWriter stderr)
	{
		if ((sourcePath == null) || (targetPath == null))
			return PathOutcome.OUTCOME_NOTHING;
		PathOutcome toReturn = PathOutcome.OUTCOME_ERROR; // until we know more specifically.
		// do a recursive copy by traversing source.
		if (_logger.isDebugEnabled())
			_logger.debug("copyOneFile from " + sourcePath + " to " + targetPath);
		GeniiPath source = new GeniiPath(sourcePath);
		if (!source.exists()) {
			_logger.error(String.format("Unable to find source file %s!", source));
			return PathOutcome.OUTCOME_NONEXISTENT;
		}
		if (!source.isFile()) {
			_logger.error(String.format("Source path %s is not a file!", source));
			return PathOutcome.OUTCOME_WRONG_TYPE;
		}
		GeniiPath target = new GeniiPath(targetPath);
		if (!target.exists()) {
			_logger.error(String.format("Unable to find source file %s!", source));
			return PathOutcome.OUTCOME_NONEXISTENT;
		}
		if (!target.isFile()) {
			_logger.error(String.format("Source path %s is not a file!", source));
			return PathOutcome.OUTCOME_WRONG_TYPE;
		}

		// In verify() we checked that offset was numeric, so this shouldn't break
		int num_offset = Integer.parseInt(offset);
		try {
			toReturn = doAppend(source, target, num_offset);
		} catch (RNSPathDoesNotExistException | RNSPathAlreadyExistsException | IOException e) {
			_logger.error("Caught exception while trying to append", e);
			return toReturn;
		}
		return toReturn;
	}
	private static PathOutcome doAppend(GeniiPath source, GeniiPath target, int offset) throws RNSPathDoesNotExistException, RNSPathAlreadyExistsException, RemoteException, IOException
	{
		_logger.debug("Inside doAppend... Source: " + source + ", target: " + target + ", offset: " + offset);
		// Get source EPR
		RNSPath current = RNSPath.getCurrent();
		RNSPath rnsSource = current.lookup(source.getName(), RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType sourceEPR = rnsSource.getEndpoint();
		
		// Get Target EPR
		RNSPath rnsTarget= current.lookup(target.getName(), RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType targetEPR = rnsTarget.getEndpoint();
		
		RandomByteIOPortType sourceStub = ClientUtils.createProxy(RandomByteIOPortType.class, sourceEPR);
		RandomByteIOTransfererFactory sourceFactory = new RandomByteIOTransfererFactory(sourceStub);
		RandomByteIOTransferer sourceTransferer = sourceFactory.createRandomByteIOTransferer();
		RandomByteIOPortType targetStub = ClientUtils.createProxy(RandomByteIOPortType.class, targetEPR);
		RandomByteIOTransfererFactory targetFactory = new RandomByteIOTransfererFactory(targetStub);
		RandomByteIOTransferer targetTransferer = targetFactory.createRandomByteIOTransferer();
		targetTransferer.append(sourceTransferer.read(offset, 10, 1, 0));
		return PathOutcome.OUTCOME_SUCCESS;
	}
}
