package edu.virginia.vcgr.genii.client.history;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

public class HistoryEventData implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private Throwable _exception;
	private String _shortDescription;
	private String _longDescription;
	
	public HistoryEventData(String shortDescription, Throwable cause)
	{
		_shortDescription = shortDescription;
		_longDescription = null;
		_exception = cause;
	}
	
	public HistoryEventData(String shortDescription)
	{
		this(shortDescription, null);
	}
		
	final public Throwable eventException()
	{
		return _exception;
	}
	
	final public void longDescription(String longDescription)
	{
		_longDescription = longDescription;
	}
	
	final public String longDescription()
	{
		return _longDescription;
	}
	
	@Override
	final public String toString()
	{
		return _shortDescription;
	}
	
	final public String details()
	{
		StringWriter writer = new StringWriter();
		PrintWriter  pw = new PrintWriter(writer);
		
		String longDesc = longDescription();
		
		if (longDesc != null)
			pw.println(longDescription());
		
		if (_exception != null)
		{
			if (longDesc != null)
				pw.println();
			
			pw.println("Exception:");
			_exception.printStackTrace(pw);
		}
		
		pw.close();
		return writer.toString();
	}
}