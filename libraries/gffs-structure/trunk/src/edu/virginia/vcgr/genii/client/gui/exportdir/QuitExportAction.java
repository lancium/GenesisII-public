package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;

public class QuitExportAction extends AbstractAction implements
		ListSelectionListener {
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(QuitExportAction.class);

	static final private String _QUIT_EXPORT_BUTTON = "Stop Exporting";

	private JTable _table;
	private Collection<IExportChangeListener> _listeners = new ArrayList<IExportChangeListener>();

	public QuitExportAction(JTable table) {
		super(_QUIT_EXPORT_BUTTON);

		ListSelectionModel selectionModel = table.getSelectionModel();
		setEnabled(!selectionModel.isSelectionEmpty());
		selectionModel.addListSelectionListener(this);
		_table = table;
	}

	public void addExportChangeListener(IExportChangeListener listener) {
		_listeners.add(listener);
	}

	public void removeExportChangeListener(IExportChangeListener listener) {
		_listeners.remove(listener);
	}

	protected void fireExportsChanged() {
		for (IExportChangeListener listener : _listeners) {
			listener.exportsUpdated();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int selectedRow = _table.getSelectedRow();
		ExportTableModel model = (ExportTableModel) _table.getModel();

		ExportDirInformation info = model.getRow(selectedRow);

		try {

			ExportManipulator.quitExport(info.getRNSPath());

		} catch (Throwable cause) {
			GuiUtils.displayError(_table, "Export Exception", cause);
		} finally {
			try {
				ExportDirState.removeExport(info);
			} catch (FileLockException e1) {
				_logger.error("caught unexpected exception", e1);
			} catch (IOException e1) {
				_logger.error("caught unexpected exception", e1);
			}
			fireExportsChanged();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		setEnabled(!((ListSelectionModel) e.getSource()).isSelectionEmpty());
	}
}