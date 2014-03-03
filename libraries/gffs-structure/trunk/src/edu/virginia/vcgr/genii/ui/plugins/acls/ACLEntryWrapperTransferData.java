package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.util.Collection;
import java.util.Vector;

class ACLEntryWrapperTransferData
{
	private boolean _handled = false;
	private ACLList _source;
	private Collection<ACLEntryWrapper> _wrappers;

	ACLEntryWrapperTransferData(ACLList source, Collection<ACLEntryWrapper> wrappers)
	{
		_source = source;
		_wrappers = wrappers;
	}

	ACLEntryWrapperTransferData(ACLList source, ACLEntryWrapper wrapper)
	{
		_source = source;
		_wrappers = new Vector<ACLEntryWrapper>(1);
		_wrappers.add(wrapper);
	}

	final ACLList source()
	{
		return _source;
	}

	final Collection<ACLEntryWrapper> wrappers()
	{
		return _wrappers;
	}

	final void handled(boolean handled)
	{
		_handled = handled;
	}

	final boolean handled()
	{
		return _handled;
	}
}