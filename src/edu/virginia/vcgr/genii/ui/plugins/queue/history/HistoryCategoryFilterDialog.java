package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.morgan.utils.gui.GUIUtils;

import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.gjt.gui.util.ButtonPanel;

class HistoryCategoryFilterDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	static final private int SELECTION_COLUMN_WIDTH = 64;
	
	private HistoryCategoryFilterModel _model;
	private Set<HistoryEventCategory> _selectionSet = null;
	
	private void prepareColumns(TableColumnModel columnModel)
	{
		TableColumn selectionColumn = columnModel.getColumn(0);
		TableColumn categoryColumn = columnModel.getColumn(1);
		
		selectionColumn.setMaxWidth(SELECTION_COLUMN_WIDTH);
		selectionColumn.setPreferredWidth(SELECTION_COLUMN_WIDTH);
		selectionColumn.setResizable(false);
		
		categoryColumn.setCellRenderer(new DefaultTableCellRenderer()
		{
			private static final long serialVersionUID = 0L;

			@Override
			public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column)
			{
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
					row, column);
				
				HistoryEventCategory category = (HistoryEventCategory)value;
				if (category != null)
					setIcon(category.information().categoryIcon());
				
				return this;
			}
		});
	}
	
	private HistoryCategoryFilterDialog(Window owner,
		HistoryEventFilter filter)
	{
		super(owner);
		
		setTitle("History Event Category Filter");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		_model = new HistoryCategoryFilterModel(filter);
		JTable table = new JTable(_model);
		prepareColumns(table.getColumnModel());
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		content.add(new JScrollPane(table), new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		content.add(ButtonPanel.createHorizontalPanel(
			new OKAction(), new CancelAction()), new GridBagConstraints(
				0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}
	
	private class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private OKAction()
		{
			super("OK");
		}
		
		@Override
		final public void actionPerformed(ActionEvent e)
		{
			_selectionSet = _model.getSelectionSet();
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
		final public void actionPerformed(ActionEvent e)
		{
			_selectionSet = null;
			dispose();
		}
	}
	
	static void modifyFilter(Window owner, HistoryEventFilter filter)
	{
		HistoryCategoryFilterDialog dialog = 
			new HistoryCategoryFilterDialog(owner, filter);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.pack();
		GUIUtils.centerWindow(dialog);
		dialog.setVisible(true);
		
		Set<HistoryEventCategory> selectionSet = dialog._selectionSet;
		if (selectionSet != null)
			filter.categoryFilter(selectionSet);
	}
}