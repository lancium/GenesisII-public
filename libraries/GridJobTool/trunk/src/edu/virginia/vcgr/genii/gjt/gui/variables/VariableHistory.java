package edu.virginia.vcgr.genii.gjt.gui.variables;

import java.util.EnumMap;

import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinition;
import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionType;
import edu.virginia.vcgr.genii.gjt.data.variables.undef.UndefinedVariableDefinition;

public class VariableHistory
{
	private EnumMap<VariableDefinitionType, VariableDefinition> _historical;
	private VariableDefinition _current;

	public VariableHistory(VariableDefinition current)
	{
		_historical = new EnumMap<VariableDefinitionType, VariableDefinition>(VariableDefinitionType.class);

		current(current);
	}

	public VariableHistory()
	{
		this(null);
	}

	final public VariableDefinition current(VariableDefinition newDefinition)
	{
		if (newDefinition == null)
			newDefinition = new UndefinedVariableDefinition();

		_current = newDefinition;
		return _historical.put(newDefinition.type(), newDefinition);
	}

	final public VariableDefinition current()
	{
		return _current;
	}

	final public VariableDefinition getHistorical(VariableDefinitionType type)
	{
		return _historical.get(type);
	}
}