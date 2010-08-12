package edu.virginia.vcgr.genii.client.utils.units;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlJavaTypeAdapter(SizeXmlAdapter.class)
public class Size extends UnitableValue<SizeUnits>
{
	static final long serialVersionUID = 0L;
	
	@Override
	protected SizeUnits defaultUnits()
	{
		return SizeUnits.Bytes;
	}

	@Override
	protected SizeUnits parseUnits(String textRepresentation)
	{
		return SizeUnits.parse(textRepresentation);
	}

	@Override
	public double as(SizeUnits targetUnits)
	{
		return targetUnits.convert(value(), units());
	}
	
	public Size()
	{
		super();
	}

	public Size(double value, SizeUnits units)
	{
		super(value, units);
	}

	public Size(double value)
	{
		super(value);
	}

	public Size(String textRepresentation)
	{
		super(textRepresentation);
	}
}