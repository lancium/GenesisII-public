package edu.virginia.vcgr.genii.ftp;

import org.morgan.ftp.IBackend;
import org.morgan.ftp.IBackendFactory;

public class GeniiBackendFactory implements IBackendFactory
{
	private GeniiBackendConfiguration _configuration;
	
	public GeniiBackendFactory(GeniiBackendConfiguration configuration)
	{
		_configuration = configuration;
	}
	
	@Override
	public IBackend newBackendInstance()
	{
		return new GeniiBackend((GeniiBackendConfiguration)_configuration.clone());
	}
}