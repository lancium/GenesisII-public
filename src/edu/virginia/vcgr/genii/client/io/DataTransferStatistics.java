package edu.virginia.vcgr.genii.client.io;

import java.io.Serializable;
import java.util.Calendar;

public class DataTransferStatistics implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private long _bytesTransferred;
	private Calendar _transferStartTime;
	private Calendar _transferEndTime;
	
	private DataTransferStatistics()
	{
		_bytesTransferred = 0L;
		_transferStartTime = Calendar.getInstance();
	}
	
	final public DataTransferStatistics finishTransfer()
	{
		_transferEndTime = Calendar.getInstance();
		return this;
	}
	
	final public void transfer(long bytes)
	{
		_bytesTransferred += bytes;
	}
	
	final public long bytesTransferred()
	{
		return _bytesTransferred;
	}
	
	final public Calendar transferStartTime()
	{
		return _transferStartTime;
	}
	
	final public Calendar transferEndTime()
	{
		return _transferEndTime;
	}
	
	final public long transferTime()
	{
		return _transferEndTime.getTimeInMillis() -
			_transferStartTime.getTimeInMillis();
	}
	
	static public DataTransferStatistics startTransfer()
	{
		return new DataTransferStatistics();
	}
}
