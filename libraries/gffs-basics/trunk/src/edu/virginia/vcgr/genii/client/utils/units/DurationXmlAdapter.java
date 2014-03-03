package edu.virginia.vcgr.genii.client.utils.units;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;

final public class DurationXmlAdapter extends XmlAdapter<javax.xml.datatype.Duration, Duration>
{
	@Override
	public javax.xml.datatype.Duration marshal(Duration v) throws Exception
	{
		DatatypeFactory factory = DatatypeFactory.newInstance();
		return factory.newDuration((long) v.as(DurationUnits.Milliseconds));
	}

	@Override
	public Duration unmarshal(javax.xml.datatype.Duration v) throws Exception
	{
		return new Duration(String.format("%d years %d months %d days %d hours %d minutes %.3f seconds",
			v.getField(DatatypeConstants.YEARS), v.getField(DatatypeConstants.MONTHS), v.getField(DatatypeConstants.DAYS),
			v.getField(DatatypeConstants.HOURS), v.getField(DatatypeConstants.MINUTES), v.getField(DatatypeConstants.SECONDS)));
	}
}