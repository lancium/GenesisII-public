package edu.virginia.vcgr.genii.gjt.gui.variables;

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;

public class VariableTable extends JTable {
	static final long serialVersionUID = 0L;

	public VariableTable(JobDocumentContext context) {
		super(new VariableTableModel(context));

		VariableTableModel model = (VariableTableModel) getModel();
		model.setOwner(this);

		setGridColor(Color.lightGray);

		TableColumnModel cModel = getColumnModel();

		TableColumn variableNameColumn = cModel.getColumn(0);
		TableColumn typeColumn = cModel.getColumn(1);
		TableColumn valueColumn = cModel.getColumn(2);

		variableNameColumn.setHeaderValue("Variable");

		typeColumn.setHeaderValue("Variable Type");
		typeColumn.setCellRenderer(new VariableDefinitionTypeRenderer());
		typeColumn.setCellEditor(new DefaultCellEditor(
				new VariableDefinitionTypeCombo()));

		valueColumn.setHeaderValue("Values");
		valueColumn.setCellRenderer(new VariableDefinitionRenderer());
		valueColumn.setCellEditor(new VariableDefinitionCellEditor());

		setAutoCreateRowSorter(true);

		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}
}