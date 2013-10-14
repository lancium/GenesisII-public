package edu.virginia.vcgr.genii.client.history;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.WSName;

public class WSNamingHistoryEventSource extends AbstractHistoryEventSource
{
	static final long serialVersionUID = 0L;

	private WSName _name;

	public WSNamingHistoryEventSource(WSName name, HistoryEventSource knownTo, HistoryEventSource aka)
	{
		super(knownTo, aka);

		if (name == null)
			throw new IllegalArgumentException("Name cannot be null.");

		_name = name;
	}

	public WSNamingHistoryEventSource(WSName name, HistoryEventSource knownTo)
	{
		this(name, knownTo, null);
	}

	public WSNamingHistoryEventSource(EndpointReferenceType epr, HistoryEventSource knownTo, HistoryEventSource aka)
	{
		this(new WSName(epr), knownTo, aka);
	}

	public WSNamingHistoryEventSource(EndpointReferenceType epr, HistoryEventSource knownTo)
	{
		this(new WSName(epr), knownTo);
	}

	final public WSName name()
	{
		return _name;
	}

	@Override
	public String toString()
	{
		return _name.toString();
	}

	@Override
	public Object identity()
	{
		return _name;
	}
}