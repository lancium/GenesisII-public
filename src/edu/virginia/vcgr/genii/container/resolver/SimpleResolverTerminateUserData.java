package edu.virginia.vcgr.genii.container.resolver;

import org.apache.axis.types.URI;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.common.notification.UserDataType;

class SimpleResolverTerminateUserData
{
	private URI _epi = null;
	private int _version = -1;
	private String _guid = null;
	
	public SimpleResolverTerminateUserData(URI epi, int version, String guid)
	{
		_epi = epi;
		_version = version;
		_guid = guid;
	}
	
	public SimpleResolverTerminateUserData(UserDataType userData)
		throws Exception
	{
		if (userData == null || (userData.get_any() == null) )
			throw new Exception(
				"Missing required user data in notification payload");
		MessageElement []data = userData.get_any();
		if (data.length != 3)
			throw new Exception(
				"Invalid user data for notification payload");

		for (MessageElement elem : data)
		{
			QName elemName = elem.getQName();
			if (elemName.equals(SimpleResolverUtils.REFERENCE_RESOLVER_EPI_QNAME))
			{
				_epi = new URI(elem.getValue());
			} else if (elemName.equals(SimpleResolverUtils.REFERENCE_RESOLVER_VERSION_QNAME))
			{
				String versionStr = (String) elem.getObjectValue(String.class);
				_version = (new Integer(versionStr)).intValue();
			} else if (elemName.equals(SimpleResolverUtils.REFERENCE_RESOLVER_GUID_QNAME))
			{
				_guid = (String) elem.getObjectValue(String.class);
			} else
			{
				throw new Exception(
					"Unknown user data found in notification payload.");
			}
		}
		
		if (_epi == null || _version == -1 || _guid == null)
		{
			throw new Exception(
				"Incomplete user data for notification payload");
		}
	}
	
	public URI getEPI()
	{
		return _epi;
	}
	
	public int getVersion()
	{
		return _version;
	}

	public String getSubscriptionGUID()
	{
		return _guid;
	}
}