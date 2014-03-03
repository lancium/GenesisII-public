package edu.virginia.vcgr.genii.gjt.data.stage;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import edu.virginia.vcgr.genii.gjt.data.DefaultDataItem;
import edu.virginia.vcgr.genii.gjt.data.JobDocumentConstants;
import edu.virginia.vcgr.genii.gjt.data.ModificationBroker;
import edu.virginia.vcgr.genii.gjt.data.variables.Clearable;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.xml.PostUnmarshallListener;

public class StageList extends DefaultDataItem implements Clearable,
		PostUnmarshallListener, Iterable<DataStage> {
	static final long serialVersionUID = 0L;

	@XmlTransient
	private ParameterizableBroker _pBroker = null;

	@XmlTransient
	private ModificationBroker _mBroker = null;

	@XmlElement(namespace = JobDocumentConstants.DOCUMENT_NAMESPACE, name = "data-stage")
	private Vector<DataStage> _stages = new Vector<DataStage>();

	public StageList() {
	}

	public StageList(ParameterizableBroker pBroker, ModificationBroker mBroker) {
		_pBroker = pBroker;
		_mBroker = mBroker;

		addParameterizableListener(pBroker);
		addModificationListener(mBroker);
	}

	public DataStage add() {
		DataStage newStage = new DataStage(_pBroker, _mBroker);
		_stages.add(newStage);
		fireJobDescriptionModified();

		return newStage;
	}

	public void remove(int index) {
		DataStage stage = _stages.remove(index);
		if (stage != null) {
			stage.clear();
			fireJobDescriptionModified();
		}
	}

	public DataStage get(int index) {
		return _stages.get(index);
	}

	public int size() {
		return _stages.size();
	}

	@Override
	public void clear() {
		for (DataStage stage : _stages)
			stage.clear();

		if (_stages.size() > 0)
			fireJobDescriptionModified();

		_stages.clear();
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

		fireJobDescriptionModified();
	}

	@Override
	public Iterator<DataStage> iterator() {
		return Collections.unmodifiableList(_stages).iterator();
	}
}