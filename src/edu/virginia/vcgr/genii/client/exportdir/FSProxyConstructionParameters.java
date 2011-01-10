package edu.virginia.vcgr.genii.client.exportdir;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

import edu.virginia.g3.fsview.FSViewConnectionInformation;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;

public class FSProxyConstructionParameters extends ConstructionParameters
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	@XmlElement(namespace = "http://genesisII.virginia.edu/fsproxy",
		name = "connection-information", required = true, nillable = false)
	private FSViewConnectionInformation _connectionInformation;
	
	public FSProxyConstructionParameters(FSViewConnectionInformation connectionInformation)
	{
		_connectionInformation = connectionInformation;
	}
	
	public FSProxyConstructionParameters()
	{
		this(null);
	}
	
	public FSViewConnectionInformation connectionInformation()
	{
		return _connectionInformation;
	}
}