package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.ArrayList;
import java.util.Collection;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.ITool;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.security.axis.AclAuthZClientTool;
import edu.virginia.vcgr.genii.client.security.axis.AxisAcl;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.security.acl.Acl;
import edu.virginia.vcgr.genii.security.acl.AclEntry;
import edu.virginia.vcgr.genii.security.acl.X509PatternAclEntry;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public class ChmodTool extends BaseGridTool {
	static final private String _DESCRIPTION = "config/tooldocs/description/dchmod";
	static final private String _USAGE = "config/tooldocs/usage/uchmod";
	static final private String _MANPAGE = "config/tooldocs/man/chmod";

	static private Log _logger = LogFactory.getLog(ChmodTool.class);

	private String _username;
	private String _password;
	private String _hashedpass;
	private boolean _everyone;
	private String _pattern;
	private boolean _recursive;

	public ChmodTool() {
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE),
				false, ToolCategory.SECURITY);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Option({ "username" })
	public void setUsername(String arg) {
		_username = arg;
	}

	@Option({ "password" })
	public void setPassword(String arg) {
		_password = arg;
	}

	@Option({ "hashedpass" })
	public void setHashedpass(String arg) {
		_hashedpass = arg;
	}

	@Option({ "everyone" })
	public void setEveryone() {
		_everyone = true;
	}

	@Option({ "pattern" })
	public void setPattern(String arg) {
		_pattern = arg;
	}

	@Option({ "recursive", "R" })
	public void setRecursive() {
		_recursive = true;
	}

	@Override
	protected void verify() throws ToolException {
		boolean haveUsername = (_username != null);
		boolean havePassword = (_password != null || _hashedpass != null);
		if (haveUsername != havePassword)
			throw new InvalidToolUsageException();
		if (_password != null && _hashedpass != null)
			throw new InvalidToolUsageException();
		int reqArgs = 2;
		if ((_username == null) && (!_everyone))
			reqArgs++;
		if (numArguments() != reqArgs)
			throw new InvalidToolUsageException();
	}

	@Override
	protected int runCommand() throws Throwable {
		boolean isRecursive = _recursive;
		String modeString = getArgument(1);
		String certificate = null;
		if ((_username == null) && (!_everyone))
			certificate = getArgument(2);

		/*
		 * Create an AclEntry that represents "everyone", or
		 * "username/password", or a certificate read from a local file or a
		 * grid file, or a certificate read from the metadata of a resource.
		 */
		AclEntry newEntry = null;
		if (_everyone) {
			newEntry = null;
		} else if (_username != null) {
			if (_password != null)
				newEntry = new UsernamePasswordIdentity(_username, _password,
						true);
			else
				newEntry = new UsernamePasswordIdentity(_username, _hashedpass,
						false);
		} else {
			X509Identity identity = AclAuthZClientTool
					.downloadIdentity(new GeniiPath(certificate));
			if (_pattern == null)
				newEntry = identity;
			else
				newEntry = new X509PatternAclEntry(identity, new X500Principal(
						_pattern));
		}

		Collection<GeniiPath.PathMixIn> paths = GeniiPath
				.pathExpander(getArgument(0));
		if (paths == null) {
			String msg = "Path does not exist or is not accessible: "
					+ getArgument(0);
			stdout.println(msg);
			_logger.warn(msg);
			return 1;
		}
		boolean anyOkay = false;
		for (GeniiPath.PathMixIn path : paths) {
			_logger.debug("calling chmod on path: " + path);
			if (path._rns == null) {
				String msg = "This is not an RNS path: " + path.toString();
				stdout.println(msg);
				_logger.warn(msg);
			} else {
				chmod(path._rns, modeString, newEntry, isRecursive);
				anyOkay = true;
			}
		}
		if (anyOkay)
			return 0;
		else
			return 1;
	}

	static private void chmod(RNSPath pathRNS, String modeString,
			AclEntry newEntry, boolean isRecursive) throws Throwable {
		TypeInformation currentType = new TypeInformation(pathRNS.getEndpoint());
		if (isRecursive && currentType.isRNS()) {
			/*
			 * only check that we can list a directory's contents when doing a
			 * recursive chmod.
			 */
			if (_logger.isDebugEnabled())
				_logger.debug("found RNS to traverse at " + pathRNS);

			Collection<RNSPath> entries = null;
			try {
				entries = pathRNS.listContents();
			} catch (Throwable e) {
				_logger.warn("failed to list contents on " + pathRNS
						+ ", error is: " + e.getMessage());
			}
			ArrayList<RNSPath> subdirs = new ArrayList<RNSPath>();

			if (entries != null) {
				for (RNSPath entry : entries) {
					TypeInformation type = new TypeInformation(
							entry.getEndpoint());
					if (type.isRNS()) {
						subdirs.add(entry);
					} else if (type.isByteIO()) {
						chmod(entry, modeString, newEntry, isRecursive);
					}
				}

				for (RNSPath entry : subdirs) {
					chmod(entry, modeString, newEntry, isRecursive);
				}
			}

		}

		GenesisIIBaseRP rp = (GenesisIIBaseRP) ResourcePropertyManager
				.createRPInterface(pathRNS.getEndpoint(), GenesisIIBaseRP.class);
		AuthZConfig config = rp.getAuthZConfig();
		if (config == null)
			config = AclAuthZClientTool.getEmptyAuthZConfig();

		// Modify the resource's current AuthZConfig.
		Acl acl = AxisAcl.decodeAcl(config);
		AxisAcl.chmod(acl, modeString, newEntry);
		config = AxisAcl.encodeAcl(acl);

		// upload new authz config to resource
		rp.setAuthZConfig(config);
	}

	@Override
	public void addArgument(String argument) throws ToolException {
		if (_setter == null)
			_setter = new OptionSetter(this);
		Class<? extends ITool> toolClass = getClass();
		if (argument.startsWith("--"))
			handleLongOptionFlag(toolClass, argument.substring(2));
		else if (argument.startsWith("-")) {
			if (argument.charAt(1) == 'r' || argument.charAt(1) == 'w'
					|| argument.charAt(1) == 'x') {
				_arguments.add(argument);
			} else {
				handleShortOptionFlag(toolClass, argument.substring(1));
			}
		} else
			_arguments.add(argument);
	}

}