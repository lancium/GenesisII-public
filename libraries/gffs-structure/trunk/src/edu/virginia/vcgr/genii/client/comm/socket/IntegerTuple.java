package edu.virginia.vcgr.genii.client.comm.socket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegerTuple
{
	private int _first;
	private int _second;
	private int _third;

	public IntegerTuple(int first, int second, int third)
	{
		_first = first;
		_second = second;
		_third = third;
	}

	final public int first()
	{
		return _first;
	}

	final public int second()
	{
		return _second;
	}

	final public int third()
	{
		return _third;
	}

	@Override
	public String toString()
	{
		return String.format("(%d, %d, %d)", _first, _second, _third);
	}

	static final private Pattern TUPLE_PATTERN = Pattern.compile("^\\D*(\\d+)\\D+(\\d+)\\D+(\\d+)\\D*$");

	static public IntegerTuple parseIntegerTuple(String value)
	{
		Matcher matcher = TUPLE_PATTERN.matcher(value);
		if (!matcher.matches())
			throw new NumberFormatException(String.format("Unable to parse integer tuple \"%s\".", value));

		return new IntegerTuple(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
			Integer.parseInt(matcher.group(3)));
	}
}