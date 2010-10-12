package edu.virginia.vcgr.genii.container.cservices.history;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.morgan.util.Pair;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.client.history.HistoryEventData;

abstract class AbstractHistoryContext implements HistoryContext
{
	private Map<String, String> _properties;
	private HistoryEventCategory _category;
	
	protected AbstractHistoryContext(Map<String, String> properties,
		HistoryEventCategory category)
	{
		if (properties == null)
			properties = new HashMap<String, String>();
		if (category == null)
			category = HistoryEventCategory.Default;
		
		_properties = new HashMap<String, String>(properties);
		_category = category;
	}
	
	final private class HistoryContextWriter extends HistoryEventWriter
	{
		private HistoryEventData _data;
		private HistoryEventLevel _level;
		private HistoryEventToken _token = null;
		
		private HistoryContextWriter(HistoryEventLevel level,
			HistoryEventData data)
		{
			super(new StringWriter());
			
			_data = data;
			_level = level;
		}
		
		@Override
		synchronized final public void close()
		{
			StringWriter writer = (StringWriter)out;
			
			super.close();
			
			if (_data != null)
			{
				StreamUtils.close(writer);
				_data.longDescription(writer.toString());
				
				_token = logEvent(_level, _data);
				
				_data = null;
			}
		}
		
		@Override
		final public HistoryEventToken getToken()
		{
			return _token;
		}
	}
	
	@Override
	final public HistoryEventToken trace(Throwable cause,
		String shortDescriptionFormat, Object... formatArguments)
	{
		return logEvent(HistoryEventLevel.Trace, new HistoryEventData(
			String.format(shortDescriptionFormat, formatArguments),
			cause));
	}

	@Override
	final public HistoryEventToken debug(Throwable cause,
		String shortDescriptionFormat, Object... formatArguments)
	{
		return logEvent(HistoryEventLevel.Debug, new HistoryEventData(
			String.format(shortDescriptionFormat, formatArguments),
			cause));
	}

	@Override
	final public HistoryEventToken info(Throwable cause,
		String shortDescriptionFormat, Object... formatArguments)
	{
		return logEvent(HistoryEventLevel.Information, new HistoryEventData(
			String.format(shortDescriptionFormat, formatArguments),
			cause));
	}

	@Override
	final public HistoryEventToken warn(Throwable cause,
		String shortDescriptionFormat, Object... formatArguments)
	{
		return logEvent(HistoryEventLevel.Warning, new HistoryEventData(
			String.format(shortDescriptionFormat, formatArguments),
			cause));
	}

	@Override
	final public HistoryEventToken error(Throwable cause, 
		String shortDescriptionFormat, Object... formatArguments)
	{
		return logEvent(HistoryEventLevel.Error, new HistoryEventData(
			String.format(shortDescriptionFormat, formatArguments),
			cause));
	}

	@Override
	final public HistoryEventToken trace(
		String shortDescriptionFormat, Object... formatArguments)
	{
		return logEvent(HistoryEventLevel.Trace, new HistoryEventData(
			String.format(shortDescriptionFormat, formatArguments)));
	}

	@Override
	final public HistoryEventToken debug(
		String shortDescriptionFormat, Object... formatArguments)
	{
		return logEvent(HistoryEventLevel.Debug, new HistoryEventData(
			String.format(shortDescriptionFormat, formatArguments)));
	}

	@Override
	final public HistoryEventToken info(
		String shortDescriptionFormat, Object... formatArguments)
	{
		return logEvent(HistoryEventLevel.Information, new HistoryEventData(
			String.format(shortDescriptionFormat, formatArguments)));
	}

	@Override
	final public HistoryEventToken warn(
		String shortDescriptionFormat, Object... formatArguments)
	{
		return logEvent(HistoryEventLevel.Warning, new HistoryEventData(
			String.format(shortDescriptionFormat, formatArguments)));
	}

	@Override
	final public HistoryEventToken error(
		String shortDescriptionFormat, Object... formatArguments)
	{
		return logEvent(HistoryEventLevel.Error, new HistoryEventData(
			String.format(shortDescriptionFormat, formatArguments)));
	}

