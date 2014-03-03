package edu.virginia.vcgr.genii.gjt.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.data.variables.Clearable;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.xml.PostUnmarshallListener;

public class EnvironmentList extends DefaultDataItem implements Clearable,
		PostUnmarshallListener, Iterable<StringStringFilesystemTriple> {
	static final long serialVersionUID = 0L;

	@XmlTransient
	private ParameterizableBroker _pBroker = null;

	@XmlTransient
	private ModificationBroker _mBroker = null;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "value")
	private Vector<StringStringFilesystemTriple> _items = new Vector<StringStringFilesystemTriple>();

	public EnvironmentList() {
	}

	public EnvironmentList(ParameterizableBroker pBroker,
			ModificationBroker mBroker) {
		_pBroker = pBroker;
		_mBroker = mBroker;

		addParameterizableListener(pBroker);
		addModificationListener(mBroker);
	}

	public void add(StringStringFilesystemTriple value) {
		_items.add(value);
		fireParameterizableStringModified("", value.getKey());
		fireParameterizableStringModified("", value.getValue());
		fireJobDescriptionModified();
	}

	public void remove(int index) {
		StringStringFilesystemTriple item = _items.remove(index);
		if (item != null) {
			fireParameterizableStringModified(item.getKey(), "");
			fireParameterizableStringModified(item.getValue(), "");
			fireJobDescriptionModified();
		}
	}

	public StringStringFilesystemTriple get(int index) {
		return _items.get(index);
	}

	public int size() {
		return _items.size();
	}

	@Override
	public void clear() {
		for (StringStringFilesystemTriple item : _items) {
			fireParameterizableStringModified(item.getKey(), "");
			fireParameterizableStringModified(item.getValue(), "");
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

		for (StringStringFilesystemTriple item : _items) {
			fireParameterizableStringModified("", item.getKey());
			fireParameterizableStringModified("", item.getValue());
		}

		fireJobDescriptionModified();
	}

	@Override
	public Iterator<StringStringFilesystemTriple> iterator() {
		return Collections.unmodifiableList(_items).iterator();
	}
}