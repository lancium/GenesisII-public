package edu.virginia.vcgr.genii.container.deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.axis.types.NCName;
import org.ggf.jsdl.hpcp.HPCProfileApplication_Type;
import org.ggf.jsdl.hpcp.DirectoryName_Type;
import org.ggf.jsdl.hpcp.Environment_Type;
import org.ggf.jsdl.hpcp.FileName_Type;

public class HPCApplicationReifier
{
	static public HPCProfileApplication_Type reifyApplication(
		File deployDirectory,
		AbstractReifier reifier,
		HPCProfileApplication_Type application)
	{
		application.setEnvironment(
			reifyEnvironment(deployDirectory, reifier, application.getEnvironment()));
		
		FileName_Type binary = application.getExecutable();
		if (binary == null)
		{
			binary = new FileName_Type(reifier.getBinaryName(deployDirectory));
			application.setExecutable(binary);
		}
		
		DirectoryName_Type directory = new DirectoryName_Type(
			reifier.getCWD(deployDirectory));
		application.setWorkingDirectory(directory);
		
		return application;
	}
	
	static private Environment_Type[] reifyEnvironment(
		File deployDirectory,
		AbstractReifier reifier, Environment_Type []original)
	{
		boolean isWindows = isWindows();
		boolean handledPath = false;
		boolean handledLibraryPath = false;
		
		Collection<Environment_Type> ret =
			new ArrayList<Environment_Type>();
		
		if (original == null)
			original = new Environment_Type[0];
		for (Environment_Type env : original)
		{
			String name = env.getName().toString();
			
			if (isWindows && name.equalsIgnoreCase("path"))
			{
				env.set_value(
					modifyPath(env.get_value(), 
						reifier.getAdditionalPaths(deployDirectory)));
				env.set_value(
					modifyPath(env.get_value(), 
						reifier.getAdditionalLibraryPaths(deployDirectory)));
				handledPath = true;
				handledLibraryPath = true;
			} else if (!isWindows && name.equals("PATH"))
			{
				env.set_value(
					modifyPath(env.get_value(),
						reifier.getAdditionalPaths(deployDirectory)));
				handledPath = true;
			} else if (!isWindows && name.equals("LD_LIBRARY_PATH"))
			{
				env.set_value(
					modifyPath(env.get_value(),
						reifier.getAdditionalLibraryPaths(deployDirectory)));
				handledLibraryPath = true;
			}
		}
		
		if (!handledPath)
		{
			Environment_Type env = new Environment_Type(
				modifyPath(null, reifier.getAdditionalPaths(deployDirectory)));
			env.setName(new NCName("PATH"));
			
			if (isWindows)
			{
				env.set_value(
					modifyPath(env.get_value(),
						reifier.getAdditionalLibraryPaths(deployDirectory)));
				handledLibraryPath = true;
			}
			
			ret.add(env);
		}
		
		if (!handledLibraryPath)
		{
			Environment_Type env = new Environment_Type(
				modifyPath(null, reifier.getAdditionalLibraryPaths(deployDirectory)));
			env.setName(new NCName("LD_LIBRARY_PATH"));
		}
			
		return ret.toArray(new Environment_Type[0]);
	}
	
	static private boolean isWindows()
	{
		return (System.getProperty("os.name").startsWith("Windows"));
	}
	
	static private String modifyPath(String original,
		String[] newPaths)
	{
		for (String newPath : newPaths)
		{
			if (original == null)
				original = newPath;
			else
				original = original + File.pathSeparatorChar + newPath;
		}
		
		return original;
	}
}