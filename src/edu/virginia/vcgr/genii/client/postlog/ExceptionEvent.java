package edu.virginia.vcgr.genii.client.postlog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import edu.virginia.vcgr.genii.client.cmd.GetHostName;

public class ExceptionEvent implements PostEvent
{
	static private final String MESSAGE_HEADER = "message";
	static private final String MACHINE_HEADER = "machine";
	static private final String EXCEPTION_HEADER = "exception";
	
	private String _message;
	private Throwable _cause;
	private String _causeString = null;
	private String _machineName = null;
	
	public ExceptionEvent(String message, Throwable cause)
	{
		_message = message;
		_cause = cause;
		
		if (_message == null)
			_message = "No message details available.";
		if (_cause == null)
			_causeString = "No exception details available.";
	}
	
	synchronized private String getCause()
	{
		if (_causeString == null)
		{
			StringWriter sWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(sWriter);
			_cause.printStackTrace(writer);
			writer.flush();
			_causeString = sWriter.toString();
		}
		
		return _causeString;
	}
	
	synchronized private String getHostName()
	{
		if (_machineName == null)
		{
			try
			{
				_machineName = GetHostName.getHostName();
			}
			catch (Throwable cause)
			{
				_machineName = "Unknown";
			}
			
			if (_machineName == null)
				_machineName = "Unknown";
		}
		
		return _machineName;
	}
	
	@Override
	public Map<String, String> content()
	{
		Map<String, String> ret = new HashMap<String, String>(3);
				
		ret.put(MACHINE_HEADER, getHostName());
		ret.put(MESSAGE_HEADER, _message);
		ret.put(EXCEPTION_HEADER, getCause());
		
		return ret;
	}
}