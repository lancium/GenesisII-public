package edu.virginia.vcgr.genii.gjt.gui.variables;

import javax.swing.table.DefaultTableCellRenderer;

public class VariableDefinitionTypeRenderer extends DefaultTableCellRenderer {
	static final long serialVersionUID = 0l;

	@Override
	public void setValue(Object value) {
		VariableHistory history = (VariableHistory) value;

		setIcon(null);
		setText(history.current().type().toString());
	}
}