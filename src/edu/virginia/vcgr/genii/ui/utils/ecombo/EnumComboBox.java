package edu.virginia.vcgr.genii.ui.utils.ecombo;

import javax.swing.JComboBox;

public class EnumComboBox<Type extends Enum<Type>> extends JComboBox
{
	static final long serialVersionUID = 0L;
	
	public EnumComboBox(Class<Type> enumerationClass,
		EnumComboSort sort, boolean includeNull)
	{
		super(new EnumComboModel<Type>(enumerationClass, sort, includeNull));
		setRenderer(new EnumComboBoxRenderer());
	}
}