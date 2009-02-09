package edu.virginia.vcgr.genii.client.exec.install.windows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.virginia.vcgr.genii.client.exec.AbstractExecutionTask;

public class JavaServiceWrapper
{
	static public abstract class JavaServiceWrapperTask 
		extends AbstractExecutionTask
	{
		private File _wrapperExe;
		private File _configurationFile;
		
		protected Map<String, String> _variables;
		
		protected JavaServiceWrapperTask(
			File wrapperExe, File configurationFile) throws IOException
		{
			_wrapperExe = wrapperExe;
			_configurationFile = configurationFile;
			_variables = new HashMap<String, String>();
			
			CommonFunctions.checkPath(_wrapperExe, true);
			CommonFunctions.checkPath(_configurationFile, false);
		}
		
		protected void addWrapper(Collection<String> cLine)
		{
			cLine.add(_wrapperExe.getAbsolutePath());
		}
		
		protected void addConfigurationFile(Collection<String> cLine)
		{
			cLine.add(_configurationFile.getAbsolutePath());
		}
		
		protected void addVariables(Collection<String> cLine)
		{
			for (String key : _variables.keySet())
			{
				cLine.add(String.format(
					"set.%s=%s", key, _variables.get(key)));
			}
		}
		
		public void addVariable(String variableName, String value)
		{
			_variables.put(variableName, value);
		}
	}
	
	static private class InstallServiceWrapper extends JavaServiceWrapperTask
	{
		private String _account;
		private String _password;
		
		public InstallServiceWrapper(
			File wrapperExe, File configurationFile,
			String account, String password) throws IOException
		{
			super(wrapperExe, configurationFile);
			
			_account = account;
			_password = password;
		}
		
		@Override
		public String[] getCommandLine()
		{
			Collection<String> cLine = new ArrayList<String>();
			
			addWrapper(cLine);
			cLine.add("-i");
			addConfigurationFile(cLine);
			if (_password == null)
				cLine.add("wrapper.ntservice.password.prompt=TRUE");
			else
			{
				cLine.add("wrapper.ntservice.password.prompt=FALSE");
				cLine.add(String.format(
					"wrapper.ntservice.password=%s", _password));
			}
			
			cLine.add(String.format("wrapper.ntservice.account=%s", _account));
			
			addVariables(cLine);
			
			return cLine.toArray(new String[cLine.size()]);
		}
		
		@Override
		public String toString()
		{
			return "Install Java Service Wrapper";
		}
	}
	
	static private class UninstallServiceWrapper extends JavaServiceWrapperTask
	{
		public UninstallServiceWrapper(
			File wrapperExe, File configurationFile) throws IOException
		{
			super(wrapperExe, configurationFile);
		}
		
		@Override
		public String[] getCommandLine()
		{
			Collection<String> cLine = new ArrayList<String>();
			
			addWrapper(cLine);
			cLine.add("-r");
			addConfigurationFile(cLine);
			addVariables(cLine);
			
			return cLine.toArray(new String[cLine.size()]);
		}
		
		@Override
		public String toString()
		{
			return "Uninstall Java Service Wrapper";
		}
	}
	
	static public JavaServiceWrapperTask installServiceWrapper(
		File wrapperExe, File configurationFile,
		String account, String password) throws IOException
	{
		return new InstallServiceWrapper(wrapperExe, configurationFile,
			account, password);
	}
	
	static public JavaServiceWrapperTask installServiceWrapper(
		File wrapperExe, File configurationFile,
		String account) throws IOException
	{
		return installServiceWrapper(wrapperExe, configurationFile,
			account, null);
	}
	
	static public JavaServiceWrapperTask uninstallServiceWrapper(
		File wrapperExe, File configurationFile) throws IOException
	{
		return new UninstallServiceWrapper(wrapperExe, configurationFile);
	}
}