package edu.virginia.vcgr.genii.client.history;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.morgan.util.io.StreamUtils;

public class HistoryEvent implements Serializable
{
	static final long serialVersionUID = 0l;
	
	static public Comparator<HistoryEvent> SEQUENCE_NUMBER_COMPARATOR =
		new Comparator<HistoryEvent>()
		{
			@Override
			final public int compare(HistoryEvent o1, HistoryEvent o2)
			{
				return o1._eventNumber.compareTo(o2.eventNumber());
			}
		};
	

	static private String formatSources(HistoryEventSource source)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(source);
		HistoryEventSource knownTo = source.knownTo();
		if (knownTo != null)
			builder.append(String.format(" (knownTo: %s)",
				formatSources(knownTo)));
		HistoryEventSource aka = source.alsoKnownAs();
		if (aka != null)
			builder.append(String.format(" <- %s",
				formatSources(aka)));
		
		return builder.toString();
	}
	
	static private void printDataDetails(PrintWriter writer, String prefix,
		HistoryEventData data)
	{
		String desc = data.longDescription();
		if (desc != null)
			writer.format("%s%s\n", prefix, desc);
		Throwable cause = data.eventException();
		if (cause != null)
		{
			writer.format("%s%s\n", prefix, cause);
			for (StackTraceElement e : cause.getStackTrace())
			{
				writer.format("%s%s\n", prefix, e);
			}
		}
	}
	
	private Calendar _eventTimestamp;
	private SequenceNumber _eventNumber;
	private HistoryEventSource _eventSource;
	private HistoryEventLevel _eventLevel;
	private HistoryEventCategory _eventCategory;
	private Map<String, String> _eventProperties =
		new HashMap<String, String>();
	
	private HistoryEventData _eventData;

	public HistoryEvent(SequenceNumber eventNumber,
		Calendar eventTimestamp,
		HistoryEventSource eventSource, HistoryEventLevel eventLevel,
		HistoryEventCategory eventCategory,
		Map<String, String> eventProperties, HistoryEventData eventData)
	{		
		if (eventNumber == null)
			throw new IllegalArgumentException(
				"Event number cannot be null.");
				
		_eventTimestamp = eventTimestamp;
		_eventNumber = eventNumber;
		_eventSource = eventSource;
		_eventLevel = eventLevel;
		_eventCategory = eventCategory;
		_eventProperties.putAll(eventProperties);
		
		_eventData = eventData;
	}
	
	final public Calendar eventTimestamp()
	{
		return _eventTimestamp;
	}
	
	final public void wrapEventNumber(SequenceNumber parentNumber)
	{
		_eventNumber = _eventNumber.wrapWith(parentNumber);
	}
	
	final public SequenceNumber eventNumber()
	{
		return _eventNumber;
	}
	
	final public HistoryEventSource eventSource()
	{
		return _eventSource;
	}
	
	final public HistoryEventLevel eventLevel()
	{
		return _eventLevel;
	}
	
	final public HistoryEventCategory eventCategory()
	{
		return _eventCategory;
	}
	
	final public Map<String, String> eventProperties()
	{
		return Collections.unmodifiableMap(_eventProperties);
	}
	
	final public HistoryEventData eventData()
	{
		return _eventData;
	}
	
	@Override
	final public String toString()
	{
		StringWriter writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		
		pw.format("[%s, %s] Event %s @ %tc\n", _eventCategory, _eventLevel, 
			_eventNumber, _eventTimestamp);
		pw.format("\tSource:  %s\n", formatSources(_eventSource));
		pw.format("\tProperties:  %s\n", _eventProperties);
		pw.format("\tReason:  %s\n", _eventData);
		printDataDetails(pw, "\t\t", _eventData);
		pw.println();
		pw.close();
		StreamUtils.close(writer);
		return writer.toString();
	}
	
	final public String title()
	{
		return String.format("[%1$tF %1$tr]  %2$s",
			_eventTimestamp, _eventData);
	}
}
