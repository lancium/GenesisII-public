package edu.virginia.vcgr.genii.gjt.gui.stage;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;
import edu.virginia.vcgr.genii.gjt.data.stage.DataStage;
import edu.virginia.vcgr.genii.gjt.data.stage.StageData;
import edu.virginia.vcgr.genii.gjt.data.stage.StageList;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;
import edu.virginia.vcgr.jsdl.CreationFlag;

class DataStageTableModel extends AbstractTableModel
{
	static final long serialVersionUID = 0L;

	private StageList _stageList;
	private boolean _isStageIn;

	private JComponent _owner = null;

	DataStageTableModel(StageList stageList, boolean isStageIn)
	{
		_stageList = stageList;
		_isStageIn = isStageIn;
	}

	void setOwner(JComponent owner)
	{
		_owner = owner;
	}

	@Override
	public int getColumnCount()
	{
		if(_isStageIn)
			return 7;
		else
			return 8;
	}

	@Override
	public int getRowCount()
	{
		return _stageList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		DataStage stage = _stageList.get(rowIndex);

		switch (columnIndex) {
			case 0:
				return stage.filename();

			case 1:
				return stage.current().protocol();

			case 2:
				return stage;

			case 3:
				return stage.creationFlag();

			case 4:
				return stage.deleteOnTerminate();

			case 5:
				return stage.handleAsArchive();

			case 6:
				return stage.filesystemType();
				
			case 7:
				return stage.alwaysStageOut();
		}

		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		DataStage stage = _stageList.get(rowIndex);

		switch (columnIndex) {
			case 0:
				stage.filename((String) aValue);
				break;

			case 1:
				// We tried to switch what type of stage it is.
				StageProtocol protocol = (StageProtocol) aValue;
				StageData newData = stage.activate(SwingUtilities.getWindowAncestor(_owner), protocol);
				if (newData != null)
					fireTableCellUpdated(rowIndex, 2);

				break;

			case 2:
				// nothing to do
				break;

			case 3:
				stage.creationFlag((CreationFlag) aValue);
				break;

			case 4:
				stage.deleteOnTerminate(((Boolean) aValue).booleanValue());
				break;

			case 5:
				stage.handleAsArchive(((Boolean) aValue).booleanValue());
				break;

			case 6:
				stage.filesystemType((FilesystemType) aValue);
				break;
				
			case 7:
				stage.alwaysStageOut(((Boolean) aValue).booleanValue());
				break;
		}

		fireTableCellUpdated(rowIndex, columnIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return true;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		switch (columnIndex) {
			case 0:
				return String.class;
			case 1:
				return StageProtocol.class;
			case 2:
				return DataStage.class;
			case 3:
				return CreationFlag.class;
			case 4:
				return Boolean.class;
			case 5:
				return Boolean.class;
			case 6:
				return FilesystemType.class;
			case 7:
				return Boolean.class;
		}

		return null;
	}

	public DataStage addRow()
	{
		DataStage ret = _stageList.add();
		fireTableRowsInserted(_stageList.size() - 1, _stageList.size() - 1);

		return ret;
	}

	public void removeRow(int row)
	{
		_stageList.remove(row);
		fireTableRowsDeleted(row, row);
	}
}