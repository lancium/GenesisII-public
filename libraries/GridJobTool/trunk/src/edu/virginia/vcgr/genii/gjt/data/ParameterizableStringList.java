package edu.virginia.vcgr.genii.gjt.data;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.gjt.data.variables.Clearable;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.xml.PostUnmarshallListener;

public class ParameterizableStringList extends DefaultDataItem implements Clearable, PostUnmarshallListener, Iterable<String>
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "value")
	private Vector<String> _values = new Vector<String>();

	public ParameterizableStringList()
	{
	}

	public ParameterizableStringList(ParameterizableBroker pBroker, ModificationBroker mBroker)
	{
		addParameterizableListener(pBroker);
		addModificationListener(mBroker);
	}

	public void add(String value)
	{
		_values.add(value);
		fireParameterizableStringModified("", value);
		fireJobDescriptionModified();
	}

	public void remove(String value)
	{
		if (_values.remove(value)) {
			fireParameterizableStringModified(value, "");
			fireJobDescriptionModified();
		}
	}

	public String get(int index)
	{
		return _values.get(index);
	}

	public int size()
	{
		return _values.size();
	}

	@Override
	public void clear()
	{
		for (String value : _values)
			fireParameterizableStringModified(value, "");
		if (_values.size() > 0)
			fireJobDescriptionModified();

		_values.clear();
	}

	@Override
	public void postUnmarshall(ParameterizableBroker parameterBroker, ModificationBroker modificationBroker)
	{
		/* SHouldn't need to do this */
		/*
		 * addParameterizableListener(parameterBroker); addModificationListener(modificationBroker);
		 */

		for (String value : _values)
			fireParameterizableStringModified("", value);

		fireJobDescriptionModified();
	}

	@Override
	public Iterator<String> iterator()
	{
		return Collections.unmodifiableList(_values).iterator();
	}
}