package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class QuitExportAction extends AbstractAction implements ListSelectionListener
{
	static final long serialVersionUID = 0L;
	
	static final private String _QUIT_EXPORT_BUTTON = "Quit Export";
	
	public QuitExportAction(ListSelectionModel selectionModel)
	{
		super(_QUIT_EXPORT_BUTTON);
		
		setEnabled(!selectionModel.isSelectionEmpty());
		selectionModel.addListSelectionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		setEnabled(!((ListSelectionModel)e.getSource()).isSelectionEmpty());
	}
}