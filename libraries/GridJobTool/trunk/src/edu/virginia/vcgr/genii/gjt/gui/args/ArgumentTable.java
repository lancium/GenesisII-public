package edu.virginia.vcgr.genii.gjt.gui.args;

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import edu.virginia.vcgr.genii.gjt.data.FilesystemAssociatedStringList;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemMap;
import edu.virginia.vcgr.genii.gjt.gui.fs.FilesystemCellRenderer;
import edu.virginia.vcgr.genii.gjt.gui.fs.FilesystemCombo;
import edu.virginia.vcgr.genii.gjt.gui.util.DefaultGrayedEmptyCellRenderer;

class ArgumentTable extends JTable
{
	static final long serialVersionUID = 0L;

	ArgumentTable(FilesystemMap filesystemMap, FilesystemAssociatedStringList arguments)
	{
		super(new ArgumentTableModel(arguments));

		setShowHorizontalLines(true);
		setShowGrid(true);
		setGridColor(Color.lightGray);

		TableColumnModel cModel = getColumnModel();
		TableColumn indexColumn = cModel.getColumn(0);
		TableColumn textColumn = cModel.getColumn(1);
		TableColumn fsColumn = cModel.getColumn(2);

		indexColumn.setHeaderValue("");
		indexColumn.setMaxWidth(125);

		textColumn.setHeaderValue("Argument");
		textColumn.setCellRenderer(new DefaultGrayedEmptyCellRenderer("Argument"));

		fsColumn.setHeaderValue("Filesystem");
		fsColumn.setCellRenderer(new FilesystemCellRenderer());
		fsColumn.setCellEditor(new DefaultCellEditor(new FilesystemCombo(filesystemMap)));

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}
}