package edu.virginia.vcgr.appmgr.security;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public class VerifiableClassLoader extends URLClassLoader
{
	private Verifier _verifier;

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		Class<?> ret = super.findClass(name);
		_verifier.verify(name, ret.getSigners());
		return ret;
	}

	public VerifiableClassLoader(Verifier verifier, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory)
	{
		super(urls, parent, factory);

		if (verifier == null)
			throw new IllegalArgumentException("Verifier parameter cannot be null.");

		_verifier = verifier;
	}

	public VerifiableClassLoader(Verifier verifier, URL[] urls, ClassLoader parent)
	{
		super(urls, parent);

		if (verifier == null)
			throw new IllegalArgumentException("Verifier parameter cannot be null.");

		_verifier = verifier;
	}

	public VerifiableClassLoader(Verifier verifier, URL[] urls)
	{
		this(verifier, urls, Thread.currentThread().getContextClassLoader());
	}
}