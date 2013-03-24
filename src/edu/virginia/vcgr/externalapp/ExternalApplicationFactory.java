package edu.virginia.vcgr.externalapp;

import org.w3c.dom.Element;

public interface ExternalApplicationFactory
{
	public ExternalApplication createApplication(Element configuration);
}