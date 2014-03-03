package edu.virginia.vcgr.genii.client.dialog;

import java.util.Collection;

public interface CheckBoxDialog extends Dialog
{
	public Collection<MenuItem> getCheckedItems();
}