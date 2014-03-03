package edu.virginia.vcgr.genii.container.replicatedExport;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;

@XmlAccessorType(XmlAccessType.NONE)
class RExportSubscriptionUserData extends AdditionalUserData
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS, name = "primary-local-path", nillable = false, required = true)
	private String _primaryLocalPath = null;

	@SuppressWarnings("unused")
	private RExportSubscriptionUserData()
	{
	}

	public RExportSubscriptionUserData(String localFilename)
	{
		_primaryLocalPath = localFilename;
	}

	final public String getPrimaryLocalPath()
	{
		return _primaryLocalPath;
	}
}