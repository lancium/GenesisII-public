package edu.virginia.vcgr.genii.gjt.gui.util;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.virginia.vcgr.genii.gjt.data.ParameterizableStringList;
import edu.virginia.vcgr.genii.gjt.gui.icons.ShapeIcons;

@SuppressWarnings("rawtypes")
class ParameterizableStringListEditorDialog extends JDialog {
	static final long serialVersionUID = 0L;

	private boolean _cancelled = true;

	private DefaultListModel _listModel = new DefaultListModel();
	private MinusAction _minus = new MinusAction();
	private JList _list;
	private String _prompt;

	@SuppressWarnings("unchecked")
	ParameterizableStringListEditorDialog(Window owner, String title,
			String prompt, ParameterizableStringList stringList) {
		super(owner, title);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		_prompt = prompt;

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		for (String value : stringList)
			_listModel.addElement(value);

		_list = new JList(_listModel);
		_list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scroller = new JScrollPane(_list);

		_list.addListSelectionListener(new ListSelectionListenerImpl());

		content.add(scroller, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						5, 5, 0, 5), 5, 5));
		content.add(ButtonPanel.createHorizontalPanel(new SimpleIconButton(
				ShapeIcons.Plus, new PlusAction()), new SimpleIconButton(
				ShapeIcons.Minus, _minus)), new GridBagConstraints(0, 1, 1, 1,
				1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 5, 5, 5), 5, 5));
		content.add(ButtonPanel.createHorizontalPanel(new OKAction(),
				new CancelAction()), new GridBagConstraints(0, 2, 1, 1, 1.0,
				0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
	}

	Set<String> getResults() {
		if (_cancelled)
			return null;

		Set<String> ret = new LinkedHashSet<String>();
		for (int lcv = 0; lcv < _listModel.size(); lcv++)
			ret.add(_listModel.getElementAt(lcv).toString());

		return ret;
	}

	private class OKAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private OKAction() {
			super("OK");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_cancelled = false;
			dispose();
		}
	}

	private class CancelAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private CancelAction() {
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_cancelled = true;
			dispose();
		}
	}

	@SuppressWarnings("unchecked")
	private class PlusAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		@Override
		public void actionPerformed(ActionEvent e) {
			String answer = JOptionPane.showInputDialog(_list, _prompt);
			if (answer != null)
				_listModel.addElement(answer);
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
				_listModel.removeElementAt(indices[lcv]);
		}
	}

	private class ListSelectionListenerImpl implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			_minus.setEnabled(_list.getSelectedIndices().length > 0);
		}
	}
}