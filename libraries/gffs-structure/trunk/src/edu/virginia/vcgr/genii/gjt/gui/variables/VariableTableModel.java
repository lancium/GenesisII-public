package edu.virginia.vcgr.genii.gjt.gui.variables;

import java.awt.Dialog.ModalityType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.data.JobDocument;
import edu.virginia.vcgr.genii.gjt.data.ModificationBroker;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionEditor;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionType;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableListener;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableManager;
import edu.virginia.vcgr.genii.gjt.data.variables.undef.UndefinedVariableDefinition;
import edu.virginia.vcgr.genii.gjt.gui.util.GUIUtils;

class VariableTableModel extends AbstractTableModel
{
	static final long serialVersionUID = 0L;

	private ModificationBroker _mBroker;

	private Map<String, VariableDefinition> _documentVariables;
	private Map<String, VariableHistory> _variableHistory;
	private Vector<String> _indices;

	private JComponent _owner = null;

	VariableTableModel(JobDocumentContext context)
	{
		_mBroker = context.getModificationBroker();

		JobDocument document = context.jobDocument();
		_documentVariables = document.variables();

		_variableHistory = new HashMap<String, VariableHistory>();

		for (Map.Entry<String, VariableDefinition> entry : _documentVariables.entrySet()) {
			_variableHistory.put(entry.getKey(), new VariableHistory(entry.getValue()));
		}

		for (String variable : context.variableManager().variables()) {
			if (!_variableHistory.keySet().contains(variable))
				_variableHistory.put(variable, new VariableHistory());
		}

		_indices = new Vector<String>(_variableHistory.keySet());
		Collections.sort(_indices);

		context.variableManager().addVariableListener(new VariableListenerImpl());
	}

	void setOwner(JComponent owner)
	{
		_owner = owner;
	}

	@Override
	public int getColumnCount()
	{
		return 3;
	}

	@Override
	public int getRowCount()
	{
		return _indices.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		String variable = _indices.get(rowIndex);

		switch (columnIndex) {
			case 0:
				return variable;

			case 1:
			case 2:
				return _variableHistory.get(variable);
		}

		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		switch (columnIndex) {
			case 0:
				return false;

			case 1:
				return true;
		}

		String variable = _indices.get(rowIndex);
		VariableHistory history = _variableHistory.get(variable);
		return history.current().type() != VariableDefinitionType.UndefinedVariable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		String variable = _indices.get(rowIndex);
		VariableHistory history = _variableHistory.get(variable);

		if (columnIndex == 1) {
			VariableDefinitionType newType = (VariableDefinitionType) aValue;
			VariableDefinition definition = history.getHistorical(newType);
			if (definition == null) {
				VariableDefinitionEditor<VariableDefinition> editor =
					(VariableDefinitionEditor<VariableDefinition>) newType.editorFactory().createEditor(
						SwingUtilities.getWindowAncestor(_owner));
				editor.pack();
				editor.setModalityType(ModalityType.DOCUMENT_MODAL);
				GUIUtils.centerComponent(editor);
				editor.setVisible(true);
				definition = editor.getVariableDefinition();
			}

			if (definition != null) {
				history.current(definition);
				_documentVariables.put(variable, definition);
				fireTableRowsUpdated(rowIndex, rowIndex);
				_mBroker.fireJobDescriptionModified();
			}
		} else {
			_documentVariables.put(variable, history.current());
			fireTableRowsUpdated(rowIndex, rowIndex);
			_mBroker.fireJobDescriptionModified();
		}
	}

	private class VariableListenerImpl implements VariableListener
	{
		@Override
		public void variableAdded(VariableManager manager, String variableName)
		{
			_indices.add(variableName);
			VariableHistory history = _variableHistory.get(variableName);
			if (history == null) {
				history = new VariableHistory(new UndefinedVariableDefinition());
				_variableHistory.put(variableName, history);
			}

			_documentVariables.put(variableName, history.current());
			_mBroker.fireJobDescriptionModified();

			fireTableRowsInserted(_indices.size() - 1, _indices.size() - 1);
		}

		@Override
		public void variableRemoved(VariableManager manager, String variableName)
		{
			int index = _indices.indexOf(variableName);
			if (index >= 0) {
				_indices.remove(index);
				_documentVariables.remove(variableName);
				_mBroker.fireJobDescriptionModified();
				fireTableRowsDeleted(index, index);
			}
		}
	}
}