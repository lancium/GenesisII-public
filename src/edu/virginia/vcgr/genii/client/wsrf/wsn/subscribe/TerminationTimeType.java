package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe;

import java.io.Serializable;
import java.util.Calendar;

import org.oasis_open.wsn.base.AbsoluteOrRelativeTimeType;

import edu.virginia.vcgr.genii.client.utils.units.Duration;

public abstract class TerminationTimeType implements Serializable
{
	static final long serialVersionUID = 0L;
	
	abstract AbsoluteOrRelativeTimeType toAxisType(); 
	public abstract Calendar terminationTime();
	
	static private class AbsoluteTermintationTimeType extends TerminationTimeType
	{
		static final long serialVersionUID = 0L;
		
		private Calendar _terminationTime;
		
		private AbsoluteTermintationTimeType(Calendar termintationTime)
		{
			_terminationTime = termintationTime;
		}

		@Override
		final AbsoluteOrRelativeTimeType toAxisType()
		{
			return new AbsoluteOrRelativeTimeType(_terminationTime);
		}
		
		@Override
		final public Calendar terminationTime()
		{
			return _terminationTime;
		}
	}
	
	static private class DurationTerminationTimeType extends TerminationTimeType
	{
		static final long serialVersionUID = 0L;
		
		private Duration _duration;
		
		private DurationTerminationTimeType(Duration duration)
		{
			_duration = duration;
		}

		@Override
		final AbsoluteOrRelativeTimeType toAxisType()
		{
			return new AbsoluteOrRelativeTimeType(_duration.toApacheDuration());
		}
		
		@Override
		final public Calendar terminationTime()
		{
			return _duration.getTime();
		}
	}
	
	static public TerminationTimeType newInstance(Calendar absoluteTime)
	{
		return new AbsoluteTermintationTimeType(absoluteTime);
	}
	
	static public TerminationTimeType newInstance(Duration relativeDuration)
	{
		return new DurationTerminationTimeType(relativeDuration);
	}
	
	static public TerminationTimeType newInstance(
		AbsoluteOrRelativeTimeType termTime)
	{
		if (termTime == null)
			return null;
		
		org.apache.axis.types.Duration duration = termTime.getDurationValue();
		if (duration != null)
			return newInstance(Duration.fromApacheDuration(duration));
		else
		{
			Calendar c = termTime.getDateTimeValue();
			if (c != null)
				return newInstance(c);
		}
		
		return null;
	}
}