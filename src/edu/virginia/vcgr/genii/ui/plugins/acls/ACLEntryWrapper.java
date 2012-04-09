package edu.virginia.vcgr.genii.ui.plugins.acls;

import edu.virginia.vcgr.genii.client.security.authz.acl.AclEntry;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.prefs.security.SecurityUIPreferenceSet;

public class ACLEntryWrapper
{
	private UIContext _context;
	private AclEntry _entry;
	
	ACLEntryWrapper(UIContext context, AclEntry entry)
	{
		_entry = entry;
		_context = context;
	}
	
	AclEntry entry()
	{
		return _entry;
	}
	
	public boolean equals(ACLEntryWrapper other)
	{
		if (_entry == null)
			return other._entry == null;
		if (other._entry == null)
			return false;
		
		return _entry.equals(other._entry);
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof AclEntry)
		{
			if (_entry == null)
				return false;
			return _entry.equals(other);
		}
		if (other instanceof ACLEntryWrapper)
		{
			return equals((ACLEntryWrapper)other);
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		SecurityUIPreferenceSet prefs =
			_context.preferences().preferenceSet(
				SecurityUIPreferenceSet.class);
	
		if (_entry == null)
			return "Everyone";
		else
			return _entry.describe(prefs.aclVerbosityLevel());
	}
}