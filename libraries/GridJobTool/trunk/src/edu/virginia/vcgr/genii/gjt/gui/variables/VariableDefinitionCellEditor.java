package edu.virginia.vcgr.genii.gjt.gui.variables;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionEditor;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionType;
import edu.virginia.vcgr.genii.gjt.gui.util.GUIUtils;

public class VariableDefinitionCellEditor extends AbstractCellEditor implements
		TableCellEditor {
	static final long serialVersionUID = 0l;

	private VariableHistory _currentValue = null;
	private JButton _button;

	public VariableDefinitionCellEditor() {
		_button = new JButton(new EditAction());
		_button.setBorderPainted(false);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		_currentValue = (VariableHistory) value;
		return _button;
	}

	@Override
	public Object getCellEditorValue() {
		return _currentValue;
	}

	private class EditAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			VariableDefinition current = _currentValue.current();
			VariableDefinitionType type = current.type();

			VariableDefinitionEditor<VariableDefinition> editor = (VariableDefinitionEditor<VariableDefinition>) type
					.editorFactory().createEditor(
							SwingUtilities.getWindowAncestor((JComponent) e
									.getSource()));
			editor.setFromVariableDefinition(current);
			editor.pack();
			editor.setModalityType(ModalityType.DOCUMENT_MODAL);
			GUIUtils.centerComponent(editor);
			editor.setVisible(true);
			current = editor.getVariableDefinition();
			if (current != null) {
				_currentValue.current(current);
				fireEditingStopped();
			} else
				fireEditingCanceled();
		}
	}
}