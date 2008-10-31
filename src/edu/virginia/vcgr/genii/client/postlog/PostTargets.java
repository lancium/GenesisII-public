package edu.virginia.vcgr.genii.client.postlog;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.postlog.empty.EmptyPostTarget;

public class PostTargets
{
	static private Log _logger = LogFactory.getLog(PostTargets.class);
	
	static private PostTarget _target = null;
	
	static private final String POSTER_CONFIG_FILE = "post-target.properties";
	static private final String POSTER_CLASS_PROP =
		"edu.virginia.vcgr.genii.client.postlog.class";
	
	static private Properties readProperties(File file)
	{
		Properties ret = new Properties();
		
		if (file == null || !file.exists())
			return ret;
		
		FileInputStream fin = null;
		try
		{
			fin = new FileInputStream(file);
			ret.load(fin);
			return ret;
		}
		catch (Throwable cause)
		{
			_logger.warn("Error trying to read \"" + 
				file.getAbsolutePath() + "\".", cause);
			return ret;
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	@SuppressWarnings("unchecked")
	static private PostTarget createPostTarget()
	{
		Properties props = readProperties(
			Installation.getDeployment(
			new DeploymentName()).getConfigurationFile(POSTER_CONFIG_FILE));
		String className = props.getProperty(POSTER_CLASS_PROP);
		if (className == null)
			return null;
		Class<PostTarget> ptc = null;
		
		try
		{
			ptc = (Class<PostTarget>)Thread.currentThread(
				).getContextClassLoader().loadClass(className);
		}
		catch (ClassNotFoundException cnfe)
		{
			_logger.warn("Unable to load poster class \"" + className 
				+ "\".", cnfe);
			return null;
		}

		try
		{
			try
			{
				Constructor<PostTarget> cons = ptc.getConstructor(Properties.class);
				return cons.newInstance(props);
			}
			catch (NoSuchMethodException nsme)
			{
				Constructor<PostTarget> cons = ptc.getConstructor();
				return cons.newInstance();
			}
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to instantiate poster.", cause);
		}
		
		return null;
	}
	
	static public PostTarget poster()
	{
		synchronized(PostTargets.class)
		{
			if (_target == null)
			{
				_target = createPostTarget();
				if (_target == null)
					_target = new EmptyPostTarget();
			}
		}
		
		return _target;
	}
}