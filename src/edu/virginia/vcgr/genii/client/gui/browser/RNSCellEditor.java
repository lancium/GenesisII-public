package edu.virginia.vcgr.genii.client.gui.browser;

import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;

public class RNSCellEditor extends DefaultTreeCellEditor
	implements CellEditorListener
{
	public RNSCellEditor(JTree tree, DefaultTreeCellRenderer renderer)
	{
		super(tree, renderer);
		
		realEditor.addCellEditorListener(this);
	}
	
	public RNSCellEditor(JTree tree, DefaultTreeCellRenderer renderer,
		TreeCellEditor editor)
	{
		super(tree, renderer, editor);
		
		realEditor.addCellEditorListener(this);
	}
	
	public Object getCellEditorValue()
	{
		RNSNode editingNode = 
			((RNSNode)(tree.getEditingPath().getLastPathComponent()));
		RNSEntry oldEntry = (RNSEntry)(editingNode.getUserObject());
		
		return new RNSEntry(super.getCellEditorValue().toString(),
			oldEntry.getTarget());
	}
	
	public boolean stopCellEditing()
	{
		System.err.println("Cancelling.");
		cancelCellEditing();
		return false;
	}

	public void editingCanceled(ChangeEvent e)
	{
		System.err.println("Cancel callback.");
	}

	public void editingStopped(ChangeEvent e)
	{
		((TreeCellEditor)e.getSource()).cancelCellEditing();
	}
}