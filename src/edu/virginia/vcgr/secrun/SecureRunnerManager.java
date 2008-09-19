package edu.virginia.vcgr.secrun;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.Deployment;
import edu.virginia.vcgr.genii.client.configuration.Security;

public class SecureRunnerManager implements Closeable
{
	static private Log _logger = LogFactory.getLog(SecureRunnerManager.class);
	
	static public final int DEFAULT_DESCRIPTORS_SIZE = 4;
	
	static public final String SECURE_RUNNER_PROPERTIES_FILE =
		"secure-runner.properties";
	static public final String TRUSTED_CERT_FILE_PROPERTY_PATTERN =
		"edu.virginia.vcgr.secrun.trusted-cert-file.%d";
	
	private Map<String, Collection<SecureRunnableDescriptor>> _descriptors =
		new HashMap<String, Collection<SecureRunnableDescriptor>>(
			DEFAULT_DESCRIPTORS_SIZE);
	
	public SecureRunnerManager(Certificate[] allowedCertificates, 
		File directory)
	{
		if (!directory.exists() || !directory.isDirectory())
			return;
		
		for (File entry : directory.listFiles(_filter))
		{
			addEntry(allowedCertificates, entry);
		}
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		close();
	}
	
	@Override
	synchronized public void close() throws IOException
	{
		for (String hook : _descriptors.keySet())
		{
			Collection<SecureRunnableDescriptor> descriptors =
				_descriptors.get(hook);
			if (descriptors != null)
			{
				for (SecureRunnableDescriptor descriptor : descriptors)
				{
					StreamUtils.close(descriptor);
				}
			}
		}
		
		_descriptors.clear();
	}
	
	public void run(String hook, Properties runProperties)
	{
		try
		{
			Collection<SecureRunnableDescriptor> list = _descriptors.get(hook);
			if (list != null)
			{
				for (SecureRunnableDescriptor desc : list)
				{
					desc.run(runProperties);
				}
			}
		}
		finally
		{
			Runtime.getRuntime().gc();
		}
	}
	
	private void addEntry(Certificate []allowedCerts, File entry)
	{
		try
		{
			SecureRunnableDescriptor descriptor = 
				new SecureRunnableDescriptor(allowedCerts, entry);
			String hook = descriptor.getHook();
			Collection<SecureRunnableDescriptor> descList =
				_descriptors.get(hook);
			if (descList == null)
				_descriptors.put(hook, 
					descList = new LinkedList<SecureRunnableDescriptor>());
			
			descList.add(descriptor);
		}
		catch (Throwable cause)
		{
			_logger.error(String.format(
				"Unable to load securely downloaded library \"%s\" -- " +
				"removing it.", entry.getAbsolutePath()), cause);
			
			try
			{
				if (!entry.delete())
				{
					_logger.error(String.format(
						"Unable to delete securely downloaded library \"%s\".",
						entry.getAbsolutePath()));
				}
			}
			catch (Throwable cause2)
			{
				_logger.error(String.format(
					"Unable to delete securely downloaded library \"%s\".",
					entry.getAbsolutePath()), cause2);
			}
		}
	}
	
	static private final FilenameFilter _filter = new FilenameFilterImpl();
	static private class FilenameFilterImpl implements FilenameFilter
	{
		@Override
		public boolean accept(File dir, String name)
		{
			return (name.endsWith(".jar"));
		}	
	}
	
	static private Certificate loadCertificate(File certFile)
		throws IOException, CertificateException
	{
		InputStream in = null;
		
		try
		{
			in = new FileInputStream(certFile);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (cf.generateCertificate(in));
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static private Properties loadProperties(File inputFile)
		throws IOException
	{
		InputStream in = null;
		
		try
		{
			in = new FileInputStream(inputFile);
			Properties props = new Properties();
			props.load(in);
			return props;
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static public SecureRunnerManager createSecureRunnerManager(
		Deployment deployment)
	{
		Security sec = deployment.security();
		Collection<Certificate> certs = new LinkedList<Certificate>();
		File config = deployment.getConfigurationFile(
			SECURE_RUNNER_PROPERTIES_FILE);
		try
		{
			Properties configProperties = loadProperties(config);
			int lcv = 0;
			while (true)
			{
				String property = String.format(TRUSTED_CERT_FILE_PROPERTY_PATTERN,
					lcv++);
				String value = configProperties.getProperty(property);
				if (value != null)
				{
					try
					{
						certs.add(loadCertificate(sec.getSecurityFile(value)));
					}
					catch (Throwable cause)
					{
						_logger.warn(String.format(
							"Unable to load a certificate specified by " +
							"\"%s\" -- continuing with degraded functionallity.", 
							value), cause);
					}
				} else
					break;
			}
		}
		catch (Throwable cause2)
		{
			_logger.error("Unable to load secure runner manager.", cause2);
		}
		
		return new SecureRunnerManager(certs.toArray(new Certificate[0]),
			deployment.secureRunnableDirectory());
	}
}