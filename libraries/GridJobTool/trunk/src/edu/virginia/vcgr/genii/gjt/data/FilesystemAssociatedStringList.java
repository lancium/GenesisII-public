package edu.virginia.vcgr.genii.gjt.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.data.variables.Clearable;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.xml.PostUnmarshallListener;

public class FilesystemAssociatedStringList extends DefaultDataItem implements Clearable, PostUnmarshallListener,
	Iterable<StringFilesystemPair>
{
	static final long serialVersionUID = 0L;

	@XmlTransient
	private ParameterizableBroker _pBroker = null;

	@XmlTransient
	private ModificationBroker _mBroker = null;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "value")
	private Vector<StringFilesystemPair> _items = new Vector<StringFilesystemPair>();

	public FilesystemAssociatedStringList()
	{
	}

	public FilesystemAssociatedStringList(ParameterizableBroker pBroker, ModificationBroker mBroker)
	{
		_pBroker = pBroker;
		_mBroker = mBroker;

		addParameterizableListener(pBroker);
		addModificationListener(mBroker);
	}

	public void add(StringFilesystemPair value)
	{
		_items.add(value);
		fireParameterizableStringModified("", value.get());
		fireJobDescriptionModified();
	}

	public void remove(int index)
	{
		StringFilesystemPair item = _items.remove(index);
		if (item != null) {
			fireParameterizableStringModified(item.get(), "");
			fireJobDescriptionModified();
		}
	}

	public void moveUp(int index)
	{
		StringFilesystemPair pair = _items.remove(index);
		_items.insertElementAt(pair, index - 1);
		fireJobDescriptionModified();
	}

	public void moveDown(int index)
	{
		StringFilesystemPair pair = _items.remove(index);
		_items.insertElementAt(pair, index + 1);
		fireJobDescriptionModified();
	}

	public StringFilesystemPair get(int index)
	{
		return _items.get(index);
	}

	public int size()
	{
		return _items.size();
	}

	@Override
	public void clear()
	{
		for (StringFilesystemPair item : _items)
			fireParameterizableStringModified(item.get(), "");
		if (_items.size() > 0)
			fireJobDescriptionModified();

		_items.clear();
	}

	public ParameterizableBroker getParameterizableBroker()
	{
		return _pBroker;
	}

	public ModificationBroker getModificationBroker()
	{
		return _mBroker;
	}

	@Override
	public void postUnmarshall(ParameterizableBroker parameterBroker, ModificationBroker modificationBroker)
	{
		_pBroker = parameterBroker;
		_mBroker = modificationBroker;

		/*
		 * Don't need to do this because we derive off of a class that automatically does it
		 */
		/*
		 * addParameterizableListener(parameterBroker); addModificationListener(modificationBroker);
		 */

		for (StringFilesystemPair item : _items)
			fireParameterizableStringModified("", item.get());

		fireJobDescriptionModified();
	}

	@Override
	public Iterator<StringFilesystemPair> iterator()
	{
		return Collections.unmodifiableList(_items).iterator();
	}
}