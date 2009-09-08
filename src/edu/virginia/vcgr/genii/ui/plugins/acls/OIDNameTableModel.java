package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.util.EnumMap;

import javax.security.auth.x500.X500Principal;
import javax.swing.table.AbstractTableModel;

class OIDNameTableModel extends AbstractTableModel
{
	static final long serialVersionUID = 0L;
	
	private EnumMap<OIDNames, String> _values =
		new EnumMap<OIDNames, String>(OIDNames.class);
	private OIDNames []_keyArray;
	
	OIDNameTableModel()
	{
		for (OIDNames name : OIDNames.values())
		{
			if (name.toString() != null)
				_values.put(name, "");
		}
		
		_keyArray = new OIDNames[_values.keySet().size()];
		_keyArray = _values.keySet().toArray(_keyArray);
	}
	
	X500Principal formPrincipal()
	{
		StringBuilder builder = new StringBuilder();
		
		for (OIDNames name : _keyArray)
		{
			String value = _values.get(name);
			if (value != null && value.trim().length() > 0)
			{
				if (builder.length() > 0)
					builder.append(", ");

				builder.append(String.format("%s=%s",
					name.name(), value.trim()));
			}
		}
		
		if (builder.length() == 0)
			return null;
		
		return new X500Principal(builder.toString());
	}
	
	@Override
	public int getColumnCount()
	{
		return 2;
	}

	@Override
	public int getRowCount()
	{
		return _values.keySet().size();
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if (columnIndex == 0)
			return;
		
		if (rowIndex >= _keyArray.length)
			return;
		
		OIDNames name = _keyArray[rowIndex];
		_values.put(name, aValue.toString());
		fireTableCellUpdated(rowIndex, columnIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		OIDNames name = _keyArray[rowIndex];
		return (columnIndex == 0) ? name :
			_values.get(name);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return (columnIndex == 0) ? OIDNames.class : String.class;
	}

	@Override
	public String getColumnName(int column)
	{
		return (column == 0) ? "OID Category" : "Value";
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex == 1;
	}
}