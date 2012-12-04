package edu.virginia.vcgr.genii.ui.plugins.matchparam;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.morgan.util.Pair;
import org.morgan.utils.gui.ButtonPanel;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.container.q2.matching.MatchingParamEnum;
import edu.virginia.vcgr.genii.ui.utils.ShapeIcons;
import edu.virginia.vcgr.genii.ui.utils.SimpleIconButton;

@SuppressWarnings("rawtypes")
class MatchingParameterDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	private MatchingParameterModel _model;
	private JTable _table;
	private boolean _committed = false;
	
	@SuppressWarnings("unchecked")
    MatchingParameterDialog(Component ownerComponent,
		Collection<Pair<String, String>> parameters)
	{
		super(SwingUtilities.getWindowAncestor(ownerComponent),
			"Matching Parameters");
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		_model = new MatchingParameterModel(parameters);
		_table = new JTable(_model);
		_table.putClientProperty("terminateEditOnFocusLost",
			Boolean.TRUE);
		_table.setSelectionMode(
			ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		TableColumn typeColumn = _table.getColumnModel().getColumn(2);
		JComboBox comboBox = new JComboBox();
		comboBox.addItem(MatchingParamEnum.requires);
		comboBox.addItem(MatchingParamEnum.supports);
		typeColumn.setCellEditor(new DefaultCellEditor(comboBox));
		
		
		content.add(new JScrollPane(_table), new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 5, 5));
		
		MinusAction minusAction = new MinusAction();
		JButton plusButton = new SimpleIconButton(ShapeIcons.Plus,
			new PlusAction());
		JButton minusButton = new SimpleIconButton(ShapeIcons.Minus,
			minusAction);
		
		_table.getSelectionModel().addListSelectionListener(minusAction);
		
		content.add(ButtonPanel.createHorizontalButtonPanel(
			plusButton, minusButton), new GridBagConstraints(
				0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 5, 5));
		
		content.add(ButtonPanel.createHorizontalButtonPanel(
			new CommitAction(), new CancelAction()), new GridBagConstraints(
				0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private class PlusAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private PlusAction()
		{
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String name = JOptionPane.showInputDialog(
				_table, "What would you like to call the new matching parameter?",
				"New Matching Parameter", JOptionPane.QUESTION_MESSAGE);
			if (name != null && name.length() > 0)
			{
				String value = JOptionPane.showInputDialog(
					_table, 
					String.format("What value should \"%s\" have?", name),
					"New Matching Parameter", JOptionPane.QUESTION_MESSAGE);
				if (value != null && value.length() > 0)
				{
					_model.addParameter(name, value);
				}
			}
		}
	}
	
	private class MinusAction extends AbstractAction
		implements ListSelectionListener
	{
		static final long serialVersionUID = 0L;
		
		private MinusAction()
		{
			setEnabled(false);
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int []rows = _table.getSelectedRows();
			if (rows != null && rows.length > 0)
			{
				for (int lcv = rows.length - 1; lcv >= 0; lcv--)
					_model.removeParameter(rows[lcv]);
			}
		}

		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			setEnabled(_table.getSelectedRowCount() > 0);
		}
	}
	
	private class CommitAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private CommitAction()
		{
			super("Commit");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			_committed = true;
			dispose();
		}
	}
	
	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private CancelAction()
		{
			super("Cancel");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
	}
	
	static Collection<Pair<Pair<String, String>, MatchingParameterOperation>>
		handleMatchingParameters(Component ownerComponent, 
			Collection<Pair<String, String>> parameters)
	{
		MatchingParameterDialog dialog = new MatchingParameterDialog(
			ownerComponent, parameters);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		
		dialog.pack();
		GuiUtils.centerComponent(dialog);
		
		dialog.setVisible(true);
		
		if (dialog._committed)
			return dialog._model.generateOperations();
		
		return null;
	}
}