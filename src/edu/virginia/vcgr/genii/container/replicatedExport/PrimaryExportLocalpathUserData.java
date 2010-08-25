package edu.virginia.vcgr.genii.container.replicatedExport;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;

public class PrimaryExportLocalpathUserData extends AdditionalUserData
{
	static final long serialVersionUID = 0L;
	
	@XmlAttribute(name = "primary-export-localpath", required = true)
	private String _primaryExportLocalpath;
	
	@SuppressWarnings("unused")
	private PrimaryExportLocalpathUserData()
	{
	}
	
	public PrimaryExportLocalpathUserData(String path)
	{
		_primaryExportLocalpath = path;
	}
	
	final public String path()
	{
		return _primaryExportLocalpath;
	}
}