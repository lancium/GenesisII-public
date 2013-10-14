package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

class OIDNameTable extends JTable
{
	static final long serialVersionUID = 0L;

	OIDNameTable()
	{
		super(new OIDNameTableModel());

		addFocusListener(new FocusAdapter()
		{

			@Override
			public void focusLost(FocusEvent e)
			{
				TableCellEditor editor = getCellEditor();
				if (editor != null)
					editor.stopCellEditing();
			}
		});
	}
}