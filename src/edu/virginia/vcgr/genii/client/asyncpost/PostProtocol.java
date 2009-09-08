package edu.virginia.vcgr.genii.client.asyncpost;

import java.io.OutputStream;
import java.net.URI;

public interface PostProtocol
{
	public String[] handledProtocols();
	
	public OutputStream postStream(URI target);
}