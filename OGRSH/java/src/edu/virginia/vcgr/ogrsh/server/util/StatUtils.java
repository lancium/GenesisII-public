package edu.virginia.vcgr.ogrsh.server.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class StatUtils 
{
	static private Log _logger = LogFactory.getLog(StatUtils.class);
	
	static public long generateInodeNumber(EndpointReferenceType target)
	{
		WSName name = new WSName(target);
		if (name.isValidWSName())
		{
			return name.getEndpointIdentifier().toString().hashCode();
		} else
		{
			_logger.warn("Trying to generate an INode number of a target which"
				+ "does not implement the WS-Naming specification.");
			
			try
			{
				byte []array = EPRUtils.toBytes(target);
				long result = 0;
				for (byte d : array)
				{
					result ^= d;
				}
				
				return result;
			}
			catch (ResourceException re)
			{
				_logger.fatal("Unexpected error while trying to serialize EPR.", re);
				throw new RuntimeException(re);
			}
		}
	}
}