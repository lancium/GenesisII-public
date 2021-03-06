package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;

public class GetBESFactoryAttributesTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dgetbesattributes";
	static final private String _USAGE = "config/tooldocs/usage/uget-bes-attributes";
	static final private String _MANPAGE = "config/tooldocs/man/get-bes-attributes";

	public GetBESFactoryAttributesTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.ADMINISTRATION);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if (gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("<target> must be a grid path. ");
		RNSPath path = lookup(gPath, RNSPathQueryFlags.MUST_EXIST);

		GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, path.getEndpoint());

		GetFactoryAttributesDocumentResponseType resp = bes.getFactoryAttributesDocument(new GetFactoryAttributesDocumentType());

		ObjectSerializer.serialize(stdout, resp, new QName("http://tempuri.org", "bes-factory-attributes"));
		stdout.flush();

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}
}