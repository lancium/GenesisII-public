package edu.virginia.vcgr.genii.container.cservices.history;

import java.io.Serializable;
import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.history.SequenceNumber;

public interface HistoryEventToken extends Serializable
{
	public SequenceNumber retrieve() throws SQLException;
}