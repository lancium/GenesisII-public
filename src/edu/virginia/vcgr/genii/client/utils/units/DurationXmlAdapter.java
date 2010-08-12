package edu.virginia.vcgr.genii.client.utils.units;

import javax.xml.bind.annotation.adapters.XmlAdapter;

final public class DurationXmlAdapter
	extends XmlAdapter<String, Duration>
{
	@Override
	final public String marshal(Duration v) throws Exception
	{
		return v.toString();
	}

	@Override
	final public Duration unmarshal(String v) throws Exception
	{
		return new Duration(v);
	}
}