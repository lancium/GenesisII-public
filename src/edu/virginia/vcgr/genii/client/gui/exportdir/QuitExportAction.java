package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class QuitExportAction extends AbstractAction implements ListSelectionListener
{
	static final long serialVersionUID = 0L;
	
	static final private String _QUIT_EXPORT_BUTTON = "Quit Export";
	
	private JTable _table;
	
	public QuitExportAction(JTable table)
	{
		super(_QUIT_EXPORT_BUTTON);
		
		ListSelectionModel selectionModel = table.getSelectionModel();
		setEnabled(!selectionModel.isSelectionEmpty());
		selectionModel.addListSelectionListener(this);
		_table = table;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		int selectedRow = _table.getSelectedRow();
		ExportTableModel model = (ExportTableModel)_table.getModel();
		
		ExportDirInformation info = model.getRow(selectedRow);
		System.err.format("User want's to quit %s -> %s\n",
			info.getLocalPath(), info.getRNSPath());
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		setEnabled(!((ListSelectionModel)e.getSource()).isSelectionEmpty());
	}
}