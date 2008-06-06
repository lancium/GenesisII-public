package edu.virginia.vcgr.genii.client.dialog;

public class SimpleCheckBoxItem extends SimpleMenuItem implements CheckBoxItem
{
	private boolean _isEditable;
	private boolean _isChecked;
	
	public SimpleCheckBoxItem(String tag, Object content, 
		boolean isChecked, boolean isEditable)
	{
		super(tag, content);
		
		_isChecked = isChecked;
		_isEditable = isEditable;
	}
	
	@Override
	public boolean isChecked()
	{
		return _isChecked;
	}

	@Override
	public void setChecked(boolean isChecked)
	{
		_isChecked = isChecked;
	}

	@Override
	public boolean isEditable()
	{
		return _isEditable;
	}

	@Override
	public void setEditable(boolean isEditable)
	{
		_isEditable = isEditable;
	}
}