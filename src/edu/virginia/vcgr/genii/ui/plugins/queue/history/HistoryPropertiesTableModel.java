package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.util.Arrays;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

class HistoryPropertiesTableModel extends AbstractTableModel
{
	static final long serialVersionUID = 0L;
	
	private String []_propertyNames = new String[0];
	private String []_propertyValues = new String[0];
	
	final void setProperties(Map<String, String> properties)
	{
		if (properties == null)
		{
			_propertyNames = new String[0];
			_propertyValues = new String[0];
		} else
		{
			_propertyNames = new String[properties.size()];
			_propertyValues = new String[properties.size()];
			
			properties.keySet().toArray(_propertyNames);
			Arrays.sort(_propertyNames);
			
			for (int lcv = 0; lcv < _propertyNames.length; lcv++)
				_propertyValues[lcv] = properties.get(_propertyNames[lcv]);
		}
		
		fireTableDataChanged();
	}
	
	@Override
	final public int getRowCount()
	{
		return _propertyNames.length;
	}

	@Override
	final public int getColumnCount()
	{
		return 2;
	}

	@Override
	final public Object getValueAt(int rowIndex, int columnIndex)
	{
		String []array;
		
		switch (columnIndex)
		{
			case 0 :
				array = _propertyNames;
				break;
				
			default :
				array = _propertyValues;
				break;
		}
		
		return array[rowIndex];
	}

	@Override
	final public String getColumnName(int column)
	{
		switch (column)
		{
			case 0 :
				return "Property Names";
				
			default :
				return "Property Values";
		}
	}

	@Override
	final public Class<?> getColumnClass(int columnIndex)
	{
		return String.class;
	}

	@Override
	final public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}
}