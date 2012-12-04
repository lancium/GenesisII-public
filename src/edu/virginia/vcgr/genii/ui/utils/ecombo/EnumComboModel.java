package edu.virginia.vcgr.genii.ui.utils.ecombo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

@SuppressWarnings("rawtypes")
class EnumComboModel<Type extends Enum<Type>> extends AbstractListModel
	implements ComboBoxModel
{
	static final long serialVersionUID = 0l;
	
	private Vector<Type> _values;
	private Object _selectedValue = null;
	
	EnumComboModel(Class<Type> enumClass, EnumComboSort sort,
		boolean includeNull)
	{
		if (sort == null)
			sort = EnumComboSort.Alphabetically;
		
		Type[] enumConstants = enumClass.getEnumConstants();
		_values = new Vector<Type>(enumConstants.length +
			(includeNull ? 1 : 0));
		
		if (includeNull)
			_values.add(null);
		
		Comparator<Type> comparator;
		
		if (sort == EnumComboSort.Alphabetically)
			comparator = new AlphabeticComparator<Type>();
		else
			comparator = new OrdinalComparator<Type>();
		
		Arrays.sort(enumConstants, comparator);
		for (Type value : enumConstants)
			_values.add(value);
	}

	@Override
	public Object getSelectedItem()
	{
		return _selectedValue;
	}

	@Override
	public void setSelectedItem(Object anItem)
	{
		_selectedValue = anItem;
	}

	@Override
	public Object getElementAt(int index)
	{
		return _values.get(index);
	}

	@Override
	public int getSize()
	{
		return _values.size();
	}

	static private class AlphabeticComparator<Type extends Enum<Type>>
		implements Comparator<Type>
	{
		@Override
		final public int compare(Type o1, Type o2)
		{
			return o1.toString().compareTo(o2.toString());
		}
	}
	
	static private class OrdinalComparator<Type extends Enum<Type>>
		implements Comparator<Type>
	{
		@Override
		final public int compare(Type o1, Type o2)
		{
			return o1.ordinal() - o2.ordinal();
		}
	}
}