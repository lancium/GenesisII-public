package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;

public class ExportDirDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	static final private String _TITLE = "Export Directory Tool";
	static final private String _EXPORTS_TITLE = "Current Exports";
	
	private ExportTableModel _model;
	private JTable _table;
	
	public ExportDirDialog() throws FileLockException
	{
		Container container;
		JPanel panel;
		JButton button;
		
		setTitle(_TITLE);
		
		container = getContentPane();
		container.setLayout(new GridBagLayout());
		
		panel = createExportList();
		container.add(panel, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		ExportDataAction action = new ExportDataAction(this);
		action.addExportChangeListener(_model);
		button = new JButton(action);
		container.add(button, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		
		QuitExportAction quitAction = new QuitExportAction(_table);
		quitAction.addExportChangeListener(_model);
		button = new JButton(quitAction);
		container.add(button, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
	}
	
	private JPanel createExportList() throws FileLockException
	{
		JPanel panel = new JPanel(new GridBagLayout());
		
		panel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(Color.BLACK),
			_EXPORTS_TITLE, TitledBorder.LEFT, TitledBorder.TOP));
		
		_model = new ExportTableModel();
		_table = new JTable(_model);
		_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		panel.add(new JScrollPane(_table), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		return panel;
	}
}