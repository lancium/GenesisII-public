package edu.virginia.vcgr.genii.container.bes.activity;

public class NoSuchActivityFault extends Exception
{
	static final long serialVersionUID = 0L;
	
	private String _activity;
	
	public NoSuchActivityFault(String activity)
	{
		super(String.format("Activity \"%s\" cannot be found.\n", activity));
		_activity = activity;
	}
	
	public NoSuchActivityFault(String activity, Throwable cause)
	{
		super(String.format("Activity \"%s\" cannot be found.\n", 
			activity), cause);
		_activity = activity;
	}
	
	public String getActivityID()
	{
		return _activity;
	}
}