package edu.virginia.vcgr.genii.client.pwrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

public class ProcessWrapperFactory
{
    static private Log _logger = LogFactory.getLog(ProcessWrapperFactory.class);
    
	static private class ProcessThreadFactory implements ThreadFactory
	{
		@Override
		public Thread newThread(Runnable r)
		{
			Thread th = new Thread(r, "Process Wrapper Worker");
			th.setDaemon(false);
			
			return th;
		}
	}
	
	synchronized static private File findCommonPWrapper(File originalPath,
		File targetDirectory) throws IOException
	{
		if (!targetDirectory.exists())
			targetDirectory.mkdirs();

		if (!targetDirectory.exists() || !targetDirectory.isDirectory())
			throw new FileNotFoundException(String.format(
				"Unable to locate target directory \"%s\".",
				targetDirectory));
		
		File targetPath = new File(targetDirectory, originalPath.getName());
		if (!targetPath.exists() ||
			targetPath.lastModified() < originalPath.lastModified())
		{
			File tmp = new File(targetPath.getParentFile(),
				targetPath.getName() + ".tmp");
			FileOutputStream fos = null;
			FileInputStream fin = null;
			
			try
			{
				fin = new FileInputStream(originalPath);
				fos = new FileOutputStream(tmp);
				
				StreamUtils.copyStream(fin, fos);
				fos.close();
				fos = null;
				tmp.renameTo(targetPath);
			}
			finally
			{
				StreamUtils.close(fin);
				StreamUtils.close(fos);
				tmp.delete();
			}
		}
		
		if (!targetPath.canExecute())
			targetPath.setExecutable(true, false);
		
		return targetPath;
	}
	
	static private ExecutorService _processThreadPool =
		Executors.newCachedThreadPool(new ProcessThreadFactory());
	
	static public ProcessWrapper createWrapper(File commonDirectory,
		OperatingSystemNames desiredOSName,
		ProcessorArchitecture desiredArch) throws ProcessWrapperException
	{
		if (commonDirectory == null)
			throw new IllegalArgumentException(
				"Common Directory cannot be null.");
		
		if (desiredOSName == null)
			desiredOSName = OperatingSystemNames.getCurrentOperatingSystem();
		
		if (desiredArch == null)
			desiredArch = ProcessorArchitecture.getCurrentArchitecture();
		
		File binDir = Installation.getProcessWrapperBinPath();
		File pwrapperPath = null;

		_logger.info("ProcessWrapperFactory: arch is " + desiredArch + ", OS is " + desiredOSName);		
		
		if (desiredOSName.isWindows())
		{
			if (desiredArch == null || 
				desiredArch == ProcessorArchitecture.x86 ||
				desiredArch == ProcessorArchitecture.x86_32 ||
				desiredArch == ProcessorArchitecture.x86_64)
				pwrapperPath = new File(binDir, "pwrapper-winxp.exe");
		} else if (desiredOSName == OperatingSystemNames.LINUX)
		{
			if (desiredArch == null ||
				desiredArch == ProcessorArchitecture.x86 || 
				desiredArch == ProcessorArchitecture.x86_32)
				pwrapperPath = new File(binDir, "pwrapper-linux-32");
			else if (desiredArch == ProcessorArchitecture.x86_64)
				pwrapperPath = new File(binDir, "pwrapper-linux-64");
		} else if (desiredOSName == OperatingSystemNames.MACOS)
		{
			if (desiredArch == null ||
				desiredArch == ProcessorArchitecture.x86 ||
				desiredArch == ProcessorArchitecture.x86_32 ||
				desiredArch == ProcessorArchitecture.x86_64)
				pwrapperPath = new File(binDir, "pwrapper-macosx");
		}
		
		if (pwrapperPath == null)
			pwrapperPath = new File(binDir, "pwrapper.sh");
		
		try
		{
			pwrapperPath = findCommonPWrapper(pwrapperPath, commonDirectory);
		}
		catch (IOException e)
		{
			throw new ProcessWrapperException(
				"Unable to find common pwrapper.", e);
		}
		
		return new ProcessWrapper(_processThreadPool, pwrapperPath);
	}
	
	static public ProcessWrapper createWrapper(File commonDirectory)
		throws ProcessWrapperException
	{
		return createWrapper(commonDirectory, null, null);
	}
}
