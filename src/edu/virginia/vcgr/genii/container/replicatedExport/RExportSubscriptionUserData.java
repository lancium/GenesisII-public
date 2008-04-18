package edu.virginia.vcgr.genii.container.replicatedExport;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.common.notification.UserDataType;

class RExportSubscriptionUserData
{
	private String _primaryLocalPath = null;
	
	public static QName _PRIMARY_LOCALPATH_QNAME = new QName(
			GenesisIIConstants.GENESISII_NS, "primary-export-localpath");
	
	public RExportSubscriptionUserData(String localFilename)
	{
		_primaryLocalPath = localFilename;
	}
	
	public RExportSubscriptionUserData(UserDataType userData)
		throws Exception
	{
		if (userData == null || (userData.get_any() == null) )
			throw new Exception(
				"Missing required user data in notification payload");
		MessageElement []data = userData.get_any();
		if (data.length != 1)
			throw new Exception(
				"Invalid user data for notification payload: empty");

		for (MessageElement elem : data){
			QName elemName = elem.getQName();
			if (elemName.equals(_PRIMARY_LOCALPATH_QNAME)){
				_primaryLocalPath = (String) elem.getObjectValue(String.class);
			} 
			else{
				throw new Exception(
					"Unknown user data found in notification payload.");
			}
		}
		
		if (_primaryLocalPath == null) {
			throw new Exception(
				"Incomplete user data for notification payload: missing path.");
		}
	}
	
	public String getPrimaryLocalPath()
	{
		return _primaryLocalPath;
	}

}