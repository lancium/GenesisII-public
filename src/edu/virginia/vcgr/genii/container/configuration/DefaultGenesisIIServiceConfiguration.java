package edu.virginia.vcgr.genii.container.configuration;

import edu.virginia.vcgr.genii.container.Container;

final class DefaultGenesisIIServiceConfiguration
	extends BaseGenesisIIServiceConfiguration
{
	@Override
	final public Long defaultServiceCertificateLifetime()
	{
		return Container.getDefaultCertificateLifetime();
	}

	@Override
	final public Long defaultResourceCertificateLifetime()
	{
		return Container.getDefaultCertificateLifetime();
	}
}