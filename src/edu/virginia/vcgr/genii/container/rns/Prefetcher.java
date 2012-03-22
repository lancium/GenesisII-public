package edu.virginia.vcgr.genii.container.rns;

import java.io.Closeable;
import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.attrs.AttributePreFetcher;
import edu.virginia.vcgr.genii.container.common.AttributesPreFetcherFactory;

public class Prefetcher 
{
	static private Log _logger = LogFactory.getLog(Prefetcher.class);
	public static MessageElement[] preFetch(EndpointReferenceType target,
			MessageElement []existingAttributes, 
			AttributesPreFetcherFactory factory)
		{
					
			AttributePreFetcher preFetcher = null;
			
			try
			{
				preFetcher = factory.getPreFetcher(target);
				if (preFetcher == null)
					return existingAttributes;
				Collection<MessageElement> attrs = preFetcher.preFetch();
				if (attrs == null)
					return existingAttributes;
				
				if (existingAttributes != null)
				{
					for (MessageElement element : existingAttributes)
						attrs.add(element);
				}
				
				return attrs.toArray(new MessageElement[attrs.size()]);
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to pre-fetch attributes.", cause);
			}
			finally
			{
				if (preFetcher != null && (preFetcher instanceof Closeable))
					StreamUtils.close((Closeable)preFetcher);
			}
			
			return existingAttributes;
		}
	
		
}
