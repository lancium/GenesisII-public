package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.EnumSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.UIContext;

class HistoryTreePanel extends JPanel
{
	static final long serialVersionUID = 0L;
	
	HistoryTreePanel(UIContext context, 
		RNSPath queue, String ticketNumber,
		Collection<HistoryEvent> events)
	{
		super(new GridBagLayout());
		
		HistoryEventFilter filter = new HistoryEventFilter(
			HistoryEventLevel.Information,
			EnumSet.allOf(HistoryEventCategory.class));
		
		JTree tree = new HistoryEventTree(context,
			events, filter);
		
		add(new FilterPanel(filter), new GridBagConstraints(
			0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(new JScrollPane(tree), new GridBagConstraints(
			0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
	}
}