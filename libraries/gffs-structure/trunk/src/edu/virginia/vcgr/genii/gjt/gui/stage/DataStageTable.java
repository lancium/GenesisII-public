package edu.virginia.vcgr.genii.gjt.gui.stage;

import java.awt.Color;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.data.stage.StageList;
import edu.virginia.vcgr.genii.gjt.data.stage.StageProtocol;
import edu.virginia.vcgr.genii.gjt.gui.fs.FilesystemCellRenderer;
import edu.virginia.vcgr.genii.gjt.gui.fs.FilesystemCombo;
import edu.virginia.vcgr.jsdl.CreationFlag;

@SuppressWarnings("rawtypes")
public class DataStageTable extends JTable
{
	static final long serialVersionUID = 0L;

	@SuppressWarnings("unchecked")
	static private JComboBox createStageProtocolComboBox(boolean isStageIn)
	{
		Vector<StageProtocol> protocols = new Vector<StageProtocol>(StageProtocol.values().length);

		for (StageProtocol protocol : StageProtocol.values()) {
			if (isStageIn) {
				if (protocol.stageDirection().canRead())
					protocols.add(protocol);
			} else {
				if (protocol.stageDirection().canWrite())
					protocols.add(protocol);
			}
		}

		return new JComboBox(protocols);
	}

	@SuppressWarnings("unchecked")
	DataStageTable(FilesystemMap filesystemMap, StageList stageList, boolean isStageIn)
	{
		super(new DataStageTableModel(stageList));
		DataStageTableModel model = (DataStageTableModel) getModel();
		model.setOwner(this);

		setShowHorizontalLines(true);
		setShowGrid(true);
		setGridColor(Color.lightGray);

		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(false);

		TableColumnModel cModel = getColumnModel();
		TableColumn filenameColumn = cModel.getColumn(0);
		TableColumn stageProtocolColumn = cModel.getColumn(1);
		TableColumn stageURIColumn = cModel.getColumn(2);
		TableColumn creationFlagColumn = cModel.getColumn(3);
		TableColumn deleteOnTerminateColumn = cModel.getColumn(4);
		TableColumn fsColumn = cModel.getColumn(5);

		filenameColumn.setHeaderValue("Filename");

		stageProtocolColumn.setHeaderValue("Transfer Protocol");
		stageProtocolColumn.setCellEditor(new DefaultCellEditor(createStageProtocolComboBox(isStageIn)));

		stageURIColumn.setHeaderValue("Stage URI");
		stageURIColumn.setCellRenderer(new DataStageCellRenderer());
		stageURIColumn.setCellEditor(new DataStageCellEditor());
		stageURIColumn.setMinWidth(200);

		creationFlagColumn.setHeaderValue("Creation Mode");
		creationFlagColumn.setCellEditor(new DefaultCellEditor(new JComboBox(CreationFlag.values())));

		deleteOnTerminateColumn.setHeaderValue("Delete on Terminate?");

		fsColumn.setHeaderValue("Filesystem");
		fsColumn.setCellRenderer(new FilesystemCellRenderer());
		fsColumn.setCellEditor(new DefaultCellEditor(new FilesystemCombo(filesystemMap)));

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}
}
