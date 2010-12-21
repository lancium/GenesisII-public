package edu.virginia.vcgr.genii.client.cmd.tools.queue.gui;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.rns.EntryType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.iterator.WSIterable;
import edu.virginia.vcgr.genii.client.queue.CurrentResourceInformation;
import edu.virginia.vcgr.genii.client.queue.QueueConstants;
import edu.virginia.vcgr.genii.client.rfork.ResourceForkUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.enhancedrns.IterateListRequestType;
import edu.virginia.vcgr.genii.enhancedrns.IterateListResponseType;

public class QueueManager extends BaseGridTool
{
	static private final String _DESCRIPTION = 
		"Manage jobs in a queue from a graphical interface.";
	static private final String _USAGE =
		"qmgr <queue-path>";
	
	public QueueManager()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		stderr.println("This tool has been deprecated.  To access the queue manager GUI, please use the client-ui tool.");
		
		if (stderr != null)
			return 0;
		
		GeniiPath path = new GeniiPath(getArgument(0));
		EndpointReferenceType epr = RNSPath.getCurrent().lookup(
			path.path()).getEndpoint();
		epr = ResourceForkUtils.stripResourceForkInformation(epr);
		
		RNSPath tmp = new RNSPath(epr);
		RNSPath resources = tmp.lookup("resources");
		
		JAXBContext context = JAXBContext.newInstance(
			CurrentResourceInformation.class);
		Unmarshaller u = context.createUnmarshaller();
		
		EnhancedRNSPortType rns = ClientUtils.createProxy(
			EnhancedRNSPortType.class, resources.getEndpoint());
		IterateListResponseType resp = rns.iterateList(
			new IterateListRequestType());
		WSIterable<EntryType> iterable = new WSIterable<EntryType>(EntryType.class, resp.getResult(),
			100, true);
		for (EntryType entry : iterable)
		{
			MessageElement []any = entry.get_any();
			if (any != null)
			{
				for (MessageElement e : any)
				{
					QName name = e.getQName();
					if (name.equals(
						QueueConstants.CURRENT_RESOURCE_INFORMATION_QNAME))
					{
						CurrentResourceInformation cri = u.unmarshal(
							e, CurrentResourceInformation.class).getValue();
						stdout.format("%s:  %s\n", entry.getEntry_name(), cri);
						break;
					}
				}
			} else
			{
				stdout.format("%s\n", entry.getEntry_name());
			}
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException(
				"Command must contain the path to a grid queue.");
	}
}