package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;

import org.ggf.bes.factory.ActivityStateEnumeration;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class SetupOGRSHPhase extends AbstractExecutionPhase
{
	static final long serialVersionUID = 0L;
	
	private String _storedContextFilename;
	private String _configFilename;
	
	public SetupOGRSHPhase(String storedContextFilename, String configFilename)
	{
		super(new ActivityState(
			ActivityStateEnumeration.Running, "preparing-ogrsh", false));
		
		_storedContextFilename = storedContextFilename;
		_configFilename = configFilename;
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		File configFile = new File(
			context.getCurrentWorkingDirectory(), _configFilename);
		File contextFile = new File(
			context.getCurrentWorkingDirectory(), _storedContextFilename);
		PrintStream config = null;
		
		try
		{
			config = new PrintStream(configFile);
			writeHeader(config);
			
			writeGlobalConfig(config, "/home/bes-job");
			writeLocalProvider(config);
			writeGridProvider(config, contextFile);
			writeMount(config, "/home/bes-job", "local-fs-provider", "local-fs-session",
				context.getCurrentWorkingDirectory().getAbsolutePath());
			writeMount(config, "/bin", "local-fs-provider", 
				"local-fs-session", "/bin");
			writeMount(config, "/proc", "local-fs-provider", 
				"local-fs-session", "/proc");
			writeMount(config, "/sbin", "local-fs-provider", 
				"local-fs-session", "/sbin");
			writeMount(config, "/dev", "local-fs-provider", 
				"local-fs-session", "/dev");
			writeMount(config, "/usr", "local-fs-provider", 
				"local-fs-session", "/usr");
			writeMount(config, "/tmp", "local-fs-provider", 
				"local-fs-session", "/tmp");
			writeMount(config, "/etc", "local-fs-provider", 
				"local-fs-session", "/etc");
			writeMount(config, "/var", "local-fs-provider", 
				"local-fs-session", "/var");
			writeMount(config, "/uva-genii", "genesisII-fs-provider", 
				"uva-geniinet-session", "/");
			
			writeFooter(config);
		}
		finally
		{
			StreamUtils.close(config);
		}
	}
	
	static private void writeLocalProvider(PrintStream config)
	{
		config.println("<ogrsh:fs-provider name=\"local-fs-provider\"");
		config.println("provider-library=\"libLocalOGRSHProvider.so\"");
		config.println("provider-creator=\"createLocalOGRSHProvider\">");
		config.println("<ogrsh:session name=\"local-fs-session\"/>");
		config.println("</ogrsh:fs-provider>");
	}
	
	static private void writeGridProvider(PrintStream config, File storedContextFile)
		throws MalformedURLException
	{
		config.println("<ogrsh:fs-provider name=\"genesisII-fs-provider\"");
		config.println("provider-library=\"libGenesisIIOGRSHProvider.so\"");
		config.println("provider-creator=\"createGenesisIIOGRSHProvider\">");
		config.println("<ogrsh:session name=\"uva-geniinet-session\"");
		config.format("stored-context-url=\"%s\"/>\n", 
			storedContextFile.getAbsoluteFile().toURI().toURL());
		config.println("</ogrsh:fs-provider>");
	}
	
	static private void writeMount(PrintStream config, 
		String mountLocation, String mountProvider, String mountSession,
		String rootDirectory)
	{
		config.format("<ogrsh:mount location=\"%s\" provider=\"%s\"\n", 
			mountLocation, mountProvider);
		config.format("session=\"%s\">\n", mountSession);
		config.format("<ogrsh:root-directory>%s</ogrsh:root-directory>\n", 
			rootDirectory);
		config.format("</ogrsh:mount>\n");
	}
	
	static private void writeGlobalConfig(PrintStream config, String homedir)
	{
		config.println("<ogrsh:global-config>");
        config.format("<ogrsh:home-dir>%s</ogrsh:home-dir>\n", homedir);
        config.println("</ogrsh:global-config>");
	}
	
	static private void writeHeader(PrintStream config) throws IOException
	{
		config.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		config.println("<ogrsh:configuration");
		config.println("	xmlns:ogrsh=\"http://vcgr.cs.virginia.edu/ogrsh\"");
		config.println("	xmlns:genii=\"http://vcgr.cs.virginia.edu/Genesis-II/fs-provider\">");
	}
	
	static private void writeFooter(PrintStream config) throws IOException
	{
		config.println("</ogrsh:configuration>");
	}
}