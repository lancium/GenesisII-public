package edu.virginia.vcgr.genii.container.replicatedExport.resolver;

import org.apache.axis.types.URI;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.common.notification.UserDataType;

public class RExportResolverTerminateUserData
{
	private URI _epi = null;
	
	public RExportResolverTerminateUserData(URI epi){
		_epi = epi;
	}
	
	public RExportResolverTerminateUserData(UserDataType userData)
		throws Exception{
		if (userData == null || (userData.get_any() == null) )
			throw new Exception(
				"Missing required user data in notification payload");
		MessageElement []data = userData.get_any();
		if (data.length != 1)
			throw new Exception(
				"Invalid user data for notification payload"); 
		
		for (MessageElement elem : data)
		{
			QName elemName = elem.getQName();
			if (elemName.equals(RExportResolverUtils.REFERENCE_RESOLVER_EPI_QNAME))
			{
				_epi = new URI(elem.getValue());
			} else
			{
				throw new Exception(
					"Unknown user data found in notification payload of RExportRexolver");
			}
		}
		
		if (_epi == null)
		{
			throw new Exception(
				"Incomplete user data for notification payload of RExportRexolver");
		}
	}
	
	public URI getEPI()
	{
		return _epi;
	}
}