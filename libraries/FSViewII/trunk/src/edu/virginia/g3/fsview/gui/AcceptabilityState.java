package edu.virginia.g3.fsview.gui;

final public class AcceptabilityState
{
	private Class<?> _source;
	private String _message;

	private AcceptabilityState(Class<?> source, String message)
	{
		_source = source;
		_message = message;
	}

	final public Class<?> source()
	{
		return _source;
	}

	final public boolean isAcceptable()
	{
		return _message == null;
	}

	@Override
	final public String toString()
	{
		if (_message == null)
			return "Acceptable";

		return _message;
	}

	static public AcceptabilityState accept(Class<?> source)
	{
		return new AcceptabilityState(source, null);
	}

	static public AcceptabilityState deny(Class<?> source, String message)
	{
		if (message == null)
			throw new IllegalArgumentException("Message cannot be null.");

		return new AcceptabilityState(source, message);
	}
}
