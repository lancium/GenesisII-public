package edu.virginia.vcgr.externalapp;

import org.w3c.dom.Element;

public class PictureViewerFactory implements ExternalApplicationFactory
{
	@Override
	final public ExternalApplication createApplication(Element configuration)
	{
		return new PictureViewerApplication();
	}
}