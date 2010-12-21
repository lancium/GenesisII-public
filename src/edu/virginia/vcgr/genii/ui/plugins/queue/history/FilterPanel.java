package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.ui.utils.ecombo.EnumComboBox;
import edu.virginia.vcgr.genii.ui.utils.ecombo.EnumComboSort;

class FilterPanel extends JPanel
{
	static final long serialVersionUID = 0l;
	
	private HistoryEventFilter _filter;
	
	FilterPanel(HistoryEventFilter filter)
	{
		super(new GridBagLayout());
		
		_filter = filter;
		
		add(new JLabel("Minimum Event Level"),
			new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		
		EnumComboBox<HistoryEventLevel> levelBox = 
			new EnumComboBox<HistoryEventLevel>(HistoryEventLevel.class, 
				EnumComboSort.ByOrdinal, false, LevelIcon.ICON_MAP);
		levelBox.setSelectedItem(filter.levelFilter());
		levelBox.addItemListener(new LevelSelectionListener());
		
		add(levelBox,
			new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 5, 5));
		
		add(new JButton(new CategoryFilterAction()),
			new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
	}
	
	private class LevelSelectionListener implements ItemListener
	{
		@Override
		public void itemStateChanged(ItemEvent e)
		{
			JComboBox box = (JComboBox)e.getSource();
			HistoryEventLevel level = (HistoryEventLevel)box.getSelectedItem();
			if (level != null)
				_filter.levelFilter(level);
		}
	}
	
	private class CategoryFilterAction extends AbstractAction
	{
		static final long serialVersionUID = 0l;
		
		private CategoryFilterAction()
		{
			super("Displayed Categories");
		}
		
		@Override
		final public void actionPerformed(ActionEvent e)
		{
			HistoryCategoryFilterDialog.modifyFilter(
				SwingUtilities.getWindowAncestor(FilterPanel.this),
				_filter);
		}
	}
}