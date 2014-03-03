package edu.virginia.vcgr.genii.client.utils.units;

import javax.xml.bind.annotation.adapters.XmlAdapter;

final public class ClockSpeedXmlAdapter extends XmlAdapter<String, ClockSpeed>
{
	@Override
	final public String marshal(ClockSpeed v) throws Exception
	{
		return v.toString();
	}

	@Override
	final public ClockSpeed unmarshal(String v) throws Exception
	{
		return new ClockSpeed(v);
	}
}