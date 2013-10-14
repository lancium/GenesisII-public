package edu.virginia.vcgr.genii.gjt.gui.variables;

import java.util.Vector;

import javax.swing.JComboBox;

import edu.virginia.vcgr.genii.gjt.data.variables.VariableDefinitionType;

@SuppressWarnings("rawtypes")
public class VariableDefinitionTypeCombo extends JComboBox
{
	static final long serialVersionUID = 0L;

	static private Vector<VariableDefinitionType> getItems()
	{
		Vector<VariableDefinitionType> ret = new Vector<VariableDefinitionType>();

		ret.add(VariableDefinitionType.UndefinedVariable);

		for (VariableDefinitionType type : VariableDefinitionType.values()) {
			if (type != VariableDefinitionType.UndefinedVariable)
				ret.add(type);
		}

		return ret;
	}

	@SuppressWarnings("unchecked")
	public VariableDefinitionTypeCombo()
	{
		super(getItems());
	}
}