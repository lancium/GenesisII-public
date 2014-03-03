package edu.virginia.vcgr.genii.gjt.data;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.variables.Clearable;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.xml.PostUnmarshallListener;

public class ParameterizableString extends DefaultDataItem implements
		Clearable, PostUnmarshallListener {
	@XmlAttribute(name = "value")
	private String _value = "";

	public ParameterizableString() {
		set(null);
	}

	public ParameterizableString(ParameterizableBroker pBroker,
			ModificationBroker mBroker) {
		addParameterizableListener(pBroker);
		addModificationListener(mBroker);

		set(null);
	}

	public boolean isEmpty() {
		return _value.equals("");
	}

	public String get() {
		return _value;
	}

	public void set(String value) {
		if (value == null)
			value = "";

		String oldValue = _value;
		_value = value;

		if (!oldValue.equals(_value)) {
			fireParameterizableStringModified(oldValue, _value);
			fireJobDescriptionModified();
		}
	}

	@Override
	public String toString() {
		return _value;
	}

	@Override
	public void clear() {
		set("");
	}

	@Override
	public void postUnmarshall(ParameterizableBroker parameterBroker,
			ModificationBroker modificationBroker) {
		/* Don't need to do this */
		/*
		 * addParameterizableListener(parameterBroker);
		 * addModificationListener(modificationBroker);
		 */

		fireParameterizableStringModified("", _value);
		fireJobDescriptionModified();
	}
}