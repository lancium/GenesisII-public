package edu.virginia.vcgr.genii.container.invoker.timing;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.container.context.WorkingContext;

public class TimingSink
{
	static private Log _logger = LogFactory.getLog(TimingSink.class);

	static final private String CONTEXT_PROPERTY_NAME = "edu.virginia.vcgr.genii.container.invoker.timing.sink";

	static TimingSink createTimingSink(Class<?> serviceClass, Method targetMethod)
	{
		TimingSink sink = new TimingSink(serviceClass, targetMethod);

		try {
			WorkingContext context = WorkingContext.getCurrentWorkingContext();
			context.setProperty(CONTEXT_PROPERTY_NAME, sink);
		} catch (Throwable cause) {
			_logger.warn("Unable to set timing sink for working context.", cause);
		}

		return sink;
	}

	static public TimingSink sink()
	{
		TimingSink sink = null;

		try {
			WorkingContext context = WorkingContext.getCurrentWorkingContext();
			sink = (TimingSink) context.getProperty(CONTEXT_PROPERTY_NAME);
		} catch (Throwable cause) {
			_logger.warn("Unable to get timing sink for working context.", cause);
		}

		if (sink == null)
			sink = new TimingSink(TimingSink.class, null);

		return sink;
	}

	static private TimingLogger _timingLogger = new TimingLogger("timing.txt");

	private Class<?> _serviceClass;
	private Method _targetMethod;
	private Map<String, List<Long>> _events = new HashMap<String, List<Long>>();

	void log()
	{
		_timingLogger.log(_serviceClass, _targetMethod, _events);
	}

	private TimingSink(Class<?> serviceClass, Method targetMethod)
	{
		_serviceClass = serviceClass;
		_targetMethod = targetMethod;
	}

	final public Timer getTimer(String eventName)
	{
		List<Long> timeList;

		synchronized (_events) {
			timeList = _events.get(eventName);
			if (timeList == null)
				_events.put(eventName, timeList = new LinkedList<Long>());
		}

		TimerImpl ret = new TimerImpl(timeList);
		ret.start();
		return ret;
	}

	private class TimerImpl implements Timer
	{
		private List<Long> _timeList;
		private long _start = 0L;

		private TimerImpl(List<Long> timeList)
		{
			_timeList = timeList;
		}

		private void start()
		{
			_start = System.currentTimeMillis();
		}

		@Override
		public void noteTime()
		{
			long now = System.currentTimeMillis();
			synchronized (_timeList) {
				_timeList.add(new Long(now - _start));
			}
		}
	}
}
