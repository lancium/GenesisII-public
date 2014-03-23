package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.rp_2.SetResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.UpdateType;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class SetResourcePropertiesTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dset-resource-properties";
	static final private String _USAGE = "config/tooldocs/usage/uset-resource-properties";
	static final private LoadFileResource _MANPAGE = new LoadFileResource("config/tooldocs/man/set-resource-properties");

	public SetResourcePropertiesTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.GENERAL);
		addManPage(_MANPAGE);
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException
	{
		GeniiPath target = new GeniiPath(getArgument(0));
		GeniiPath rpFile = new GeniiPath(getArgument(1));

		if (target.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException(String.format("Target (%s) must be a grid path!", target));

		InputStream in = null;
		try {
			in = rpFile.openInputStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new ToolException("parsing error: " + e.getLocalizedMessage(), e);
			}
			MessageElement me;
			try {
				me = new MessageElement(builder.parse(in).getDocumentElement());
			} catch (SAXException e) {
				throw new ToolException("parsing error: " + e.getLocalizedMessage(), e);
			}
			Collection<MessageElement> properties = new LinkedList<MessageElement>();
			Iterator<?> iter = me.getChildElements();
			while (iter.hasNext()) {
				MessageElement obj = (MessageElement)iter.next();
				me = (MessageElement) obj;
				properties.add(me);
			}

			RNSPath targetPath = RNSPath.getCurrent().lookup(target.path(), RNSPathQueryFlags.MUST_EXIST);
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, targetPath.getEndpoint());
			common.setResourceProperties(new SetResourceProperties(null, new UpdateType(properties
				.toArray(new MessageElement[properties.size()])), null));
			return 0;
		} finally {
			org.morgan.util.io.StreamUtils.close(in);
		}
	}
}