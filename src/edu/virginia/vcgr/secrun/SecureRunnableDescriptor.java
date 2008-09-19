package edu.virginia.vcgr.secrun;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

public class SecureRunnableDescriptor implements Closeable
{
	static private Log _logger = LogFactory.getLog(
		SecureRunnableDescriptor.class);
	
	static public final String SECURE_RUNNER_DESCRIPTION_RESOURCE =
		"META-INF/secure-runnable/runnable-description.properties";
	
	static public final String SECURE_RUNNER_HOOK_PROPERTY =
		"edu.virginia.vcgr.secrun.hook";
	static public final String SECURE_RUNNER_CLASS_PROPERTY =
		"edu.virginia.vcgr.secrun.class";
	
	private String _hook;
	private File _jarFile;
	private SecureRunnable _runnable;
	
	static private Properties loadProperties(ClassLoader loader,
		String resource) throws IOException
	{
		InputStream in = null;
		try
		{
			in = loader.getResourceAsStream(resource);
			if (in == null)
				throw new IOException(String.format(
					"Unable to load resource \"%s\".", resource));
			Properties props = new Properties();
			props.load(in);
			return props;
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	@SuppressWarnings("unchecked")
	static private SecureRunnable instantiateRunnable(
		ClassLoader loader, String className) 
			throws ClassNotFoundException, InstantiationException, 
				SecurityException, NoSuchMethodException, 
				IllegalArgumentException, IllegalAccessException, 
				InvocationTargetException
	{
		Class<?> tmpClass = loader.loadClass(className);
		if (!SecureRunnable.class.isAssignableFrom(tmpClass))
			throw new InstantiationException(String.format(
				"\"%s\" does not implement the SecureRunnable interface.", 
				className));
		Class<? extends SecureRunnable> runnableClass =
			(Class<? extends SecureRunnable>)tmpClass;
		
		Constructor<? extends SecureRunnable> cons =
			runnableClass.getConstructor();
		return cons.newInstance();
	}
	
	private SecureRunnableDescriptor(ClassLoader loader)
		throws IOException, SecurityException, IllegalArgumentException, 
			ClassNotFoundException, InstantiationException, 
			NoSuchMethodException, IllegalAccessException, 
			InvocationTargetException
	{
		Properties secRunProperties = loadProperties(loader,
			SECURE_RUNNER_DESCRIPTION_RESOURCE);
		
		_hook = secRunProperties.getProperty(SECURE_RUNNER_HOOK_PROPERTY);
		if (_hook == null)
			throw new IOException(
				"Secure runner description does not include a hook.");
		
		String className = secRunProperties.getProperty(
			SECURE_RUNNER_CLASS_PROPERTY);
		if (className == null)
			throw new IOException(
				"Secure runner description does not include a class.");
		
		_runnable = instantiateRunnable(loader, className);
	}
	
	public SecureRunnableDescriptor(Certificate []allowedCerts,
		File jarFile, ClassLoader parent)
		throws IOException, SecurityException, IllegalArgumentException, 
			ClassNotFoundException, InstantiationException, 
			NoSuchMethodException, IllegalAccessException, 
			InvocationTargetException
	{
		this(new SecureRunnerClassLoader(allowedCerts,
			new URL[] { jarFile.toURI().toURL() }, parent));
		_jarFile = jarFile;
	}
	
	public SecureRunnableDescriptor(Certificate []allowedCerts, File jarFile)
		throws IOException, SecurityException, IllegalArgumentException, 
			ClassNotFoundException, InstantiationException, 
			NoSuchMethodException, IllegalAccessException, 
			InvocationTargetException
	{
		this(new SecureRunnerClassLoader(allowedCerts,
			new URL[] { jarFile.toURI().toURL() } ));
		_jarFile = jarFile;
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		close();
	}
	
	@Override
	synchronized public void close() throws IOException
	{
		if (_runnable != null)
		{
			_runnable = null;
			_hook = null;
			_jarFile = null;
		}
	}
	
	public String getHook()
	{
		return _hook;
	}
	
	public void run(Properties runProperties)
	{
		boolean remove = false;
		
		try
		{
			_runnable.run(_hook, runProperties);
			remove = true;
		}
		catch (SecureRunSecurityException srse)
		{
			_logger.error(
				"A security exception occurred while attempting to " +
				"run a securely downloaded runnable -- " +
				"removing offending library.", srse);
			remove = true;
		}
		catch (Throwable cause)
		{
			_logger.warn("An exception occurred while attempting to " +
				"run a securely downloaded runnable -- will try again later.", 
				cause);
			return;
		}
		
		try
		{
			if (remove && !_jarFile.delete())
			{
				_logger.warn("Unable to remove successfully completed " +
					"securely downloaded runnable.");
			}
		}
		catch (Throwable cause)
		{
			_logger.error(
				"Unexpected exception while attempting to remove " +
				"successfully completed securely downloaded runnable.", cause);
		}
	}
}