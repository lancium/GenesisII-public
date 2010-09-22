package edu.virginia.vcgr.genii.ui.shell;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.morgan.utils.gui.GUIUtils;

import edu.virginia.vcgr.genii.client.configuration.UserPreferences;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.shell.grid.GridExecutionContext;

public class CommandPanel extends JPanel
{
	static final long serialVersionUID = 0L;
	
	static final private Dimension OUTPUT_SIZE = new Dimension(
		500, 500);
	
	public CommandPanel(UIContext uiContext)
	{
		super(new GridBagLayout());
		
		CommandDisplay display = new CommandDisplay(uiContext);
		JLabel label = new JLabel(
			UserPreferences.preferences().shellPrompt().toString());
		label.setMinimumSize(label.getMinimumSize());
		label.setPreferredSize(label.getPreferredSize());
		label.setMaximumSize(label.getMaximumSize());
		CommandField field = new CommandField(uiContext, label, display.display(),
			new GridExecutionContext(uiContext.callingContext()), 32);
		
		JScrollPane scroller = new JScrollPane(display);
		scroller.setMinimumSize(OUTPUT_SIZE);
		scroller.setPreferredSize(OUTPUT_SIZE);
		
		add(GUIUtils.addTitle("Output", scroller), 
			new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
		add(label, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));
		add(field, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
	}
}