package edu.virginia.vcgr.genii.client.dialog;

public interface CheckBoxItem extends MenuItem
{
	public boolean isEditable();

	public void setEditable(boolean isEditable);

	public boolean isChecked();

	public void setChecked(boolean isChecked);
}