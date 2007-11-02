package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JTextField;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;

public class BrowseRNSPathAction extends AbstractAction
{
	static final long serialVersionUID = 0L;
	
	private JDialog _parent;
	private JTextField _target;
	
	public BrowseRNSPathAction(JDialog parent, String label, JTextField target)
	{
		super(label);
		
		_parent = parent;
		_target = target;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			RNSBrowserDialog dialog = new RNSBrowserDialog(_parent);
			dialog.setModalityType(ModalityType.APPLICATION_MODAL);
			dialog.pack();
			
			GuiUtils.centerComponent(dialog);
			dialog.setVisible(true);
			String selectedPath = dialog.getSelectedPath();
			if (selectedPath != null)
				_target.setText(selectedPath);
		}
		catch (Throwable t)
		{
			GuiUtils.displayError(_parent, "RNS Browse Exception", t);
		}
	}
}