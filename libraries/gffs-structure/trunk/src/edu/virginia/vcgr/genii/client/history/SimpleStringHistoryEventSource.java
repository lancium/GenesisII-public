package edu.virginia.vcgr.genii.client.history;

public class SimpleStringHistoryEventSource extends AbstractHistoryEventSource
{
	static final long serialVersionUID = 0L;

	private String _description;

	public SimpleStringHistoryEventSource(String description, HistoryEventSource knownTo, HistoryEventSource aka)
	{
		super(knownTo, aka);

		_description = description;
	}

	public SimpleStringHistoryEventSource(String description, HistoryEventSource knownTo)
	{
		this(description, knownTo, null);
	}

	@Override
	public String toString()
	{
		return _description;
	}

	@Override
	public Object identity()
	{
		HistoryEventSource aka = alsoKnownAs();
		if (aka != null)
			return aka.identity();

		return null;
	}
}