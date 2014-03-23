package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.byteio.RandomByteIOOutputStream;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.CopyMachine;
import edu.virginia.vcgr.genii.client.rns.PathOutcome;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class CopyTool extends BaseGridTool
{
	static private final String _DESCRIPTION = "config/tooldocs/description/dcp";
	static private final String _USAGE = "config/tooldocs/usage/ucp";
	static private final String _MANPAGE = "config/tooldocs/man/cp";
	static private Log _logger = LogFactory.getLog(CopyTool.class);

	boolean isRecursive = false;
	boolean isForced = false;
	boolean isReliable = false;
	boolean isReliableOnLocal = false;

	String _logLocationStr = null;
	RNSPath _logLocation = null;

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

	@Option({ "reliable" })
	public void setReliable(String logLocationStr)
	{
		isReliable = true;
		_logLocationStr = logLocationStr;
	}

	@Option({ "reliable-on-local" })
	public void setReliableOnLocal(String logLocationStr)
	{
		isReliableOnLocal = true;
		_logLocationStr = logLocationStr;
	}

	public CopyTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException
	{
		if (isReliable) {

			// RunTool.submitJob(jsdlFileName, besContainer, optJobName, null)
			return 1;
		}

		// we want the last argument extracted, because that's the target.
		String argLast = getArgument(numArguments() - 1);
		int toReturn = 0;
		for (int i = 0; i < numArguments() - 1; i++) {
			if (_logger.isDebugEnabled())
				_logger.debug("CopyTool: copying from " + getArgument(i) + " to " + argLast);
			PathOutcome ret = copy(getArgument(i), argLast, isRecursive, isForced, _logLocation, stderr);

			if (ret.differs(PathOutcome.OUTCOME_SUCCESS)) {
				String msg =
					"Failed to copy from " + getArgument(i) + " to " + argLast + " because " + PathOutcome.outcomeText(ret)
						+ ".";
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
		if (isReliable || isReliableOnLocal) {
			RNSPath current = RNSPath.getCurrent();
			try {
				_logLocation = current.lookup(_logLocationStr, RNSPathQueryFlags.DONT_CARE);
				if (!_logLocation.exists()) {

					_logLocation.createNewFile();
					DocumentBuilder dBuilder = null;
					Document doc = null;

					try {
						dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						doc = dBuilder.newDocument();
						Element rootElement = doc.createElement("files");
						doc.appendChild(rootElement);

						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						StreamResult result = new StreamResult(new RandomByteIOOutputStream(_logLocation.getEndpoint()));
						transformer.transform(source, result);
					} catch (ParserConfigurationException e) {
						_logger.error("caught ParserConfigurationException", e);
					} catch (IOException e) {
						_logger.error("caught IOException", e);
					} catch (TransformerException e) {
						_logger.error("caught TransformerException", e);
					}
				}
			} catch (RNSException e) {
				throw new InvalidToolUsageException("Cannot create log at given location");
			}

		}
	}

	/**
	 * performs a copy operation from a source to a target. if the source or target mention grid: or
	 * local:, then those are used. otherwise this assumes both are in grid: space.
	 */
	public static PathOutcome copy(String sourcePath, String targetPath, boolean recursive, boolean force, RNSPath logLocation,
		PrintWriter stderr)
	{
		if ((sourcePath == null) || (targetPath == null))
			return PathOutcome.OUTCOME_NOTHING;
		PathOutcome toReturn = PathOutcome.OUTCOME_ERROR; // until we know more specifically.
		if (!recursive) {
			toReturn = CopyMachine.copyOneFile(sourcePath, targetPath, logLocation);
		} else {
			// do a recursive copy by traversing source.
			CopyMachine mimeo = new CopyMachine(sourcePath, targetPath, null, force, stderr, logLocation);
			toReturn = mimeo.copyTree();
		}
		return toReturn;
	}
}
