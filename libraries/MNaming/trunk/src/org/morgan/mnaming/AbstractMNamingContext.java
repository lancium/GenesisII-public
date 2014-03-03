package org.morgan.mnaming;

public abstract class AbstractMNamingContext implements MNamingContext {
	private String _contextIdentifier;
	private InitialMNamingContext _initial = new InitialMNamingContext();

	final private boolean isMine(MName name) {
		return (name.contextIdentifier() == null || name.contextIdentifier()
				.equals(_contextIdentifier));
	}

	protected abstract void bindLocal(String localName, Object value)
			throws MNamingException;

	protected abstract Object rebindLocal(String localName, Object value)
			throws MNamingException;

	protected abstract Object removeLocal(String localName)
			throws MNamingException;

	protected abstract Object lookupLocal(String localName)
			throws MNamingException;

	protected AbstractMNamingContext(String contextIdentifier) {
		_contextIdentifier = contextIdentifier;
	}

	@Override
	final public String contextIdentifier() {
		return _contextIdentifier;
	}

	@Override
	final public void bind(MName name, Object value) throws MNamingException {
		if (isMine(name))
			bindLocal(name.name(), value);
		else
			_initial.bind(name, value);
	}

	@Override
	final public void bind(String name, Object value) throws MNamingException {
		bind(new MName(name), value);
	}

	@Override
	final public Object rebind(MName name, Object value)
			throws MNamingException {
		if (isMine(name))
			return rebindLocal(name.name(), value);
		else
			return _initial.rebind(name, value);
	}

	@Override
	final public Object rebind(String name, Object value)
			throws MNamingException {
		return rebind(new MName(name), value);
	}

	@Override
	final public Object remove(MName name) throws MNamingException {
		if (isMine(name))
			return removeLocal(name.name());
		else
			return _initial.remove(name);
	}

	@Override
	final public Object remove(String name) throws MNamingException {
		return remove(new MName(name));
	}

	@Override
	final public Object lookup(MName name) throws MNamingException {
		if (isMine(name))
			return lookupLocal(name.name());
		else
			return _initial.lookup(name);
	}

	@Override
	final public Object lookup(String name) throws MNamingException {
		return lookup(new MName(name));
	}

	@Override
	final public <Type> Type lookup(Class<Type> type, MName name)
			throws MNamingException {
		return type.cast(lookup(name));
	}

	@Override
	final public <Type> Type lookup(Class<Type> type, String name)
			throws MNamingException {
		return type.cast(lookup(name));
	}

	@Override
	final public Object get(MName name) throws MNamingException {
		Object value = lookup(name);
		if (value == null)
			throw new MNameNotBoundException(name.toString());
		return value;
	}

	@Override
	final public Object get(String name) throws MNamingException {
		Object value = lookup(name);
		if (value == null)
			throw new MNameNotBoundException(name);
		return value;
	}

	@Override
	final public <Type> Type get(Class<Type> type, MName name)
			throws MNamingException {
		return type.cast(get(name));
	}

	@Override
	final public <Type> Type get(Class<Type> type, String name)
			throws MNamingException {
		return type.cast(get(name));
	}
}