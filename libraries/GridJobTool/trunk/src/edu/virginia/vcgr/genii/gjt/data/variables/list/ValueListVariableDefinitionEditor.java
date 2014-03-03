package edu.virginia.vcgr.genii.gjt.data.variables.list;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionEditor;
import edu.virginia.vcgr.genii.gjt.gui.icons.ShapeIcons;
import edu.virginia.vcgr.genii.gjt.gui.util.ButtonPanel;
import edu.virginia.vcgr.genii.gjt.gui.util.SimpleIconButton;

@SuppressWarnings("rawtypes")
class ValueListVariableDefinitionEditor extends
		VariableDefinitionEditor<ValueListVariableDefinition> {
	static final long serialVersionUID = 0L;

	private JList _list;
	private DefaultListModel _model = new DefaultListModel();
	private MinusAction _minusAction = new MinusAction();

	@Override
	public ValueListVariableDefinition getVariableDefinitionImpl() {
		Collection<String> values = new Vector<String>(_model.getSize());
		for (int lcv = 0; lcv < _model.getSize(); lcv++)
			values.add((String) _model.getElementAt(lcv));

		return new ValueListVariableDefinition(values);
	}

	@SuppressWarnings("unchecked")
	public ValueListVariableDefinitionEditor(Window owner) {
		super(owner, "Value List Variable Editor");

		_list = new JList(_model);

		Container container = getContentPane();
		container.setLayout(new GridBagLayout());

		container.add(new JLabel("Values"), new GridBagConstraints(0, 0, 1, 1,
				1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 0, 5), 0, 0));
		container.add(new JScrollPane(_list), new GridBagConstraints(0, 1, 1,
				1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 5, 5, 5), 0, 0));
		container.add(ButtonPanel.createHorizontalPanel(new SimpleIconButton(
				ShapeIcons.Plus, new PlusAction()), new SimpleIconButton(
				ShapeIcons.Minus, _minusAction)), new GridBagConstraints(0, 2,
				1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		container.add(ButtonPanel.createHorizontalPanel(new OKAction(),
				new CancelAction()), new GridBagConstraints(0, 3, 1, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		_list.addListSelectionListener(new ListSelectionListenerImpl());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setFromVariableDefinition(
			ValueListVariableDefinition variableDefinition) {
		_model.clear();
		for (String value : variableDefinition._values)
			_model.addElement(value);
	}

	private class PlusAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent e) {
			String answer = JOptionPane.showInputDialog(
					ValueListVariableDefinitionEditor.this,
					"What value would you like to add?", "New Value",
					JOptionPane.QUESTION_MESSAGE);
			if (answer != null)
				_model.addElement(answer);
		}
	}

	private class MinusAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private MinusAction() {
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int[] indices = _list.getSelectedIndices();
			for (int lcv = indices.length - 1; lcv >= 0; lcv--)
				_model.removeElementAt(indices[lcv]);
		}
	}

	private class OKAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private OKAction() {
			super("OK");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			accept();
		}
	}

	private class CancelAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private CancelAction() {
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			cancel();
		}
	}

	private class ListSelectionListenerImpl implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			_minusAction.setEnabled(_list.getSelectedIndex() >= 0);
		}
	}
}