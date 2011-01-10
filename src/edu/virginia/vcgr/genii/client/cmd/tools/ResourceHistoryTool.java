package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.iterator.WSIterable;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.HistoryEventBundleType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsRequestType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType;

public class ResourceHistoryTool extends BaseGridTool
{
	static final private String DESCRIPTION =
		"Retrieves the history event list for q given resource.";
	static final private String USAGE =
		"resource-history --dump=<path> <resource-path> [resource-hint]";
	
	private GeniiPath _store = null;
	
	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1 || numArguments() > 2)
			throw new InvalidToolUsageException(
				"Incorrect number of arguments.");
		
		GeniiPath path = new GeniiPath(getArgument(0));
		if (path.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException(
				String.format("Path %s does not refer to a grid resource.",
					path));
	}

	@Override
	protected int runCommand() throws Throwable
	{
		OutputStream out = null;
		List<HistoryEvent> events = new LinkedList<HistoryEvent>();
		
		RNSPath path = RNSPath.getCurrent().lookup(getArgument(0));
		GeniiCommon common = ClientUtils.createProxy(
			GeniiCommon.class, path.getEndpoint());
		
		IterateHistoryEventsRequestType req;
		
		if (numArguments() == 2)
			req = new IterateHistoryEventsRequestType(getArgument(1));
		else
			req = new IterateHistoryEventsRequestType();
		
		IterateHistoryEventsResponseType resp = 
			common.iterateHistoryEvents(req);
		
		WSIterable<HistoryEventBundleType> iter = null;
		
		try
		{
			iter = new WSIterable<HistoryEventBundleType>(
				HistoryEventBundleType.class, resp.getResult(), 25, true);
			for (HistoryEventBundleType bundle : iter)
				events.add(
					(HistoryEvent)DBSerializer.deserialize(bundle.getData()));
		}
		finally
		{
			StreamUtils.close(iter);
		}
		
		Collections.sort(events, HistoryEvent.SEQUENCE_NUMBER_COMPARATOR);
		
		try
		{
			if (_store != null)
			{
				out = _store.openOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(out);
				oos.writeInt(events.size());
				for (HistoryEvent event : events)
					oos.writeObject(event);
				oos.close();
			} else
				for (HistoryEvent event : events)
					stdout.println(event);
		}
		finally
		{
			StreamUtils.close(out);
		}
		
		return 0;
	}

	public ResourceHistoryTool()
	{
		super(DESCRIPTION, USAGE, false);
	}
	
	@Option("dump")
	public void store(String path)
	{
		_store = new GeniiPath(path);
	}
}
