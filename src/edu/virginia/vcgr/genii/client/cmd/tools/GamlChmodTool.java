package edu.virginia.vcgr.genii.client.cmd.tools;

import javax.security.auth.x500.X500Principal;

import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.security.authz.acl.Acl;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclAuthZClientTool;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclEntry;
import edu.virginia.vcgr.genii.client.security.authz.acl.X509PatternAclEntry;
import edu.virginia.vcgr.genii.client.gpath.*;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.security.credentials.identity.X509Identity;

public class GamlChmodTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dchmod";
	static final private String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uchmod";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/chmod";

	private String _username;
	private String _password;
	private boolean _everyone;
	private String _pattern;
	
	public GamlChmodTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), 
				false, ToolCategory.SECURITY);
		addManPage(new FileResource(_MANPAGE));
	}
	
	@Option({"username"})
	public void setUsername(String arg)
	{
		_username = arg;
	}

	@Option({"password"})
	public void setPassword(String arg)
	{
		_password = arg;
	}

	@Option({"everyone"})
	public void setEveryone()
	{
		_everyone = true;
	}
	
	@Option({"pattern"})
	public void setPattern(String arg)
	{
		_pattern = arg;
	}
	
	@Override
	protected void verify() throws ToolException
	{
		if (((_username != null) && (_password == null)) ||
			((_username == null) && (_password != null)))
			throw new InvalidToolUsageException();
		int reqArgs = 2;
		if ((_username == null) && (!_everyone))
			reqArgs++;
		if (numArguments() != reqArgs)
			throw new InvalidToolUsageException();
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		String path = getArgument(0);
		String modeString = getArgument(1);
		String certificate = null;
		if ((_username == null) && (!_everyone))
			certificate = getArgument(2);
		
		// Create an AclEntry that represents "everyone", or "username/password",
		// or a certificate read from a local file or a grid file,
		// or a certificate read from the metadata of a resource.
		AclEntry newEntry = null;
		if (_everyone)
		{
			newEntry = null;
		}
		else if (_username != null) 
		{
			newEntry = new UsernamePasswordIdentity(_username, _password);
		}
		else
		{
			X509Identity identity = AclAuthZClientTool.downloadIdentity(
					new GeniiPath(certificate));
			if (_pattern == null)
				newEntry = identity;
			else
				newEntry = new X509PatternAclEntry(identity, new X500Principal(_pattern));
		}
		
		// Get the resource's current AuthZConfig.
		RNSPath current = RNSPath.getCurrent();
		RNSPath pathRNS = current.lookup(path, RNSPathQueryFlags.MUST_EXIST);
		GenesisIIBaseRP rp = (GenesisIIBaseRP) ResourcePropertyManager.createRPInterface(
				pathRNS.getEndpoint(), GenesisIIBaseRP.class);
		AuthZConfig config = rp.getAuthZConfig();
		if (config == null)
			config = AclAuthZClientTool.getEmptyAuthZConfig();
		
		// Modify the resource's current AuthZConfig.
		Acl acl = Acl.decodeAcl(config);
		acl.chmod(modeString, newEntry);
		config = Acl.encodeAcl(acl);

		// upload new authz config to resource
		rp.setAuthZConfig(config);
		return 0;
	}
}