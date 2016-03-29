package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JList;
import javax.swing.TransferHandler;

import edu.virginia.vcgr.genii.security.acl.AclEntry;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.plugins.acls.ACLPanel.DeleteAction;
import edu.virginia.vcgr.genii.ui.utils.CommonKeyStrokes;

@SuppressWarnings("rawtypes")
class ACLList extends JList
{
	static final long serialVersionUID = 0L;

	private Action _delete;

	private void setupInputMap(InputMap iMap)
	{
		Action cut = TransferHandler.getCutAction();
		Action copy = TransferHandler.getCopyAction();
		Action paste = TransferHandler.getPasteAction();

		iMap.put(CommonKeyStrokes.CUT, cut.getValue(Action.NAME));
		iMap.put(CommonKeyStrokes.COPY, copy.getValue(Action.NAME));
		iMap.put(CommonKeyStrokes.PASTE, paste.getValue(Action.NAME));
		iMap.put(CommonKeyStrokes.DELETE, _delete.getValue(Action.NAME));
		// hmmm: remap to "go up a level" --> iMap.put(CommonKeyStrokes.BACKSPACE,
		// _delete.getValue(Action.NAME));
	}

	private void setupActionMap(ActionMap aMap)
	{
		Action cut = TransferHandler.getCutAction();
		Action copy = TransferHandler.getCopyAction();
		Action paste = TransferHandler.getPasteAction();

		aMap.put(cut.getValue(Action.NAME), cut);
		aMap.put(copy.getValue(Action.NAME), copy);
		aMap.put(paste.getValue(Action.NAME), paste);
		aMap.put(_delete.getValue(Action.NAME), _delete);
		aMap.put(_delete.getValue(Action.NAME), _delete);
	}

	@SuppressWarnings("unchecked")
	ACLList(DeleteAction deleteAction)
	{
		super(new DefaultListModel());
		deleteAction.setACLList(this);

		_delete = deleteAction;

		setupInputMap(getInputMap());
		setupActionMap(getActionMap());

		DefaultListModel model = (DefaultListModel) getModel();

		model.addElement("Retrieving information...");
		setEnabled(false);
	}

	@SuppressWarnings("unchecked")
	void cancel()
	{
		DefaultListModel model = (DefaultListModel) getModel();
		model.removeAllElements();
		model.addElement("Cancelled!");
	}

	@SuppressWarnings("unchecked")
	void error()
	{
		DefaultListModel model = (DefaultListModel) getModel();
		model.removeAllElements();
		model.addElement("Error!");
	}

	void updating()
	{
		setEnabled(false);
	}

	@SuppressWarnings("unchecked")
	void set(UIContext context, Collection<AclEntry> entries)
	{
		DefaultListModel model = (DefaultListModel) getModel();
		model.removeAllElements();
		for (AclEntry entry : entries)
			model.addElement(new ACLEntryWrapper(context, entry));
		if (entries.isEmpty()) {
			model.addElement("<no permissions>");
		}
		setEnabled(true);
	}

	boolean contains(ACLEntryWrapper wrapper)
	{
		DefaultListModel model = (DefaultListModel) getModel();
		for (int lcv = 0; lcv < model.getSize(); lcv++)
			if (model.elementAt(lcv).equals(wrapper))
				return true;

		return false;
	}

	Transferable createTransferable()
	{
		@SuppressWarnings("deprecation")
		Object[] values = getSelectedValues();
		if (values == null || values.length == 0)
			return null;

		Collection<ACLEntryWrapper> wrappers = new Vector<ACLEntryWrapper>(values.length);
		for (Object value : values)
			wrappers.add((ACLEntryWrapper) value);

		return new ACLTransferable(new ACLEntryWrapperTransferData(ACLList.this, wrappers));
	}
}
