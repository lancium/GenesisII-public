package edu.virginia.vcgr.secrun;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.cert.Certificate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SecureRunnerClassLoader extends URLClassLoader
{
	static private Log _logger = LogFactory.getLog(SecureRunnerClassLoader.class);

	private Certificate[] _allowedCertificates;

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		Class<?> ret = super.findClass(name);
		Object[] signers = ret.getSigners();
		if (signers != null) {
			for (Object signer : signers) {
				if (signer instanceof Certificate) {
					Certificate test = (Certificate) signer;
					for (Certificate cert : _allowedCertificates) {
						if (cert.equals(test)) {
							if (_logger.isTraceEnabled())
								_logger.trace(String.format("Allowing creation of secure class \"%s\".", name));
							return ret;
						}
					}
				}
			}
		}

		throw new SecureRunSecurityException(String.format("Class \"%s\" is not trusted.", name));
	}

	public SecureRunnerClassLoader(Certificate[] allowedCertificates, URL[] urls, ClassLoader parent,
		URLStreamHandlerFactory factory)
	{
		super(urls, parent, factory);

		_allowedCertificates = allowedCertificates;
	}

	public SecureRunnerClassLoader(Certificate[] allowedCertificates, URL[] urls, ClassLoader parent)
	{
		super(urls, parent);

		_allowedCertificates = allowedCertificates;
	}

	public SecureRunnerClassLoader(Certificate[] allowedCertificates, URL[] urls)
	{
		super(urls, Thread.currentThread().getContextClassLoader());

		_allowedCertificates = allowedCertificates;
	}
}