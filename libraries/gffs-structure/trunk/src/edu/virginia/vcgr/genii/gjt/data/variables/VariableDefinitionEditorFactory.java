package edu.virginia.vcgr.genii.gjt.data.variables;

import java.awt.Window;

public interface VariableDefinitionEditorFactory
{
	public VariableDefinitionEditor<? extends VariableDefinition> createEditor(Window owner);
}