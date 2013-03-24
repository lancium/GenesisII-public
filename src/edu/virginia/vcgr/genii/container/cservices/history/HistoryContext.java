package edu.virginia.vcgr.genii.container.cservices.history;

import java.util.Map;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.client.history.HistoryEventData;

public interface HistoryContext extends Cloneable
{
	public HistoryEventToken logEvent(HistoryEventLevel level, HistoryEventData data);

	public HistoryEventToken trace(Throwable cause, String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventToken debug(Throwable cause, String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventToken info(Throwable cause, String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventToken warn(Throwable cause, String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventToken error(Throwable cause, String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventToken trace(String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventToken debug(String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventToken info(String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventToken warn(String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventToken error(String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventWriter createTraceWriter(Throwable cause, String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventWriter createDebugWriter(Throwable cause, String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventWriter createInfoWriter(Throwable cause, String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventWriter createWarnWriter(Throwable cause, String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventWriter createErrorWriter(Throwable cause, String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventWriter createTraceWriter(String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventWriter createDebugWriter(String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventWriter createInfoWriter(String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventWriter createWarnWriter(String shortDescriptionFormat, Object... formatArguments);

	public HistoryEventWriter createErrorWriter(String shortDescriptionFormat, Object... formatArguments);

	public void category(HistoryEventCategory category);

	public HistoryEventCategory category();

	public Map<String, String> properties();

	public void setProperty(String propertyName, String propertyValue);

	public String removeProperty(String propertyName);

	public ICallingContext setContextProperties(ICallingContext ctxt);

	public Object clone();
}