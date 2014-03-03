package edu.virginia.vcgr.genii.container.cservices.history;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.history.SequenceNumber;

class VerbatimHistoryEventToken implements HistoryEventToken
{
	static final long serialVersionUID = 0L;

	private SequenceNumber _number;

	public VerbatimHistoryEventToken(SequenceNumber number)
	{
		_number = number;
	}

	@Override
	final public SequenceNumber retrieve() throws SQLException
	{
		return _number;
	}
}
