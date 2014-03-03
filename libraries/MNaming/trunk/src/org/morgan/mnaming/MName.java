package org.morgan.mnaming;

public class MName {
	private String _contextIdentifier;
	private String _name;

	private void initialize(String contextIdentifier, String name) {
		if (name == null)
			throw new IllegalArgumentException("Name cannot be null.");

		_contextIdentifier = contextIdentifier;
		_name = name;

		if (name.contains(":"))
			throw new IllegalArgumentException(String.format(
					"%s is an invalid MNaming name.", this));
	}

	public MName(String contextIdentifier, String name) {
		initialize(contextIdentifier, name);
	}

	public MName(String name) {
		int index = name.indexOf(':');
		if (index < 0)
			initialize(null, name);
		else if (index == 0)
			throw new IllegalArgumentException(String.format(
					"%s is an invalid MNaming name.", name));
		else
			initialize(name.substring(0, index), name.substring(index + 1));
	}

	final public String contextIdentifier() {
		return _contextIdentifier;
	}

	final public String name() {
		return _name;
	}

	@Override
	final public String toString() {
		if (_contextIdentifier != null)
			return String.format("%s:%s", _contextIdentifier, _name);
		else
			return _name;
	}
}