package edu.virginia.vcgr.genii.client.asyncpost;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class PostProtocols
{
	static private Map<String, PostProtocol> _postProtocols = null;
	
	synchronized static public OutputStream openPostProtocol(URI target)
	{
		PostProtocol poster;
		
		if (_postProtocols == null)
		{
			_postProtocols = new HashMap<String, PostProtocol>();
			ServiceLoader<PostProtocol> loader = ServiceLoader.load(
				PostProtocol.class);
			for (PostProtocol pp : loader)
			{
				for (String protocol : pp.handledProtocols())
					_postProtocols.put(protocol, pp);
			}
		}
		
		synchronized(_postProtocols)
		{
			poster = _postProtocols.get(target.getScheme());
		}
		
		if (poster == null)
		{
			return new OutputStream()
			{
				@Override
				public void write(int b) throws IOException
				{
					// do nothing
				}
			};
		}
		
		return poster.postStream(target);
	}
}