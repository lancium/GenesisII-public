package edu.virginia.vcgr.genii.ui.utils.ecombo;

import java.util.Map;

import javax.swing.Icon;
import javax.swing.JComboBox;

@SuppressWarnings("rawtypes")
public class EnumComboBox<Type extends Enum<Type>> extends JComboBox
{
	static final long serialVersionUID = 0L;
	
	@SuppressWarnings("unchecked")
    public EnumComboBox(Class<Type> enumerationClass,
		EnumComboSort sort, boolean includeNull, Map<Type, Icon> iconMap)
	{
		super(new EnumComboModel<Type>(enumerationClass, sort, includeNull));
		setRenderer(new EnumComboBoxRenderer<Type>(iconMap));
	}
}