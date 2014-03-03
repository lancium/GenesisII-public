package org.morgan.mnaming;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

final public class InitialMNamingContext implements MNamingContext {
	static private Map<String, MNamingContext> _contexts = new HashMap<String, MNamingContext>();

	static {
		for (MNamingContext context : ServiceLoader.load(MNamingContext.class)) {
			String contextIdentifier = context.contextIdentifier();
			_contexts.put(contextIdentifier, context);
		}
	}

	private MNamingContext contextFor(MName name) throws MNamingException {
		String cId = name.contextIdentifier();
		if (cId == null)
			throw new MNameNotBoundException(name.toString());

		MNamingContext ctxt = _contexts.get(cId);
		if (ctxt == null)
			throw new MNameNotBoundException(name.toString());

		return ctxt;
	}

	private MNamingContext contextFor(String name) throws MNamingException {
		return contextFor(new MName(name));
	}

	@Override
	public String contextIdentifier() {
		throw new UnsupportedOperationException(
				"Initial context does not have a context identifier.");
	}

	@Override
	final public void bind(MName name, Object value) throws MNamingException {
		contextFor(name).bind(name, value);
	}

	@Override
	final public void bind(String name, Object value) throws MNamingException {
		contextFor(name).bind(name, value);
	}

	@Override
	final public Object rebind(MName name, Object value)
			throws MNamingException {
		return contextFor(name).rebind(name, value);
	}

	@Override
	final public Object rebind(String name, Object value)
			throws MNamingException {
		return contextFor(name).rebind(name, value);
	}

	@Override
	final public Object remove(MName name) throws MNamingException {
		return contextFor(name).remove(name);
	}

	@Override
	final public Object remove(String name) throws MNamingException {
		return contextFor(name).remove(name);
	}

	@Override
	final public Object lookup(MName name) throws MNamingException {
		return contextFor(name).lookup(name);
	}

	@Override
	final public Object lookup(String name) throws MNamingException {
		return contextFor(name).lookup(name);
	}

	@Override
	final public <Type> Type lookup(Class<Type> type, MName name)
			throws MNamingException {
		return contextFor(name).lookup(type, name);
	}

	@Override
	final public <Type> Type lookup(Class<Type> type, String name)
			throws MNamingException {
		return contextFor(name).lookup(type, name);
	}

	@Override
	final public Object get(MName name) throws MNamingException {
		return contextFor(name).get(name);
	}

	@Override
	final public Object get(String name) throws MNamingException {
		return contextFor(name).get(name);
	}

	@Override
	final public <Type> Type get(Class<Type> type, MName name)
			throws MNamingException {
		return contextFor(name).get(type, name);
	}

	@Override
	final public <Type> Type get(Class<Type> type, String name)
			throws MNamingException {
		return contextFor(name).get(type, name);
	}

	@Override
	final public void clear() {
		// There is nothing to clear in the initial naming context.
	}

	final public void clearAll() {
		for (MNamingContext context : _contexts.values())
			context.clear();
	}
}