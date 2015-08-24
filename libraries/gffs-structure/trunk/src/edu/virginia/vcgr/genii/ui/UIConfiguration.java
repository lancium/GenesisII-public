package edu.virginia.vcgr.genii.ui;

import java.net.URI;

import edu.virginia.vcgr.genii.client.ClientProperties;

public class UIConfiguration
{
	public UIConfiguration()
	{
	}

	public URI errorReportTarget()
	{
		return URI.create(ClientProperties.getClientProperties().getErrorReportTarget());
	}
}