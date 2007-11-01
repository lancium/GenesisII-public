package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class QuitExportAction extends AbstractAction implements ListSelectionListener
{
	static final long serialVersionUID = 0L;
	
	static final private String _QUIT_EXPORT_BUTTON = "Quit Export";
	
	private JTable _table;
	private Collection<IExportChangeListener> _listeners = new ArrayList<IExportChangeListener>();
	
	public QuitExportAction(JTable table)
	{
		super(_QUIT_EXPORT_BUTTON);
		
		ListSelectionModel selectionModel = table.getSelectionModel();
		setEnabled(!selectionModel.isSelectionEmpty());
		selectionModel.addListSelectionListener(this);
		_table = table;
	}
	
	public void addExportChangeListener(IExportChangeListener listener)
	{
		_listeners.add(listener);
	}
	
	public void removeExportChangeListener(IExportChangeListener listener)
	{
		_listeners.remove(listener);
	}
	
	protected void fireExportsChanged()
	{
		for (IExportChangeListener listener : _listeners)
		{
			listener.exportsUpdated();
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			int selectedRow = _table.getSelectedRow();
			ExportTableModel model = (ExportTableModel)_table.getModel();
			
			ExportDirInformation info = model.getRow(selectedRow);
	
			// TODO
			/*
			ExportManipulator.quitExport(info.getRootEndpoint());
			*/
			ExportDirState.removeExport(info);
			fireExportsChanged();
		}
		catch (Throwable cause)
		{
			JOptionPane.showMessageDialog(_table, cause.getLocalizedMessage(), "Export Exception",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		setEnabled(!((ListSelectionModel)e.getSource()).isSelectionEmpty());
	}
}