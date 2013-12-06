package edu.virginia.vcgr.genii.ui.plugins.logs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.ui.plugins.logs.tree.DisplayByType;
import edu.virginia.vcgr.genii.ui.plugins.logs.tree.LogTree;

public class LogTreePanel extends JPanel
{

	Log _logger = LogFactory.getLog(LogTreePanel.class);

	private LogTree _browserTree;

	private JComboBox _displayByBox;

	private static final long serialVersionUID = 1L;

	public LogTreePanel(LogTree browserTree)
	{
		super(new GridBagLayout());

		add(new JScrollPane(browserTree), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		_browserTree = browserTree;

		String[] options = { "Display logs by...", "RPC-ID", "command/procedure" };
		_displayByBox = new JComboBox(options);
		_displayByBox.setSelectedIndex(0);
		_displayByBox.addActionListener(new DisplayBoxListener());
		add(_displayByBox, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
	}

	private class DisplayBoxListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			int which = _displayByBox.getSelectedIndex();
			if (which == 0)
				return;
			if (which == 1)
				_browserTree.displayTreeBy(DisplayByType.DISPLAY_BY_RPC_ID);
			else
				_browserTree.displayTreeBy(DisplayByType.DISPLAY_BY_COMMAND);
		}
	}
}
