package edu.virginia.vcgr.genii.gjt.gui.resource;

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import edu.virginia.vcgr.genii.gjt.data.MatchingParameterList;
import edu.virginia.vcgr.genii.gjt.gui.util.DefaultGrayedEmptyCellRenderer;

@SuppressWarnings("rawtypes")
public class MatchingParameterTable extends JTable
{
	static final long serialVersionUID = 0L;

	@SuppressWarnings("unchecked")
	MatchingParameterTable(MatchingParameterList parameters)
	{
		super(new MatchingParameterTableModel(parameters));

		setShowHorizontalLines(true);
		setShowGrid(true);
		setGridColor(Color.lightGray);

		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);

		TableColumnModel cModel = getColumnModel();
		TableColumn keyColumn = cModel.getColumn(0);
		TableColumn valueColumn = cModel.getColumn(1);
		TableColumn typeColumn = cModel.getColumn(2);

		keyColumn.setHeaderValue("Variable");
		keyColumn.setCellRenderer(new DefaultGrayedEmptyCellRenderer("Variable"));

		valueColumn.setHeaderValue("Value");
		valueColumn.setCellRenderer(new DefaultGrayedEmptyCellRenderer("Value"));

		JComboBox comboBox = new JComboBox();
		comboBox.addItem("requires");
		comboBox.addItem("supports");
		typeColumn.setHeaderValue("Type");
		typeColumn.setCellRenderer(new DefaultGrayedEmptyCellRenderer("Type"));
		typeColumn.setCellEditor(new DefaultCellEditor(comboBox));

		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
	}
}