	@Override
	final public HistoryEventWriter createTraceWriter(Throwable cause,
		String shortDescriptionFormat, Object... formatArguments)
	{
		return new HistoryContextWriter(HistoryEventLevel.Trace,
			new HistoryEventData(
				String.format(shortDescriptionFormat, formatArguments), 
				cause));
	}

	@Override
	final public HistoryEventWriter createDebugWriter(Throwable cause,
		String shortDescriptionFormat, Object... formatArguments)
	{
		return new HistoryContextWriter(HistoryEventLevel.Debug,
			new HistoryEventData(
				String.format(shortDescriptionFormat, formatArguments), 
				cause));
	}

	@Override
	final public HistoryEventWriter createInfoWriter(Throwable cause,
		String shortDescriptionFormat, Object... formatArguments)
	{
		return new HistoryContextWriter(HistoryEventLevel.Information,
			new HistoryEventData(
				String.format(shortDescriptionFormat, formatArguments), 
				cause));
	}

	@Override
	final public HistoryEventWriter createWarnWriter(Throwable cause,
		String shortDescriptionFormat, Object... formatArguments)
	{
		return new HistoryContextWriter(HistoryEventLevel.Warning,
			new HistoryEventData(
				String.format(shortDescriptionFormat, formatArguments), 
				cause));
	}

	@Override
	final public HistoryEventWriter createErrorWriter(Throwable cause,
		String shortDescriptionFormat, Object... formatArguments)
	{
		return new HistoryContextWriter(HistoryEventLevel.Error,
			new HistoryEventData(
				String.format(shortDescriptionFormat, formatArguments), 
				cause));
	}

	@Override
	final public HistoryEventWriter createTraceWriter(String shortDescriptionFormat,
		Object... formatArguments)
	{
		return new HistoryContextWriter(HistoryEventLevel.Trace,
			new HistoryEventData(
				String.format(shortDescriptionFormat, formatArguments)));
	}

	@Override
	final public HistoryEventWriter createDebugWriter(String shortDescriptionFormat,
		Object... formatArguments)
	{
		return new HistoryContextWriter(HistoryEventLevel.Debug,
			new HistoryEventData(
				String.format(shortDescriptionFormat, formatArguments)));
	}

	@Override
	final public HistoryEventWriter createInfoWriter(String shortDescriptionFormat,
		Object... formatArguments)
	{
		return new HistoryContextWriter(HistoryEventLevel.Information,
			new HistoryEventData(
				String.format(shortDescriptionFormat, formatArguments)));
	}

	@Override
	final public HistoryEventWriter createWarnWriter(String shortDescriptionFormat,
		Object... formatArguments)
	{
		return new HistoryContextWriter(HistoryEventLevel.Warning,
			new HistoryEventData(
				String.format(shortDescriptionFormat, formatArguments)));
	}

	@Override
	final public HistoryEventWriter createErrorWriter(String shortDescriptionFormat,
		Object... formatArguments)
	{
		return new HistoryContextWriter(HistoryEventLevel.Error,
			new HistoryEventData(
				String.format(shortDescriptionFormat, formatArguments)));
	}
	
	@Override
	final public void category(HistoryEventCategory category)
	{
		_category = category;
	}
	
	@Override
	final public HistoryEventCategory category()
	{
		return _category;
	}

	final public Map<String, String> properties()
	{
		synchronized(_properties)
		{
			return new HashMap<String, String>(_properties);
		}
	}
	@Override
	final public void setProperty(String propertyName, String propertyValue)
	{
		synchronized(_properties)
		{
			_properties.put(propertyName, propertyValue);
		}
	}

	@Override
	final public String removeProperty(String propertyName)
	{
		synchronized(_properties)
		{
			return _properties.remove(propertyName);
		}
	}
	
	@Override
	final public ICallingContext setContextProperties(ICallingContext ctxt)
	{
		Map<String, String> properties = properties();
		
		Collection<Serializable> list = new ArrayList<Serializable>(
			properties.size());
		for (Map.Entry<String, String> entry : properties.entrySet())
		{
			list.add(new Pair<String, String>(
				entry.getKey(), entry.getValue()));
		}
			
		ctxt = ctxt.deriveNewContext();
		ctxt.setProperty(HistoryContextFactory.CALLING_CONTEXT_PROPERTIES_KEY,
			list);
		return ctxt;
	}
	
	@Override
	public abstract Object clone();
}