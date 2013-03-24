package edu.virginia.vcgr.externalapp;

import org.w3c.dom.Element;

public class GridJobToolApplicationFactory implements ExternalApplicationFactory
{
	@Override
	public ExternalApplication createApplication(Element configuration)
	{
		return new GridJobToolApplication();
	}
}