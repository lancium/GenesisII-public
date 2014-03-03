package edu.virginia.vcgr.genii.gjt.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.data.variables.Clearable;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.xml.PostUnmarshallListener;

public class MatchingParameterList extends DefaultDataItem implements
		Clearable, PostUnmarshallListener, Iterable<StringStringPair> {
	static final long serialVersionUID = 0L;

	@XmlTransient
	private ParameterizableBroker _pBroker = null;

	@XmlTransient
	private ModificationBroker _mBroker = null;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "value")
	private Vector<StringStringPair> _items = new Vector<StringStringPair>();

	public MatchingParameterList() {
	}

	public MatchingParameterList(ParameterizableBroker pBroker,
			ModificationBroker mBroker) {
		_pBroker = pBroker;
		_mBroker = mBroker;

		addParameterizableListener(pBroker);
		addModificationListener(mBroker);
	}

	public void add(StringStringPair value) {
		_items.add(value);
		fireParameterizableStringModified("", value.name());
		fireParameterizableStringModified("", value.value());
		fireJobDescriptionModified();
	}

	public void remove(int index) {
		StringStringPair item = _items.remove(index);
		if (item != null) {
			fireParameterizableStringModified(item.name(), "");
			fireParameterizableStringModified(item.value(), "");
			fireJobDescriptionModified();
		}
	}

	public StringStringPair get(int index) {
		return _items.get(index);
	}

	public int size() {
		return _items.size();
	}

	@Override
	public void clear() {
		for (StringStringPair item : _items) {
			fireParameterizableStringModified(item.name(), "");
			fireParameterizableStringModified(item.value(), "");
		}

		if (_items.size() > 0)
			fireJobDescriptionModified();

		_items.clear();
	}

	public ParameterizableBroker getParameterizableBroker() {
		return _pBroker;
	}

	public ModificationBroker getModificationBroker() {
		return _mBroker;
	}

	@Override
	public void postUnmarshall(ParameterizableBroker parameterBroker,
			ModificationBroker modificationBroker) {
		_pBroker = parameterBroker;
		_mBroker = modificationBroker;

		/*
		 * Don't need to do this because we derive off of a class that
		 * automatically does it
		 */
		/*
		 * addParameterizableListener(parameterBroker);
		 * addModificationListener(modificationBroker);
		 */

		for (StringStringPair item : _items) {
			fireParameterizableStringModified("", item.name());
			fireParameterizableStringModified("", item.value());
		}

		fireJobDescriptionModified();
	}

	@Override
	public Iterator<StringStringPair> iterator() {
		return Collections.unmodifiableList(_items).iterator();
	}
}