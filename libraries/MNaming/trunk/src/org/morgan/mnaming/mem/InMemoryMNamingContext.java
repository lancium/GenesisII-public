package org.morgan.mnaming.mem;

import java.util.HashMap;
import java.util.Map;

import org.morgan.mnaming.AbstractMNamingContext;
import org.morgan.mnaming.MNameAlreadyBoundException;
import org.morgan.mnaming.MNamingException;

final public class InMemoryMNamingContext extends AbstractMNamingContext
{
	static final public String CONTEXT_IDENTIFIER = "mem";

	private Map<String, Object> _values = new HashMap<String, Object>();

	public InMemoryMNamingContext()
	{
		super(CONTEXT_IDENTIFIER);
	}

	@Override
	final protected void bindLocal(String localName, Object value) throws MNamingException
	{
		synchronized (_values) {
			if (_values.containsKey(localName))
				throw new MNameAlreadyBoundException(localName);
			rebindLocal(localName, value);
		}
	}

	@Override
	final protected Object rebindLocal(String localName, Object value) throws MNamingException
	{
		synchronized (_values) {
			return _values.put(localName, value);
		}
	}

	@Override
	final protected Object removeLocal(String localName) throws MNamingException
	{
		synchronized (_values) {
			return _values.remove(localName);
		}
	}

	@Override
	final protected Object lookupLocal(String localName) throws MNamingException
	{
		synchronized (_values) {
			return _values.get(localName);
		}
	}

	@Override
	final public void clear()
	{
		synchronized (_values) {
			_values.clear();
		}
	}
}