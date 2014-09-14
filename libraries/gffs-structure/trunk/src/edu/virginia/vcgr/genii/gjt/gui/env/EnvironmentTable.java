package edu.virginia.vcgr.genii.gjt.gui.env;

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import edu.virginia.vcgr.genii.gjt.data.EnvironmentList;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.gui.fs.FilesystemCellRenderer;
import edu.virginia.vcgr.genii.gjt.gui.fs.FilesystemCombo;
import edu.virginia.vcgr.genii.gjt.gui.util.DefaultGrayedEmptyCellRenderer;

class EnvironmentTable extends JTable
{
	static final long serialVersionUID = 0L;

	EnvironmentTable(FilesystemMap filesystemMap, EnvironmentList environment)
	{
		super(new EnvironmentTableModel(environment));

		setShowHorizontalLines(true);
		setShowGrid(true);
		setGridColor(Color.lightGray);

		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);

		TableColumnModel cModel = getColumnModel();
		TableColumn keyColumn = cModel.getColumn(0);
		TableColumn valueColumn = cModel.getColumn(1);
		TableColumn fsColumn = cModel.getColumn(2);

		keyColumn.setHeaderValue("Variable");
		keyColumn.setCellRenderer(new DefaultGrayedEmptyCellRenderer("Variable"));

		valueColumn.setHeaderValue("Value");
		valueColumn.setCellRenderer(new DefaultGrayedEmptyCellRenderer("Value"));

		fsColumn.setHeaderValue("Filesystem");
		fsColumn.setCellRenderer(new FilesystemCellRenderer());
		fsColumn.setCellEditor(new DefaultCellEditor(new FilesystemCombo(filesystemMap)));

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}
}