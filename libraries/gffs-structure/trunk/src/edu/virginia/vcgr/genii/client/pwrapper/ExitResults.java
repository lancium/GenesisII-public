package edu.virginia.vcgr.genii.client.pwrapper;

import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class ExitResults
{
	private int _exitCode;
	private ElapsedTime _userTime;
	private ElapsedTime _kernelTime;
	private ElapsedTime _wallclockTime;
	private long _maximumRSS;
	private String _processorID;

	public ExitResults(int exitCode, long userTime, long kernelTime, long wallclockTime, long maxRSS, String processorID)
	{
		_exitCode = exitCode;
		_userTime = new ElapsedTime(userTime);
		_kernelTime = new ElapsedTime(kernelTime);
		_wallclockTime = new ElapsedTime(wallclockTime);
		_maximumRSS = maxRSS;
		_processorID = processorID;
	}

	final public int exitCode()
	{
		return _exitCode;
	}

	final public ElapsedTime userTime()
	{
		return _userTime;
	}

	final public ElapsedTime kernelTime()
	{
		return _kernelTime;
	}

	final public ElapsedTime wallclockTime()
	{
		return _wallclockTime;
	}

	final public long maximumRSS()
	{
		return _maximumRSS;
	}

	final public String processorID()
	{
		return _processorID;
	}

	@Override
	public String toString()
	{
		return String.format("Exit Code:  %d, User Time:  %d ms, " + "Kernel Time:  %d ms, Wallclock Time: %d ms, " + "Max RSS:  %d bytes, " + "Processor ID: %s\n",
			_exitCode, _userTime.as(TimeUnit.MICROSECONDS), _kernelTime.as(TimeUnit.MICROSECONDS), _wallclockTime.as(TimeUnit.MICROSECONDS),
			_maximumRSS, _processorID);
	}
}