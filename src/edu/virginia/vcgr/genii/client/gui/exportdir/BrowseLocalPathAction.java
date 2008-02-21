package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.morgan.util.gui.FileChooserHelper;

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
		JFileChooser chooser;
		
		chooser = FileChooserHelper.createFileChooser();
		if (chooser == null)
		{
			JOptionPane.showMessageDialog(_parent,
				"The Java File Chooser widget has failed to respond in a timely manner.\n" +
				"Unfortunately, this is a known bug that has yet to be fixed in the Java\n" +
				"Platform.  In the mean time, please simply type in the name of the local\n" +
				"path that you wish to export.");
		}
		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		
		if (chooser.showOpenDialog(_parent) == JFileChooser.APPROVE_OPTION)
		{
			_target.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}
}