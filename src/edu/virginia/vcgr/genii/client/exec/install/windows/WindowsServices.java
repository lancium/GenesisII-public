package edu.virginia.vcgr.genii.client.exec.install.windows;

import edu.virginia.vcgr.genii.client.exec.AbstractExecutionTask;
import edu.virginia.vcgr.genii.client.exec.ExecutionTask;

public class WindowsServices
{
	static private class StartServiceTask extends AbstractExecutionTask
	{
		private String _name;
		
		private StartServiceTask(String name)
		{
			_name = name;
		}
		
		@Override
		public String[] getCommandLine()
		{
			return new String[] {
				"net", "start", _name
			};
		}
		
		@Override
		public String toString()
		{
			return String.format("Start Windows Service \"%s\"",
				_name);
		}
	}
	
	static private class StopServiceTask extends AbstractExecutionTask
	{
		private String _name;
		
		private StopServiceTask(String name)
		{
			_name = name;
		}
		
		@Override
		public String[] getCommandLine()
		{
			return new String[] {
				"net", "stop", _name
			};
		}
		
		@Override
		public String toString()
		{
			return String.format("Stop Windows Service \"%s\"",
				_name);
		}
	}
	
	static public ExecutionTask createStartServiceTask(String serviceName)
	{
		return new StartServiceTask(serviceName);
	}
	
	static public ExecutionTask createStopServiceTask(String serviceName)
	{
		return new StopServiceTask(serviceName);
	}
}