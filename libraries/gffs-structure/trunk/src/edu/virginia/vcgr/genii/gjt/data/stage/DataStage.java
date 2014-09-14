package edu.virginia.vcgr.genii.gjt.data.stage;

import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.util.EnumMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import edu.virginia.vcgr.genii.gjt.data.DefaultDataItem;
import edu.virginia.vcgr.genii.gjt.data.ModificationBroker;
import edu.virginia.vcgr.genii.gjt.data.Modifyable;
import edu.virginia.vcgr.genii.gjt.data.analyze.Analysis;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.genii.gjt.data.stage.undef.UndefinedStageData;
import edu.virginia.vcgr.genii.gjt.data.variables.Clearable;
import edu.virginia.vcgr.genii.gjt.data.variables.Parameterizable;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.xml.PostUnmarshallListener;
import edu.virginia.vcgr.genii.gjt.gui.util.GUIUtils;
import edu.virginia.vcgr.jsdl.CreationFlag;

public class DataStage extends DefaultDataItem implements PostUnmarshallListener, Clearable
{
	@XmlTransient
	private ParameterizableBroker _pBroker;

	@XmlTransient
	private ModificationBroker _mBroker;

	@XmlAttribute(name = "filename")
	private String _filename = "";

	@XmlAttribute(name = "filesystem")
	private FilesystemType _filesystem = FilesystemType.Default;

	@XmlAttribute(name = "delete-on-terminate")
	private boolean _deleteOnTerminate = true;

	@XmlAttribute(name = "creation-flag")
	private CreationFlag _creationFlag = CreationFlag.overwrite;

	@XmlJavaTypeAdapter(SerializableStageDataAdapter.class)
	private Map<StageProtocol, StageData> _history = new EnumMap<StageProtocol, StageData>(StageProtocol.class);

	@XmlAttribute(name = "current-stage-protocol")
	private StageProtocol _current = StageProtocol.undefined;

	public DataStage()
	{
		_history.put(StageProtocol.undefined, new UndefinedStageData());
	}

	public DataStage(ParameterizableBroker pBroker, ModificationBroker mBroker)
	{
		this();

		_pBroker = pBroker;
		_mBroker = mBroker;

		addParameterizableListener(pBroker);
		addModificationListener(mBroker);
	}

	public FilesystemType filesystemType()
	{
		return _filesystem;
	}

	public void filesystemType(FilesystemType newType)
	{
		_filesystem = newType;
		fireJobDescriptionModified();
	}

	public boolean deleteOnTerminate()
	{
		return _deleteOnTerminate;
	}

	public void deleteOnTerminate(boolean deleteOnTerminate)
	{
		if (_deleteOnTerminate != deleteOnTerminate)
			fireJobDescriptionModified();

		_deleteOnTerminate = deleteOnTerminate;
	}

	public CreationFlag creationFlag()
	{
		return _creationFlag;
	}

	public void creationFlag(CreationFlag creationFlag)
	{
		if (_creationFlag != creationFlag)
			fireJobDescriptionModified();

		_creationFlag = creationFlag;
	}

	public StageData current()
	{
		return _history.get(_current);
	}

	@SuppressWarnings("unchecked")
	public StageData edit(Window owner)
	{
		StageData current = _history.get(_current);
		if (current != null) {
			StageEditor<StageData> editor = (StageEditor<StageData>) current.protocol().factory().createEditor(owner);
			if (editor != null) {
				editor.setInitialData(current);
				editor.pack();
				editor.setModalityType(ModalityType.DOCUMENT_MODAL);
				GUIUtils.centerComponent(editor);
				editor.setVisible(true);
				current = editor.getStageData();
				if (current != null) {
					if (current instanceof Parameterizable)
						((Parameterizable) current).addParameterizableListener(_pBroker);
					if (current instanceof Modifyable)
						((Modifyable) current).addModificationListener(_mBroker);

					current.activate();
					_history.put(_current, current).deactivate();
					return current;
				}
			}
		}

		return null;
	}

	public StageData activate(Window owner, StageProtocol protocol)
	{
		StageData data = _history.get(protocol);
		if (data == null) {
			StageEditor<? extends StageData> editor = protocol.factory().createEditor(owner);
			editor.setModalityType(ModalityType.DOCUMENT_MODAL);
			editor.pack();
			GUIUtils.centerComponent(editor);
			editor.setVisible(true);
			data = editor.getStageData();
			if (data != null) {
				if (data instanceof Parameterizable)
					((Parameterizable) data).addParameterizableListener(_pBroker);
				if (data instanceof Modifyable)
					((Modifyable) data).addModificationListener(_mBroker);

				_history.put(protocol, data);
			}
		}

		if (data != null) {
			StageData oldData = _history.get(_current);
			oldData.deactivate();

			_current = protocol;
			data.activate();
			fireJobDescriptionModified();
			return data;
		} else
			return null;
	}

	public String filename()
	{
		return _filename;
	}

	public void filename(String filename)
	{
		String old = _filename;
		_filename = filename;

		fireParameterizableStringModified(old, _filename);
		fireJobDescriptionModified();
	}

	@Override
	public void postUnmarshall(ParameterizableBroker parameterBroker, ModificationBroker modificationBroker)
	{
		_pBroker = parameterBroker;
		_mBroker = modificationBroker;

		if (_current != null) {
			StageData data = _history.get(_current);
			if (data != null)
				data.activate();
		}
	}

	@Override
	public void clear()
	{
		fireParameterizableStringModified(_filename, "");

		if (_current != null) {
			StageData sd = _history.get(_current);
			if (sd != null)
				sd.deactivate();
		}
	}

	public void analyze(Analysis analysis)
	{
		if (_filename == null || _filename.isEmpty())
			analysis.addError("Cannot have a data stage with no filename.");

		StageData data = current();
		data.analyze(_filename, analysis);
	}
}