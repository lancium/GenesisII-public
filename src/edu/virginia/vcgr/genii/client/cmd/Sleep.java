package edu.virginia.vcgr.genii.client.cmd;

public class Sleep
{
	static public void main(String []args) throws Throwable
	{
		if (args.length > 0)
		{
			Integer sleepSecs = new Integer(args[0]);
			Thread.sleep(sleepSecs.longValue()*1000L);
		}
	}
}