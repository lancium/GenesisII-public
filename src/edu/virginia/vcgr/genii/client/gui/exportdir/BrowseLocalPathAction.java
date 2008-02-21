package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

public class BrowseLocalPathAction extends AbstractAction
{
	static final long serialVersionUID = 0L;
	
	private Component _parent;
	private JTextField _target;
	
	public BrowseLocalPathAction(Component parent, String label, JTextField target)
	{
		super(label);
		
		_parent = parent;
		_target = target;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		JFileChooser chooser = new JFileChooser();
		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		
		if (chooser.showOpenDialog(_parent) == JFileChooser.APPROVE_OPTION)
			_target.setText(chooser.getSelectedFile().getAbsolutePath());
	}
}