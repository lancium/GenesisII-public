package edu.virginia.vcgr.genii.ui.plugins.matchparam;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.morgan.util.Pair;

class MatchingParameterModel extends AbstractTableModel
{
	static final long serialVersionUID = 0L;
	
	private Vector<Pair<String, String>> _originalParameters;
	private Vector<Pair<String, String>> _parameters;
	
	MatchingParameterModel(Collection<Pair<String, String>> parameters)
	{
		_originalParameters = new Vector<Pair<String,String>>(parameters);
		_parameters = new Vector<Pair<String,String>>(parameters);
	}
	
	final void addParameter(String name, String value)
	{
		_parameters.add(new Pair<String, String>(name, value));
		fireTableRowsInserted(_parameters.size() - 1, _parameters.size() - 1);
	}
	
	final void removeParameter(int row)
	{
		_parameters.remove(row);
		fireTableRowsDeleted(row, row);
	}
	
	final Collection<Pair<Pair<String, String>, MatchingParameterOperation>>
		generateOperations()
	{
		Collection<Pair<Pair<String, String>, MatchingParameterOperation>> ret =
			new LinkedList<Pair<Pair<String,String>,MatchingParameterOperation>>();
		
		for (Pair<String, String> original : _originalParameters)
		{
			if (!_parameters.contains(original))
				ret.add(new Pair<Pair<String,String>, MatchingParameterOperation>(
					original, MatchingParameterOperation.Delete));
		}
		
		for (Pair<String, String> newP : _parameters)
		{
			if (!_originalParameters.contains(newP))
				ret.add(new Pair<Pair<String,String>, MatchingParameterOperation>(
					newP, MatchingParameterOperation.Add));
		}
		
		return ret;
	}
	
	@Override
	public int getColumnCount()
	{
		return 2;
	}

	@Override
	public int getRowCount()
	{
		return _parameters.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		Pair<String, String> parameter = _parameters.get(rowIndex);
		return (columnIndex == 0) ? parameter.first() : parameter.second();
	}

	@Override
	public String getColumnName(int column)
	{
		if (column == 0)
			return "Parameter Name";
		else
			return "Parameter Value";
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return true;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		Pair<String, String> parm = _parameters.get(rowIndex);
		
		if (columnIndex == 0)
			parm = new Pair<String, String>(aValue.toString(), parm.second());
		else
			parm = new Pair<String, String>(parm.first(), aValue.toString());
		
		_parameters.set(rowIndex, parm);
		
		fireTableRowsUpdated(rowIndex, rowIndex);
	}
}
