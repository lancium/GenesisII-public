package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import edu.virginia.vcgr.genii.ui.UIContext;

class EveryoneTransferHandler extends TransferHandler
{
	static final long serialVersionUID = 0L;

	private UIContext _context;
	
	EveryoneTransferHandler(UIContext context)
	{
		_context = context;
	}
	
	@Override
	protected Transferable createTransferable(JComponent c)
	{
		return new ACLTransferable(new ACLEntryWrapperTransferData(
			null, new ACLEntryWrapper(_context, null)));
	}

	
	@Override
	public int getSourceActions(JComponent c)
	{
		return LINK;
	}
}