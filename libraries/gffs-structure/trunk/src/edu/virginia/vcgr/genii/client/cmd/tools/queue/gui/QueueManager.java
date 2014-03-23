package edu.virginia.vcgr.genii.client.cmd.tools.queue.gui;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSMetadataType;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.cmd.tools.ToolCategory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.queue.CurrentResourceInformation;
import edu.virginia.vcgr.genii.client.queue.QueueConstants;
import edu.virginia.vcgr.genii.client.rfork.ResourceForkUtils;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSIterable;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;

public class QueueManager extends BaseGridTool
{
	static private final String _DESCRIPTION = "config/tooldocs/description/dqmgr";
	static private final String _USAGE = "config/tooldocs/usage/uqmgr";
	static final private String _MANPAGE = "config/tooldocs/man/qmgr";

	public QueueManager()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), true, ToolCategory.MISC);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException
	{
		stderr.println("This tool has been deprecated.  To access the queue manager GUI, please use the client-ui tool.");

		if (stderr != null)
			return 0;

		GeniiPath path = new GeniiPath(getArgument(0));
		EndpointReferenceType epr = RNSPath.getCurrent().lookup(path.path()).getEndpoint();
		epr = ResourceForkUtils.stripResourceForkInformation(epr);

		RNSPath tmp = new RNSPath(epr);
		RNSPath resources = tmp.lookup("resources");

		JAXBContext context;
		try {
			context = JAXBContext.newInstance(CurrentResourceInformation.class);
		} catch (JAXBException e) {
			throw new IOException("JAXB error: " + e.getLocalizedMessage(), e);
		}
		Unmarshaller u;
		try {
			u = context.createUnmarshaller();
		} catch (JAXBException e) {
			throw new IOException("JAXB error: " + e.getLocalizedMessage(), e);
		}

		EnhancedRNSPortType rns = ClientUtils.createProxy(EnhancedRNSPortType.class, resources.getEndpoint());
		RNSIterable iterable = new RNSIterable(rns.lookup(null), null, RNSConstants.PREFERRED_BATCH_SIZE);
		for (RNSEntryResponseType entry : iterable) {
			RNSMetadataType mdt = entry.getMetadata();
			MessageElement[] any = (mdt == null) ? null : mdt.get_any();
			if (any != null) {
				for (MessageElement e : any) {
					QName name = e.getQName();
					if (name.equals(QueueConstants.CURRENT_RESOURCE_INFORMATION_QNAME)) {
						CurrentResourceInformation cri;
						try {
							cri = u.unmarshal(e, CurrentResourceInformation.class).getValue();
						} catch (JAXBException e2) {
							throw new IOException("JAXB error: " + e2.getLocalizedMessage(), e2);
						}
						stdout.format("%s:  %s\n", entry.getEntryName(), cri);
						break;
					}
				}
			} else {
				stdout.format("%s\n", entry.getEntryName());
			}
		}

		StreamUtils.close(iterable.getIterable());
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException("Command must contain the path to a grid queue.");
	}
}
