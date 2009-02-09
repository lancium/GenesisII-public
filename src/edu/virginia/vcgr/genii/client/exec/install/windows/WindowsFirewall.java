package edu.virginia.vcgr.genii.client.exec.install.windows;

import edu.virginia.vcgr.genii.client.exec.AbstractExecutionTask;
import edu.virginia.vcgr.genii.client.exec.ExecutionTask;

public class WindowsFirewall
{
	static public enum FirewallPortTypes
	{
		TCP(),
		UDP(),
		ALL()
	}
	
	static private class OpenPortTask extends AbstractExecutionTask
	{
		private int _port;
		private String _name;
		private FirewallPortTypes _portType;
		
		private OpenPortTask(String name, int port, FirewallPortTypes portType)
		{
			_port = port;
			_name = name;
			_portType = portType;
		}

		@Override
		public String[] getCommandLine()
		{
			return new String[] {
				"netsh", "firewall", "add", "portopening",
				String.format("protocol=%s", _portType),
				String.format("name=%s", _name),
				String.format("port=%d", _port)
			};
		}
		
		@Override
		public String toString()
		{
			return String.format("Open Firewall Port %s(%s)",
				_portType, _port);
		}
	}
	
	static private class ClosePortTask extends AbstractExecutionTask
	{
		private int _port;
		private FirewallPortTypes _portType;
		
		private ClosePortTask(int port, FirewallPortTypes portType)
		{
			_port = port;
			_portType = portType;
		}
		
		@Override
		public String[] getCommandLine()
		{
			return new String[] {
				"netsh", "firewall", "delete", "portopening",
				String.format("protocol=%s", _portType),
				String.format("port=%d", _port)
			};
		}
		
		@Override
		public String toString()
		{
			return String.format("Close Firewall Port %s(%s)",
				_portType, _port);
		}
	}
	
	static public ExecutionTask createOpenPortTask(String name, int port, 
		FirewallPortTypes portType)
	{
		return new OpenPortTask(name, port, portType);
	}
	
	static public ExecutionTask createClosePortTask(int port,
		FirewallPortTypes portType)
	{
		return new ClosePortTask(port, portType);
	}
}