package org.morgan.ftp.backends.local;

import org.morgan.ftp.IBackend;
import org.morgan.ftp.IBackendFactory;

public class LocalBackendFactory implements IBackendFactory
{
	private LocalBackendConfiguration _configuration;
	
	public LocalBackendFactory(LocalBackendConfiguration conf)
	{
		if (conf == null)
			throw new IllegalArgumentException("Local Backend Configuration cannot be null.");
		
		_configuration = conf;
	}
	
	@Override
	public IBackend newBackendInstance()
	{
		return new LocalBackend(_configuration);
	}
}