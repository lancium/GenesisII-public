package edu.virginia.vcgr.genii.ui.debug;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.debug.dnd.DnDDebugTransferHandler;

public class DnDDebugTextArea extends JTextArea
{
	static final long serialVersionUID = 0L;
	
	private DnDDebugTextArea(UIContext uiContext)
	{
		super(25, 25);
		
		setTransferHandler(new DnDDebugTransferHandler(uiContext));
	}
	
	static public JComponent createPanel(UIContext uiContext)
	{
		DnDDebugTextArea area = new DnDDebugTextArea(uiContext);
		JScrollPane scroller = new JScrollPane(area);
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
			"DnD Debug Area"));
		panel.add(scroller, new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		return panel;
	}
}