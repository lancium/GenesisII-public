package edu.virginia.vcgr.genii.container.cservices.history;

import java.io.PrintWriter;
import java.io.Writer;

public abstract class HistoryEventWriter extends PrintWriter
{
	protected HistoryEventWriter(Writer writer)
	{
		super(writer);
	}
	
	public abstract HistoryEventToken getToken();
}