package edu.virginia.vcgr.genii.ui.rns;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.morgan.utils.gui.ButtonPanel;

import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.ui.UIContext;

final public class RNSSelectionDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	private OKAction _ok = new OKAction();
	private RNSTree _tree;
	private RNSSelectionFilter _filter;
	private RNSFilledInTreeObject _selectedObject = null;
	
	private RNSFilledInTreeObject getSelectedObject()
	{
		TreePath selectedPath = _tree.getSelectionPath();
		if (selectedPath != null)
		{
			Object value = selectedPath.getLastPathComponent();
			if (value != null && value instanceof RNSTreeNode)
			{
				RNSTreeNode treeNode = (RNSTreeNode)value;
				value = treeNode.getUserObject();
				if (value != null && value instanceof RNSFilledInTreeObject)
				{
					RNSFilledInTreeObject fObj = (RNSFilledInTreeObject)value;
					if (_filter != null)
					{
						if (_filter.accept(fObj.path(), fObj.endpoint(), 
							fObj.typeInformation(), fObj.endpointType(), 
							fObj.isLocal()))
							return fObj;
					} else
						return fObj;
						
				}
			}
		}
		
		return null;
	}
	
	public RNSSelectionDialog(Window owner, UIContext uiContext,
		RNSSelectionFilter filter) throws RNSPathDoesNotExistException
	{
		super(owner);
		setTitle("Grid Resource Selection");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		_filter = filter;
		uiContext = (UIContext)uiContext.clone();
		_tree = new RNSTree(uiContext.applicationContext(), uiContext);
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		_tree.getSelectionModel().setSelectionMode(
			TreeSelectionModel.SINGLE_TREE_SELECTION);
		_tree.getSelectionModel().addTreeSelectionListener(new SelectionListenerImpl());
		
		content.add(new JScrollPane(_tree), new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		
		content.add(ButtonPanel.createHorizontalButtonPanel(
			_ok, new CancelAction()), new GridBagConstraints(
				0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		
		Dimension d = new Dimension(500, 500);
		setMinimumSize(d);
		setPreferredSize(d);
		setSize(d);
	}
	
	final public RNSFilledInTreeObject selectedRNSPath() 
	{
		return _selectedObject;
	}
	
	private class SelectionListenerImpl implements TreeSelectionListener
	{
		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			RNSFilledInTreeObject selectedObject = getSelectedObject();
			if (selectedObject == null)
			{
				if (_tree.getSelectionCount() > 0)
					_tree.clearSelection();
			}
			
			_ok.setEnabled(_tree.getSelectionCount() > 0);
		}
	}
	
	private class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private OKAction()
		{
			super("Select");
			
			setEnabled(false);
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			_selectedObject = getSelectedObject();
			dispose();
		}
	}
	
	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private CancelAction()
		{
			super("Cancel");
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			_selectedObject = null;
			dispose();
		}
	}
}