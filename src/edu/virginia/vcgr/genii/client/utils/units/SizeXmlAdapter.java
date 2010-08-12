package edu.virginia.vcgr.genii.client.utils.units;

import javax.xml.bind.annotation.adapters.XmlAdapter;

final public class SizeXmlAdapter
	extends XmlAdapter<String, Size>
{
	@Override
	final public String marshal(Size v) throws Exception
	{
		return v.toString();
	}

	@Override
	final public Size unmarshal(String v) throws Exception
	{
		return new Size(v);
	}
}